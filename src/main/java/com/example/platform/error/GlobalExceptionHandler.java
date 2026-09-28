package com.example.platform.error;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * The single place where exceptions become HTTP responses (RFC 9457 Problem Details).
 * Logs exactly once, with level chosen by category.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "https://errors.example.com/";

    @ExceptionHandler(AppException.class)
    ResponseEntity<ProblemDetail> handleApp(AppException ex, HttpServletRequest req) {
        logByCategory(ex.errorCode(), ex);
        ProblemDetail body = problem(ex.errorCode(), safeDetail(ex), req);
        ex.details().forEach(body::setProperty);
        return ResponseEntity.status(ex.errorCode().status()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ErrorCode code = PlatformErrorCode.VALIDATION_FAILED;
        log.warn("{} {} fields={}", code.code(), req.getRequestURI(), ex.getBindingResult().getFieldErrorCount());
        ProblemDetail body = problem(code, code.title(), req);
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(),
                        "message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .toList();
        body.setProperty("errors", errors);
        return ResponseEntity.status(code.status()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.MALFORMED_REQUEST, ex, req);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.CONCURRENT_MODIFICATION, ex, req);
    }

    /** Unmapped constraint violations. Prefer mapping known constraints to domain codes in the repository adapter. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.DATA_CONFLICT, ex, req);
    }

    @ExceptionHandler(QueryTimeoutException.class)
    ResponseEntity<ProblemDetail> handleQueryTimeout(QueryTimeoutException ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.DEPENDENCY_TIMEOUT, ex, req);
    }

    @ExceptionHandler(TransientDataAccessException.class)
    ResponseEntity<ProblemDetail> handleTransientDb(TransientDataAccessException ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.DATABASE_UNAVAILABLE, ex, req);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    ResponseEntity<ProblemDetail> handleCircuitOpen(CallNotPermittedException ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.DEPENDENCY_UNAVAILABLE, ex, req);
    }

    /** Anything not classified is a defect in this project. */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnknown(Exception ex, HttpServletRequest req) {
        return simple(PlatformErrorCode.INTERNAL_ERROR, ex, req);
    }

    // ---------------------------------------------------------------------

    private ResponseEntity<ProblemDetail> simple(ErrorCode code, Exception ex, HttpServletRequest req) {
        logByCategory(code, ex);
        return ResponseEntity.status(code.status()).body(problem(code, code.title(), req));
    }

    private ProblemDetail problem(ErrorCode code, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(code.status(), detail);
        pd.setType(URI.create(TYPE_BASE + code.code()));
        pd.setTitle(code.title());
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("code", code.code());
        pd.setProperty("category", code.category().name());
        pd.setProperty("retryable", code.category().retryable());
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }

    /** Business messages are written for the client; system/project details stay in logs. */
    private String safeDetail(AppException ex) {
        return ex.category() == ErrorCategory.BUSINESS ? ex.getMessage() : ex.errorCode().title();
    }

    private void logByCategory(ErrorCode code, Throwable ex) {
        switch (code.category()) {
            case BUSINESS -> log.info("{} {}", code.code(), ex.getMessage());
            case PROJECT -> {
                if (code.status().is4xxClientError()) log.warn("{} {}", code.code(), ex.getMessage());
                else log.error("{} {}", code.code(), ex.getMessage(), ex);
            }
            case SYSTEM -> log.error("{} {}", code.code(), ex.getMessage(), ex);
        }
    }
}
