# PR quality gates: unit tests, coverage, Sonar

Every PR build (Jenkins `Jenkinsfile`) must pass all of these before merge.

| Gate | Tool | Fails the PR when |
|---|---|---|
| Migration rules | `scripts/harness-gates.sh` | An existing migration is changed, or a new one is misnamed |
| Unit and architecture tests | `./gradlew check` | Any test fails (JUnit, ArchUnit, error catalog) |
| Coverage | JaCoCo `jacocoTestCoverageVerification` | Line coverage is below the minimum (80% suggested) |
| Static analysis and new-code coverage | SonarQube quality gate | The gate fails (recommended: 80% coverage on new code, no new bugs or vulnerabilities, duplication under 3%) |

## Required Gradle setup (`build.gradle.kts`)

The Gradle build files are not in this repo copy. Add them to your project:

```kotlin
plugins {
    java
    jacoco
    id("org.sonarqube") version "5.1.0.4882"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports { xml.required = true; html.required = true }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { counter = "LINE"; minimum = "0.80".toBigDecimal() } }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "your-project-key")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}
```

## Jenkins and Sonar setup

1. Install the SonarQube Scanner plugin. Add the server in Manage Jenkins > System, named `sonar`, with a token credential.
2. In SonarQube, add a webhook to `<jenkins>/sonarqube-webhook/` so `waitForQualityGate` returns.
3. Bind the project to Bitbucket (DevOps Platform Integration) for PR decoration. This needs Developer Edition or higher.
4. In Bitbucket, require the Jenkins build to succeed as a merge check.

