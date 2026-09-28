# Error Handling and Fault Tolerance Standard

Every failure ends as exactly one category, with a catalog code, an HTTP status, a retry rule and a safe message.

## 1. Categories

| | SYSTEM (S) | BUSINESS (B) | PROJECT (P) |
|---|---|---|---|
| Meaning | Infrastructure or dependency failed | A domain rule rejected the request | Our own application logic, input contract or config is wrong |
| Examples | DB unreachable, query timeout, broker down, downstream 5xx/timeout, circuit open | Insufficient balance, order already shipped, limit exceeded, duplicate (user-caused) | Invalid payload / validation, missing config, mapping error, unexpected null, inter-module contract mismatch |
| Java type | `SystemException` | `BusinessException` | `ProjectException` |
| HTTP | 503 (dependency down), 504 (timeout), 500 (unknown) | 409 (state conflict) or 422 (rule violated) | 400 (bad input), 500 (defect) |
| Retryable | Yes if the operation is idempotent | Never | Never |
| Log | ERROR + stack trace | INFO/WARN, no stack trace | 400: WARN no stack; 500: ERROR + stack |
| Alert | Error rate, circuit open | No (business metric only) | Yes for 500s (defect) |
| Client message | Generic "temporarily unavailable" + traceId | Specific business reason | Field errors (400) or generic + traceId (500) |

Note: "Project error" here means an error owned by this project's code or configuration (not infrastructure,
not a business rule). Rename if your team uses a different term, e.g. "Application" or "Technical".

## 2. Codes

Format `<DOMAIN>-<S|B|P>-<NNNN>`. Ranges: 1000-4999 business/project by feature, 5000-5999 system, 9000-9999 generic.

- One catalog: `src/main/resources/error-codes.yaml`. The Confluence error catalog page is generated from it.
- A code is never reused for a different meaning. Deprecated codes stay in the file with `deprecated: true`.
- Messages in the catalog are client-safe. Put internal detail in logs only.

## 3. API response (RFC 9457 Problem Details)

```json
{
  "type": "https://errors.example.com/ORD-B-1001",
  "title": "Order already shipped",
  "status": 409,
  "detail": "Order 8f1c... cannot be cancelled after shipment.",
  "instance": "/orders/8f1c.../cancel",
  "code": "ORD-B-1001",
  "category": "BUSINESS",
  "retryable": false,
  "traceId": "4bf92f3577b34da6",
  "errors": [ { "field": "quantity", "message": "must be >= 1" } ]
}
```

## 4. Coding rules

1. Throw only `SystemException`, `BusinessException`, `ProjectException` (or subclasses) with a catalog code.
2. Wrap third-party exceptions at the boundary (client adapters, repositories) into SYSTEM/BUSINESS; keep the cause.
3. No `catch (Exception|Throwable)` except in the global handler and top-level message listeners.
4. Never swallow. Never log-and-rethrow (the handler logs once).
5. One `@RestControllerAdvice` maps everything; unknown exceptions -> `PLT-P-9999` 500.
6. DB mapping: unique/FK/check violation caused by user data -> BUSINESS 409/422; connection, lock timeout, deadlock -> SYSTEM (retryable); `OptimisticLockException` -> BUSINESS 409 "changed by someone else".
7. Put `traceId` in MDC for every request and message.

## 5. Resilience rules (Resilience4j)

| Concern | Rule |
|---|---|
| Timeouts | Every HTTP/DB/broker call has explicit connect + read timeout. Default HTTP read 2s; DB statement 5s. |
| Retry | SYSTEM only, idempotent only, max 3 attempts, exponential backoff with jitter (200ms x2). No nested retries across layers. |
| Circuit breaker | Per dependency; open -> fail fast with `*-S-5xxx`, fallback if the wiki defines one. |
| Bulkhead | Separate thread pool / semaphore for each critical dependency. |
| Idempotency | Non-idempotent POSTs require `Idempotency-Key` header; stored in `idempotency_keys` table. |
| Outbox | Events written to `outbox_events` in the same transaction; relay publishes and marks sent. |
| Consumers | Idempotent (dedupe by event id); after N failures -> DLQ with error code + alert. |
| Sagas | Each step has a documented compensation. |
| Observability | Counter per error code and category; alert on SYSTEM rate and circuit-open events. |
