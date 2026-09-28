Follow the project rules in [AGENTS.md](../AGENTS.md). The most important ones:

- Every error is a `SystemException`, `BusinessException` or `ProjectException` with a code from `src/main/resources/error-codes.yaml` (see `docs/standards/error-handling-standard.md`).
- Never edit an existing Flyway migration; add a new `V<yyyyMMddHHmm>__<verb>_<object>.sql`.
- Public domain service methods carry `@Requirement("REQ-...")` linking to the Confluence requirement.
- Cite `file:line` or a quoted wiki line for every review finding. Write `unknown` instead of guessing.
