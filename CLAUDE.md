# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OGNL (Object-Graph Navigation Language) is an expression language for Java that provides powerful object graph
navigation capabilities. It is used by various frameworks including Apache Struts and supports features like:

- Property navigation with JavaBeans patterns
- Method invocation with dynamic type resolution
- Collection operations (projection, selection)
- Lambda expressions and evaluation contexts
- Type conversion and member access control

## Development Environment

- **Language**: Java 17
- **Build Tool**: Maven (multi-module project)
- **Testing**: JUnit 5 (Jupiter)
- **Parser Generator**: JavaCC (for OGNL expression grammar)

## Project Structure

This is a multi-module Maven project:

- **ognl/** - Core OGNL library (main module)
    - Source: `ognl/src/main/java/ognl/`
    - Tests: `ognl/src/test/java/ognl/`
    - Grammar: `ognl/src/main/javacc/ognl.jj` (JavaCC grammar file)
- **benchmarks/** - JMH performance benchmarks
- **docs/** - Documentation (Language Guide, Developer Guide, Version Notes)

## Essential Commands

### Building and Testing

- `mvn clean install` - Clean, build, and install to local repository (default goal is `install`)
- `mvn test` - Run full test suite (607 tests across all modules)
- `cd ognl && mvn test` - Run tests in core module only
- `mvn test -Dtest=ClassName` - Run specific test class (e.g., `-Dtest=OgnlContextTest`)
- `mvn test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=Pattern` - Run tests matching pattern without failing if none
  found
- `mvn compile` - Compile main sources (includes JavaCC parser generation)

### Code Coverage and Quality

- `mvn clean test -Pcoverage` - Run tests with JaCoCo coverage (generates XML report for SonarCloud)
- `mvn sonar:sonar -Pcoverage` - Run SonarCloud analysis (requires coverage profile)

**SonarCloud Integration:**

- **Project Key:** `orphan-oss_ognl`
- **Project URL:** https://sonarcloud.io/project/overview?id=orphan-oss_ognl
- **Quality Gate:** Must pass for all PRs
- **New Code Period:** Since last analysis on main branch

**Viewing SonarCloud Issues:**

To view issues for a specific pull request:
```
https://sonarcloud.io/project/issues?issueStatuses=OPEN%2CCONFIRMED&sinceLeakPeriod=true&pullRequest=[PR_NUMBER]&id=orphan-oss_ognl
```

Example for PR #496:
```
https://sonarcloud.io/project/issues?issueStatuses=OPEN%2CCONFIRMED&sinceLeakPeriod=true&pullRequest=496&id=orphan-oss_ognl
```

**Using SonarQube MCP Tools:**

When addressing SonarCloud issues, use the available MCP tools:

```java
// Search for issues in the project
mcp__sonarqube__search_sonar_issues_in_projects(
    projects: ["orphan-oss_ognl"],
    pullRequestId: "496"
)

// Get details about a specific rule
mcp__sonarqube__show_rule(key: "java:S3776")

// Change issue status (accept, falsepositive, reopen)
mcp__sonarqube__change_sonar_issue_status(
    key: "issue-key",
    status: ["accept"]
)
```

**Common SonarCloud Rules for OGNL:**

- **java:S3776** - Cognitive Complexity (threshold: 15)
  - Extract complex conditions into helper methods
  - Reduce nesting levels
  - Break down large methods

- **java:S1066** - Mergeable if statements
  - Combine consecutive if statements when possible

- **java:S1161** - Missing @Override annotation
  - Always add @Override for overridden methods

- **java:S6201** - Pattern matching for instanceof
  - Use Java 16+ pattern matching: `if (obj instanceof Type type)`

- **java:S1192** - String literals duplication
  - Extract repeated string literals as constants

- **java:S127** - Loop counter modification
  - Avoid modifying loop counters within loop body

**SonarCloud Best Practices:**

1. **Address New Issues Only:** Focus on issues introduced in your PR, not pre-existing ones
2. **Run Analysis Locally:** Use `mvn sonar:sonar -Pcoverage` before pushing
3. **Review Quality Gate:** Ensure all new code meets quality standards
4. **Document Suppressions:** If an issue must be accepted, document why in commit message
5. **Maintain Coverage:** Aim for >80% code coverage on new code

### Benchmarks

- `cd benchmarks && mvn clean install` - Build benchmarks uber-jar
- `java -jar benchmarks/target/benchmarks.jar` - Run JMH benchmarks

### JavaCC Parser Generation

- Parser is auto-generated from `ognl/src/main/javacc/ognl.jj` during compilation
- Generated files go to `ognl/target/generated-sources/java/`
- To regenerate AST files, uncomment `<nodePackage>*.jtree</nodePackage>` in pom.xml and change goal to `jtree-javacc`

## Feature Development Rules

### 1. Context and Root Object Handling

**Critical Rule**: Never break context root preservation during nested evaluations.

- **Issue**: The `addDefaultContext()` method in `Ognl.java` can overwrite original root contexts during list processing
- **Solution Pattern**: Preserve original root when:
    - Initial context exists with non-null root
    - Context contains user variables (`size() > 0`)
    - New root differs from existing root (indicates nested evaluation)
- **Test Coverage**: Always add tests for context preservation in collection operations

### 2. Backward Compatibility

**Mandatory**: All changes must maintain backward compatibility.

- **Verification**: Full test suite (607 tests) must pass
- **Regression Testing**: Run existing tests before and after changes
- **API Stability**: Public methods in `Ognl` class are part of stable API
- **Default Behavior**: When in doubt, preserve existing behavior

### 3. Test-Driven Development

**Required Process**:

1. Create comprehensive unit tests that reproduce the issue
2. Verify tests fail with current implementation
3. Implement fix with minimal scope
4. Verify tests pass and no regressions occur
5. Test edge cases and error conditions

**Test Naming**: Use descriptive names that explain the scenario being tested

```java
// Good
testContextRootPreservationWithListSelection()

testIssue390ReproduceBug()

// Bad
testBug()

testContext()
```

### 4. Expression Evaluation Safety

**Security Rules**:

- Respect `MemberAccess` restrictions for private/protected access
- Honor expression length limits (`expressionMaxLength`)
- Validate all user inputs in expression parsing
- Use stricter invocation mode to prevent dangerous method calls

**Performance Rules**:

- Cache parsed expressions when possible
- Use pre-compiled accessors for repeated evaluations
- Avoid reflection when direct access is available

### 5. Collection and List Processing

**Key Considerations**:

- `#root` should always refer to original context root
- `#this` changes scope during collection iteration
- Preserve user context variables during projection/selection
- Handle empty collections and null elements gracefully

**Example Pattern**:

```java
// In ASTProject/ASTSelect - preserve context during iteration
for(Enumeration<?> e = elementsAccessor.getElements(source); e.

hasMoreElements(); ){
Object next = e.nextElement();
// Use context that preserves original root but updates current object
Object result = expr.getValue(preservedContext, next);
}
```

### 6. Error Handling and Diagnostics

**Exception Hierarchy**:

- `OgnlException` - Base exception for OGNL operations
- `NoSuchPropertyException` - Property not found
- `MethodFailedException` - Method invocation failed
- `ExpressionSyntaxException` - Malformed expression

**Debugging Support**:

- Enable tracing with `context.setTraceEvaluations(true)`
- Use `context.getLastEvaluation()` for error analysis
- Provide meaningful error messages with context

### 7. Type Conversion and Coercion

**Conversion Rules**:

- Numeric types follow widening precedence
- String concatenation for non-numeric operations
- Boolean coercion: null/zero → false, non-null/non-zero → true
- Collection interpretation varies by type (arrays, Lists, Maps, etc.)

### 8. Memory and Performance

**Optimization Guidelines**:

- Use object pooling for frequently created objects (like `Evaluation`)
- Cache class metadata and method lookups
- Minimize reflection overhead with compiled accessors
- Clear evaluation stacks and temporary objects

### 9. Null Handling

**Null Safety**:

- Support `NullHandler` for custom null behaviors
- Chain navigation should fail gracefully on null intermediate values
- Distinguish between null properties and missing properties
- Handle null in collections and projections

### 10. Code Quality Standards

**Implementation Rules**:

- Follow existing code patterns and naming conventions
- Add comprehensive JavaDoc for public APIs
- Include performance considerations in comments
- Use meaningful variable names that reflect OGNL concepts

**Testing Standards**:

- Test both positive and negative cases
- Include boundary conditions and edge cases
- Test with different object types (POJOs, Maps, Collections)
- Verify proper exception handling and error messages

## Common Pitfalls to Avoid

1. **Context Root Overwriting**: Always preserve original root during nested evaluations
2. **Type Confusion**: Remember that OGNL is dynamically typed - handle type coercion carefully
3. **Infinite Recursion**: Guard against circular object references in evaluation
4. **Memory Leaks**: Clear evaluation contexts and cached references appropriately
5. **Security Bypass**: Never allow unrestricted member access or dangerous method calls

## Issue Analysis Process

When analyzing OGNL issues:

1. **Understand the Expression**: Parse and break down the OGNL expression
2. **Trace Evaluation Flow**: Follow the evaluation path through AST nodes
3. **Check Context State**: Examine root, variables, and evaluation stack
4. **Identify Interaction Points**: Look for where features interact (collections + context, etc.)
5. **Create Minimal Reproduction**: Strip down to essential elements
6. **Test Fix Isolation**: Ensure fix doesn't affect unrelated functionality

## File Structure Understanding

- **Core Evaluation**: `Ognl.java`, `OgnlContext.java`, `SimpleNode.java`
- **AST Nodes**: `AST*.java` files for different expression types
- **Property Access**: `*PropertyAccessor.java` for different object types
- **Type System**: `OgnlRuntime.java`, `TypeConverter.java`
- **Collections**: `*ElementsAccessor.java` for iteration support
- **Security**: `MemberAccess.java`, `AbstractMemberAccess.java`

## Testing Strategy

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test expression evaluation end-to-end
- **Performance Tests**: Benchmark critical paths and caching
- **Security Tests**: Verify access restrictions and input validation
- **Regression Tests**: Maintain coverage for all reported issues

Following these guidelines ensures robust, backward-compatible, and well-tested enhancements to the OGNL library.