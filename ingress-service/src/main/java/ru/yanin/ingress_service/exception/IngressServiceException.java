package ru.yanin.ingress_service.exception;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

/**
 * @author Vyacheslav Yanin
 */
@Getter
@Accessors(fluent = true)
public class IngressServiceException extends RuntimeException {

    private final HttpStatus responseStatus;
    private final String errorCode;

    protected IngressServiceException (String message, String errorCode, HttpStatus responseStatus, Throwable cause) {
        super(message, cause);
        this.responseStatus = responseStatus;
        this.errorCode = errorCode;
    }

    protected IngressServiceException (String message, String errorCode, HttpStatus responseStatus) {
        this(message, errorCode, responseStatus, null);
    }
}
