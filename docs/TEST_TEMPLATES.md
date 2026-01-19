# Dual-Mode Test Templates

This document provides ready-to-use templates for common dual-mode test scenarios in OGNL.

---

## 📋 Basic Templates

### Template 1: Simple getValue Test

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testSimpleExpression(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    YourRootObject root = new YourRootObject();

    // Act
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "propertyName", context, root, mode);

    // Assert
    assertEquals(expectedValue, result);
}
```

### Template 2: setValue Test

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testSetValue(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    YourRootObject root = new YourRootObject();
    Object newValue = "test value";

    // Act
    DualModeTestUtils.prepareAndSetValue(
            "propertyName", context, root, newValue, mode);

    // Assert
    Object actual = DualModeTestUtils.prepareAndEvaluate(
            "propertyName", context, root, mode);
    assertEquals(newValue, actual);
}
```

### Template 3: Exception Test

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testInvalidExpression(OgnlExecutionMode mode) {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    YourRootObject root = new YourRootObject();

    // Act & Assert
    assertThrows(Exception.class, () ->
        DualModeTestUtils.prepareAndEvaluate(
                "invalid.expression", context, root, mode)
    );
}
```

---

## 🔢 Numeric Literal Tests

### Template 4: BigDecimal Literals

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testBigDecimalLiterals(OgnlExecutionMode mode) throws Exception {
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();

    // Test basic BigDecimal literal with 'b' suffix
    assertEquals(BigDecimal.valueOf(1),
            DualModeTestUtils.prepareAndEvaluate("1b", context, root, mode));

    // Test negative BigDecimal
    assertEquals(BigDecimal.valueOf(-1),
            DualModeTestUtils.prepareAndEvaluate("-1b", context, root, mode));

    // Test BigDecimal with uppercase 'B'
    assertEquals(BigDecimal.valueOf(2.5),
            DualModeTestUtils.prepareAndEvaluate("2.5B", context, root, mode));

    // Test BigDecimal arithmetic
    assertEquals(BigDecimal.valueOf(7),
            DualModeTestUtils.prepareAndEvaluate("5+2b", context, root, mode));
}
```

### Template 5: BigInteger Literals

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testBigIntegerLiterals(OgnlExecutionMode mode) throws Exception {
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();

    // Test basic BigInteger literal with 'h' suffix
    assertEquals(BigInteger.valueOf(1),
            DualModeTestUtils.prepareAndEvaluate("1h", context, root, mode));

    // Test negative BigInteger
    assertEquals(BigInteger.valueOf(-1),
            DualModeTestUtils.prepareAndEvaluate("-1h", context, root, mode));

    // Test BigInteger with uppercase 'H'
    assertEquals(BigInteger.valueOf(100),
            DualModeTestUtils.prepareAndEvaluate("100H", context, root, mode));

    // Test BigInteger arithmetic
    assertEquals(BigInteger.valueOf(7),
            DualModeTestUtils.prepareAndEvaluate("5h+2", context, root, mode));
}
```

---

## 🏗️ Property Access Tests

### Template 6: Property Chain

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testPropertyChain(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    User root = new User();
    root.setAddress(new Address());
    root.getAddress().setCity("San Francisco");

    // Act
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "address.city", context, root, mode);

    // Assert
    assertEquals("San Francisco", result);
}
```

### Template 7: Method Invocation

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testMethodInvocation(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    User root = new User();
    root.setName("John Doe");

    // Act
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "getName()", context, root, mode);

    // Assert
    assertEquals("John Doe", result);
}
```

### Template 8: Array Access

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testArrayAccess(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();
    root.setArray(new String[]{"one", "two", "three"});

    // Act
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "array[1]", context, root, mode);

    // Assert
    assertEquals("two", result);
}
```

---

## 📝 Context Variable Tests

### Template 9: Context Variables

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testContextVariables(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    context.put("userName", "Alice");
    context.put("count", 42);
    Root root = new Root();

    // Act & Assert
    assertEquals("Alice",
            DualModeTestUtils.prepareAndEvaluate("#userName", context, root, mode));

    assertEquals(42,
            DualModeTestUtils.prepareAndEvaluate("#count", context, root, mode));

    // Test expression using context variable
    assertEquals(Boolean.TRUE,
            DualModeTestUtils.prepareAndEvaluate("#count > 40", context, root, mode));
}
```

---

## 🎯 Operator Tests

### Template 10: Arithmetic Operators

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testArithmeticOperators(OgnlExecutionMode mode) throws Exception {
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();

    // Addition
    assertEquals(7,
            DualModeTestUtils.prepareAndEvaluate("5+2", context, root, mode));

    // Subtraction
    assertEquals(3,
            DualModeTestUtils.prepareAndEvaluate("5-2", context, root, mode));

    // Multiplication
    assertEquals(10,
            DualModeTestUtils.prepareAndEvaluate("5*2", context, root, mode));

    // Division
    assertEquals(2,
            DualModeTestUtils.prepareAndEvaluate("5/2", context, root, mode));

    // Modulo
    assertEquals(1,
            DualModeTestUtils.prepareAndEvaluate("5%2", context, root, mode));
}
```

