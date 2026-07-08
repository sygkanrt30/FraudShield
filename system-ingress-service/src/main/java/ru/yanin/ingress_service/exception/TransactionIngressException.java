package ru.yanin.ingress_service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Vyacheslav Yanin
 */
public class TransactionIngressException extends IngressServiceException {

    private static final String ERROR_CODE = "TRANSACTION_INGRESS_ERROR";

    public TransactionIngressException(String message) {
        super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public TransactionIngressException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
