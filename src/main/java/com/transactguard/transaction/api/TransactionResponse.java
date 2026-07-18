package com.transactguard.transaction.api;

import com.transactguard.transaction.domain.PaymentTransaction;
import com.transactguard.transaction.domain.RiskDecision;
import com.transactguard.transaction.domain.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Schema(description = "Stored transaction decision with risk scoring details.")
public record TransactionResponse(
        @Schema(example = "9463f1c1-6d6b-49fe-a454-b1dfdcb8d994")
        UUID transactionId,
        @Schema(example = "acct_market_001")
        String accountId,
        @Schema(example = "merchant_urban_roast")
        String merchantId,
        @Schema(example = "5812")
        String merchantCategoryCode,
        @Schema(example = "US")
        String merchantCountry,
        @Schema(example = "42.25")
        BigDecimal amount,
        @Schema(example = "USD")
        String currency,
        @Schema(example = "APPROVED")
        TransactionStatus status,
        @Schema(example = "APPROVE")
        RiskDecision riskDecision,
        @Schema(example = "0")
        int riskScore,
        @Schema(example = "[\"LOW_RISK_PROFILE\"]")
        List<String> riskReasons,
        Instant createdAt
) {

    public static TransactionResponse from(PaymentTransaction transaction) {
        List<String> reasons = transaction.getRiskReasons().isBlank()
                ? List.of()
                : Arrays.asList(transaction.getRiskReasons().split(","));

        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getMerchantId(),
                transaction.getMerchantCategoryCode(),
                transaction.getMerchantCountry(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getRiskDecision(),
                transaction.getRiskScore(),
                reasons,
                transaction.getCreatedAt()
        );
    }
}
