---
name: Java error handling
description: Error taxonomy and resilience rules for Java code
applyTo: "src/**/*.java"
---
- Throw only `SystemException` (infrastructure/dependency), `BusinessException` (domain rule) or `ProjectException` (our code, input contract or config), each with an `ErrorCode` enum constant that also exists in `src/main/resources/error-codes.yaml`.
- Never throw `RuntimeException`, `Exception` or `Throwable`. Never catch them outside `GlobalExceptionHandler`.
- Never swallow an exception, and never log-and-rethrow; the global handler logs once.
- Wrap third-party exceptions at the boundary and keep the cause.
- Every outbound call has explicit timeouts. Retry only SYSTEM errors on idempotent calls (max 3, exponential backoff with jitter). Use a circuit breaker per dependency.
- Non-idempotent POST endpoints require an `Idempotency-Key`. Events go through the transactional outbox.
- Full rules: docs/standards/error-handling-standard.md
