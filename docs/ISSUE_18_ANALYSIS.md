# Issue #18: Unit Tests Failing for Interpreted OGNL Expressions

**Issue**: https://github.com/orphan-oss/ognl/issues/18
**Status**: Open since March 10, 2016
**Priority**: High - Test Coverage Gap

## Executive Summary

The OGNL test suite has a critical coverage gap: it primarily validates compiled expressions while failing to adequately test interpreted expressions. When expressions run in interpreted mode (without bytecode compilation), **85 out of 735 tests fail**, indicating significant bugs in the interpreted execution path that have gone undetected.

## Background

### OGNL Execution Modes

OGNL supports two distinct execution modes:

#### 1. Interpreted Mode (Default)
- Expressions are parsed into an Abstract Syntax Tree (AST)
- Evaluation walks the AST nodes using `node.getValue(context, source)`
- No compilation or bytecode generation occurs
- Code path: `Ognl.java:484` → `node.getValue(evaluationContext, root)`

#### 2. Compiled Mode (Explicit)
- After parsing, expressions can be compiled to bytecode using Javassist
- Creates an `ExpressionAccessor` instance set on the node via `setAccessor()`
- Evaluation uses the compiled accessor for better performance
- Code path: `Ognl.java:482` → `node.getAccessor().get(evaluationContext, root)`

**Key code in** `Ognl.java:481-485`:
```java
if (node.getAccessor() != null) {
    result = node.getAccessor().get(evaluationContext, root);  // Compiled path
} else {
    result = node.getValue(evaluationContext, root);          // Interpreted path
}
```

### The Problem

Currently, the test suite inadvertently tests primarily (or exclusively) the compiled path, leading to:

1. **Inconsistent behavior**: The two execution paths produce different results for the same expressions
2. **Hidden bugs**: Bugs in the interpreted implementation go undetected
3. **Unreliable default behavior**: Since interpreted mode is the default, users may encounter bugs that tests don't catch
4. **Code quality issues**: Different AST node implementations may have bugs in either:
   - Their `getValueBody()` method (interpreted execution)
   - Their `toGetSourceString()` method (compiled code generation)

### Test Failure Categories

Based on the issue report, failures occur in:

- **Arithmetic and Logical Operators** (`ArithmeticAndLogicalOperatorsTest`)
  - Binary literals
  - Hexadecimal literals
  - Float literals
  - BigDecimal operations
  - BigInteger operations

- **Numeric Parsing** (`NumberFormatExceptionTest`)
  - Float/int/BigDecimal conversions
  - Type coercion edge cases

- **Property Access** (`PropertyTest`, `InterfaceInheritanceTest`)
  - Interface inheritance
  - Indexed properties
  - Complex property chains

- **Array Operations** (`ArrayElementsTest`, `IndexedPropertyTest`)
  - Element access
  - Index resolution
  - Boundary conditions

- **Method Invocations** (`MethodTest`, `MethodWithConversionTest`)
  - Type conversion
  - Method resolution
  - Overload selection

- **Setter Operations** (`SetterTest`)
  - Collection index setters
  - Property setters with type conversion

## Root Cause Analysis

The fundamental issue is that:

1. **Both execution paths should produce identical results**, but they currently don't
2. **Tests implicitly compile expressions**, either through explicit `compileExpression()` calls or through framework behavior
3. **No systematic verification** ensures both modes work correctly
4. **Bugs accumulate** in the less-tested interpreted path over time

## Proposed Solution

### Phase 1: Test Infrastructure Enhancement

**Goal**: Modify test infrastructure to systematically run each test in both execution modes.

#### Approach Options

##### Option A: JUnit 5 Parameterized Tests (Recommended)

Create a parameterized test approach that runs each test in both modes:

```java
public enum OgnlExecutionMode {
    INTERPRETED,
    COMPILED
}

@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testExpression(OgnlExecutionMode mode) throws Exception {
    Object expression = prepareExpression("someExpression", mode);
    Object result = Ognl.getValue(expression, context, root);
    assertEquals(expectedValue, result);
}

private Object prepareExpression(String expr, OgnlExecutionMode mode) throws Exception {
    Object parsed = Ognl.parseExpression(expr);
    if (mode == OgnlExecutionMode.COMPILED) {
        return Ognl.compileExpression(context, root, expr);
    }
    return parsed;
}
```

**Advantages**:
- Clean, declarative syntax
- Each mode shows up as separate test in reports
- Easy to run just one mode for debugging
- Leverages JUnit 5 features

**Disadvantages**:
- Requires updating all existing tests
- More invasive change to test codebase

