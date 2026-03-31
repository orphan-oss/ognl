# Issue #18 — Compiled vs Interpreted Mode Parity

## Context

Issue [#18](https://github.com/orphan-oss/ognl/issues/18) reports 85 test failures + 3 errors when running OGNL expressions in compiled mode vs interpreted mode. [PR #555](https://github.com/orphan-oss/ognl/pull/555) (draft) fixes 3 compiler bugs (ASTConst Long/Float suffixes, ASTOr boolean boxing) and adds a dual-mode test harness (`DualModeEvaluationTest`). ~80 failures remain.

## Current State

### Already Fixed (PR #555)
- `ASTConst.toGetSourceString()` — Long `L` and Float `f` suffixes
- `ASTOr.toGetSourceString()` — unconditional `($w)` boxing on both ternary branches
- 70+ dual-mode tests, 11 `@Disabled` for known BigDecimal/BigInteger/instanceof limitations

### Remaining Failures (~80) by Category

| # | Category | Failures | Test Class(es) | Key Code Path | Difficulty |
|---|----------|----------|-----------------|---------------|------------|
| 1 | Primitive null handling | ~4 | `PrimitiveNullHandlingTest` | `ObjectPropertyAccessor` setter with null-to-primitive | Medium |
| 2 | Property access/setting | ~3 | `PropertyTest` | `ASTProperty.toGetSourceString()` type context | Medium |
| 3 | Setter with conversion | ~8 | `SetterWithConversionTest` | `ObjectPropertyAccessor.getSourceSetter()` | Medium |
| 4 | Setter paths | ~12 | `SetterTest` | `ASTChain.toSetSourceString()`, `MapPropertyAccessor`, `ListPropertyAccessor` | Medium |
| 5 | Number format exceptions | ~6 | `NumberFormatExceptionTest` | Setter type conversion error handling | Medium |
| 6 | Indexed properties | ~5 | `IndexedPropertyTest`, `IndexAccessTest` | `ASTProperty` dynamic subscripts (^, \|, $) | Medium |
| 7 | Array elements | ~3 | `ArrayElementsTest` | `ASTRootVarRef` + `ASTChain` root casting | Medium |
| 8 | Method calls | ~4 | `MethodTest`, `MethodWithConversionTest` | `ASTMethod.toGetSourceString()` parameter conversion | Medium-Hard |
| 9 | Interface inheritance | ~2 | `InterfaceInheritanceTest` | `ASTProperty` interface type resolution | Hard |
| 10 | Generics | ~1 | `GenericsTest` | Generic type erasure in compiled code | Hard |
| 11 | BigDecimal/BigInteger ops | ~30 | (covered by @Disabled) | Javassist can't use Java operators on these types | Hard (redesign) |

## Recommended Approach: Incremental PRs

Each PR should: add dual-mode tests for its category, fix what's fixable, `@Disable` what needs deeper work.

### PR 2: Primitive Null Handling & Basic Property Access (categories 1-2)
**Files to modify:**
- `ognl/src/main/java/ognl/ObjectPropertyAccessor.java` — `getSourceSetter()` null-to-primitive handling
- `ognl/src/test/java/ognl/test/DualModeEvaluationTest.java` — add NullToPrimitive and PropertyAccess setter tests

**Investigation steps:**
1. Read `PrimitiveNullHandlingTest` to understand exact expressions and expected values
2. Read `ObjectPropertyAccessor.getSourceSetter()` and compare with interpreted path in `OgnlRuntime.setProperty()`
3. Trace what compiled code is generated for `intValue` setter with null value
4. Fix: ensure compiled setter calls `OgnlOps.convertValue()` for null-to-primitive conversion

### PR 3: Setter Paths & Type Conversion (categories 3-5)
**Files to modify:**
- `ognl/src/main/java/ognl/ObjectPropertyAccessor.java`
- `ognl/src/main/java/ognl/MapPropertyAccessor.java`
- `ognl/src/main/java/ognl/ListPropertyAccessor.java`
- `ognl/src/main/java/ognl/ASTChain.java` — `toSetSourceString()`

**Investigation steps:**
1. Read `SetterTest` and `SetterWithConversionTest` expressions
2. Read `MapPropertyAccessor.getSourceSetter()` — handles `map.newValue`, `map[key]`
3. Read `ListPropertyAccessor.getSourceSetter()` — handles `settableList[0]`
4. Compare interpreted setter flow (`OgnlRuntime.setProperty`) with compiled setter generation
5. Focus on type conversion during set operations

### PR 4: Indexed Properties & Array Elements (categories 6-7)
**Files to modify:**
- `ognl/src/main/java/ognl/ASTProperty.java` — dynamic subscript compilation (^, |, $)
- `ognl/src/main/java/ognl/ASTRootVarRef.java` — root array casting

**Investigation steps:**
1. Read `IndexedPropertyTest` and `ArrayElementsTest`
2. Trace `ASTProperty.toGetSourceString()` for indexed access (lines 144-200)
3. Check how `DynamicSubscript` is handled in compiled path
4. Check `#root[1]` compilation path through `ASTRootVarRef` + `ASTChain`

### PR 5: Method Calls (category 8)
**Files to modify:**
- `ognl/src/main/java/ognl/ASTMethod.java` — parameter type conversion

**Investigation steps:**
1. Read `MethodTest` and `MethodWithConversionTest`
2. Trace `ASTMethod.toGetSourceString()` parameter generation (lines 167-200)
3. Check varargs and type conversion for method parameters
4. Compare with interpreted `OgnlRuntime.callMethod()` flow

### PR 6: Interface Inheritance & Generics (categories 9-10)
**Files to modify:**
- `ognl/src/main/java/ognl/ASTProperty.java` — interface type resolution
- `ognl/src/main/java/ognl/enhance/ExpressionCompiler.java` — `getSuperOrInterfaceClass()`

### PR 7: BigDecimal/BigInteger (category 11) — Deferred
This is ~30 failures requiring the compiler to generate `OgnlOps` helper calls instead of Java operators for BigDecimal/BigInteger arithmetic. This is a significant redesign and should be a separate initiative.

## Key Files Reference

### Compiler infrastructure
- `ognl/src/main/java/ognl/enhance/ExpressionCompiler.java` — core compiler, generates Javassist classes
- `ognl/src/main/java/ognl/enhance/OgnlExpressionCompiler.java` — compiler interface
- `ognl/src/main/java/ognl/OgnlRuntime.java` — `getChildSource()` (line 2578), `isBoolean()` (line 2530)

### AST nodes (source generation)
- `ognl/src/main/java/ognl/ASTProperty.java` — property get/set (~520 lines)
- `ognl/src/main/java/ognl/ASTMethod.java` — method calls (~450 lines)
- `ognl/src/main/java/ognl/ASTChain.java` — expression chains (~420 lines)
- `ognl/src/main/java/ognl/ASTList.java` — list literals (~175 lines)
- `ognl/src/main/java/ognl/ASTSequence.java` — expression sequences (~145 lines)
- `ognl/src/main/java/ognl/ASTOr.java` — logical OR (already fixed)
- `ognl/src/main/java/ognl/ASTConst.java` — constants (already fixed)

### Property accessors (setter source generation)
- `ognl/src/main/java/ognl/ObjectPropertyAccessor.java` — default accessor
- `ognl/src/main/java/ognl/MapPropertyAccessor.java` — map access
- `ognl/src/main/java/ognl/ListPropertyAccessor.java` — list access
- `ognl/src/main/java/ognl/ArrayPropertyAccessor.java` — array access

### Test infrastructure
- `ognl/src/test/java/ognl/test/DualModeEvaluationTest.java` — dual-mode test harness
- `ognl/src/test/java/ognl/test/enhance/ExpressionCompilerTest.java` — compiler unit tests

### Existing test classes to add dual-mode coverage for
- `ognl/src/test/java/ognl/test/SetterTest.java`
- `ognl/src/test/java/ognl/test/SetterWithConversionTest.java`
- `ognl/src/test/java/ognl/test/PropertyTest.java`
- `ognl/src/test/java/ognl/test/PrimitiveNullHandlingTest.java`
- `ognl/src/test/java/ognl/test/NumberFormatExceptionTest.java`
- `ognl/src/test/java/ognl/test/IndexedPropertyTest.java`
- `ognl/src/test/java/ognl/test/IndexAccessTest.java`
- `ognl/src/test/java/ognl/test/MethodTest.java`
- `ognl/src/test/java/ognl/test/ArrayElementsTest.java`
- `ognl/src/test/java/ognl/test/InterfaceInheritanceTest.java`
- `ognl/src/test/java/ognl/test/GenericsTest.java`

## Debugging Tips

- Add temporary logging in `ExpressionCompiler.compileExpression()` to see generated Java source
- The compiled source is valid Java passed to Javassist — reading it reveals type mismatches
- `OgnlRuntime.getChildSource()` catches `ArithmeticException` and returns `"0"` with `int.class` (line 2587) — this can mask real issues
- Compare interpreted flow (`getValueBody`/`setValueBody`) with compiled flow (`toGetSourceString`/`toSetSourceString`) side by side for each AST node

## Verification

For each PR, run:
```bash
./mvnw test -pl ognl                                   # Full suite — must stay green
./mvnw test -pl ognl -Dtest=DualModeEvaluationTest     # Dual-mode tests specifically
```