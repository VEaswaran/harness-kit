# <WS number and name> report

- Run date:
- Commit SHA / branch:
- Confluence space:
- Scope: (what was reviewed)

## Summary
One paragraph: overall state and the top 3 risks. Counts: Blocker n / Major n / Minor n.

## Findings

| ID | Severity | Req / page | Location | Finding | Evidence | Fix | Route to |
|---|---|---|---|---|---|---|---|
| WS3-001 | Blocker | Data > Orders | V202610011200__create_orders.sql | `amount` is `double precision` | line 7 | New migration to `numeric(19,4)` | - |

Severity: **Blocker** = data loss, money, security or integrity risk / release stopper; **Major** = design violation or missing behaviour; **Minor** = hygiene.

## Unknowns
Questions the wiki does not answer (never guessed).
