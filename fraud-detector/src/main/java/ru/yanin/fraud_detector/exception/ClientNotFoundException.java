package ru.yanin.fraud_detector.exception;

/**
 * @author Vyacheslav Yanin
 */
public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String message) {
        super(message);
    }

    public ClientNotFoundException() {
        super("Client not found");
    }
}
