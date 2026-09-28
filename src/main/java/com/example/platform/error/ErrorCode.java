package com.example.platform.error;

import org.springframework.http.HttpStatus;

/**
 * Implemented by one enum per domain (OrderErrorCode, PaymentErrorCode, ...).
 * Every constant must exist in src/main/resources/error-codes.yaml (enforced by ErrorCatalogTest).
 */
public interface ErrorCode {
    /** e.g. "ORD-B-1001" */
    String code();
    /** Client-safe short title. */
    String title();
    HttpStatus status();

    default ErrorCategory category() {
        return ErrorCategory.fromCode(code());
    }
}
