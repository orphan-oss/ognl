# Null-Safe Navigation Operator (.?)

## Overview

The null-safe navigation operator `.?` provides a safe way to navigate object graphs without throwing exceptions when encountering null intermediate values. This feature is inspired by similar operators in Kotlin (`.?`), C# (`.?`), TypeScript (`.?`), Groovy (`.?`), and PHP 8.0 (`?->`).

## Motivation

### Current Behavior

In standard OGNL, navigating through null objects typically throws an exception:

```java
// If user.profile is null, this throws an exception
String home = (String) Ognl.getValue("user.profile.home", context, root);
```

Users must use workarounds:
1. Ternary expressions: `user.profile != null ? user.profile.home : null`
2. Custom NullHandler implementations
3. Relying on short-circuit behavior (system property dependent)

### Problems with Current Approaches

1. **Verbose**: Ternary expressions become unwieldy for deep navigation
2. **Global**: NullHandler affects all property access for a class
3. **Inconsistent**: Short-circuit behavior varies by configuration
4. **Unclear**: Not explicit in the expression what is null-safe

### Proposed Solution

Explicit null-safe operator that works at any level:

```java
// Returns null gracefully if any intermediate value is null
String home = (String) Ognl.getValue("user.?profile.?home", context, root);
```

## Syntax

### Operator: `.?`

We chose `.?` (dot-question, "safe dot") for the following reasons:

1. **Visual clarity**: The `.?` reads as "safe dot" - a dot with safety check
2. **Distinction**: Different from Kotlin/TypeScript `?.` (question-dot), giving OGNL its own identity
3. **No ambiguity**: No conflict with existing `?:` ternary operator
4. **Precedence clarity**: Aligns with existing dot operator parsing

### Grammar

The operator is added to the navigation chain production in the JavaCC grammar:

```
navigationChain() :=
    primaryExpression()
    ( "." <navigation-element>
    | ".?" <navigation-element>  // New: null-safe navigation
    | "[" <index> "]"
    | ...
    )*
```

## Semantics

### Basic Property Access

```java
// If obj is null, returns null instead of throwing exception
obj.?property
obj.?method()
obj.?field
```

### Chaining

Each `.?` operator is independent and short-circuits only that specific access:

```java
// Regular chain: exception if any value is null
a.b.c.d

// Fully null-safe: null if any value is null
a.?b.?c.?d

// Mixed: exception if 'a' is null, null if 'b' is null, exception if 'c' is null
a.b.?c.d
```

### Method Calls

Null-safe operator works with method invocations:

```java
user.?getProfile().?getHome()

// If user is null: does not call getProfile(), returns null
// If user.getProfile() is null: does not call getHome(), returns null
```

### Indexed Access

Null-safe operator works with array/collection access:

```java
array?.?[0]           // Null if array is null
map?.?["key"]         // Null if map is null
user.?addresses?.?[0] // Null if user or addresses is null
```

### Edge Cases

#### 1. Null Root Object

```java
null.?property  // Returns null
```

#### 2. Multiple Null-Safe Operators

```java
a?.?b?.?c?.?d  // Each level checked independently
```

#### 3. Combination with Other Operators

```java
// Null coalescing with safe access
(user.?profile ?: defaultProfile).home

// Conditional with safe access
user.?profile.?verified ? "yes" : "no"

// Method call results
user.?getScores().?{? #this > 50}  // Projection on potentially null collection
```

#### 4. Assignment (Setter) Semantics

**Phase 1 (Current)**: Read-only support
```java
user.?profile.?home  // Supported: returns null if any part is null
```

**Future Phase**: Setter semantics (to be determined)
```java
user.?profile.?home = "new value"  // TBD: Should this be a no-op if user or profile is null?
```

Decision deferred to gather user feedback on expected behavior.

## Implementation Design

### 1. Parser Level (ognl.jj)

#### Token Definition

```java
TOKEN:
{
    < NULL_SAFE_DOT: ".?" >
}
```

#### Grammar Production

Modify `navigationChain()` to recognize `.?`:

