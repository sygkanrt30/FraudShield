package ru.yanin.fraud_detector.exception;

/**
 * @author Vyacheslav Yanin
 */
public class ClientNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Client not found";

    public ClientNotFoundException(String message) {
        super(message);
    }

    public ClientNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
}
