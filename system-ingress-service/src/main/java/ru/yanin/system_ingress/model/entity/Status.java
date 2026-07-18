package ru.yanin.system_ingress.model.entity;

/**
 * Represents the processing status of a transaction in the ingress pipeline.
 *
 * <p>Possible statuses:</p>
 * <ul>
 *   <li>{@link #PENDING} — Transaction is received and waiting to be processed</li>
 *   <li>{@link #SENT_TO_KAFKA} — Transaction has been successfully sent to Kafka</li>
 *   <li>{@link #KAFKA_ERROR} — Error occurred while sending to Kafka (eligible for retry)</li>
 *   <li>{@link #INTERNAL_ERROR} — Critical internal error during processing</li>
 *   <li>{@link #EXPIRED} - Transaction was in {@link #PENDING} for a very long time,after which it is considered expired </li>
 * </ul>
 *
 * @author Vyacheslav Yanin
 */
public enum Status {
    PENDING,
    SENT_TO_KAFKA,
    KAFKA_ERROR,
    INTERNAL_ERROR,
    EXPIRED
}
