# CI/CD Configuration for Dual-Mode Testing

## Overview

This guide provides instructions for configuring CI/CD pipelines to enforce dual-mode test execution and prevent regression in execution mode parity.

## Requirements

### Mandatory Dual-Mode Testing

**Policy**: All new tests MUST verify both INTERPRETED and COMPILED execution modes.

**Enforcement**: CI/CD pipelines MUST fail when:
1. Tests produce different results between modes
2. New tests don't use dual-mode infrastructure
3. Coverage differs significantly (>5%) between modes

## Maven Configuration

### Running Dual-Mode Tests

**Default behavior** (both modes enabled):
```bash
mvn test
```

**Single mode for debugging** (INTERPRETED only):
```bash
mvn test -Dognl.test.dualMode.enabled=false
```

### Test Execution Commands

**Run all tests in dual-mode**:
```bash
mvn clean test
```

**Run specific test class**:
```bash
mvn test -Dtest=ArithmeticAndLogicalOperatorsTest
```

**Run specific test method in both modes**:
```bash
mvn test -Dtest=ArithmeticAndLogicalOperatorsTest#doubleValuedArithmeticExpressions
```

**Run with coverage**:
```bash
mvn clean test -Pcoverage
```

## GitHub Actions Configuration

### Sample Workflow

Create `.github/workflows/dual-mode-tests.yml`:

```yaml
name: Dual-Mode Testing

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  dual-mode-tests:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven

    - name: Run Dual-Mode Tests
      run: mvn clean test -B
      env:
        # Ensure dual-mode is enabled (default)
        MAVEN_OPTS: "-Dognl.test.dualMode.enabled=true"

    - name: Check Test Results
      if: always()
      run: |
        # Fail if any tests failed
        if [ -f target/surefire-reports/*.txt ]; then
          if grep -q "FAILURE\|ERROR" target/surefire-reports/*.txt; then
            echo "Tests failed - check for mode-specific failures"
            exit 1
          fi
        fi

    - name: Generate Coverage Report
      run: mvn jacoco:report -Pcoverage

    - name: Upload Test Results
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-results
        path: target/surefire-reports/

    - name: Upload Coverage Report
      uses: actions/upload-artifact@v3
      with:
        name: coverage-report
        path: target/site/jacoco/
```

### Pull Request Checks

Add to `.github/workflows/pr-checks.yml`:

```yaml
name: PR Dual-Mode Validation

on:
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  validate-dual-mode:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
      with:
        fetch-depth: 0  # Full history for comparison

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven

    - name: Check for New Tests
      id: check_tests
      run: |
        # Find new test files in this PR
        NEW_TESTS=$(git diff --name-only origin/${{ github.base_ref }}...HEAD | grep -E "Test\.java$" || true)
        echo "new_tests=$NEW_TESTS" >> $GITHUB_OUTPUT

        if [ -n "$NEW_TESTS" ]; then
          echo "New test files detected:"
          echo "$NEW_TESTS"
        fi

    - name: Validate Dual-Mode Usage
      if: steps.check_tests.outputs.new_tests != ''
      run: |
        # Check that new tests use dual-mode infrastructure
        INVALID_TESTS=""

        for TEST_FILE in ${{ steps.check_tests.outputs.new_tests }}; do
          if ! grep -q "@ParameterizedTest" "$TEST_FILE"; then
            INVALID_TESTS="$INVALID_TESTS\n$TEST_FILE"
          elif ! grep -q "OgnlExecutionMode" "$TEST_FILE"; then
            INVALID_TESTS="$INVALID_TESTS\n$TEST_FILE"
          fi
        done

        if [ -n "$INVALID_TESTS" ]; then
          echo "ERROR: The following test files don't use dual-mode infrastructure:"
          echo -e "$INVALID_TESTS"
          echo ""
          echo "All new tests must use @ParameterizedTest with OgnlExecutionMode."
          echo "See docs/DUAL_MODE_TESTING_GUIDE.md for examples."
          exit 1
        fi

    - name: Run Tests in Both Modes
      run: mvn clean test -B

    - name: Compare Mode Coverage
      run: |
        # This is a placeholder - actual implementation would need
        # custom tooling to track per-mode coverage
        echo "Coverage comparison would go here"
        # Could use jacoco reports and custom analysis
```

## Jenkins Configuration

