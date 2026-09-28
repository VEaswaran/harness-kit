`harness-gates.sh <target-branch>` works in any CI. Bitbucket Pipelines calls it from `bitbucket-pipelines.yml`.
For Jenkins or Bamboo (Bitbucket Data Center), run it in the PR build with the PR's target branch, then `./gradlew check`.
