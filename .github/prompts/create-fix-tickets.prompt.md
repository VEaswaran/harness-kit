---
name: create-fix-tickets
description: Turn approved findings from reports/summary.md into one Jira task per workstream
agent: harness-orchestrator
argument-hint: "Approved finding IDs and Jira project key, e.g. 'PROJ: WS3-001, WS3-004, WS4-002'"
tools: ['read', 'search', 'atlassian/*']
---

Approved findings and Jira project: ${input:approved}

1. Read `reports/summary.md` and the per-workstream reports for exactly these finding IDs. Ignore every other finding.
2. Group them by workstream. Draft one Jira task per workstream:
   - Summary: `[WSn] Fix approved findings: <short theme>`
   - Description: a table of the findings (ID, severity, location, finding, fix), then these rules:
     follow AGENTS.md; new Flyway migrations only; one commit per finding with the ID in the message;
     `./gradlew check` must pass; branch name `wsN/<ticket-key>`.
3. Show me the drafts. Create them in Jira only after I say "create".
