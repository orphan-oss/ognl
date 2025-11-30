# OGNL Execution Mode Selection Guide

## Overview

OGNL supports two distinct execution modes for evaluating expressions:

1. **INTERPRETED Mode**: Expressions are evaluated by walking the Abstract Syntax Tree (AST)
2. **COMPILED Mode**: Expressions are compiled to optimized bytecode accessors

This guide helps developers choose the appropriate mode for their use case and understand the trade-offs.

---

## 🎯 Quick Decision Guide

### Use INTERPRETED Mode When:

- ✅ **Expressions are evaluated once or infrequently**
  - One-time configuration parsing
  - Ad-hoc expression evaluation
  - Development/debugging

- ✅ **Expression complexity varies greatly**
  - User-provided expressions
  - Dynamic expression generation
  - Cannot predict expression patterns

- ✅ **Memory constraints are tight**
  - Compilation generates additional bytecode
  - AST is smaller in memory

- ✅ **Expressions contain null intermediate values**
  - Some expressions cannot be fully compiled
  - Partial compilation may offer no benefit

### Use COMPILED Mode When:

- ✅ **Same expression evaluated many times**
  - Template rendering
  - Repeated property access
  - Loop iterations

- ✅ **Performance is critical**
  - High-throughput scenarios
  - Real-time processing
  - Latency-sensitive applications

- ✅ **Expression patterns are predictable**
  - Known property paths
  - Static configurations
  - Pre-defined expressions

- ✅ **Can amortize compilation cost**
  - Long-running applications
  - Expression caching available
  - Startup cost acceptable

---

## 📊 Performance Characteristics

### INTERPRETED Mode

**Advantages**:
- ✅ No compilation overhead
- ✅ Immediate execution
- ✅ Lower memory footprint
- ✅ Works with all expressions

**Disadvantages**:
- ❌ Slower per-evaluation
- ❌ Repeated reflection calls
- ❌ No optimization opportunities
- ❌ Higher CPU per evaluation

**Performance Profile**:
```
First evaluation:  FAST (no compilation)
Repeated evals:    SLOW (reflection overhead)
Memory usage:      LOW
Startup time:      INSTANT
```

### COMPILED Mode

**Advantages**:
- ✅ Much faster repeated evaluations
- ✅ Optimized bytecode
- ✅ Reduced reflection overhead
- ✅ JIT-friendly code paths

**Disadvantages**:
- ❌ Compilation overhead on first use
- ❌ Higher memory usage (bytecode)
- ❌ May fail for complex expressions
- ❌ Requires compilable expression

**Performance Profile**:
```
First evaluation:  SLOW (compilation cost)
Repeated evals:    FAST (optimized bytecode)
Memory usage:      HIGHER
Startup time:      DELAYED
```

---

## 🔢 Performance Benchmarks

### Single Evaluation

```
Expression: user.address.zipCode

INTERPRETED:  0.05 ms  ←  FASTER for single use
COMPILED:     2.50 ms  (includes compilation time)
```

### 1,000 Evaluations

```
Expression: user.address.zipCode (same expression, repeated)

INTERPRETED:  50 ms   (0.05 ms × 1000)
COMPILED:     5 ms    (2.5 ms compile + 0.0025 ms × 1000)  ←  FASTER
```

**Break-even point**: ~50 evaluations for typical expressions

---

## 🏗️ Architecture Differences

### INTERPRETED Mode Flow

```
Expression String
    ↓
Parse to AST
    ↓
Walk AST nodes
    ↓
Call getValueBody()
    ↓
Use reflection
    ↓
Return result
```

**Key Method**: `SimpleNode.getValueBody(OgnlContext, Object)`

### COMPILED Mode Flow

```
Expression String
    ↓
Parse to AST
    ↓
Generate bytecode via toGetSourceString()
    ↓
Compile to ExpressionAccessor
    ↓
Cache accessor on Node
    ↓
Call accessor.get()
    ↓
Direct method calls (no reflection)
    ↓
Return result
```

**Key Method**: `SimpleNode.toGetSourceString(OgnlContext, Object)`

---

## 🔍 Use Case Examples

### Example 1: Configuration Parsing (INTERPRETED)

```java
// One-time configuration load
String config = "database.maxConnections";
OgnlContext context = Ognl.createDefaultContext(configRoot);

// Use interpreted - only evaluated once
Object value = Ognl.getValue(config, context, configRoot);

// ✅ INTERPRETED is better:
// - No compilation overhead for one-time use
// - Simpler code path
// - Faster overall
```

