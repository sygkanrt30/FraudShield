package ru.yanin.system_ingress.model.entity;

/**
 * @author Vyacheslav Yanin
 */
public enum Status {
    PENDING,
    SENT_TO_KAFKA,
    KAFKA_ERROR,
    INTERNAL_ERROR
}
