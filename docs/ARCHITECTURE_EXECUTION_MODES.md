# OGNL Execution Modes Architecture

## Overview

OGNL implements two distinct execution paths for evaluating expressions. This document explains the technical architecture and implementation differences.

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    OGNL Expression String                    │
└────────────────────────────┬────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  Parser (JavaCC) │
                    └────────┬────────┘
                             │
            ┌────────────────▼────────────────┐
            │     Abstract Syntax Tree (AST)   │
            │         (SimpleNode tree)        │
            └────────────┬────────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │                                 │
   INTERPRETED                       COMPILED
        │                                 │
        ▼                                 ▼
┌───────────────┐              ┌──────────────────┐
│ getValueBody()│              │toGetSourceString()│
└──────┬────────┘              └────────┬─────────┘
       │                                 │
       ▼                                 ▼
┌───────────────┐              ┌──────────────────┐
│AST Navigation │              │Bytecode Generation│
└──────┬────────┘              └────────┬─────────┘
       │                                 │
       ▼                                 ▼
┌───────────────┐              ┌──────────────────┐
│  Reflection   │              │ExpressionAccessor│
└──────┬────────┘              └────────┬─────────┘
       │                                 │
       ▼                                 ▼
┌───────────────┐              ┌──────────────────┐
│    Result     │              │  Direct Method   │
└───────────────┘              │      Calls       │
                               └────────┬─────────┘
                                        │
                                        ▼
                               ┌──────────────────┐
                               │     Result       │
                               └──────────────────┘
```

---

## 🔍 INTERPRETED Mode

### Core Implementation

**Key Class**: `ognl.SimpleNode`
**Key Method**: `getValueBody(OgnlContext context, Object source)`

### Execution Flow

1. **Parse**: Expression string → AST
2. **Navigate**: Walk AST tree recursively
3. **Evaluate**: Call `getValueBody()` on each node
4. **Reflect**: Use Java reflection to access properties/methods
5. **Return**: Final value

### Example AST Node: `ASTProperty`

```java
public class ASTProperty extends SimpleNode {
    @Override
    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException {
        // Get property name
        Object property = children[0].getValue(context, source);

        // Use reflection to get property value
        return OgnlRuntime.getProperty(context, source, property);
    }
}
```

### Characteristics

- **Every evaluation** calls `getValueBody()`
- **Reflection overhead** on each property access
- **No caching** of method lookups
- **Runtime type determination**
- **Works with all expressions**

---

## ⚡ COMPILED Mode

### Core Implementation

**Key Class**: `ognl.SimpleNode`
**Key Method**: `toGetSourceString(OgnlContext context, Object target)`
**Key Interface**: `ognl.ExpressionAccessor`

### Execution Flow

1. **Parse**: Expression string → AST
2. **Generate**: Walk AST calling `toGetSourceString()` on each node
3. **Build**: Construct Java source code
4. **Compile**: Generate bytecode via `ExpressionCompiler`
5. **Cache**: Store `ExpressionAccessor` on node
6. **Execute**: Call `accessor.get(context, target)`
7. **Return**: Final value

### Example AST Node: `ASTProperty`

```java
public class ASTProperty extends SimpleNode {
    @Override
    public String toGetSourceString(OgnlContext context, Object target) {
        // Generate Java code for property access
        String source = children[0].toGetSourceString(context, target);

        // Return direct method call code
        return source + ".get" + capitalize(propertyName) + "()";
    }
}
```

### Generated Accessor Example

For expression `user.name`:

```java
public class OgnlExpressionAccessor implements ExpressionAccessor {
    @Override
    public Object get(OgnlContext context, Object root) {
        return ((User) root).getName();
    }

