# Code Review Checklist for OGNL

## Dual-Mode Testing Requirements

This checklist ensures all contributions maintain execution mode parity between INTERPRETED and COMPILED evaluation paths.

---

## 📋 General Test Requirements

### ✅ All New Tests

- [ ] **Uses `@ParameterizedTest` annotation**
  - ❌ Not: `@Test`
  - ✅ Use: `@ParameterizedTest(name = "[{index}] mode={0}")`

- [ ] **Includes `@EnumSource(OgnlExecutionMode.class)`**
  - Required for dual-mode execution

- [ ] **Test method has `OgnlExecutionMode mode` parameter**
  - ❌ `void testExpression() throws Exception`
  - ✅ `void testExpression(OgnlExecutionMode mode) throws Exception`

- [ ] **Uses `DualModeTestUtils` for expression evaluation**
  - ❌ `Ognl.getValue(expr, context, root)`
  - ✅ `DualModeTestUtils.prepareAndEvaluate(expr, context, root, mode)`

- [ ] **Imports are correct**
  ```java
  import ognl.test.util.DualModeTestUtils;
  import ognl.test.util.OgnlExecutionMode;
  import org.junit.jupiter.params.ParameterizedTest;
  import org.junit.jupiter.params.provider.EnumSource;
  ```

---

## 🔍 Expression Evaluation Tests

### ✅ getValue Operations

- [ ] **Uses `prepareAndEvaluate()` for simple cases**
  ```java
  Object result = DualModeTestUtils.prepareAndEvaluate(
      "expression", context, root, mode);
  ```

- [ ] **Uses `prepareExpression()` + `evaluateNode()` for complex cases**
  ```java
  Node<OgnlContext> node = DualModeTestUtils.prepareExpression(
      "expression", context, root, mode);
  Object result = DualModeTestUtils.evaluateNode(node, context, root);
  ```

- [ ] **Assertions are identical for both modes**
  - No mode-specific assertions unless documented

- [ ] **Throws clause uses `Exception` not `OgnlException`**
  - ❌ `throws OgnlException`
  - ✅ `throws Exception`

---

## 📝 setValue Operations

### ✅ Setting Values

- [ ] **Uses `prepareAndSetValue()` for simple cases**
  ```java
  DualModeTestUtils.prepareAndSetValue(
      "property", context, root, value, mode);
  ```

- [ ] **Uses `prepareExpression()` + `setValueOnNode()` for complex cases**
  ```java
  Node<OgnlContext> node = DualModeTestUtils.prepareExpression(
      "property", context, root, mode);
  DualModeTestUtils.setValueOnNode(node, context, root, value);
  ```

- [ ] **Verifies the value was set correctly**
  ```java
  Object actual = DualModeTestUtils.prepareAndEvaluate(
      "property", context, root, mode);
  assertEquals(expected, actual);
  ```

---

## ⚠️ Exception Handling

### ✅ Exception Tests

- [ ] **Uses generic `Exception.class` in `assertThrows()`**
  - ❌ `assertThrows(OgnlException.class, ...)`
  - ✅ `assertThrows(Exception.class, ...)`
  - Rationale: Different modes may throw different exception types

- [ ] **Exception tests work in both modes**
  - INTERPRETED may throw OgnlException
  - COMPILED may throw different exceptions
  - Both should fail for the same invalid inputs

---

## 🏗️ AST Node Implementation Changes

### ✅ When Modifying AST Nodes

- [ ] **Updated `getValueBody()` for interpreted mode**
  - This is the interpreted execution path

- [ ] **Verified `toGetSourceString()` generates correct bytecode**
  - This is the compiled execution path

- [ ] **Both methods produce identical results**
  - Create dual-mode tests demonstrating parity

- [ ] **Considered null handling in both paths**
  - Null safety must match between modes

- [ ] **Type coercion is consistent**
  - Numeric conversions must match
  - String coercion must match

---

## 📚 Documentation Requirements

### ✅ Code Comments

- [ ] **Complex expressions are documented**
  - Explain what the test validates

- [ ] **Mode-specific behaviors are documented** (if intentional)
  - Rare, but document if compilation can't occur

- [ ] **JavaDoc for new public methods**
  - Especially for test utilities

### ✅ Commit Messages

- [ ] **Mentions dual-mode testing** (if applicable)
- [ ] **References related issue numbers**
- [ ] **Explains mode parity impact**

---

## 🧪 Test Coverage

### ✅ Coverage Requirements

- [ ] **New code has >80% coverage**
  - Check with `mvn test -Pcoverage`

- [ ] **Tests pass in both modes**
  - Run `mvn test` locally before pushing

- [ ] **No mode-specific failures**
  - Check CI logs for discrepancies

---

## 🎯 Specific Test Patterns

### ✅ Numeric Literal Tests

- [ ] **Tests BigDecimal literals** (if applicable)
  - Format: `-1b`, `2.0B`, `5.5b`

