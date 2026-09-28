-- Platform tables required in every service (see docs/standards/database-design-standard.md)

-- Idempotency: replay the stored response for a repeated Idempotency-Key
CREATE TABLE idempotency_keys (
    idempotency_key   varchar(100)  NOT NULL,
    request_hash      char(64)      NOT NULL,           -- SHA-256 of method + path + body
    status            varchar(20)   NOT NULL,
    response_status   integer,
    response_body     jsonb,
    created_at        timestamptz   NOT NULL DEFAULT now(),
    expires_at        timestamptz   NOT NULL,
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (idempotency_key),
    CONSTRAINT ck_idempotency_keys_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED'))
);
CREATE INDEX ix_idempotency_keys_expires_at ON idempotency_keys (expires_at);

-- Transactional outbox: written in the same transaction as the business change
CREATE TABLE outbox_events (
    id                uuid          NOT NULL,
    aggregate_type    varchar(60)   NOT NULL,
    aggregate_id      varchar(60)   NOT NULL,
    event_type        varchar(100)  NOT NULL,
    payload           jsonb         NOT NULL,
    status            varchar(20)   NOT NULL DEFAULT 'PENDING',
    attempts          integer       NOT NULL DEFAULT 0,
    last_error_code   varchar(20),                       -- catalog code, e.g. PLT-S-5003
    created_at        timestamptz   NOT NULL DEFAULT now(),
    published_at      timestamptz,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id),
    CONSTRAINT ck_outbox_events_status CHECK (status IN ('PENDING', 'PUBLISHED', 'DEAD')),
    CONSTRAINT ck_outbox_events_attempts CHECK (attempts >= 0)
);
-- Relay polls pending rows in order; partial index keeps it small
CREATE INDEX ix_outbox_events_pending ON outbox_events (created_at) WHERE status = 'PENDING';
