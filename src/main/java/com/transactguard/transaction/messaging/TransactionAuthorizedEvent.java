package com.transactguard.transaction.messaging;

import com.transactguard.transaction.domain.PaymentTransaction;
import com.transactguard.transaction.domain.RiskDecision;
import com.transactguard.transaction.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionAuthorizedEvent(
        UUID transactionId,
        String accountId,
        String merchantId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        RiskDecision riskDecision,
        int riskScore,
        Instant occurredAt
) {

    public static TransactionAuthorizedEvent from(PaymentTransaction transaction) {
        return new TransactionAuthorizedEvent(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getMerchantId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getRiskDecision(),
                transaction.getRiskScore(),
                transaction.getCreatedAt()
        );
    }
}