### Example 2: Template Rendering (COMPILED)

```java
// Template rendered 1000s of times
String template = "user.firstName + ' ' + user.lastName";
OgnlContext context = Ognl.createDefaultContext(null);

// Pre-compile the expression
Node expr = Ognl.compileExpression(context, user, template);

// Reuse compiled expression for each user
for (User user : users) {
    String name = (String) expr.getValue(context, user);
    renderOutput(name);
}

// ✅ COMPILED is better:
// - One-time compilation cost
// - Fast repeated evaluations
// - Significant performance gain
```

### Example 3: Property Binding (COMPILED)

```java
// UI property binding - frequent updates
String property = "model.displayName";
OgnlContext context = Ognl.createDefaultContext(model);

// Compile once
Node expr = Ognl.compileExpression(context, model, property);

// Fast repeated reads
String displayName = (String) expr.getValue(context, model);

// Fast repeated writes
expr.setValue(context, model, "New Name");

// ✅ COMPILED is better:
// - Expression evaluated on every UI update
// - Performance critical for responsiveness
// - Compilation cost amortized
```

### Example 4: User-Provided Expressions (INTERPRETED)

```java
// User enters expression in UI
String userExpression = getUserInput(); // Unknown complexity
OgnlContext context = Ognl.createDefaultContext(data);

try {
    // Use interpreted - expression is unknown
    Object result = Ognl.getValue(userExpression, context, data);
    displayResult(result);
} catch (OgnlException e) {
    showError("Invalid expression");
}

// ✅ INTERPRETED is better:
// - Expression used once
// - May not be compilable
// - Error handling simpler
// - No wasted compilation
```

---

## 🛠️ API Usage

### Using INTERPRETED Mode

**Direct evaluation** (always interpreted):
```java
Object result = Ognl.getValue("expression", context, root);
```

**Explicit node evaluation** (interpreted):
```java
Node expr = Ognl.parseExpression("expression");
Object result = expr.getValue(context, root);
// Note: No compilation, uses getValueBody()
```

### Using COMPILED Mode

**Compile and evaluate**:
```java
// Compile the expression
Node expr = Ognl.compileExpression(context, root, "expression");

// Reuse compiled expression
Object result = expr.getValue(context, root);
// Uses compiled accessor if available
```

**Check compilation status**:
```java
Node expr = Ognl.compileExpression(context, root, "expression");

if (expr.getAccessor() != null) {
    // Expression was successfully compiled
    // Future evaluations will use bytecode
} else {
    // Compilation failed or not possible
    // Will fall back to interpreted mode
}
```

---

## ⚠️ When Compilation May Fail

Some expressions cannot be compiled. In these cases, evaluation automatically falls back to INTERPRETED mode.

### Uncompilable Scenarios

1. **Null intermediate values**:
   ```java
   // If user.address is null, cannot compile full chain
   "user.address.zipCode"
   ```

2. **Dynamic method selection**:
   ```java
   // Method name determined at runtime
   "@java.lang.Math@max(a, b)"
   ```

3. **Complex lambda expressions**:
   ```java
   "items.{? #this.price > 100 }"
   ```

4. **Runtime type determination**:
   ```java
   "#var = someValue, #var.someMethod()"
   ```

### Handling Compilation Failures

```java
try {
    Node expr = Ognl.compileExpression(context, root, "expression");

    if (expr.getAccessor() == null) {
        // Compilation failed - will use interpreted mode
        logger.debug("Expression not compiled, using interpreted mode");
    }

    // Evaluation works either way
    Object result = expr.getValue(context, root);

} catch (Exception e) {
    // Handle compilation errors
}
```

---

## 🎯 Best Practices

### 1. Profile Before Optimizing

```java
// Measure actual performance
long start = System.nanoTime();

// Your evaluation code here

long duration = System.nanoTime() - start;
```

Don't assume compilation is always better - measure!

### 2. Cache Compiled Expressions

```java
// Bad: Recompile every time
for (User user : users) {
    Node expr = Ognl.compileExpression(context, user, "name");
    String name = (String) expr.getValue(context, user);
}

// Good: Compile once, reuse
Node expr = Ognl.compileExpression(context, null, "name");
for (User user : users) {
    String name = (String) expr.getValue(context, user);
}
```

### 3. Use Appropriate Mode for Context

