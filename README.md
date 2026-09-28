# Agentic Engineering Harness Kit — GitHub Copilot edition (Bitbucket + Gradle)

Same four workstreams as the Claude Code kit, built only on GitHub Copilot features, and usable with any
model in your Copilot model picker.

| Harness part | Copilot feature | File(s) |
|---|---|---|
| Project memory | Custom instructions | `AGENTS.md`, `.github/copilot-instructions.md` |
| Path-specific rules | Instruction files with `applyTo` | `.github/instructions/*.instructions.md` |
| Orchestrator | Custom agent with subagents | `.github/agents/harness-orchestrator.agent.md` |
| WS1-WS4 | Custom agents (read-only tools) | `.github/agents/{wiki-curator,design-verifier,db-design-reviewer,resilience-reviewer}.agent.md` |
| Fixer | Custom agent (edit tools) | `.github/agents/implementer.agent.md` |
| `/run-workstreams` | Prompt file | `.github/prompts/run-workstreams.prompt.md` |
| Confluence | MCP server in VS Code | `.vscode/mcp.json` |
| Guardrail hook | Copilot hooks (VS Code preview, Copilot CLI) | `.github/hooks/harness.json` + `scripts/` |
| Hard gates | Bitbucket Pipelines (or any CI) | `bitbucket-pipelines.yml`, `scripts/harness-gates.sh` |
| Fix tickets | Jira via the same Atlassian MCP server | `.github/prompts/create-fix-tickets.prompt.md` |
| Parallel fix work | Git worktrees + one VS Code window each | (see step 5) |

## 1. Prerequisites

- VS Code (recent version) with GitHub Copilot Chat, signed in with your org account.
- Your Copilot admin has enabled **MCP servers in Copilot**.
- `git`, `python3` (for the hook; on Windows run hooks through Git Bash), JDK 17+, Gradle wrapper (`./gradlew`), Docker (Testcontainers).

The `.github/` folder name is just where VS Code Copilot looks for agents, prompts and instructions. It works the same in a Bitbucket repo.

## 2. Copy into the repo

Copy everything into the repo root. Replace `com.example.platform`, `ORD`/`PAY` and `CONFLUENCE_SPACE` in `AGENTS.md`.
Add Gradle dependencies (versions: check Maven Central).

Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    testImplementation("org.yaml:snakeyaml")
}
tasks.test { useJUnitPlatform() }
```

Groovy DSL (`build.gradle`):

```groovy
dependencies {
    implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.2.0'
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
    testImplementation 'org.yaml:snakeyaml'
}
test { useJUnitPlatform() }
```

`spring-boot-starter-test` (already in a Spring Boot project) provides JUnit 5 and AssertJ used by the tests.

```bash
chmod +x .github/hooks/scripts/*.sh scripts/*.sh
git update-index --chmod=+x .github/hooks/scripts/*.sh scripts/*.sh   # keeps the bit for Windows committers
```

## 3. Connect Confluence

Open `.vscode/mcp.json` in VS Code and click **Start** on the `atlassian` server, then complete the Atlassian OAuth login.
VS Code asks before running MCP tools: approve read/search tools, and **never "always allow" create/update tools**.

## 4. Run the four workstreams in parallel (report-only)

In Copilot Chat:

```
/run-workstreams            (all four)
/run-workstreams ws3 ws4    (some)
```

The `harness-orchestrator` agent starts the four agents as parallel subagents. They have no edit tools, so they
can only read and return reports; the orchestrator saves them under `reports/` and writes `reports/summary.md`.

You can also pick any single agent (e.g. `db-design-reviewer`) from the agent dropdown and chat with it directly.

Models: set `model:` in each agent file to a name from your model picker (strongest reasoning model for the
orchestrator, WS3 and WS4; a faster model for WS1 and WS2), or leave it unset to use the model selected in chat.

## 5. Fix approved findings

- **Small fixes, locally:** reply with the approved IDs, then click the **Fix approved findings** handoff (goes to the `implementer` agent).
- **Tickets:** run `/create-fix-tickets PROJ: WS3-001, WS4-002` to draft one Jira task per workstream; say "create" to file them.
- **In parallel:** one git worktree and one VS Code window per ticket, each running the `implementer` agent:

```bash
git worktree add ../wt-ws3 -b ws3/PROJ-123
git worktree add ../wt-ws4 -b ws4/PROJ-124
code ../wt-ws3 ; code ../wt-ws4
# in each window: pick the implementer agent -> "Fix PROJ-123"
```

  Push each branch and open a Bitbucket pull request. (Copilot's cloud agent only works on GitHub repositories, so
  on Bitbucket the parallel work runs on your machine.)

## 6. Gates that do not depend on the AI

`bitbucket-pipelines.yml` runs on every pull request, in parallel:
- `scripts/harness-gates.sh`: blocks edits to existing migrations and checks new migration names.
- `./gradlew check`: build, ArchUnit rules and the error catalog test.

Enable Pipelines for the repo, then add a merge check that requires passing builds on your default branch
(Repository settings > Branch restrictions / Merge checks). On Bitbucket Cloud Standard, merge checks warn but don't block;
Premium can enforce them.

Bitbucket Data Center (no Pipelines): call `scripts/harness-gates.sh <target-branch>` and `./gradlew check` from your
Jenkins or Bamboo PR build instead.

The hook in `.github/hooks/` is an early warning inside the agent session; the pipeline is the real guarantee.
It finds your default branch from `origin/HEAD`; set `HARNESS_BASE_BRANCH=origin/develop` to override.

## Notes

- Tool names in agent files (`read`, `search`, `edit`, `execute/runInTerminal`, `agent`, `atlassian/*`) follow the VS Code
  tools reference; if VS Code flags an unknown tool, pick it again with the **Configure tools** button.
- The Atlassian MCP server also covers Jira and Bitbucket, so agents can read tickets and PRs with the same login.
- If your org also enables the **Claude agent** inside VS Code, the Claude Code kit (`CLAUDE.md`, `.claude/`) works there too.