##### Option B: Custom JUnit 5 Extension

Create a test extension that automatically runs tests twice:

```java
@ExtendWith(DualModeTestExtension.class)
class SomeTest {
    @Test
    @DualMode  // Custom annotation
    void testExpression() throws Exception {
        // Test code runs in both modes automatically
        Object result = Ognl.getValue("expression", context, root);
        assertEquals(expectedValue, result);
    }
}
```

**Advantages**:
- Less invasive to existing tests
- Can be applied incrementally
- Existing test code mostly unchanged

**Disadvantages**:
- More complex extension implementation
- Less explicit in test reports

##### Option C: Test Base Class Modification (2016 Approach)

Modify the test base class to wrap execution:

```java
public abstract class OgnlTestBase {

    protected void runInBothModes(TestExecutor executor) throws Exception {
        // Run in interpreted mode
        setExecutionMode(INTERPRETED);
        executor.execute();

        // Run in compiled mode
        setExecutionMode(COMPILED);
        executor.execute();
    }

    @FunctionalInterface
    interface TestExecutor {
        void execute() throws Exception;
    }
}
```

**Advantages**:
- Compatible with existing test structure
- Can be applied gradually
- Centralized mode management

**Disadvantages**:
- Requires tests to explicitly call wrapper
- Less idiomatic for JUnit 5

#### Recommended Approach

**Use Option A (Parameterized Tests)** for new tests and gradually migrate existing tests. This provides:
- Clear test reports showing which mode failed
- Explicit, maintainable test code
- Better CI/CD integration

### Phase 2: Identify and Fix Interpreted Mode Bugs

**Goal**: Systematically identify all failures in interpreted mode and fix the underlying bugs.

#### Process

1. **Enable dual-mode testing** for entire test suite
2. **Run full test suite** and capture all interpreted mode failures
3. **Categorize failures** by:
   - AST node type (ASTConst, ASTProperty, ASTMethod, etc.)
   - Feature area (arithmetic, property access, method calls, etc.)
   - Failure type (wrong result, exception, type mismatch, etc.)

4. **For each failure**:
   - Identify the AST node implementation with the bug
   - Compare compiled bytecode generation (`toGetSourceString()`) vs interpreted evaluation (`getValueBody()`)
   - Determine the correct behavior
   - Fix the interpreted implementation
   - Verify both modes now produce identical results
   - Add regression test if needed

5. **Track progress** using GitHub issues/project board

#### Common Bug Patterns to Investigate

Based on the failure categories, likely bug patterns include:

- **Type coercion differences**: Compiled mode may use different type conversion than interpreted
- **Null handling inconsistencies**: Different null-safety behavior between modes
- **Operator precedence issues**: Bytecode generation may respect precedence differently
- **Numeric literal parsing**: BigDecimal/BigInteger/hex/float literals may parse differently
- **Property accessor resolution**: Method vs field access resolution may differ
- **Collection iteration**: List/array iteration may have different semantics
- **Context variable handling**: `#root`, `#this`, and other variables may resolve differently

#### Example Fix Process

For a failing test in `ArithmeticAndLogicalOperatorsTest`:

```java
@Test
void testBigDecimalLiteral() throws Exception {
    // Fails in interpreted mode, passes in compiled mode
    assertEquals(BigDecimal.valueOf(1), Ognl.getValue("1b", context, root));
}
```

Investigation steps:
1. Find the AST node handling BigDecimal literals (likely `ASTConst`)
2. Check `getValueBody()` - does it correctly parse the "b" suffix?
3. Check `toGetSourceString()` - does bytecode generation handle it correctly?
4. Compare with similar logic for "h" (BigInteger) and "d" (double) suffixes
5. Fix the parsing/evaluation logic
6. Verify both modes now pass

### Phase 3: Prevent Future Regressions

**Goal**: Ensure all future code changes maintain parity between execution modes.

#### Measures

1. **Mandatory dual-mode testing**:
   - All new tests must verify both modes
   - CI/CD pipeline fails if modes produce different results
   - Code review checklist includes dual-mode verification

2. **Documentation**:
   - Update developer guide with dual-mode testing requirements
   - Document when/why to use compiled vs interpreted mode
   - Add examples of proper test structure

3. **Tooling**:
   - Create helper methods for common dual-mode test patterns
   - Add assertion methods that automatically check both modes
   - Consider static analysis to detect mode-specific code

4. **Test coverage**:
   - Maintain >80% coverage on both execution paths
   - Monitor coverage delta between modes
   - Flag significant differences for investigation

## Implementation Plan

### Step 1: Create Test Infrastructure (1-2 weeks)

