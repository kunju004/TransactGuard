package com.transactguard.transaction.domain;

import java.util.List;

public record RiskAssessment(
        RiskDecision decision,
        int score,
        List<String> reasons
) {
}
