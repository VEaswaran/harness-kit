package com.example.platform.error;

/** SYSTEM category error. See docs/standards/error-handling-standard.md. */
public non-sealed class SystemException extends AppException {

    public SystemException(ErrorCode code) {
        this(code, null, null);
    }

    public SystemException(ErrorCode code, String detail) {
        this(code, detail, null);
    }

    public SystemException(ErrorCode code, String detail, Throwable cause) {
        super(code, ErrorCategory.SYSTEM, detail, cause);
    }
}