```java
void navigationChain() : {
    boolean nullSafe = false;
}
{
    primaryExpression()
    (
        ( "." { nullSafe = false; } | ".?" { nullSafe = true; } )
        #Chain( 2)
        (
            ( LOOKAHEAD(2) methodCall() | propertyName() )
            | ( LOOKAHEAD(2) projection() | selection() )
            | "(" expression() ")"
        )
        {
            // Set null-safe flag on the created ASTChain node
            ((ASTChain)jjtree.peekNode()).setNullSafe(nullSafe);
        }
    |
        // ... existing productions for indexing, etc.
    )*
}
```

### 2. AST Node Level

#### ASTChain Modifications

```java
public class ASTChain<C extends OgnlContext<C>> extends SimpleNode<C> {
    private boolean nullSafe = false;  // New field

    public void setNullSafe(boolean nullSafe) {
        this.nullSafe = nullSafe;
    }

    public boolean isNullSafe() {
        return nullSafe;
    }

    protected Object getValueBody(C context, Object source) throws OgnlException {
        Object result = source;

        // Null-safe check at chain start
        if (nullSafe && result == null) {
            return null;
        }

        // Existing short-circuit logic...
        if (shortCircuit && result == null && !(parent instanceof ASTIn)) {
            return null;
        }

        for (int i = 0, ilast = children.length - 1; i <= ilast; ++i) {
            // Null-safe check during iteration
            if (nullSafe && result == null) {
                return null;
            }

            // ... rest of existing logic
            result = children[i].getValue(context, result);
        }
        return result;
    }
}
```

#### ASTProperty Modifications

```java
public class ASTProperty<C extends OgnlContext<C>> extends SimpleNode<C> {
    private boolean nullSafe = false;  // New field

    public void setNullSafe(boolean nullSafe) {
        this.nullSafe = nullSafe;
    }

    public boolean isNullSafe() {
        return nullSafe;
    }

    protected Object getValueBody(C context, Object source) throws OgnlException {
        // Null-safe check
        if (nullSafe && source == null) {
            return null;
        }

        Object property = getProperty(context, source);
        Object result = OgnlRuntime.getProperty(context, source, property);

        if (result == null && !nullSafe) {
            // Only invoke NullHandler if not using null-safe operator
            NullHandler<C> nullHandler = OgnlRuntime.getNullHandler(
                OgnlRuntime.getTargetClass(source));
            result = nullHandler.nullPropertyValue(context, source, property);
        }

        return result;
    }
}
```

### 3. Bytecode Compiler Level

The `toGetSourceString` and `toSetSourceString` methods need to handle null-safe chains:

```java
public String toGetSourceString(C context, Object target) {
    if (nullSafe && target == null) {
        return "null";
    }

    if (nullSafe) {
        // Generate null-check wrapped access
        // Example: (target != null ? target.property : null)
        String result = generateNullSafeAccess(context, target);
        return result;
    }

    // ... existing logic for normal access
}

private String generateNullSafeAccess(C context, Object target) {
    String targetExpr = "...";  // Get target expression
    String accessExpr = "...";   // Get access expression

    return String.format("(%s != null ? %s : null)", targetExpr, accessExpr);
}
```

### 4. AST Node Flag Propagation

When parsing `.?property`, the parser needs to:

1. Create an ASTChain node (as usual)
2. Mark it as `nullSafe = true`
3. The ASTChain evaluates normally but checks for null before navigation

## Interaction with Existing Features

### 1. Short-Circuit Behavior

The null-safe operator is **explicit** and **independent** from the existing short-circuit behavior:

```java
// System property: ognl.chain.short-circuit=true (default)
user.profile.home  // May return null due to short-circuit

// System property: ognl.chain.short-circuit=false
user.profile.home  // Throws exception if profile is null

// Null-safe operator: always returns null regardless of system property
user.?profile.?home  // Always returns null if any part is null
```

### 2. NullHandler

Null-safe operator **bypasses** NullHandler for that specific access:

```java
// Without null-safe: NullHandler invoked if property returns null
user.profile  // NullHandler.nullPropertyValue() called if profile is null

// With null-safe: NullHandler NOT invoked
user.?profile  // Simply returns null, NullHandler not consulted
```

