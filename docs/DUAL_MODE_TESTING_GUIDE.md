# Dual-Mode Testing Guide

## Overview

The OGNL dual-mode testing infrastructure enables systematic verification that both **interpreted** and **compiled** execution paths produce identical results for any given expression. This is critical for ensuring correctness as OGNL supports two distinct evaluation strategies.

## Core Concepts

### Execution Modes

OGNL expressions can be evaluated in two ways:

1. **INTERPRETED mode**: Expressions are evaluated by walking the AST (Abstract Syntax Tree). This is the traditional execution path.

2. **COMPILED mode**: Expressions are compiled to optimized bytecode accessors using `Ognl.compileExpression()`. This provides better performance for repeated evaluations.

**Critical Requirement**: Both execution paths must produce identical results for the same expression and input.

### Testing Infrastructure

The dual-mode testing framework consists of three components:

1. **`OgnlExecutionMode`** - Enumeration defining INTERPRETED and COMPILED modes
2. **`DualModeTestUtils`** - Utility methods for expression preparation and evaluation
3. **JUnit 5 Parameterized Tests** - Framework for running tests in both modes

## Writing Dual-Mode Tests

### Basic Pattern

Use `@ParameterizedTest` with `@EnumSource(OgnlExecutionMode.class)` to run tests in both modes:

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testExpression(OgnlExecutionMode mode) throws Exception {
    // Test runs once per mode
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "property1", context, root, mode);

    assertEquals("expected", result);
}
```

### Step-by-Step Example

```java
import ognl.test.util.DualModeTestUtils;
import ognl.test.util.OgnlExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MyDualModeTest {

    private OgnlContext context;
    private MyRoot root;

    @BeforeEach
    void setUp() {
        context = new OgnlContext(new DefaultMemberAccess(false), null, null);
        root = new MyRoot();
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testPropertyAccess(OgnlExecutionMode mode) throws Exception {
        // 1. Prepare expression for the given mode
        Node<OgnlContext> expr = DualModeTestUtils.prepareExpression(
                "user.name", context, root, mode);

        // 2. Evaluate the expression
        Object result = DualModeTestUtils.evaluateNode(expr, context, root);

        // 3. Assert expected result (same for both modes)
        assertEquals("John", result);
    }
}
```

## Utility Methods

### Expression Preparation

**`prepareExpression(expression, context, root, mode)`**
- Parses expression in INTERPRETED mode
- Parses and compiles in COMPILED mode
- Returns prepared Node ready for evaluation

```java
Node<OgnlContext> expr = DualModeTestUtils.prepareExpression(
        "property1", context, root, mode);
```

### Expression Evaluation

**`evaluateNode(node, context, root)`**
- Evaluates a prepared expression node
- Works with both compiled and non-compiled nodes

```java
Object result = DualModeTestUtils.evaluateNode(expr, context, root);
```

### Convenience Methods

**`prepareAndEvaluate(expression, context, root, mode)`**
- Combines preparation and evaluation in one call
- Useful for simple test cases

```java
Object result = DualModeTestUtils.prepareAndEvaluate(
        "user.age", context, root, mode);
```

### Setting Values

**`prepareAndSetValue(expression, context, root, value, mode)`**
- Prepares expression and sets a value
- Verifies setValue works in both modes

```java
DualModeTestUtils.prepareAndSetValue(
        "user.name", context, root, "Jane", mode);
```

## Test Organization

### Test Naming

Use descriptive test names with the `(name = "[{index}] mode={0}")` parameter to show which mode is being tested:

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testMethodInvocation(OgnlExecutionMode mode) throws Exception {
    // Test implementation
}
```

This produces test output like:
```
[1] mode=INTERPRETED
[2] mode=COMPILED
```

### Assertion Messages

Include mode information in assertion messages for clearer failure reports:

```java
assertEquals("expected", result,
    "Value should match in " + DualModeTestUtils.getModeName(mode) + " mode");
```

## Common Patterns

### Pattern 1: Simple Property Access

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testProperty(OgnlExecutionMode mode) throws Exception {
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "propertyName", context, root, mode);
    assertEquals(expectedValue, result);
}
```

### Pattern 2: Method Invocation

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testMethod(OgnlExecutionMode mode) throws Exception {
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "methodName(arg1, arg2)", context, root, mode);
    assertEquals(expectedValue, result);
}
```

