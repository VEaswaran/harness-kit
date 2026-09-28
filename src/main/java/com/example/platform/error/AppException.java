package com.example.platform.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Base of the error hierarchy. Do not throw directly: use System/Business/ProjectException. */
public abstract sealed class AppException extends RuntimeException
        permits SystemException, BusinessException, ProjectException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details = new LinkedHashMap<>();

    protected AppException(ErrorCode errorCode, ErrorCategory expected, String detail, Throwable cause) {
        super(detail != null ? detail : errorCode.title(), cause);
        if (errorCode.category() != expected) {
            throw new IllegalArgumentException(
                    errorCode.code() + " is " + errorCode.category() + ", not " + expected);
        }
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() { return errorCode; }
    public ErrorCategory category() { return errorCode.category(); }
    public boolean retryable() { return errorCode.category().retryable(); }

    /** Extra client-safe fields for the problem body (e.g. orderId). Never put secrets or SQL here. */
    public AppException with(String key, Object value) {
        details.put(key, value);
        return this;
    }

    public Map<String, Object> details() { return Collections.unmodifiableMap(details); }
}