**Rationale**: The null-safe operator expresses explicit intent to handle null. If users want custom null handling, they should use regular `.` access.

### 3. Projection and Selection

Null-safe operator protects the collection access:

```java
// Exception if users is null
users.{name}

// Null if users is null
users.?{name}

// Null if users is null, otherwise projection proceeds normally
users.?{? #this.age > 18}
```

### 4. Method Invocation

Null-safe prevents method calls on null objects:

```java
// Exception if user is null
user.getProfile().getHome()

// Null if user is null (getProfile not called)
user.?getProfile().?getHome()
```

## Testing Strategy

### Test Categories

#### 1. Basic Property Access (10 tests)
- Null root object: `null.?property`
- Non-null access: `object.?property`
- Nested null-safe: `a.?b.?c`
- Mixed safe/unsafe: `a.b.?c.d`

#### 2. Method Calls (8 tests)
- Null object method: `null.?method()`
- Method chain: `obj.?method1().?method2()`
- Method with arguments: `obj.?method(arg1, arg2)`
- Mixed property and method: `obj.?property.?method()`

#### 3. Indexed Access (8 tests)
- Null array: `null?.?[0]`
- Null map: `null?.?["key"]`
- Array bounds: `array?.?[999]`
- Nested indexed: `matrix?.?[0]?.?[0]`

#### 4. Collection Operations (8 tests)
- Null projection: `null.?{name}`
- Null selection: `null.?{? #this > 0}`
- Safe projection: `list.?{name}`
- Safe selection: `list.?{? #this.verified}`

#### 5. Combined Operators (10 tests)
- Null coalescing: `obj.?prop ?: default`
- Conditional: `obj.?prop ? 'yes' : 'no'`
- Assignment context: `#var = obj.?prop`
- Arithmetic: `(obj.?value ?: 0) + 10`

#### 6. Edge Cases (12 tests)
- Multiple consecutive: `a?.?b?.?c`
- Empty strings: `obj.?""` (if allowed)
- Special values: `obj.?null`, `obj.?true`
- Variable references: `#root.?#var.?prop`
- Static references: `@Class@FIELD.?method()`
- Deep nesting: 10+ levels of null-safe access

#### 7. Bytecode Compilation (10 tests)
- Compiled null-safe access
- Performance comparison with regular access
- Null-safe in loops
- Complex expressions compilation

#### 8. Interaction Tests (10 tests)
- With NullHandler
- With short-circuit enabled/disabled
- With MemberAccess restrictions
- With TypeConverter

#### 9. Error Cases (6 tests)
- Syntax errors: `.?`, `?.?`, `..?`
- Invalid combinations
- Parser edge cases

#### 10. Regression Tests (10 tests)
- All existing tests must pass
- Performance benchmarks
- Memory usage patterns

**Total: 92 comprehensive tests** ensuring 100% code coverage

### Coverage Requirements

1. **Line Coverage**: 100% of new code
2. **Branch Coverage**: 100% of null-safe conditionals
3. **Path Coverage**: All combinations of null/non-null values
4. **Mutation Testing**: Verify tests catch intentional bugs

### Test Execution

```bash
# Run tests with coverage
mvn clean test -Pcoverage

# Generate coverage report
mvn jacoco:report

# Verify coverage thresholds
# New classes/methods must have 100% coverage
```

## Performance Considerations

### Expected Overhead

The null-safe operator introduces minimal overhead:

1. **Parsing**: One additional token check
2. **AST Construction**: One boolean flag per chain node
3. **Evaluation**: One null check per `.?` operator

### Optimization Strategies

1. **Compiler optimization**: Generate efficient bytecode with single null check
2. **Short-circuit early**: Return immediately on first null encounter
3. **No exception overhead**: Avoid try-catch blocks in null-safe paths

### Benchmarks

Benchmark comparison (to be measured):

```java
// Baseline: try-catch
try { return user.profile.home; } catch (NullPointerException e) { return null; }

// Ternary chain (current workaround)
user != null ? (user.profile != null ? user.profile.home : null) : null

// Null-safe operator (new)
user.?profile.?home
```

