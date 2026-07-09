package ru.yanin.system_ingress.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * @author Vyacheslav Yanin
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ProblemDetail catchCustomException(IngressServiceException e) {
        return handleException(e, e.responseStatus(), e.errorCode());
    }

    private ProblemDetail handleException(Exception e, HttpStatus status, String errorCode) {
        try {
            MDC.put(PropertyName.EXCEPTION_TYPE.value(), e.getClass().getSimpleName());
            MDC.put(PropertyName.DETAILS.value(), e.getMessage());
            MDC.put(PropertyName.ERROR_CODE.value(), errorCode);
            MDC.put(PropertyName.STATUS.value(), String.valueOf(status.value()));

            log.error("Exception handled: {} - {}", errorCode, e.getMessage(), e);

            var problemDetail = ProblemDetail.forStatusAndDetail(status, status.getReasonPhrase());
            problemDetail.setProperty(PropertyName.TIMESTAMP.value(), Instant.now());
            problemDetail.setProperty(PropertyName.ERROR_CODE.value(), errorCode);
            problemDetail.setProperty(PropertyName.EXCEPTION_TYPE.value(), e.getClass().getSimpleName());
            return problemDetail;
        } finally {
            MDC.clear();
        }
    }
}