### Jenkinsfile Example

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'JDK 17'
    }

    environment {
        DUAL_MODE_ENABLED = 'true'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Dual-Mode Tests') {
            steps {
                sh 'mvn clean test -B -Dognl.test.dualMode.enabled=${DUAL_MODE_ENABLED}'
            }
        }

        stage('Coverage Analysis') {
            steps {
                sh 'mvn jacoco:report -Pcoverage'

                // Publish coverage reports
                jacoco(
                    execPattern: '**/target/jacoco.exec',
                    classPattern: '**/target/classes',
                    sourcePattern: '**/src/main/java'
                )
            }
        }

        stage('Validate Mode Parity') {
            steps {
                script {
                    // Check for mode-specific failures in test reports
                    def testReports = findFiles(glob: '**/target/surefire-reports/*.xml')
                    def modeFailures = [:]

                    testReports.each { report ->
                        def content = readFile(report.path)

                        // Extract mode from test name: [1] mode=INTERPRETED
                        if (content.contains('mode=INTERPRETED') && content.contains('failure')) {
                            modeFailures['INTERPRETED'] = (modeFailures['INTERPRETED'] ?: 0) + 1
                        }
                        if (content.contains('mode=COMPILED') && content.contains('failure')) {
                            modeFailures['COMPILED'] = (modeFailures['COMPILED'] ?: 0) + 1
                        }
                    }

                    if (modeFailures.INTERPRETED != modeFailures.COMPILED) {
                        error("Mode parity violation detected! INTERPRETED: ${modeFailures.INTERPRETED}, COMPILED: ${modeFailures.COMPILED}")
                    }
                }
            }
        }
    }

    post {
        always {
            // Archive test results
            junit '**/target/surefire-reports/*.xml'

            // Archive coverage reports
            publishHTML([
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Coverage Report'
            ])
        }

        failure {
            echo 'Dual-mode tests failed! Check for execution mode discrepancies.'
        }
    }
}
```

## GitLab CI Configuration

### .gitlab-ci.yml Example

```yaml
stages:
  - test
  - validate
  - report

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  DUAL_MODE_ENABLED: "true"

cache:
  paths:
    - .m2/repository

dual-mode-tests:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn clean test -B -Dognl.test.dualMode.enabled=${DUAL_MODE_ENABLED}
  artifacts:
    when: always
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
    paths:
      - target/surefire-reports/
      - target/site/jacoco/

validate-new-tests:
  stage: validate
  image: alpine/git
  only:
    - merge_requests
  script:
    - |
      # Find new test files
      NEW_TESTS=$(git diff --name-only origin/${CI_MERGE_REQUEST_TARGET_BRANCH_NAME}...HEAD | grep -E "Test\.java$" || true)

      if [ -n "$NEW_TESTS" ]; then
        echo "Validating new test files for dual-mode usage..."

        for TEST_FILE in $NEW_TESTS; do
          if ! grep -q "@ParameterizedTest" "$TEST_FILE" || ! grep -q "OgnlExecutionMode" "$TEST_FILE"; then
            echo "ERROR: $TEST_FILE doesn't use dual-mode infrastructure"
            exit 1
          fi
        done

        echo "All new tests properly use dual-mode infrastructure ✓"
      fi

coverage-report:
  stage: report
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn jacoco:report -Pcoverage
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    paths:
      - target/site/jacoco/
