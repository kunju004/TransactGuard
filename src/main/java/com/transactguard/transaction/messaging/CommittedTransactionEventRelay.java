package com.transactguard.transaction.messaging;

import com.transactguard.transaction.application.TransactionAuthorizationService;
import com.transactguard.transaction.domain.OutboxEvent;
import com.transactguard.transaction.domain.PaymentTransaction;
import com.transactguard.transaction.repository.OutboxEventRepository;
import com.transactguard.transaction.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CommittedTransactionEventRelay {

    private static final Logger log = LoggerFactory.getLogger(CommittedTransactionEventRelay.class);

    private final PaymentTransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionEventPublisher eventPublisher;

    public CommittedTransactionEventRelay(
            PaymentTransactionRepository transactionRepository,
            OutboxEventRepository outboxEventRepository,
            TransactionEventPublisher eventPublisher
    ) {
        this.transactionRepository = transactionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(TransactionAuthorizedDomainEvent domainEvent) {
        PaymentTransaction transaction = transactionRepository.findById(domainEvent.transactionId())
                .orElseThrow(() -> new IllegalStateException("Committed transaction could not be reloaded"));

        eventPublisher.publish(TransactionAuthorizedEvent.from(transaction));

        outboxEventRepository
                .findFirstByAggregateIdAndEventTypeOrderByCreatedAtDesc(
                        transaction.getId().toString(),
                        TransactionAuthorizationService.TRANSACTION_AUTHORIZED_EVENT
                )
                .ifPresent(OutboxEvent::markPublished);

        log.info("Published transaction authorization event for transactionId={}", transaction.getId());
    }
}
