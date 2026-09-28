package com.example.platform.error;

/** PROJECT category error. See docs/standards/error-handling-standard.md. */
public non-sealed class ProjectException extends AppException {

    public ProjectException(ErrorCode code) {
        this(code, null, null);
    }

    public ProjectException(ErrorCode code, String detail) {
        this(code, detail, null);
    }

    public ProjectException(ErrorCode code, String detail, Throwable cause) {
        super(code, ErrorCategory.PROJECT, detail, cause);
    }
}