```

## Pre-Commit Hooks

### Local Validation

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash

# Pre-commit hook to validate dual-mode test usage

echo "Checking for new test files..."

# Get staged test files
STAGED_TESTS=$(git diff --cached --name-only --diff-filter=A | grep -E "Test\.java$" || true)

if [ -z "$STAGED_TESTS" ]; then
    echo "No new test files detected"
    exit 0
fi

echo "Validating dual-mode usage in new tests..."

INVALID_TESTS=""

for TEST_FILE in $STAGED_TESTS; do
    if [ -f "$TEST_FILE" ]; then
        # Check for required dual-mode annotations
        if ! grep -q "@ParameterizedTest" "$TEST_FILE"; then
            INVALID_TESTS="$INVALID_TESTS\n  - $TEST_FILE (missing @ParameterizedTest)"
        elif ! grep -q "OgnlExecutionMode" "$TEST_FILE"; then
            INVALID_TESTS="$INVALID_TESTS\n  - $TEST_FILE (missing OgnlExecutionMode)"
        elif ! grep -q "DualModeTestUtils" "$TEST_FILE"; then
            INVALID_TESTS="$INVALID_TESTS\n  - $TEST_FILE (not using DualModeTestUtils)"
        fi
    fi
done

if [ -n "$INVALID_TESTS" ]; then
    echo ""
    echo "❌ COMMIT REJECTED: New tests must use dual-mode infrastructure"
    echo ""
    echo "Invalid test files:"
    echo -e "$INVALID_TESTS"
    echo ""
    echo "Please update your tests to use:"
    echo "  - @ParameterizedTest with @EnumSource(OgnlExecutionMode.class)"
    echo "  - DualModeTestUtils for expression evaluation"
    echo ""
    echo "See docs/DUAL_MODE_TESTING_GUIDE.md for examples"
    exit 1
fi

echo "✓ All new tests properly use dual-mode infrastructure"
exit 0
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Test Failure Analysis

### Identifying Mode-Specific Failures

When tests fail in CI/CD, use these commands to identify mode-specific issues:

**Check which mode failed**:
```bash
grep -r "mode=INTERPRETED" target/surefire-reports/ | grep -i failure
grep -r "mode=COMPILED" target/surefire-reports/ | grep -i failure
```

**Compare failure counts**:
```bash
# Count INTERPRETED failures
grep -r "mode=INTERPRETED.*failure" target/surefire-reports/*.xml | wc -l

# Count COMPILED failures
grep -r "mode=COMPILED.*failure" target/surefire-reports/*.xml | wc -l
```

**Extract failed test names**:
```bash
grep -A 5 "mode=INTERPRETED.*failure" target/surefire-reports/*.xml | grep "testcase"
```

## Coverage Enforcement

### Minimum Coverage Requirements

**Policy**: Both execution modes must maintain >80% code coverage.

**Check coverage**:
```bash
mvn jacoco:report -Pcoverage
cat target/site/jacoco/index.html | grep -oP '\d+%' | head -1
```

### Coverage Comparison Script

Create `scripts/compare-coverage.sh`:

```bash
#!/bin/bash
# Compare coverage between execution modes
# Note: Requires custom instrumentation to track per-mode coverage

COVERAGE_THRESHOLD=80
COVERAGE_DIFF_THRESHOLD=5

echo "Checking test coverage requirements..."

# Run tests with coverage
mvn clean test -Pcoverage

# Extract coverage percentage
COVERAGE=$(grep -oP 'Total.*?(\d+)%' target/site/jacoco/index.html | grep -oP '\d+' || echo "0")

echo "Total coverage: ${COVERAGE}%"

if [ "$COVERAGE" -lt "$COVERAGE_THRESHOLD" ]; then
    echo "❌ Coverage ${COVERAGE}% is below threshold ${COVERAGE_THRESHOLD}%"
    exit 1
fi

echo "✓ Coverage meets minimum requirement"
exit 0
```

## Monitoring and Alerts

### CI/CD Dashboard Metrics

Track these metrics in your CI/CD dashboard:

1. **Test Execution**:
   - Total tests run (should be 2x base count for dual-mode)
   - Pass rate for INTERPRETED mode
   - Pass rate for COMPILED mode
   - Mode parity violations

2. **Coverage**:
   - Overall code coverage
   - Coverage trend over time
   - Coverage by execution mode (if instrumented)

3. **Performance**:
   - Test execution time
   - Time difference between modes
   - Build duration trends

### Slack/Teams Notifications

Example webhook payload for failures:

```json
{
  "text": "⚠️ Dual-Mode Test Failure",
  "attachments": [
    {
      "color": "danger",
      "fields": [
        {
          "title": "Project",
          "value": "OGNL",
          "short": true
        },
        {
          "title": "Branch",
          "value": "${GIT_BRANCH}",
          "short": true
        },
        {
          "title": "Mode Parity",
          "value": "VIOLATED - Different results between modes",
          "short": false
        },
        {
          "title": "Action Required",
          "value": "Check test reports and fix mode-specific bugs",
          "short": false
        }
      ]
    }
  ]
}
```

## Troubleshooting

### Common Issues

**Issue**: Tests pass individually but fail in dual-mode
- **Cause**: Test state pollution between modes
- **Solution**: Ensure `@BeforeEach` properly initializes state

**Issue**: Different exceptions in different modes
- **Cause**: Mode-specific error handling
- **Solution**: Use `Exception.class` in `assertThrows()`, not specific types

**Issue**: Coverage drops in CI but not locally
- **Cause**: CI might disable dual-mode by default
- **Solution**: Explicitly set `ognl.test.dualMode.enabled=true`

## Best Practices

1. **Always run dual-mode locally before pushing**:
   ```bash
   mvn clean test
   ```

2. **Check CI logs for mode-specific failures**:
   - Look for `[1] mode=INTERPRETED` vs `[2] mode=COMPILED` in test names

3. **Use pre-commit hooks** to catch issues early

4. **Monitor coverage trends** to prevent regression

5. **Document mode-specific behaviors** when intentional

## References

- [Dual-Mode Testing Guide](DUAL_MODE_TESTING_GUIDE.md)
- [Phase 2 Test Conversion](PHASE_2_TEST_CONVERSION.md)
- [Code Review Checklist](CODE_REVIEW_CHECKLIST.md)
