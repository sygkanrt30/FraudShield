package ru.yanin.ingress_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Vyacheslav Yanin
 */
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
enum PropertyName {
    ERROR_CODE("errorCode"),
    TIMESTAMP("timestamp"),
    DETAILS("details"),
    PATH("path"),
    EXCEPTION_TYPE("exceptionType"),
    STATUS("status");


    private final String value;
}
