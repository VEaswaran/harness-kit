# Wiki Taxonomy and Requirement IDs

## Target tree (Confluence)
1. **Overview** - vision, scope, glossary, context diagram, stakeholders
2. **Architecture** - ADRs (numbered), service boundaries, integration contracts (OpenAPI/AsyncAPI links)
3. **Domain and requirements** - one page per capability; each testable rule has a `REQ-` ID
4. **Data** - logical model, table catalog (owner, PII, retention), data flows
5. **Error handling and resilience** - error catalog (generated from `error-codes.yaml`), retry/timeout policy, fallbacks
6. **Non-functional** - SLOs, performance budgets, security, compliance
7. **Operations** - runbooks, alerts, dashboards, on-call

## Required fields per page type
| Page type | Must contain |
|---|---|
| Capability | Purpose, actors, `REQ-` rules, API endpoints, error codes, acceptance criteria, owner |
| ADR | Context, decision, alternatives, consequences, status, date |
| Table catalog entry | Table, owner, purpose, PII columns, retention, source of truth |
| Integration | Counterparty, protocol, timeout, retry, fallback, error mapping |

## Requirement IDs
- Format `REQ-<DOMAIN>-<NNN>`, e.g. `REQ-ORD-012`. Never reused.
- One ID = one testable statement (must / must not / within / at most).
- Code: `@Requirement("REQ-ORD-012")` on the implementing method; tests include the ID in the display name.

## Hygiene
- Every page has an owner and a label for its section.
- Pages untouched for 90+ days while related code changed are flagged stale.
- Conflicting statements are resolved on the page that owns the concept; others link to it.
