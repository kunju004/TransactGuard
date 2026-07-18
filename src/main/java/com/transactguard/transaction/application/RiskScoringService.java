package com.transactguard.transaction.application;

import com.transactguard.transaction.api.AuthorizeTransactionRequest;
import com.transactguard.transaction.domain.Account;
import com.transactguard.transaction.domain.RiskAssessment;
import com.transactguard.transaction.domain.RiskDecision;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RiskScoringService {

    private static final BigDecimal REVIEW_THRESHOLD_AMOUNT = new BigDecimal("5000.00");
    private static final BigDecimal HIGH_VALUE_AMOUNT = new BigDecimal("10000.00");
    private static final Set<String> WATCHLIST_COUNTRIES = Set.of("IR", "KP", "SY");
    private static final Map<String, Integer> MCC_WEIGHTS = Map.of(
            "4829", 30,
            "5967", 35,
            "6051", 40,
            "7995", 45
    );

    public RiskAssessment assess(AuthorizeTransactionRequest request, Account account) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (!account.isActive()) {
            score += 100;
            reasons.add("ACCOUNT_INACTIVE");
        }

        if (!request.currency().equals(account.getCurrency())) {
            score += 90;
            reasons.add("CURRENCY_MISMATCH");
        }

        if (request.amount().compareTo(HIGH_VALUE_AMOUNT) >= 0) {
            score += 55;
            reasons.add("HIGH_VALUE_TRANSACTION");
        } else if (request.amount().compareTo(REVIEW_THRESHOLD_AMOUNT) >= 0) {
            score += 25;
            reasons.add("ELEVATED_VALUE_TRANSACTION");
        }

        Integer mccWeight = MCC_WEIGHTS.get(request.merchantCategoryCode());
        if (mccWeight != null) {
            score += mccWeight;
            reasons.add("HIGH_RISK_MCC");
        }

        if (WATCHLIST_COUNTRIES.contains(request.merchantCountry())) {
            score += 60;
            reasons.add("COUNTRY_WATCHLIST");
        }

        int cappedScore = Math.min(score, 100);
        RiskDecision decision;
        if (cappedScore >= 80) {
            decision = RiskDecision.DECLINE;
        } else if (cappedScore >= 50) {
            decision = RiskDecision.REVIEW;
        } else {
            decision = RiskDecision.APPROVE;
        }

        if (reasons.isEmpty()) {
            reasons.add("LOW_RISK_PROFILE");
        }

        return new RiskAssessment(decision, cappedScore, List.copyOf(reasons));
    }
}
