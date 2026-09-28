---
name: run-workstreams
description: Run the four review workstreams in parallel and merge their reports
agent: harness-orchestrator
argument-hint: "Optional: ws1 ws2 ws3 ws4 (default: all)"
---

Run the workstreams: ${input:workstreams:all}.
Follow your steps exactly. Report-only: write only under `reports/`.
