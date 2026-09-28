package com.example.platform.error;

/** BUSINESS category error. See docs/standards/error-handling-standard.md. */
public non-sealed class BusinessException extends AppException {

    public BusinessException(ErrorCode code) {
        this(code, null, null);
    }

    public BusinessException(ErrorCode code, String detail) {
        this(code, detail, null);
    }

    public BusinessException(ErrorCode code, String detail, Throwable cause) {
        super(code, ErrorCategory.BUSINESS, detail, cause);
    }
}
