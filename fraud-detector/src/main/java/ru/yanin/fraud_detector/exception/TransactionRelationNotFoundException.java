package ru.yanin.fraud_detector.exception;

/**
 * @author Vyacheslav Yanin
 */
public class TransactionRelationNotFoundException extends RuntimeException {

    public TransactionRelationNotFoundException(String message) {
        super(message);
    }
}