### Template 11: Logical Operators

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testLogicalOperators(OgnlExecutionMode mode) throws Exception {
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();

    // AND
    assertEquals(Boolean.TRUE,
            DualModeTestUtils.prepareAndEvaluate("true && true", context, root, mode));

    // OR
    assertEquals(Boolean.TRUE,
            DualModeTestUtils.prepareAndEvaluate("false || true", context, root, mode));

    // NOT
    assertEquals(Boolean.FALSE,
            DualModeTestUtils.prepareAndEvaluate("!true", context, root, mode));

    // Comparison
    assertEquals(Boolean.TRUE,
            DualModeTestUtils.prepareAndEvaluate("5 > 2", context, root, mode));

    assertEquals(Boolean.TRUE,
            DualModeTestUtils.prepareAndEvaluate("5 == 5", context, root, mode));
}
```

---

## 🔄 Collection Tests

### Template 12: Collection Projection

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testCollectionProjection(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();

    List<User> users = Arrays.asList(
            new User("Alice", 30),
            new User("Bob", 25),
            new User("Charlie", 35)
    );
    root.setUsers(users);

    // Act - project names
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "users.{name}", context, root, mode);

    // Assert
    assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), result);
}
```

### Template 13: Collection Selection

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testCollectionSelection(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    Root root = new Root();

    List<User> users = Arrays.asList(
            new User("Alice", 30),
            new User("Bob", 25),
            new User("Charlie", 35)
    );
    root.setUsers(users);

    // Act - select users over 30
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "users.{? #this.age > 30}", context, root, mode);

    // Assert
    List<User> filtered = (List<User>) result;
    assertEquals(1, filtered.size());
    assertEquals("Charlie", filtered.get(0).getName());
}
```

---

## 🧪 Type Conversion Tests

### Template 14: Type Coercion

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testTypeCoercion(OgnlExecutionMode mode) throws Exception {
    OgnlContext context = Ognl.createDefaultContext(null);
    context.put("stringNum", "42");
    Root root = new Root();

    // String to number comparison
    assertEquals(Boolean.TRUE,
            DualModeTestUtils.prepareAndEvaluate("#stringNum == 42", context, root, mode));

    // String concatenation
    assertEquals("421",
            DualModeTestUtils.prepareAndEvaluate("#stringNum + 1", context, root, mode));

    // Number to string
    assertEquals("11",
            DualModeTestUtils.prepareAndEvaluate("1 + #stringNum", context, root, mode));
}
```

---

## 🎨 Complete Test Class Template

### Template 15: Full Test Class

```java
package ognl.test;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.YourRootObject;
import ognl.test.util.DualModeTestUtils;
import ognl.test.util.OgnlExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dual-mode tests for [feature description].
 *
 * <p>Each test runs in both INTERPRETED and COMPILED modes to verify
 * execution mode parity.</p>
 */
class YourFeatureTest {

    private OgnlContext context;
    private YourRootObject root;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
        root = new YourRootObject();

        // Additional setup
        root.setSomeProperty("initial value");
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testFeature1(OgnlExecutionMode mode) throws Exception {
        // Arrange
        String expression = "someProperty";

        // Act
        Object result = DualModeTestUtils.prepareAndEvaluate(
                expression, context, root, mode);

        // Assert
        assertEquals("initial value", result);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testFeature2_withSetValue(OgnlExecutionMode mode) throws Exception {
        // Arrange
        String newValue = "modified value";

        // Act
        DualModeTestUtils.prepareAndSetValue(
                "someProperty", context, root, newValue, mode);

        // Assert
        Object actual = DualModeTestUtils.prepareAndEvaluate(
                "someProperty", context, root, mode);
        assertEquals(newValue, actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testFeature3_exception(OgnlExecutionMode mode) {
        // Act & Assert
        assertThrows(Exception.class, () ->
            DualModeTestUtils.prepareAndEvaluate(
                    "nonExistentProperty", context, root, mode)
        );
    }
}
```

---

## 🛠️ Advanced Patterns

### Template 16: Null Handling

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testNullHandling(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    User root = new User();
    root.setAddress(null);  // Null intermediate value

    // Act - should handle null gracefully
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "address?.city", context, root, mode);

    // Assert
    assertNull(result);
}
```

### Template 17: Complex Expression

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testComplexExpression(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    context.put("threshold", 100);
    Root root = new Root();

    // Complex multi-part expression
    String expression = "#threshold > 50 ? 'high' : 'low'";

    // Act
    Object result = DualModeTestUtils.prepareAndEvaluate(
            expression, context, root, mode);

    // Assert
    assertEquals("high", result);
}
```

---

## 📚 Usage Guide

### How to Use These Templates

1. **Copy the appropriate template** for your use case
2. **Replace placeholders**:
   - `YourRootObject` → Your actual root object class
   - `propertyName` → Your property name
   - `expectedValue` → Your expected result
3. **Add necessary imports**
4. **Customize assertions** as needed
5. **Run tests** to verify both modes work

### Required Imports

All dual-mode tests need these imports:

```java
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.util.DualModeTestUtils;
import ognl.test.util.OgnlExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;
```

### Common Customizations

**Add context variables**:
```java
context.put("variableName", value);
```

**Test multiple expressions**:
```java
assertEquals(expected1, DualModeTestUtils.prepareAndEvaluate(expr1, context, root, mode));
assertEquals(expected2, DualModeTestUtils.prepareAndEvaluate(expr2, context, root, mode));
```

**Custom error messages**:
```java
assertEquals(expected, result, "Expression should return correct value in " +
        DualModeTestUtils.getModeName(mode) + " mode");
```

---

## ✅ Checklist

Before submitting your test:

- [ ] Uses `@ParameterizedTest` with `@EnumSource(OgnlExecutionMode.class)`
- [ ] Has `OgnlExecutionMode mode` parameter
- [ ] Uses `DualModeTestUtils` methods
- [ ] Has descriptive test name
- [ ] Includes proper assertions
- [ ] Tests pass in both modes locally
- [ ] Follows existing code style

---

## 📖 References

- [Dual-Mode Testing Guide](DUAL_MODE_TESTING_GUIDE.md)
- [Code Review Checklist](CODE_REVIEW_CHECKLIST.md)
- [Example Tests](../ognl/src/test/java/ognl/test/DualModeExampleTest.java)
