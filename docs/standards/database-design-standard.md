# Database Design Standard (PostgreSQL + Flyway + JPA)

## Structure
| Area | Rule |
|---|---|
| Keys | PK `id`: UUID (v7 preferred) or `bigint generated always as identity`. Natural keys: separate `UNIQUE`. |
| Naming | snake_case; plural table names; `pk_<t>`, `fk_<t>_<ref>`, `uq_<t>_<cols>`, `ix_<t>_<cols>`, `ck_<t>_<rule>`. |
| Audit | `created_at timestamptz not null`, `created_by varchar(100) not null`, `updated_at`, `updated_by`. |
| Concurrency | Mutable aggregates: `version bigint not null default 0` + JPA `@Version`. |
| Integrity | Use `NOT NULL`, `CHECK`, `UNIQUE`, FKs with explicit `ON DELETE` (default `RESTRICT`). |
| Money | `numeric(19,4)` + `currency char(3)`. Never float/double. |
| Time | `timestamptz`, stored UTC. Dates without time: `date`. |
| Enums | `varchar(40)` + `CHECK (col in (...))` or lookup table. JPA `@Enumerated(EnumType.STRING)`. |
| Text | `varchar(n)` with a real limit from the wiki; `text` only for free-form content. |
| Soft delete | Only if the wiki requires; `deleted_at timestamptz` + partial unique indexes `WHERE deleted_at IS NULL`. |
| PII | Listed in the wiki table catalog; encrypted/tokenised where required; retention job defined. |
| JSON | `jsonb` only for truly schemaless data; never for fields you filter or join on. |

## Indexes and queries
- Every FK column is indexed.
- Every repository query has a supporting index; verify with `EXPLAIN`.
- No unbounded `findAll()`; list endpoints are paged (keyset for large tables).
- No N+1: use fetch joins / `@EntityGraph`; collections are `LAZY`.
- `spring.jpa.hibernate.ddl-auto=validate` (never `update` or `create`).

## Transactions
- Service layer owns `@Transactional`; reads use `readOnly = true`.
- No remote HTTP calls inside a DB transaction; use the outbox.
- Keep transactions short; default isolation READ COMMITTED unless an ADR says otherwise.

## Flyway migrations
- File: `V<yyyyMMddHHmm>__<verb>_<object>.sql` (e.g. `V202610011200__create_orders.sql`).
- Applied migrations are immutable (hook-enforced). Fix forward with a new file.
- One logical change per file.
- Expand/contract: add nullable -> backfill -> constrain in a later release -> drop old in a later release.
- Large tables: `CREATE INDEX CONCURRENTLY` in its own migration with `-- flyway:executeInTransaction=false`.
- `DROP`, type narrowing, or rename require an ADR link and a rollback note in the file header.

## Platform tables (every service)
- `idempotency_keys` and `outbox_events` (see baseline migration).
