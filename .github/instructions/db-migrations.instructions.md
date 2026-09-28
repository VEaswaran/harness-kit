---
name: Database and Flyway
description: Database design and migration rules
applyTo: "**/db/migration/**,**/*Entity.java,**/*Repository.java"
---
- Never modify a migration that exists on the default branch (main/master/develop). Fix forward with a new `V<yyyyMMddHHmm>__<verb>_<object>.sql`.
- One logical change per file. Expand, then contract. Large-table indexes use `CREATE INDEX CONCURRENTLY` in their own non-transactional migration.
- Tables: plural snake_case, PK `id`, audit columns (`created_at`, `created_by`, `updated_at`, `updated_by`, timestamptz), `version` for optimistic locking on mutable aggregates.
- Money is `numeric(19,4)` + `currency char(3)`. Times are `timestamptz`. Enums are `varchar` + `CHECK`, JPA `EnumType.STRING`.
- Every FK column is indexed. Every repository query has a supporting index. No unbounded `findAll()`.
- Full rules: docs/standards/database-design-standard.md