### Pattern 3: Chained Navigation

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testChain(OgnlExecutionMode mode) throws Exception {
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "object.property.subProperty", context, root, mode);
    assertEquals(expectedValue, result);
}
```

### Pattern 4: Collection Operations

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testProjection(OgnlExecutionMode mode) throws Exception {
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "users.{name}", context, root, mode);
    assertEquals(expectedList, result);
}
```

### Pattern 5: Set Value Operations

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testSetValue(OgnlExecutionMode mode) throws Exception {
    Node<OgnlContext> expr = DualModeTestUtils.prepareExpression(
            "property", context, root, mode);

    DualModeTestUtils.setValueOnNode(expr, context, root, newValue);

    Object result = DualModeTestUtils.evaluateNode(expr, context, root);
    assertEquals(newValue, result);
}
```

## Configuration

### Enabling/Disabling Dual-Mode Testing

Control dual-mode testing via system property (useful for CI/CD):

```bash
# Run tests in both modes (default)
mvn test

# Run tests in INTERPRETED mode only
mvn test -Dognl.test.dualMode.enabled=false
```

Check if dual-mode testing is enabled:

```java
if (DualModeTestUtils.isDualModeEnabled()) {
    // Both modes will be tested
}
```

## Migrating Existing Tests

### Before (Single-Mode Test)

```java
@Test
void testExpression() throws Exception {
    Object result = Ognl.getValue("property", context, root);
    assertEquals("expected", result);
}
```

### After (Dual-Mode Test)

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testExpression(OgnlExecutionMode mode) throws Exception {
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "property", context, root, mode);
    assertEquals("expected", result);
}
```

## Best Practices

1. **Test Both Modes**: Always use parameterized tests for expression evaluation
2. **Identical Results**: Assertions should be identical for both modes
3. **Clear Naming**: Use descriptive test names with mode parameters
4. **Mode in Messages**: Include mode information in assertion failure messages
5. **Separate Setup**: Keep context/root setup in `@BeforeEach` methods
6. **Test Edge Cases**: Test null handling, exceptions, and boundary conditions in both modes
7. **Performance Tests**: Use dual-mode for correctness, not performance benchmarks

## Compilation Considerations

### When Compilation May Not Occur

Some expressions cannot be compiled, such as:
- Expressions with null intermediate values
- Complex dynamic evaluations
- Expressions using certain operators

The framework handles this gracefully:

```java
Node<OgnlContext> node = DualModeTestUtils.prepareExpression(
        expression, context, root, mode);

// Check if compilation actually occurred (for debugging)
boolean compiled = DualModeTestUtils.verifyCompilationState(node, mode);
```

### Partial Compilation

If an expression can't be fully compiled initially but might be compilable later:
- The accessor may be null
- Subsequent evaluations may trigger compilation
- Tests should still verify correct behavior

## Troubleshooting

### Test Fails in COMPILED Mode Only

This indicates a bug in the compiled accessor generation:
1. Verify the expression is valid OGNL syntax
2. Check if compilation is actually occurring
3. Compare AST evaluation vs accessor evaluation
4. Review relevant `PropertyAccessor` implementations

### Test Fails in INTERPRETED Mode Only

This is unusual but possible:
1. Check for side effects in evaluation
2. Verify context state is properly initialized
3. Review AST node implementation

### Both Modes Fail Identically

This indicates an issue with:
1. Test setup (context, root object)
2. Expression syntax
3. Expected values

## Examples

See `DualModeExampleTest.java` for comprehensive examples demonstrating:
- Simple property access
- Method invocation
- Chained property navigation
- Setting values
- Array/indexed access
- Convenience method usage

## Future Enhancements

Planned improvements to the dual-mode testing infrastructure:
- Performance comparison reporting
- Automatic compilation verification
- Enhanced error diagnostics
- Test suite migration tools

## References

- **Phase 1 Implementation**: `docs/ISSUE_18_ANALYSIS.md`
- **Example Tests**: `ognl/src/test/java/ognl/test/DualModeExampleTest.java`
- **Utilities**: `ognl/src/test/java/ognl/test/util/DualModeTestUtils.java`
- **Enum**: `ognl/src/test/java/ognl/test/util/OgnlExecutionMode.java`
