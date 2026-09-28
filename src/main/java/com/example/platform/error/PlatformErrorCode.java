package com.example.platform.error;

import org.springframework.http.HttpStatus;

/** Cross-cutting codes used by the global handler. Domain codes live in their own enums. */
public enum PlatformErrorCode implements ErrorCode {
    VALIDATION_FAILED("PLT-P-1400", "Request validation failed", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("PLT-P-1401", "Malformed request body", HttpStatus.BAD_REQUEST),
    CONCURRENT_MODIFICATION("PLT-B-1409", "Resource was changed by someone else", HttpStatus.CONFLICT),
    DATA_CONFLICT("PLT-B-1410", "Request conflicts with existing data", HttpStatus.CONFLICT),
    DUPLICATE_IDEMPOTENCY_KEY("PLT-B-1411", "Idempotency key reused with a different request", HttpStatus.UNPROCESSABLE_ENTITY),
    DATABASE_UNAVAILABLE("PLT-S-5001", "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    DEPENDENCY_TIMEOUT("PLT-S-5002", "Upstream dependency timed out", HttpStatus.GATEWAY_TIMEOUT),
    DEPENDENCY_UNAVAILABLE("PLT-S-5003", "Upstream dependency unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR("PLT-P-9999", "Unexpected internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String title;
    private final HttpStatus status;

    PlatformErrorCode(String code, String title, HttpStatus status) {
        this.code = code;
        this.title = title;
        this.status = status;
    }

    @Override public String code() { return code; }
    @Override public String title() { return title; }
    @Override public HttpStatus status() { return status; }
}
