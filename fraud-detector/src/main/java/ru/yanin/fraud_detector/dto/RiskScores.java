package ru.yanin.fraud_detector.dto;

import java.time.Instant;

/**
 * @author Vyacheslav Yanin
 */
public record RiskScores(

        double fraudsterRisk, // 0.0 – 1.0
        double victimRisk,// 0.0 – 1.0
        double overallRisk,
        RiskCategory riskCategory,
        Instant calculatedAt
) {

    public RiskScores {
        if (fraudsterRisk < 0 || fraudsterRisk > 1) {
            throw new IllegalArgumentException("fraudsterRisk must be between 0 and 1");
        }
        if (victimRisk < 0 || victimRisk > 1) {
            throw new IllegalArgumentException("victimRisk must be between 0 and 1");
        }
        if (overallRisk < 0 || overallRisk > 1) {
            throw new IllegalArgumentException("overallRisk must be between 0 and 1");
        }
    }

    public static RiskScores of(double fraudsterRisk, double victimRisk) {
        double overall = Math.max(fraudsterRisk, victimRisk);
        RiskCategory category = determineCategory(fraudsterRisk, victimRisk);

        return new RiskScores(
                fraudsterRisk,
                victimRisk,
                overall,
                category,
                Instant.now()
        );
    }

    private static RiskCategory determineCategory(double fraudsterRisk, double victimRisk) {
        if (fraudsterRisk > 0.7) return RiskCategory.FRAUDSTER;
        if (victimRisk > 0.7) return RiskCategory.VICTIM;
        if (fraudsterRisk > 0.4 && victimRisk > 0.4) return RiskCategory.MIXED;
        return RiskCategory.SAFE;
    }
}