- [ ] **Tests BigInteger literals** (if applicable)
  - Format: `-1h`, `2H`, `0xFFh`

- [ ] **Tests float/double literals** (if applicable)
  - Format: `-1d`, `2.0D`, `3.5f`, `4.0F`

- [ ] **Tests operator precedence**
  - Example: `5+2*3` vs `(5+2)*3`

### ✅ Property Access Tests

- [ ] **Tests property chains**
  - Example: `user.address.zipCode`

- [ ] **Tests method invocation**
  - Example: `user.getName()`

- [ ] **Tests indexed access**
  - Example: `array[0]`, `list[1]`

### ✅ Collection Operation Tests

- [ ] **Tests projection**
  - Example: `users.{name}`

- [ ] **Tests selection**
  - Example: `users.{? #this.age > 18}`

- [ ] **Tests context preservation**
  - Verify `#root` remains unchanged

---

## 🚨 Anti-Patterns to Avoid

### ❌ Common Mistakes

- [ ] **NOT using `@Test` for new tests**
  - All new tests must be dual-mode

- [ ] **NOT mixing `Ognl.getValue()` with `DualModeTestUtils`**
  - Pick one pattern and stick with it

- [ ] **NOT testing only INTERPRETED mode**
  - Both modes must be verified

- [ ] **NOT using mode-specific assertions**
  - Assertions should be identical unless documented

- [ ] **NOT checking compilation state**
  - Use `verifyCompilationState()` if needed

---

## 🔄 Migration Checklist

### ✅ Converting Existing Tests

- [ ] **Changed `@Test` to `@ParameterizedTest`**

- [ ] **Added `@EnumSource(OgnlExecutionMode.class)`**

- [ ] **Added `OgnlExecutionMode mode` parameter**

- [ ] **Replaced `Ognl.getValue()` with `DualModeTestUtils.prepareAndEvaluate()`**

- [ ] **Replaced `Ognl.setValue()` with `DualModeTestUtils.prepareAndSetValue()`**

- [ ] **Updated imports**

- [ ] **Changed `throws OgnlException` to `throws Exception`**

- [ ] **Verified tests pass in both modes**

---

## 🏁 Pre-Merge Checklist

### ✅ Before Requesting Review

- [ ] **All tests pass locally**
  ```bash
  mvn clean test
  ```

- [ ] **Coverage meets requirements**
  ```bash
  mvn test -Pcoverage
  ```

- [ ] **No mode-specific failures in CI**

- [ ] **Documentation updated** (if applicable)

- [ ] **DUAL_MODE_TESTING_GUIDE.md consulted**

### ✅ For Reviewers

- [ ] **All checklist items verified**

- [ ] **Test names are descriptive**

- [ ] **No unnecessary complexity**

- [ ] **Follows existing patterns**

- [ ] **CI pipeline passes**

---

## 📖 Reference Documentation

### Required Reading

- [Dual-Mode Testing Guide](DUAL_MODE_TESTING_GUIDE.md)
- [Phase 2 Test Conversion](PHASE_2_TEST_CONVERSION.md)
- [CI/CD Configuration](CI_CD_DUAL_MODE_CONFIGURATION.md)

### Examples

See these files for proper dual-mode test patterns:
- `ognl/src/test/java/ognl/test/DualModeExampleTest.java`
- `ognl/src/test/java/ognl/test/ArithmeticAndLogicalOperatorsTest.java`
- `ognl/src/test/java/ognl/test/NumberFormatExceptionTest.java`

---

## ✨ Quick Reference

### Template for New Test

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testYourFeature(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    Object root = new YourRootObject();

    // Act
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "your.expression", context, root, mode);

    // Assert
    assertEquals(expected, result);
}
```

### Template for setValue Test

```java
@ParameterizedTest(name = "[{index}] mode={0}")
@EnumSource(OgnlExecutionMode.class)
void testSetValue(OgnlExecutionMode mode) throws Exception {
    // Arrange
    OgnlContext context = Ognl.createDefaultContext(null);
    Object root = new YourRootObject();
    Object value = "new value";

    // Act
    DualModeTestUtils.prepareAndSetValue(
            "property", context, root, value, mode);

    // Assert
    Object actual = DualModeTestUtils.prepareAndEvaluate(
            "property", context, root, mode);
    assertEquals(value, actual);
}
```

---

## 📞 Getting Help

If you have questions about dual-mode testing:

1. Check the [Dual-Mode Testing Guide](DUAL_MODE_TESTING_GUIDE.md)
2. Review example tests in `ognl/src/test/java/ognl/test/`
3. Ask in pull request comments
4. Open a GitHub issue with the `testing` label

---

**Remember**: The goal is to ensure that INTERPRETED and COMPILED modes always produce identical results. This prevents subtle bugs and ensures OGNL works correctly regardless of which execution path is chosen.
