# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OGNL (Object-Graph Navigation Language) is an expression language for getting and setting properties of Java objects,
used by frameworks including Apache Struts. Key features: property navigation (JavaBeans), method invocation,
collection operations (projection/selection), lambda expressions, type conversion, and member access control.

## Development Environment

- **Language**: Java 17 (CI also tests against Java 21 and 25)
- **Build Tool**: Maven with wrapper (`./mvnw`), multi-module project
- **Testing**: JUnit Jupiter 6.x
- **Parser Generator**: JJTree + JavaCC (grammar at `ognl/src/main/jjtree/ognl.jjt`)

## Project Structure

- **ognl/** — Core library (source: `ognl/src/main/java/ognl/`, tests: `ognl/src/test/java/ognl/`)
- **benchmarks/** — JMH performance benchmarks
- **docs/** — Language Guide, Developer Guide, Version Notes

## Essential Commands

```bash
# Build
./mvnw clean install                    # Full build + install
./mvnw compile                          # Compile only (includes JavaCC parser generation)

# Tests
./mvnw test                             # Full test suite
./mvnw test -pl ognl                    # Core module tests only
./mvnw test -pl ognl -Dtest=ClassName   # Single test class
./mvnw test -pl ognl -Dtest=Pattern -Dsurefire.failIfNoSpecifiedTests=false  # Pattern match

# Coverage & Quality
./mvnw clean test -Pcoverage            # JaCoCo coverage report
./mvnw sonar:sonar -Pcoverage           # SonarCloud analysis

# Benchmarks
cd benchmarks && ../mvnw clean install && java -jar target/benchmarks.jar
```

## JavaCC Parser Generation

- `ognl/src/main/jjtree/ognl.jjt` is the **only** grammar source — edit the grammar there
- During `generate-sources`, the `jjtree-javacc` goal runs JJTree then JavaCC on it, emitting
  `OgnlParser`, `OgnlParserConstants`, `OgnlParserTokenManager`, `Token`, `ParseException`,
  `TokenMgrError` and `JavaCharStream` into `ognl/target/generated-sources/java/`
- The 46 `AST*.java` node classes, `JJTOgnlParserState` and `OgnlParserTreeConstants` are
  hand-maintained in `ognl/src/main/java/ognl/`; JJTree skips generating anything that already
  exists in the source root, so they are never clobbered
- To generate AST scaffolding for a **new** node type, uncomment `<nodePackage>*.jtree</nodePackage>`
  in `ognl/pom.xml`, build, and copy what you need out of the `ognl.jtree` package
- There is no checked-in `ognl.jj`. It used to exist as committed JJTree output that was then
  hand-edited, which let it drift from the `.jjt` for years (#613)

## Architecture

### Evaluation Flow

1. Expression string → parsed into AST tree via JavaCC (`OgnlParser`)
2. AST evaluated against a root object within an `OgnlContext`
3. Each AST node type handles its own evaluation via `SimpleNode.getValue()`/`setValue()`
4. `OgnlRuntime` resolves properties, methods, and fields via reflection (with caching)
5. Results pass through `TypeConverter` when type coercion is needed

## SonarCloud

- **Project**: `orphan-oss_ognl` — https://sonarcloud.io/project/overview?id=orphan-oss_ognl
- **Quality Gate**: Must pass for all PRs
- **PR issues URL**: `https://sonarcloud.io/project/issues?issueStatuses=OPEN%2CCONFIRMED&sinceLeakPeriod=true&pullRequest=[PR_NUMBER]&id=orphan-oss_ognl`
- Focus on new issues only, not pre-existing ones
- Aim for >80% coverage on new code

## Critical Development Rules

### Context Root Preservation

**Always preserve the original context root during nested evaluations.** The `addDefaultContext()` method in `Ognl.java`
can overwrite original root contexts during list processing. Preserve original root when:
- Initial context exists with non-null root
- Context contains user variables (`size() > 0`)
- New root differs from existing root (indicates nested evaluation)
- `#root` must always refer to original context root
- `#this` changes scope during collection iteration
- Preserve user context variables during projection/selection (`ASTProject`/`ASTSelect`)

### Constraints

- Public methods in `Ognl` class are stable API — maintain backward compatibility
- Respect `MemberAccess` restrictions for private/protected access
- Honor expression length limits (`expressionMaxLength`)
- Use stricter invocation mode to prevent dangerous method calls