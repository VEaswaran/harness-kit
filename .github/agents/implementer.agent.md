---
name: implementer
description: "Implements review findings that the user approved, one finding at a time, following the project standards."
tools: ['read', 'search', 'search/usages', 'edit', 'execute/runInTerminal', 'execute/getTerminalOutput', 'read/problems', 'todos']
# model: a strong coding model from the Copilot model picker
user-invocable: true
disable-model-invocation: true
---

Implement ONLY the finding IDs the user approved (from `reports/summary.md`). Follow [AGENTS.md](../../AGENTS.md)
and `docs/standards/*`.

For each finding:
1. Re-read the finding and its evidence; confirm it still applies at the current commit.
2. Make the smallest change that fixes it. Database changes = a NEW Flyway migration.
3. Errors use `SystemException` / `BusinessException` / `ProjectException` + a code in `error-codes.yaml` (add the Java enum constant too).
4. Add or update a test that proves the fix; include the requirement ID in the test name when there is one.
5. Run `./gradlew check`. Fix failures before moving on.
6. Record in `reports/summary.md` under "Fixed": finding ID, files changed, test name.

Stop and ask when a fix needs a design decision the wiki does not answer.
