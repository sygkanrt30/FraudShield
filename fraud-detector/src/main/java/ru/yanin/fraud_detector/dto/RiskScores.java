package ru.yanin.fraud_detector.dto;

import java.util.List;

/**
 * @author Vyacheslav Yanin
 */
public record RiskScores(

        // === ОСНОВНЫЕ РИСКИ ===
        double fraudsterRisk,      // 0.0 – 1.0
        double victimRisk,         // 0.0 – 1.0

        // === ВСПОМОГАТЕЛЬНЫЕ ===
        double overallRisk,        // Максимум из двух
        String riskCategory,       // FRAUDSTER, VICTIM, MIXED, SAFE

        // === ДЕТАЛИ ДЛЯ АУДИТА ===
        List<RiskFactor> factors,  // Что повлияло на риск

        // === МЕТАДАННЫЕ ===
        long calculatedAt

) {
    /**
     * Компактный конструктор для валидации
     */
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

    /**
     * Фабричный метод для создания с автоматическим расчётом
     */
    public static RiskScores of(double fraudsterRisk, double victimRisk, List<RiskFactor> factors) {
        double overall = Math.max(fraudsterRisk, victimRisk);
        String category = determineCategory(fraudsterRisk, victimRisk);

        return new RiskScores(
                fraudsterRisk,
                victimRisk,
                overall,
                category,
                factors,
                System.currentTimeMillis()
        );
    }

    /**
     * Определение категории риска
     */
    private static String determineCategory(double fraudsterRisk, double victimRisk) {
        if (fraudsterRisk > 0.7) return "FRAUDSTER";
        if (victimRisk > 0.7) return "VICTIM";
        if (fraudsterRisk > 0.4 && victimRisk > 0.4) return "MIXED";
        return "SAFE";
    }

    /**
     * Фактор, повлиявший на риск
     */
    public record RiskFactor(
            String name,           // "HIGH_PAGERANK", "LARGE_TRANSFERS"
            String description,    // "Client has PageRank > 0.7"
            double contribution    // 0.0 – 1.0 (вклад в общий риск)
    ) {
        public RiskFactor {
            if (contribution < 0 || contribution > 1) {
                throw new IllegalArgumentException("contribution must be between 0 and 1");
            }
        }
    }
}
