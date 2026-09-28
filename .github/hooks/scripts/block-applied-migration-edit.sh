#!/usr/bin/env bash
# preToolUse hook for Copilot (CLI, cloud agent, VS Code): deny edits to Flyway migrations that
# already exist on the default branch (origin/HEAD, or HARNESS_BASE_BRANCH). Handles both the Copilot input shape (toolName/toolArgs)
# and the VS Code/Claude shape (tool_name/tool_input). Needs python3 (or python) and git.
set -uo pipefail
input="$(cat)"
PY="$(command -v python3 || command -v python || true)"
[[ -z "$PY" ]] && exit 0
file="$(printf '%s' "$input" | "$PY" -c '
import json, sys
try:
    d = json.load(sys.stdin)
except Exception:
    sys.exit(0)
def walk(o):
    if isinstance(o, str):
        try:
            o = json.loads(o)
        except Exception:
            return
    if isinstance(o, dict):
        for k, v in o.items():
            if k in ("path", "file_path", "filePath") and isinstance(v, str):
                print(v); return True
            if isinstance(v, (dict, str, list)) and walk(v):
                return True
    if isinstance(o, list):
        for v in o:
            if walk(v): return True
for key in ("toolArgs", "tool_input", "toolInput"):
    if key in d and walk(d[key]):
        break
')"
[[ -z "$file" ]] && exit 0
case "$file" in
  *db/migration/V*.sql|*db/migration/R*.sql) ;;
  *) exit 0 ;;
esac
root="$(git rev-parse --show-toplevel 2>/dev/null)" || exit 0
cd "$root"
rel="${file#"$root"/}"
base="${HARNESS_BASE_BRANCH:-}"
if [[ -z "$base" ]]; then
  base="$(git symbolic-ref -q --short refs/remotes/origin/HEAD 2>/dev/null || true)"
fi
if [[ -z "$base" ]]; then
  for b in origin/main origin/master origin/develop main master develop; do
    git rev-parse --verify -q "$b" >/dev/null && { base="$b"; break; }
  done
fi
[[ -z "$base" ]] && exit 0
if git cat-file -e "${base}:${rel}" 2>/dev/null; then
  reason="${rel} is already on ${base}. Flyway migrations are immutable; create a new V<timestamp>__*.sql instead."
  printf '{"permissionDecision":"deny","permissionDecisionReason":"%s","hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"%s"}}\n' "$reason" "$reason"
  echo "BLOCKED: $reason" >&2
fi
exit 0
