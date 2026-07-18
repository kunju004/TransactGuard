package com.transactguard.transaction.application;

import com.transactguard.transaction.api.AuthorizeTransactionRequest;
import com.transactguard.transaction.domain.Account;
import com.transactguard.transaction.domain.RiskAssessment;
import com.transactguard.transaction.domain.RiskDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringServiceTest {

    private final RiskScoringService riskScoringService = new RiskScoringService();

    @Test
    void approvesLowRiskDomesticPurchase() {
        RiskAssessment assessment = riskScoringService.assess(
                request(new BigDecimal("42.25"), "5411", "US", "USD"),
                activeAccount()
        );

        assertThat(assessment.decision()).isEqualTo(RiskDecision.APPROVE);
        assertThat(assessment.score()).isZero();
        assertThat(assessment.reasons()).containsExactly("LOW_RISK_PROFILE");
    }

    @Test
    void sendsElevatedValueRiskyCategoryToReview() {
        RiskAssessment assessment = riskScoringService.assess(
                request(new BigDecimal("5000.00"), "7995", "US", "USD"),
                activeAccount()
        );

        assertThat(assessment.decision()).isEqualTo(RiskDecision.REVIEW);
        assertThat(assessment.score()).isEqualTo(70);
        assertThat(assessment.reasons())
                .contains("ELEVATED_VALUE_TRANSACTION", "HIGH_RISK_MCC");
    }

    @Test
    void declinesInactiveAccount() {
        RiskAssessment assessment = riskScoringService.assess(
                request(new BigDecimal("25.00"), "5411", "US", "USD"),
                new Account("acct_inactive", "USD", new BigDecimal("100.00"), false)
        );

        assertThat(assessment.decision()).isEqualTo(RiskDecision.DECLINE);
        assertThat(assessment.reasons()).contains("ACCOUNT_INACTIVE");
    }

    private Account activeAccount() {
        return new Account("acct_test_001", "USD", new BigDecimal("1000.00"), true);
    }

    private AuthorizeTransactionRequest request(BigDecimal amount, String mcc, String country, String currency) {
        return new AuthorizeTransactionRequest(
                "acct_test_001",
                "tok_testCard0001",
                "merchant_urban_roast",
                mcc,
                country,
                currency,
                amount,
                "idem-test-0001"
        );
    }
}
