// Harness gates for pull requests. Use a Multibranch Pipeline with the Bitbucket Branch Source plugin
// (or Bitbucket Server integration) so CHANGE_TARGET is set for PR builds.
// Then require this build to pass in Bitbucket merge checks.
pipeline {
  agent any
  options {
    timestamps()
    disableConcurrentBuilds(abortPrevious: true)
  }
  tools {
    // Name must match a JDK 21 installation in Manage Jenkins > Tools.
    jdk 'jdk21'
  }
  environment {
    TESTCONTAINERS_RYUK_DISABLED = 'true'
  }
  stages {
    stage('Checkout full history') {
      steps {
        checkout scm
        sh 'git fetch --unshallow || true'
      }
    }
    stage('Checks') {
      parallel {
        stage('Flyway migration gates') {
          when { changeRequest() }
          steps {
            sh 'chmod +x scripts/harness-gates.sh && scripts/harness-gates.sh "$CHANGE_TARGET"'
          }
        }
        stage('Build, ArchUnit and error catalog tests') {
          steps {
            // Needs Docker on the agent for Testcontainers.
            // check = unit tests + ArchUnit + error catalog; jacoco* = coverage report and minimum-coverage gate.
            sh 'chmod +x gradlew && ./gradlew check jacocoTestReport jacocoTestCoverageVerification --no-daemon'
          }
          post {
            always {
              junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
              archiveArtifacts artifacts: 'build/reports/**', allowEmptyArchive: true
            }
          }
        }
      }
    }
    stage('Sonar analysis') {
      steps {
        // Server name must match Manage Jenkins > System > SonarQube servers.
        withSonarQubeEnv('sonar') {
          script {
            def args = '-Dsonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml'
            if (env.CHANGE_ID) {
              // PR analysis (needs Developer Edition or higher for PR decoration).
              args += " -Dsonar.pullrequest.key=${env.CHANGE_ID}" +
                      " -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH}" +
                      " -Dsonar.pullrequest.base=${env.CHANGE_TARGET}"
            }
            sh "./gradlew sonar ${args} --no-daemon"
          }
        }
      }
    }
    stage('Sonar quality gate') {
      steps {
        // Fails the PR build if the gate (new-code coverage, bugs, smells, duplication) fails.
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }
  }
}

