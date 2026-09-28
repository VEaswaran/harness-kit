#!/usr/bin/env bash
# Hard gates for every pull request, whoever (person or AI) wrote it.
# Usage: scripts/harness-gates.sh <target-branch>     e.g. scripts/harness-gates.sh develop
set -euo pipefail
target="${1:?target branch required}"
git fetch -q origin "+refs/heads/${target}:refs/remotes/origin/${target}"
range="origin/${target}...HEAD"
fail=0

changed="$(git diff --name-only --diff-filter=MDR "$range" -- '*/db/migration/*' || true)"
if [[ -n "$changed" ]]; then
  echo "ERROR: applied Flyway migrations are immutable. Changed or deleted:"; echo "$changed"; fail=1
fi

bad="$(git diff --name-only --diff-filter=A "$range" -- '*/db/migration/*.sql' \
      | xargs -r -n1 basename | grep -Ev '^V[0-9]{12}__[a-z0-9_]+\.sql$' || true)"
if [[ -n "$bad" ]]; then
  echo "ERROR: bad migration name(s), use V<yyyyMMddHHmm>__verb_object.sql:"; echo "$bad"; fail=1
fi

[[ $fail -eq 0 ]] || exit 1
echo "Migration gates passed."
