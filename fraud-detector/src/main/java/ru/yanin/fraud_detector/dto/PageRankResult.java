package ru.yanin.fraud_detector.dto;

import java.util.UUID;

/**
 * @author Vyacheslav Yanin
 */
public record PageRankResult(UUID clientId, Double score) {}