- [ ] Create `OgnlExecutionMode` enum
- [ ] Implement parameterized test approach
- [ ] Add helper methods for expression preparation
- [ ] Create test utilities for mode verification
- [ ] Document testing patterns

### Step 2: Enable Dual-Mode Testing (1 week)

- [ ] Add system property to enable/disable dual-mode testing
- [ ] Configure CI/CD to run with dual-mode enabled
- [ ] Create test report showing mode-specific failures
- [ ] Document how to run tests in specific modes for debugging

### Step 3: Baseline Current State (3-5 days)

- [ ] Run full test suite with dual-mode enabled
- [ ] Capture all 85+ interpreted mode failures
- [ ] Create GitHub issues for each failure category
- [ ] Prioritize fixes based on severity/impact

### Step 4: Fix Interpreted Mode Bugs (4-8 weeks)

Priority order:
1. **Critical**: Numeric literal parsing (affects many tests)
2. **High**: Arithmetic operators (core functionality)
3. **High**: Property access (common use case)
4. **Medium**: Array operations
5. **Medium**: Method invocations
6. **Low**: Edge cases and less common operations

For each category:
- [ ] Identify affected AST nodes
- [ ] Analyze differences between modes
- [ ] Implement fixes
- [ ] Add regression tests
- [ ] Verify no compiled mode regressions
- [ ] Update documentation

### Step 5: Documentation and Cleanup (1 week)

- [ ] Update CLAUDE.md with dual-mode testing guidance
- [ ] Update DeveloperGuide.md with execution mode details
- [ ] Add examples to LanguageGuide.md if needed
- [ ] Create migration guide for existing tests
- [ ] Document any known limitations or incompatibilities

## Success Criteria

The issue will be considered resolved when:

1. ✅ All 735 tests pass in both interpreted and compiled modes
2. ✅ No behavioral differences between modes (except performance)
3. ✅ CI/CD enforces dual-mode testing for all PRs
4. ✅ Documentation clearly explains both execution modes
5. ✅ Developer guide includes dual-mode testing requirements

## Risks and Mitigation

### Risk: Test Execution Time Doubles

**Impact**: CI/CD builds take twice as long
**Probability**: High
**Mitigation**:
- Make dual-mode testing optional via system property
- Run interpreted-only tests in parallel
- Only run dual-mode on critical test suites
- Cache compiled expressions where possible

### Risk: Many Failures to Fix Initially

**Impact**: Long timeline to resolve all issues
**Probability**: High
**Mitigation**:
- Fix incrementally by category
- Mark known failures with @Disabled and tracking issues
- Prioritize most common/critical failures
- Accept partial progress milestones

### Risk: Some Expressions May Be Fundamentally Incompatible

**Impact**: Cannot achieve 100% parity
**Probability**: Low
**Mitigation**:
- Document incompatibilities clearly
- Provide error messages guiding users to alternative
- Consider whether incompatibility indicates design flaw
- Evaluate if feature should be deprecated

### Risk: Breaking Changes in Existing Code

**Impact**: Users may rely on current (incorrect) behavior
**Probability**: Medium
**Mitigation**:
- Document behavioral changes in release notes
- Provide migration guide for affected users
- Consider deprecation period for major changes
- Add system property to enable legacy behavior if needed

## Timeline Estimate

- **Phase 1**: 2-3 weeks (test infrastructure)
- **Phase 2**: 6-10 weeks (bug fixes)
- **Phase 3**: 1-2 weeks (documentation and enforcement)

**Total**: 9-15 weeks for complete resolution

Can be parallelized and incremental - partial improvements provide value throughout.

## Benefits

1. **Improved Code Quality**: Both execution paths thoroughly tested
2. **Bug Detection**: Catch mode-specific issues before release
3. **User Confidence**: Both modes work reliably
4. **Future-Proofing**: New features must work in both modes from day one
5. **Better Documentation**: Clear guidance on when to use each mode
6. **Performance Insights**: Understanding where compilation helps most

## Related Issues

- Original issue: https://github.com/orphan-oss/ognl/issues/18
- OGNL-4 milestone (mentioned in issue discussion)

## References

- OGNL source: `ognl/src/main/java/ognl/Ognl.java`
- Expression compiler: `ognl/src/main/java/ognl/enhance/ExpressionCompiler.java`
- Test infrastructure: `ognl/src/test/java/ognl/test/`
- Existing dual-mode test: `ognl/src/test/java/ognl/test/enhance/ExpressionCompilerTest.java`

---

**Document Version**: 1.0
**Last Updated**: 2025-11-30
**Author**: Claude Code Analysis