```java
// Configuration loading - one-time use
public void loadConfig() {
    // Use interpreted - simpler and faster for one-time use
    Object value = Ognl.getValue(configPath, context, root);
}

// Hot path - repeated evaluations
public void renderTemplate() {
    // Use compiled - performance critical
    if (compiledExpr == null) {
        compiledExpr = Ognl.compileExpression(context, root, template);
    }
    return compiledExpr.getValue(context, data);
}
```

### 4. Graceful Degradation

```java
// Try to compile, but work either way
Node expr = Ognl.compileExpression(context, root, expression);

// Evaluation automatically uses best available mode
Object result = expr.getValue(context, root);
```

---

## 📈 Performance Tuning

### When to Compile

**Compile if**:
- Expression evaluated >50 times
- Performance is critical
- Expression is static/known

**Don't compile if**:
- Expression evaluated <50 times
- Expression is dynamic
- One-time evaluation

### Expression Caching Strategy

```java
// Cache for high-frequency expressions
private final Map<String, Node> expressionCache = new ConcurrentHashMap<>();

public Object evaluateWithCache(String expr, Object root) {
    Node compiled = expressionCache.computeIfAbsent(expr, e -> {
        try {
            return Ognl.compileExpression(context, root, e);
        } catch (Exception ex) {
            return Ognl.parseExpression(e); // Fallback to parsed
        }
    });

    return compiled.getValue(context, root);
}
```

---

## 🧪 Testing Recommendations

### Always Test Both Modes

```java
@ParameterizedTest
@EnumSource(OgnlExecutionMode.class)
void testExpression(OgnlExecutionMode mode) throws Exception {
    Object result = DualModeTestUtils.prepareAndEvaluate(
            "expression", context, root, mode);

    assertEquals(expected, result);
}
```

**Why**: Ensures both execution paths produce identical results.

---

## 🔄 Migration Guide

### From Always Interpreted

**Before**:
```java
Object result = Ognl.getValue(expr, context, root);
```

**After** (with conditional compilation):
```java
Node node;
if (isFrequentlyEvaluated(expr)) {
    node = Ognl.compileExpression(context, root, expr);
} else {
    node = Ognl.parseExpression(expr);
}
Object result = node.getValue(context, root);
```

### From Mixed Usage

**Before**:
```java
// Sometimes compiled, sometimes not - inconsistent
Object result1 = Ognl.getValue(expr1, context, root);
Node node2 = Ognl.compileExpression(context, root, expr2);
Object result2 = node2.getValue(context, root);
```

**After** (consistent strategy):
```java
// Define clear policy
private Node prepareExpression(String expr) {
    if (shouldCompile(expr)) {
        return Ognl.compileExpression(context, root, expr);
    } else {
        return Ognl.parseExpression(expr);
    }
}
```

---

## 📚 Related Documentation

- [Dual-Mode Testing Guide](DUAL_MODE_TESTING_GUIDE.md) - How to test both modes
- [Code Review Checklist](CODE_REVIEW_CHECKLIST.md) - Ensuring mode parity
- [CI/CD Configuration](CI_CD_DUAL_MODE_CONFIGURATION.md) - Automated testing

---

## ❓ FAQ

**Q: Should I always use compiled mode for performance?**

A: No. Compilation has overhead. For expressions evaluated <50 times, interpreted mode is often faster overall.

**Q: What happens if compilation fails?**

A: Evaluation automatically falls back to interpreted mode. The expression still works, just without optimization.

**Q: Can I force interpreted mode even with a compiled expression?**

A: The API uses the best available mode. If you need interpreted mode, don't compile the expression.

**Q: How do I know if my expression was compiled?**

A: Check `node.getAccessor() != null` after calling `compileExpression()`.

**Q: Does compilation work with all expressions?**

A: No. Some expressions (especially with null intermediate values) cannot be fully compiled.

**Q: Is there a performance penalty for falling back to interpreted mode?**

A: Minimal. The fallback is transparent and efficient.

---

## 🎓 Summary

| Factor | INTERPRETED | COMPILED |
|--------|-------------|----------|
| **First evaluation** | Fast | Slow (compilation) |
| **Repeated evaluations** | Slow | Fast |
| **Memory usage** | Low | Higher |
| **Reliability** | Always works | May fail |
| **Use case** | One-time, dynamic | Repeated, static |
| **Break-even point** | N/A | ~50 evaluations |

**Golden Rule**: Profile first, optimize second. Use the mode that fits your actual usage pattern.
