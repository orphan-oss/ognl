# Issue #18 — Compiled vs Interpreted Mode Parity

## Context

Issue [#18](https://github.com/orphan-oss/ognl/issues/18) reports 85 test failures + 3 errors when running OGNL expressions in compiled mode vs interpreted mode. [PR #555](https://github.com/orphan-oss/ognl/pull/555) fixed 3 compiler bugs (ASTConst Long/Float suffixes, ASTOr boolean boxing). [PR #556](https://github.com/orphan-oss/ognl/pull/556) added 82 dual-mode tests confirming categories 1-10 largely work.

## Current State (2026-04-01)

### Dual-Mode Test Coverage: 225 tests (213 active, 12 disabled)

Comprehensive dual-mode testing across all categories reveals that the compiled mode works correctly for the vast majority of expressions. The original estimate of ~80 remaining failures was significantly higher than reality.

### Confirmed Working (all pass in both modes)

| Category | Tests | Description |
|----------|-------|-------------|
| Constants | 11 | int, long, double, float, true/false, null, string, char, hex, octal |
| Integer arithmetic | 10 | negation, add, subtract, multiply, divide, modulus, precedence |
| Double arithmetic | 6 | negation, add, multiply, divide |
| Bitwise operations | 6 | not, shifts, xor, or |
| Logical expressions | 12 | not, comparisons, equality, ternary, short-circuit |
| Property access | 21 | simple, nested, array, map (dot/bracket/concat), boolean, ternary, string concat |
| Setter paths | 8 | map set, list index, property, string, special index ($) |
| Setter with conversion | 9 | int↔double, int↔string, string↔float |
| Index access | 16 | variable, object, method, generic, self, boolean array, tab search, 2D |
| Dynamic subscripts | 4 | ^, $, \| on maps and arrays |
| Complex expressions | 10 | #this, #root, sub-expressions, nested ternaries, list literals |
| Array elements | 7 | char array, list literals, set with int/string, root array access |
| Method calls (Root) | 10 | no-arg, string-arg, static, format with conversion, nested |
| Method calls (Simple) | 23 | hashCode, boolean ternary, varargs, enum, messages.format variants, testMethods |
| Interface inheritance | 19 | myMap access, BeanProvider, custom list, null keys, bracket access |
| Primitive null handling | 7 | set null on int/float/boolean, verify defaults |
| Indexed properties | 10 | getValues, indexed access, ^/\|/$, getTitle, source.total |
| Generics | 2 | service.getFullMessageFor, ids set/get |
| IndexedSetObject | 1 | thing["x"].val set/get |

### Known Compiler Limitations (13 `@Disabled` tests)

| # | Category | Tests | Root Cause | Fixable? |
|---|----------|-------|------------|----------|
| 1 | BigDecimal arithmetic | 8 | Java operators can't apply to BigDecimal in generated source | Hard (redesign) |
| 2 | BigInteger arithmetic | — | (covered by BigDecimal disabled class) | Hard (redesign) |
| 3 | instanceof expressions | 2 | Compiler generates invalid source: 'missing member name' | Medium |
| 4 | Float subtraction | 1 | Compiler widens float to double in arithmetic | Low priority |
| 5 | ~~String escaping in concat~~ | ~~1~~ | ~~Fixed: replaced `"` → `'` substitution with proper `\"` escaping in ASTAdd~~ | **Fixed** |
| 6 | Side-effect methods | 1 | Compiler evaluates expression during type inference, double-calling side-effect methods like EvenOdd.getNext() | Hard (architectural) |

### What Happened to the ~80 Failures?

The original issue #18 reported 85 failures. Investigation shows:
1. **PR #555 fixed** 3 bugs (ASTConst Long/Float, ASTOr boxing) — these unlocked many expressions
2. **Most categories work correctly** — comprehensive testing of 212 expressions confirms parity
3. **The remaining real failures** are limited to the 12 disabled tests above (string escaping fixed)
4. **The original count** likely included cascading failures where one bug caused multiple test failures

## Remaining Work

### ~~PR: String Escaping Fix (category 5)~~ — DONE
**Fixed in PR #TBD.** Root cause: `ASTAdd.toGetSourceString()` lines 208-214 replaced `"` with `'` in string constants during compiled concatenation. Fix: use proper Java string escaping (`\"`) instead of single-quote substitution, and preserve `&quot;` as literal text.

### PR: instanceof Support (category 3)
**Difficulty:** Medium
**Files to investigate:**
- `ognl/src/main/java/ognl/ASTInstanceof.java` — `toGetSourceString()`

### Deferred: BigDecimal/BigInteger (categories 1-2 in old plan)
**Difficulty:** Hard (significant redesign)
- Requires compiler to generate `OgnlOps` helper calls instead of Java operators
- ~8 `@Disabled` tests covering this limitation
- Should be a separate initiative

### Deferred: Side-effect methods during compilation (category 6)
**Difficulty:** Hard (architectural)
- The compiler evaluates expressions during `toGetSourceString()` for type inference
- Methods with side effects (like `EvenOdd.getNext()`) are called during compilation AND at runtime
- Would require separating type inference from expression evaluation

## Key Files Reference

### Compiler infrastructure
- `ognl/src/main/java/ognl/enhance/ExpressionCompiler.java` — core compiler
- `ognl/src/main/java/ognl/OgnlRuntime.java` — `getChildSource()`, `isBoolean()`

### AST nodes (source generation)
- `ognl/src/main/java/ognl/ASTProperty.java` — property get/set
- `ognl/src/main/java/ognl/ASTMethod.java` — method calls
- `ognl/src/main/java/ognl/ASTChain.java` — expression chains
- `ognl/src/main/java/ognl/ASTConst.java` — constants (string escaping bug here)
- `ognl/src/main/java/ognl/ASTAdd.java` — string concatenation
- `ognl/src/main/java/ognl/ASTInstanceof.java` — instanceof (broken)

### Test infrastructure
- `ognl/src/test/java/ognl/test/DualModeEvaluationTest.java` — 225 dual-mode tests

## Verification

```bash
./mvnw test -pl ognl                                   # Full suite — 951 tests, must stay green
./mvnw test -pl ognl -Dtest=DualModeEvaluationTest     # Dual-mode tests — 225 tests (212 active + 13 disabled)
```