    @Override
    public void set(OgnlContext context, Object root, Object value) {
        ((User) root).setName((String) value);
    }
}
```

### Characteristics

- **One-time compilation** cost
- **Direct method calls** (no reflection)
- **JIT-optimized** bytecode
- **Cached accessor** for reuse
- **May fail** for complex expressions

---

## 🔀 Comparison

| Aspect | INTERPRETED | COMPILED |
|--------|-------------|----------|
| **Entry Point** | `getValueBody()` | `accessor.get()` |
| **Property Access** | `OgnlRuntime.getProperty()` | `object.getProperty()` |
| **Method Calls** | `Method.invoke()` | Direct invocation |
| **Type Checking** | Runtime | Compile-time + Runtime |
| **Optimization** | None | JIT-friendly |
| **Code Path** | AST navigation | Generated bytecode |
| **Caching** | Method cache only | Full accessor cache |

---

## 📦 Key Classes

### Core Evaluation

**`ognl.Ognl`**
- Entry point for expression evaluation
- `getValue()` - Interpreted mode
- `compileExpression()` - Compiled mode

**`ognl.SimpleNode`**
- Base class for all AST nodes
- Implements both execution paths
- `getValueBody()` for interpreted
- `toGetSourceString()` for compiled

**`ognl.OgnlContext`**
- Evaluation context
- Stores variables, root object
- Provides type converter, member access

### Compiled Mode Classes

**`ognl.ExpressionAccessor`**
- Interface for compiled accessors
- `get(context, target)` - Get value
- `set(context, target, value)` - Set value

**`ognl.ExpressionCompiler`**
- Generates accessor bytecode
- Compiles Java source to class
- Creates accessor instances

**`ognl.enhance.ExpressionAccessor`**
- Runtime-generated implementation
- Optimized for specific expression
- Direct method calls

---

## 🎯 AST Node Types

### Expression Nodes

Each AST node type implements both modes:

**`ASTConst`** - Constant values
- Interpreted: Returns constant directly
- Compiled: Generates literal in code

**`ASTProperty`** - Property access
- Interpreted: `OgnlRuntime.getProperty()`
- Compiled: `object.getProperty()`

**`ASTMethod`** - Method invocation
- Interpreted: `Method.invoke()`
- Compiled: Direct method call

**`ASTChain`** - Chained expressions
- Interpreted: Sequential evaluation
- Compiled: Chained method calls

**`ASTList`** - List literals
- Interpreted: Constructs list at runtime
- Compiled: Generates list creation code

---

## 🔧 Compilation Process

### Step 1: Source Generation

```java
// Expression: user.address.city
Node expr = Ognl.parseExpression("user.address.city");

// Generate source code
String source = expr.toGetSourceString(context, user);
// Result: "((User)$2).getAddress().getCity()"
```

### Step 2: Accessor Creation

```java
// Compile to bytecode
ExpressionAccessor accessor = expressionCompiler.compile(context, expr, source);

// Cache on node
expr.setAccessor(accessor);
```

### Step 3: Execution

```java
// Future evaluations use cached accessor
Object value = expr.getAccessor().get(context, user);
// Direct method calls, no reflection
```

---

## 🚫 Compilation Limitations

### Cannot Compile When:

1. **Null intermediate values**
   ```java
   user.address.city  // If user.address is null
   ```

2. **Dynamic types**
   ```java
   #root[someKey]  // Key determined at runtime
   ```

3. **Complex lambda expressions**
   ```java
   collection.{? #this.property > value}
   ```

4. **Runtime method selection**
   ```java
   @className@methodName(args)  // Method from string
   ```

### Fallback Behavior

When compilation fails:
- `accessor` remains null
- Evaluation uses `getValueBody()` instead
- No error thrown
- Transparent fallback

---

## 💡 Implementation Best Practices

### For AST Node Authors

When implementing a new AST node:

1. **Implement `getValueBody()`** (required)
   - Always works
   - Handle all edge cases
   - Proper null checking

2. **Implement `toGetSourceString()`** (optional but recommended)
   - Enable compilation for your node
   - Generate type-safe code
   - Handle casting properly

3. **Test both paths**
   - Use dual-mode tests
   - Verify identical results
   - Test compilation failures

### Code Generation Guidelines

**Type Safety**:
```java
// Good - includes cast
"((" + targetClass.getName() + ")$2).getProperty()"

// Bad - missing cast
"$2.getProperty()"
```

**Null Safety**:
```java
// Good - null check
"($2 != null ? ((" + type + ")$2).getValue() : null)"

// Bad - no null check
"((" + type + ")$2).getValue()"
```

---

## 🔬 Debugging

### Check Execution Mode

```java
Node expr = Ognl.compileExpression(context, root, "expression");

if (expr.getAccessor() != null) {
    System.out.println("Using COMPILED mode");
} else {
    System.out.println("Using INTERPRETED mode");
}
```

### Force Interpreted Mode

```java
// Don't compile - always interpreted
Node expr = Ognl.parseExpression("expression");
Object value = expr.getValue(context, root);
```

### Inspect Generated Code

```java
// Get generated source code
String source = expr.toGetSourceString(context, root);
System.out.println("Generated: " + source);
```

---

## 📚 Related Documentation

- [Execution Mode Selection Guide](EXECUTION_MODE_SELECTION_GUIDE.md)
- [Dual-Mode Testing Guide](DUAL_MODE_TESTING_GUIDE.md)
- [Code Review Checklist](CODE_REVIEW_CHECKLIST.md)

---

## 🎓 Summary

**INTERPRETED Mode**:
- Uses `getValueBody()` method
- Reflection-based property access
- Works for all expressions
- Slower but reliable

**COMPILED Mode**:
- Uses `toGetSourceString()` → bytecode
- Direct method calls
- Faster repeated evaluations
- May fail for complex expressions

**Key Principle**: Both modes must produce identical results for the same expression and input.
