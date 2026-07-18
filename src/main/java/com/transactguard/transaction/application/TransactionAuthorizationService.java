package com.transactguard.transaction.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactguard.transaction.api.AuthorizeTransactionRequest;
import com.transactguard.transaction.api.TransactionResponse;
import com.transactguard.transaction.domain.Account;
import com.transactguard.transaction.domain.OutboxEvent;
import com.transactguard.transaction.domain.PaymentTransaction;
import com.transactguard.transaction.domain.RiskAssessment;
import com.transactguard.transaction.domain.RiskDecision;
import com.transactguard.transaction.domain.TransactionStatus;
import com.transactguard.transaction.exception.BusinessException;
import com.transactguard.transaction.messaging.TransactionAuthorizedDomainEvent;
import com.transactguard.transaction.messaging.TransactionAuthorizedEvent;
import com.transactguard.transaction.repository.AccountRepository;
import com.transactguard.transaction.repository.OutboxEventRepository;
import com.transactguard.transaction.repository.PaymentTransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionAuthorizationService {

    public static final String TRANSACTION_AUTHORIZED_EVENT = "TRANSACTION_AUTHORIZED";

    private final AccountRepository accountRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RiskScoringService riskScoringService;
    private final HashingService hashingService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TransactionAuthorizationService(
            AccountRepository accountRepository,
            PaymentTransactionRepository transactionRepository,
            OutboxEventRepository outboxEventRepository,
            RiskScoringService riskScoringService,
            HashingService hashingService,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.riskScoringService = riskScoringService;
        this.hashingService = hashingService;
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public TransactionResponse authorize(AuthorizeTransactionRequest request) {
        String idempotencyKeyHash = hashingService.sha256(request.idempotencyKey());
        String requestFingerprint = hashingService.fingerprint(request);

        PaymentTransaction existing = transactionRepository.findByIdempotencyKeyHash(idempotencyKeyHash)
                .orElse(null);
        if (existing != null) {
            if (!existing.getRequestFingerprint().equals(requestFingerprint)) {
                throw new BusinessException(HttpStatus.CONFLICT, "Idempotency key was already used for a different request");
            }
            return TransactionResponse.from(existing);
        }

        Account account = accountRepository.findByAccountIdForUpdate(request.accountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Account was not found"));

        RiskAssessment assessment = riskScoringService.assess(request, account);
        TransactionStatus status = statusFor(assessment.decision());
        List<String> reasons = assessment.reasons();

        if (status == TransactionStatus.APPROVED && !account.canCover(request.amount())) {
            status = TransactionStatus.DECLINED;
            reasons = appendReason(reasons, "INSUFFICIENT_FUNDS");
        }

        if (status == TransactionStatus.APPROVED) {
            account.debit(request.amount());
        }

        PaymentTransaction transaction = new PaymentTransaction(
                request.accountId(),
                request.merchantId(),
                request.merchantCategoryCode(),
                request.merchantCountry(),
                request.currency(),
                request.amount(),
                status,
                assessment.decision(),
                assessment.score(),
                String.join(",", reasons),
                idempotencyKeyHash,
                requestFingerprint
        );

        PaymentTransaction saved = transactionRepository.save(transaction);
        outboxEventRepository.save(new OutboxEvent(
                saved.getId().toString(),
                TRANSACTION_AUTHORIZED_EVENT,
                toPayload(TransactionAuthorizedEvent.from(saved))
        ));
        applicationEventPublisher.publishEvent(new TransactionAuthorizedDomainEvent(saved.getId()));

        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Transaction was not found"));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listAccountTransactions(String accountId) {
        if (accountRepository.findByAccountId(accountId).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Account was not found");
        }

        return transactionRepository.findTop25ByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    private TransactionStatus statusFor(RiskDecision decision) {
        return switch (decision) {
            case APPROVE -> TransactionStatus.APPROVED;
            case REVIEW -> TransactionStatus.PENDING_REVIEW;
            case DECLINE -> TransactionStatus.DECLINED;
        };
    }

    private List<String> appendReason(List<String> reasons, String reason) {
        return java.util.stream.Stream.concat(reasons.stream(), java.util.stream.Stream.of(reason))
                .toList();
    }

    private String toPayload(TransactionAuthorizedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize transaction event", ex);
        }
    }
}
