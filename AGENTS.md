# Agent instructions (GitHub Copilot, all models)

## Context
- Greenfield service. Java 17+, Spring Boot 3.x, Spring Data JPA, PostgreSQL, Flyway, Resilience4j, JUnit 5, Testcontainers.
- Design source of truth: Confluence space `CONFLUENCE_SPACE` (set your key here), read through the `atlassian` MCP server.
- Base package: `com.example.platform`. Domains: `order` (ORD), `payment` (PAY). Add yours.

## Standards (read before reviewing or writing code)
- Errors: [error-handling-standard.md](docs/standards/error-handling-standard.md)
- Database: [database-design-standard.md](docs/standards/database-design-standard.md)
- Wiki structure and requirement IDs: [wiki-taxonomy.md](docs/standards/wiki-taxonomy.md)
- Report format for every review agent: [report-template.md](docs/standards/report-template.md)

## Rules for every agent and every model
1. Cite evidence for every finding: `path/File.java:line`, a migration file name, or a quoted Confluence line with the page title.
2. If the wiki is silent, write `unknown` and raise a gap. Never invent a requirement.
3. Review runs are report-only. Only the orchestrator writes files, and only under `reports/`.
4. Never edit an existing Flyway migration. Create a new one.
5. Never create or update a Confluence page without explicit user approval in this chat.
6. Every thrown error is a `SystemException`, `BusinessException` or `ProjectException` with a code from `src/main/resources/error-codes.yaml`.
7. Public methods in `..domain..` services carry `@Requirement("REQ-<DOMAIN>-<NNN>")`.

## Commands
- Build: `./gradlew check`
- Architecture + catalog tests: `./gradlew test --tests '*ArchitectureTest' --tests '*ErrorCatalogTest'`
- Parallel review: run the `run-workstreams` prompt (type `/run-workstreams` in Copilot Chat)

## Repository
- Hosted on Bitbucket; default branch is the one `origin/HEAD` points to. Pull requests run `bitbucket-pipelines.yml`.
- Build tool is Gradle (wrapper `./gradlew`). Never add Maven files.
