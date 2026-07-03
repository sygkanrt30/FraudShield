package ru.yanin.ingress_service.model.entity;

/**
 * @author Vyacheslav Yanin
 */
public enum Status {
    PENDING,
    SENT_TO_KAFKA,
    KAFKA_ERROR,
    INTERNAL_ERROR
}