Expected: Null-safe operator should be **faster** than try-catch and **comparable** to ternary chain.

## Future Enhancements

### Phase 2: Write Support

Setter semantics for null-safe chains:

```java
// Option 1: Silent no-op (Kotlin style)
user.?profile.?home = "new"  // Does nothing if user or profile is null

// Option 2: Create intermediate objects
user.?profile.?home = "new"  // Creates profile if null (risky)

// Option 3: Error
user.?profile.?home = "new"  // Compile error or runtime exception
```

**Decision**: Deferred pending community feedback.

### Phase 3: Null Coalescing Assignment

```java
user.?profile.?home ?: "default"  // Already works
user.?profile.?home ?= "default"  // Future: assign only if null
```

### Phase 4: Null-Safe Method Reference

```java
users.?{.?getName()}  // Null-safe within projection
```

## Migration Guide

### Existing Code

All existing OGNL expressions remain unchanged and fully compatible.

### Adopting Null-Safe Operator

**Before:**
```java
// Verbose ternary
String home = (user != null && user.getProfile() != null)
    ? user.getProfile().getHome()
    : null;

// Or with try-catch
try {
    home = user.getProfile().getHome();
} catch (NullPointerException e) {
    home = null;
}
```

**After:**
```java
// Concise and explicit
String home = Ognl.getValue("user.?getProfile().?getHome()", context, root);
```

### Interaction with NullHandler

If you have custom NullHandler implementations:

```java
// Old: NullHandler always invoked on null properties
class MyNullHandler implements NullHandler {
    public Object nullPropertyValue(C context, Object target, Object property) {
        return "DEFAULT";  // Always called when property is null
    }
}

// New behavior:
user.profile      // Returns "DEFAULT" via NullHandler if profile is null
user.?profile     // Returns null, NullHandler NOT invoked
```

**Recommendation**: Use `.?` when you want explicit null returns. Use regular `.` when you want NullHandler behavior.

## Examples

### Real-World Use Cases

#### 1. User Profile Navigation

```java
// Get user's home city, null if any intermediate value is null
String city = Ognl.getValue("user.?profile.?address.?city", context, root);
```

#### 2. Optional Configuration

```java
// Get optional config value
Integer timeout = Ognl.getValue("config.?database.?timeout ?: 3000", context, root);
```

#### 3. Collection Processing

```java
// Get names of active users, null if users list is null
List<String> names = Ognl.getValue("users.?{? #this.active}.?{name}", context, root);
```

#### 4. API Response Handling

```java
// Navigate JSON-like structure safely
Object data = Ognl.getValue("response.?body.?data.?items?.?[0].?id", context, root);
```

#### 5. Conditional UI Rendering

```java
// Check if user has admin role, false if user is null
boolean isAdmin = Ognl.getValue("user.?roles.?contains('ADMIN') ?: false", context, root);
```

## References

### Similar Features in Other Languages

1. **Kotlin**: `.?` operator
   - Docs: https://kotlinlang.org/docs/null-safety.html

2. **C#**: `.?` operator (Null-conditional operator)
   - Docs: https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/operators/member-access-operators#null-conditional-operators--and-

3. **TypeScript**: `.?` (Optional chaining)
   - Docs: https://www.typescriptlang.org/docs/handbook/release-notes/typescript-3-7.html#optional-chaining

4. **Groovy**: `.?` (Safe navigation operator)
   - Docs: https://groovy-lang.org/operators.html#_safe_navigation_operator

5. **PHP 8.0**: `?->` (Nullsafe operator)
   - Docs: https://php.watch/versions/8.0/null-safe-operator

6. **Swift**: `?` (Optional chaining)
   - Docs: https://docs.swift.org/swift-book/LanguageGuide/OptionalChaining.html

## Version History

- **v3.6.0** (Proposed): Initial implementation of `.?` null-safe operator
  - Basic property and method access
  - Comprehensive test coverage
  - Documentation and migration guide

---

**Document Status**: Draft
**Last Updated**: 2025-11-07
**Authors**: OGNL Development Team
**Related Issues**: [To be added]
