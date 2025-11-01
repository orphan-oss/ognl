# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OGNL (Object-Graph Navigation Language) is an expression language for Java providing object graph navigation, method
invocation, collection operations (selection `{? ...}`, projection `{...}`), and lambda expressions with `#this` and
`#root` references.

**Important**: This is the `ognl-3-4-x` branch (Java 8 compatible). The main branch uses Java 17+.

## Development Environment

- **Java**: Version 8 (source/target 1.8)
- **Build**: Maven with wrapper (`./mvnw`)
- **Testing**: JUnit 5 with JUnit 4 vintage compatibility
- **Parser**: JavaCC-based (`src/main/javacc/`, `src/main/jjtree/`)

## Essential Commands

```bash
# Build and run all tests (947 tests expected)
./mvnw clean test

# Run specific test
./mvnw test -Dtest=Issue472CustomMethodAccessorTest

# Build without tests
./mvnw clean package -DskipTests=true

# Coverage report
./mvnw clean test -Pcoverage

# Regenerate parser (after grammar changes)
./mvnw clean generate-sources
```

## Architecture

### Expression Evaluation Flow

1. **Parse**: `Ognl.parseExpression(String)` → AST tree
2. **Evaluate**: `Ognl.getValue(tree, context, root)` → Walk AST against root object
3. **Context**: `OgnlContext` preserves root object and variables throughout evaluation

**CRITICAL**: During nested evaluations (e.g., list operations calling `Ognl.getValue(expr, context, listItem)`), the
`root` parameter changes but `context.getRoot()` must remain constant for `#root` references to work.

### Core Components

**ognl.Ognl** - Main API entry point

- `parseExpression(String)` - Parse to AST
- `getValue(tree, context, root)` - Evaluate expression
- `setValue(tree, context, root, value)` - Set property
- `createDefaultContext(root)` - Create context

**ognl.OgnlContext** - Execution context

- `getRoot()` - Original root object (must persist across nested evaluations)
- `getCurrentObject()` - Current evaluation target (changes during navigation)
- Variables: `#root` (context root), `#this` (current item), `#varName` (custom variables)

**ognl.AST\*** - Abstract Syntax Tree nodes

- `ASTChain` - Property chains (`user.address.city`)
- `ASTProperty` - Single property access
- `ASTMethod` - Method invocation
- `ASTSelect` - Collection selection `{? condition}`
- `ASTProject` - Collection projection `{expression}`
- `ASTRootVarRef` - `#root` references
- `ASTThisVarRef` - `#this` references

**ognl.OgnlRuntime** - Runtime utilities

- Method/property caching for performance
- Type conversion registry
- Accessor registration

**Accessor System** - Extensibility for custom access

- `PropertyAccessor` - Custom property get/set
- `MethodAccessor` - Custom method invocation
- `ElementsAccessor` - Collection element access

### Exception Hierarchy

- `OgnlException` (base, checked)
    - `NoSuchPropertyException` - Property doesn't exist
    - `InappropriateExpressionException` - Wrong expression type for operation
    - `MethodFailedException` - Method invocation failed
    - `ExpressionSyntaxException` - Invalid syntax

## Critical Development Rules

### 1. Context Root Preservation (MOST IMPORTANT)

**Problem**: When evaluating nested expressions (list operations with lambdas), `Ognl.getValue()` is called with list
items as the `root` parameter. The root parameter changes, but `context.getRoot()` must remain the original root for
`#root` references.

**Rules**:

- Never create new context in `Ognl.getValue()` that overwrites `context.getRoot()`
- Pass context directly without wrapping
- Test `#root` references in list selection/projection

**Historical Issue**: PR #204 introduced `addDefaultContext(root, context)` creating new context and overwriting root,
breaking Issue #472.

### 2. Null Handling

Current behavior: Accessing properties on null throws `OgnlException`:

```java
// If obj is null: OgnlException("source is null for getProperty(null, 'property')")
obj.property
```

**Rules**:

- Tests must use try-catch-fail pattern expecting `OgnlException`
- Error messages must include property name and null source
- No short-circuit optimizations returning null instead of throwing

### 3. Java 8 Compatibility

**Wrong**: `List.of("a", "b")` (Java 9+)  
**Correct**: `Arrays.asList("a", "b")` (Java 8)

- Avoid Java 9+ APIs
- Use anonymous classes over lambdas in critical code
- Test on Java 8 and 17 (CI matrix)

### 4. Parser Modifications

1. Edit grammar in `src/main/javacc/` or `src/main/jjtree/`
2. Run `./mvnw clean generate-sources`
3. Generated files → `target/generated-sources/java/`
4. Never manually edit generated AST files
5. Test thoroughly - affects all expressions

### 5. Test Requirements

Every bug fix needs regression test:

```java
public class Issue123DescriptiveNameTest {
    private OgnlContext context;
    private TestRootObject rootObject;
    
    @Before
    public void setUp() throws OgnlException {  // Don't forget throws
        rootObject = new TestRootObject();
        context = Ognl.createDefaultContext(rootObject);
    }
    
    @Test
    public void testSpecificScenario() throws OgnlException {
        String expression = "testList.{? #this.equals(#root.targetValue)}";
        Object result = Ognl.getValue(expression, context, rootObject);
        // Assertions...
    }
}
```

- Create `Issue###DescriptiveTest.java` for GitHub issues
- Test edge cases and `#root` preservation
- Verify 947 tests pass: `./mvnw clean test`

## Common Pitfalls

### 1. Context Root Overwriting

❌ `OgnlContext newContext = addDefaultContext(listItem, context);` - Overwrites root  
✅ `Ognl.getValue(expr, context, listItem);` - Preserves context.getRoot()

### 2. Java 9+ APIs

❌ `List.of(...)`, `Map.of(...)`, `var`  
✅ `Arrays.asList(...)`, `new HashMap<>()`, explicit types

### 3. Null Property Access Tests

❌ `assertNull(Ognl.getValue("null.property", context, root));`  
✅ Use try-catch expecting `OgnlException` with message pattern

### 4. Missing throws OgnlException

Methods calling `OgnlRuntime` methods need `throws OgnlException`

### 5. Infinite Recursion

Custom accessors must not call back into OGNL with same property

## Issue Analysis Workflow

1. Create minimal test reproducing the problem
2. Use `Ognl.parseExpression()` to inspect AST
3. Enable tracing: `context.setTraceEvaluations(true)`
4. Check `context.getRoot()` vs current evaluation target
5. Search git history (especially PR #204, #390)
6. Write regression test before fixing

## Key Files

**Core API**: `Ognl.java`, `OgnlContext.java`, `OgnlRuntime.java`  
**Property Access**: `ASTProperty.java`, `ASTChain.java`, `PropertyAccessor.java`  
**Collections**: `ASTList.java`, `ASTSelect.java`, `ASTProject.java`  
**Variables**: `ASTVarRef.java`, `ASTRootVarRef.java`, `ASTThisVarRef.java`  
**Parser Grammar**: `src/main/javacc/OgnlParser.jj`  
**Tests**: `src/test/java/ognl/test/Issue*Test.java`

## Branch Info

- **main**: Java 17+, version 3.5.x
- **ognl-3-4-x**: Java 8, version 3.4.x (THIS BRANCH)

All PRs to this branch must maintain Java 8 compatibility.
