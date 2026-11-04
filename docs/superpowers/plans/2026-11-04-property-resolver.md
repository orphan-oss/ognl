# Pluggable Property Resolver Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** One installable `PropertyResolver` that every property-name route in OGNL goes through, and that an embedding application (Struts) can query, on main and on `ognl-3-4-x`.

**Architecture:** The resolver turns (class, property name, access kind) into an ordered list of `PropertyCandidate`s, each a member plus its step. `DefaultPropertyResolver` builds today's lists from the existing raw lookups (`getGetMethod`, `getSetMethod`, `getReadMethod`, `getWriteMethod`, `getField`, property descriptors), which stay unchanged. Evaluation (`ObjectPropertyAccessor` get/set, the per-step `OgnlRuntime` methods, `has…`, indexed access and, on main, the compiled-mode lookups) consults the installed resolver's cached candidates. The same predicate that drives the walk backs a public query helper.

**Tech Stack:** Java 17+ on main (records, pattern `instanceof` already in use) / Java 8 on 3.4.x; JUnit 5; Maven (`./mvnw`); JMH benchmarks in `benchmarks/`.

**Spec:** `docs/superpowers/specs/2026-11-04-property-resolver-design.md`

## Global Constraints

- The default resolver gives exactly today's resolution on each line. Every existing test passes unchanged, and the Task 1 characterization tests pass before and after each task.
- No property-name route inside OGNL resolves members without the installed resolver (main). On 3.4.x, compiled-only code paths are excluded and their gaps documented.
- `MemberAccess` stays the only access-control layer. The resolver narrows candidates and never grants access.
- Existing public `OgnlRuntime` signatures are unchanged.
- Installation is global and static: `OgnlRuntime.setPropertyResolver(PropertyResolver)` (null resets to the default) and `OgnlRuntime.getPropertyResolver()`. Install swaps a `volatile` field first and clears the candidate cache second. It never throws because resolution has already happened.
- Javadoc on `setPropertyResolver` states: global per OGNL classloader; applications sharing one OGNL jar share the resolver; install before the first evaluation; a late install leaks (expressions compiled earlier keep their members, and an in-flight lookup can write an old result back after the clear).
- A resolver is a pure function of (class, name, kind), because results are cached.
- Out of scope: `ASTMethod` method calls, `ASTStaticField`, fixing the known quirks (`getWriteMethod`'s final `getReadMethod(target, "set" + name, …)` call, `"` stripping, bridge-method callability).
- 3.4.x: Java 8 only (no records, `List.of`, `var`, pattern `instanceof`), tested on Temurin 8, branch name suffixed `-3.4.x`, ported with `git format-patch | git am -3 -p2 --include='src/*'`.
- Every PR: `/security-review` before `gh pr create` (on 3.4.x, rescope it to `origin/ognl-3-4-x...HEAD`); the SonarCloud gate must pass on main; new-code coverage >80%.
- Commits: conventional (`feat(runtime): …`), with the `Refs #755` and `Co-Authored-By` footers.

## Decisions the plan makes (beyond the spec)

These are planner calls, not Łukasz's. Each is flagged so a reviewer can push back.

1. **Raw lookups stay raw.** `getGetMethod`, `getSetMethod`, `getReadMethod`, `getWriteMethod`, `getField`, `getDeclaredMethods`, `getPropertyDescriptor(s)` and `isFieldAccessible(…, Class, …)` keep returning exactly what they return today, and they ignore an installed resolver. They are the default resolver's building blocks, and making them resolver-aware would recurse. Everything that *evaluates* or *answers `has…`* becomes resolver-aware: `getMethodValue`, `setMethodValue`, `getFieldValue`, `setFieldValue`, `hasGetMethod`, `hasSetMethod`, `hasField`, `hasGetProperty`, `hasSetProperty`, `getIndexedPropertyType`, `getIndexedProperty`, `setIndexedProperty`, and the `ObjectPropertyAccessor` get/set walk. Javadoc on each raw lookup says it ignores the installed resolver.
2. **Only the candidate cache is resolver-derived.** Installing a resolver clears that cache alone. `clearCache()` and `clearAdditionalCache()` clear it too, because default candidates are built from the raw caches.
3. **Indexed calls:** when the resolver's indexed candidates are exactly the overload set `callMethod` would search, OGNL keeps calling `callMethod` (registered `MethodAccessor`s still apply, and the default stays exact). When the list is narrowed, OGNL calls `callAppropriateMethod` over the candidates only.
4. **`ASTProperty` resolves both classes:** descriptor detection stays on `getInterfaceClass(source.getClass())` as today, while the resolver is asked about `source.getClass()`, the class `get/setIndexedProperty` use. One access therefore asks the resolver about one class.
5. **Compiled mode (main):** a member the resolver didn't offer raises `UnsupportedCompilationException`, so the expression falls back to interpreted evaluation (`ExpressionCompiler:417–435`) and then follows the resolver. "Offered" means the candidate list contains a method with the same name and parameter types (a field: same name), so interface-vs-implementation copies of one method match.
6. **A resolver returning `null`, or a list containing `null`,** is a programming error: `NullPointerException` naming the resolver class, class, name and kind.
7. **Release notes:** `docs/VersionNotes.md` stopped at 3.4.3, so notes go in the PR bodies and the GitHub release. No VersionNotes edit.

## File Structure

| File | Responsibility |
|---|---|
| `ognl/src/main/java/ognl/PropertyResolver.java` (new) | public SPI interface |
| `ognl/src/main/java/ognl/PropertyAccessKind.java` (new) | `READ, WRITE, INDEXED_READ, INDEXED_WRITE` |
| `ognl/src/main/java/ognl/PropertyCandidate.java` (new) | record `(Member member, Step step)` + nested `enum Step { ACCESSOR, FIELD, LENIENT, INDEXED }` |
| `ognl/src/main/java/ognl/DefaultPropertyResolver.java` (new) | today's lists from raw lookups |
| `ognl/src/main/java/ognl/PropertyResolution.java` (new, package-private, final) | installed resolver, candidate cache, the shared `isUsable` predicate, `isOffered` |
| `ognl/src/main/java/ognl/OgnlRuntime.java` | install API, cache clearing, resolver-aware evaluation / `has…` / indexed, query helper |
| `ognl/src/main/java/ognl/ObjectPropertyAccessor.java` | candidate walk in `getPossibleProperty` / `setPossibleProperty`; compiled guards in `getSourceAccessor`, `getSourceSetter`, `getPropertyClass` |
| `ognl/src/main/java/ognl/ListPropertyAccessor.java` | compiled guards (`:168`, `:210`) |
| `ognl/src/main/java/ognl/ASTProperty.java` | indexed type check against the resolver; compiled guards in `toGetSourceString` / `toSetSourceString` |
| `ognl/src/test/java/ognl/PropertyResolutionCharacterizationTest.java` (new) | today's observable behaviour, written first |
| `ognl/src/test/java/ognl/PropertyResolutionFixtures.java` (new) | shared beans and a denying `MemberAccess` |
| `ognl/src/test/java/ognl/DefaultPropertyResolverTest.java` (new) | default lists equal the raw lookups |
| `ognl/src/test/java/ognl/PropertyResolverInstallTest.java` (new) | install, reset, cache clear, null handling |
| `ognl/src/test/java/ognl/NarrowingPropertyResolverTest.java` (new) | no hole per route; order honoured; query helper |

Tests that install a resolver must reset it in `@AfterEach` (`OgnlRuntime.setPropertyResolver(null)`) and must not run in parallel with other tests. Check `junit-platform.properties` for parallel execution, and if it is enabled, annotate those classes with `@Isolated`.

## Review Focus

1. **Hot-path cost.** Every property read and write now does a candidate-cache lookup. If the key allocates per access (a record key), interpreted property chains can slow down measurably. Expected: no regression outside error in a same-machine A/B (Task 7).
2. **The same `Method` appearing twice in a WRITE list** (a refused setter that is also the lenient write method). Expected: denied twice and `NoSuchPropertyException`, as today, with no double invocation when allowed. Pinned in Task 3.
3. **A resolver that reorders steps** (FIELD before ACCESSOR on write). Expected: OGNL honours the order and writes the field. Pinned in Task 3.
4. **`ignoreReadMethods` contexts under a resolver that offers only LENIENT.** Expected: not found, the same as today for a lenient-only property. Pinned in Task 3.
5. **Indexed overload narrowing with a boxed index** (`foo[0]` where the resolver leaves out `getFoo(int)` but keeps `getFoo(String)`). Expected: `getFoo(int)` is unreachable, and OGNL calls `getFoo(String)` only if conversion allows it, otherwise it fails. It never silently calls `getFoo(int)`. Pinned in Task 4.

---

### Task 1: Characterization tests (no production change)

**Files:**
- Create: `ognl/src/test/java/ognl/PropertyResolutionFixtures.java`
- Create: `ognl/src/test/java/ognl/PropertyResolutionCharacterizationTest.java`

**Interfaces:**
- Produces (fixtures, all `public static` nested in `PropertyResolutionFixtures`, used by Tasks 2–5):
  - `LombokBean` — `private String uRange` with `getURange()` / `setURange(String)` (naive capitalisation)
  - `SpecBean` — `getuRange()` / `setuRange(String)`
  - `LenientBean` — `public boolean hasChildren()`, `public String label()`, no getters
  - `FieldBean` — `public String name` (no accessors), `public final String fixed = "f"`, `public static String shared = "s"`
  - `SubFieldBean extends FieldBean` — empty
  - `BothBean` — `public String value` field and `getValue()` / `setValue(String)` that prefix `"m:"`, so method vs field is observable
  - `ConvBean` — `public int count`, `public Object lastLenient`, `public void count(Object o) { lastLenient = o; }`
  - `IndexedBean` — `String[] items` with `getItems()`, `getItems(int)`, `setItems(int, String)`, `getItems(String)` (overload by key type), `Map<String,String> attrs` with `getAttr(String)` / `setAttr(String, String)`
  - `Point` — `record Point(int x, int y)` (main only)
  - `DenyingMemberAccess<C>` extends test `DefaultMemberAccess<C>`, with `DenyingMemberAccess(String... deniedMemberNames)`. `isAccessible` returns false for a member whose `getName()` is in the set, and otherwise `super.isAccessible`.
  - `static <C extends OgnlContext<C>> C context(Object root, String... deniedMemberNames)`, which is `Ognl.createDefaultContext(root, new DenyingMemberAccess<>(denied))`.

- [ ] **Step 1: Write the characterization tests.** Each test evaluates through `Ognl.getValue` / `Ognl.setValue` with `context(...)`. They assert what the code at `b7d3016b` does. When a test's expectation is unknown here, write the expectation the comment states, run the test, and if it fails, record the observed behaviour and note it in the commit message, because these tests pin today's behaviour, not the plan's guess.

```java
@Test void lombokNameReadsAndWrites()                // "uRange" on LombokBean: setValue "a" then getValue == "a"
@Test void specNameReadsAndWrites()                  // "uRange" on SpecBean
@Test void lenientHasReads()                         // "children" on LenientBean == false
@Test void lenientPlainNameReads()                   // "label" on LenientBean == label()
@Test void ignoreReadMethodsHidesLenientRead()       // context.setIgnoreReadMethods(true); "label" throws NoSuchPropertyException
@Test void publicFieldReadsAndWrites()               // "name" on FieldBean
@Test void superclassFieldReads()                    // "name" on SubFieldBean
@Test void finalFieldWriteFails()                    // setValue "fixed" throws NoSuchPropertyException; value stays "f"
@Test void staticFieldReadThrowsPlainOgnlException() // getValue "shared": OgnlException whose class is recorded exactly (assertEquals(<observed>.class, ex.getClass()))
@Test void methodWinsOverField()                     // BothBean "value" set "x" -> field == "m:x"; get == "m:m:x" or as observed
@Test void deniedSetterFallsBackToField()            // context(bean, "setValue"); set "x" -> bean.value == "x"
@Test void deniedGetterGoesToFieldNotLenient()       // context(bean, "getValue"); get returns field value
@Test void fieldConversionFailureFallsBackToLenient() // ConvBean set "count" = new Object(): lastLenient == that object, or the observed exception
@Test void indexedIntReadAndWrite()                  // "items[1]" read; "items[1]" = "z" writes via setItems(int,String)
@Test void indexedOverloadByIndexType()              // "items['k']" reaches getItems(String) (or as observed)
@Test void objectIndexedReadAndWrite()               // "attr['k']" set "v" then get == "v"
@Test void recordComponentReads()                    // main only: "x" on new Point(1,2) == 1
@Test void hasGetAndSetPropertyToday()               // OgnlRuntime.hasGetProperty/hasSetProperty for: getter-only true; LenientBean "label" false; FieldBean "name" true/true; "shared" false/false; "fixed" true/false; Point "x" true/false
@Test void compiledReadMatchesInterpreted()          // Ognl.compileExpression(ctx, bean, "uRange") then getValue == interpreted value; same for "name", "items[1]"
```

- [ ] **Step 2: Run them against unchanged code**

Run: `./mvnw test -pl ognl -Dtest=PropertyResolutionCharacterizationTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: all PASS, after recording any observed-behaviour adjustments.

- [ ] **Step 3: Commit**

```bash
git add ognl/src/test/java/ognl/PropertyResolution*.java
git commit -m "test(runtime): pin today's property-name resolution before #755"
```

---

### Task 2: Resolver API, default resolver, installation

**Files:**
- Create: `PropertyResolver.java`, `PropertyAccessKind.java`, `PropertyCandidate.java`, `DefaultPropertyResolver.java`, `PropertyResolution.java` (all `ognl/src/main/java/ognl/`)
- Modify: `ognl/src/main/java/ognl/OgnlRuntime.java` (`clearCache()` `:399`, `clearAdditionalCache()` `:418`, new install methods)
- Test: `ognl/src/test/java/ognl/DefaultPropertyResolverTest.java`, `ognl/src/test/java/ognl/PropertyResolverInstallTest.java`

**Interfaces:**
- Produces:
  - `public interface PropertyResolver { List<PropertyCandidate> candidates(Class<?> targetClass, String propertyName, PropertyAccessKind kind); }`
  - `public enum PropertyAccessKind { READ, WRITE, INDEXED_READ, INDEXED_WRITE }`
  - `public record PropertyCandidate(Member member, Step step)`, whose compact constructor requires non-null arguments and `step == FIELD` ⇔ `member instanceof Field` (else `IllegalArgumentException`). Factories: `accessor(Method)`, `field(Field)`, `lenient(Method)`, `indexed(Method)`.
  - `public class DefaultPropertyResolver implements PropertyResolver` with `public static final DefaultPropertyResolver INSTANCE`; public constructor (subclassable).
  - `OgnlRuntime.setPropertyResolver(PropertyResolver)`, `OgnlRuntime.getPropertyResolver()`
  - package-private `PropertyResolution.candidates(Class<?>, String, PropertyAccessKind) -> List<PropertyCandidate>` (cached, unmodifiable), `PropertyResolution.clear()`

Default lists (exact order):
- `READ`: `accessor(getGetMethod(c, n))` if non-null, **otherwise** `lenient(getReadMethod(c, n))` if non-null; then `field(getField(c, n))` if non-null.
- `WRITE`: `accessor(getSetMethod(null, c, n))` if non-null; then `field(getField(c, n))` if non-null; then `lenient(getWriteMethod(c, n))` if non-null. Duplicates stay.
- `INDEXED_READ` / `INDEXED_WRITE`: `pd = getPropertyDescriptor(c, n)`. If it is an `IndexedPropertyDescriptor` or `ObjectIndexedPropertyDescriptor` with a non-null indexed read (write) method `m`, return `indexed(x)` for each `x` in `getMethods(c, m.getName(), false)`, in that list's order. Otherwise return an empty list.
- `c == null`: call the raw lookups exactly as today's callers would. Do not add a special case.

- [ ] **Step 1: Write the failing tests**

```java
// DefaultPropertyResolverTest
@Test void readPrefersAccessorAndOmitsLenient()     // BothBean READ == [accessor(getValue), field(value)]
@Test void readFallsBackToLenientWithoutGetter()    // LenientBean "label" READ == [lenient(label())]
@Test void writeListsAccessorFieldLenient()         // BothBean WRITE == [accessor(setValue), field(value), lenient(getWriteMethod(BothBean,"value"))]
@Test void lombokBaseNames()                        // LombokBean "uRange" READ.get(0).member() == getURange
@Test void indexedReadListsAllOverloads()           // IndexedBean "items" INDEXED_READ members == getMethods(IndexedBean,"getItems",false)
@Test void objectIndexedWrite()                     // IndexedBean "attr" INDEXED_WRITE members == [setAttr]
@Test void unknownPropertyIsEmpty()                 // all four kinds empty for "nope"
@Test void candidateRejectsMismatchedStep()         // new PropertyCandidate(someField, ACCESSOR) throws IllegalArgumentException
// PropertyResolverInstallTest
@Test void defaultIsInstalled()                     // getPropertyResolver() == DefaultPropertyResolver.INSTANCE
@Test void installAndReset()                        // set custom -> get returns it; set(null) -> INSTANCE
@Test void installClearsCandidateCache()            // query READ under default, install resolver returning [], query again -> []
@Test void clearCacheClearsCandidates()             // counting resolver: call twice -> 1 invocation; clearCache(); call -> 2
@Test void nullResultIsRejected()                   // resolver returning null -> NullPointerException mentioning the property name
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -pl ognl -Dtest='DefaultPropertyResolverTest,PropertyResolverInstallTest' -Dsurefire.failIfNoSpecifiedTests=false`
Expected: compilation failure (types don't exist).

- [ ] **Step 3: Implement the types, `DefaultPropertyResolver`, `PropertyResolution` and the install methods.** The cache is a `ConcurrentHashMap` keyed by (class, name, kind). The key must tolerate a null class, and its design should avoid allocating on the hot path where practical (Review Focus 1). For example, use a `ClassValue`-backed per-class map of `name → EnumMap<kind, list>`, with a separate slot for the null class. `setPropertyResolver` writes the volatile field, then calls `PropertyResolution.clear()`. `clearCache()` and `clearAdditionalCache()` also call `PropertyResolution.clear()`. Javadoc text per Global Constraints, plus the raw-lookup javadoc from Decision 1.

- [ ] **Step 4: Run tests and the characterization suite**

Run: `./mvnw test -pl ognl -Dtest='DefaultPropertyResolverTest,PropertyResolverInstallTest,PropertyResolutionCharacterizationTest' -Dsurefire.failIfNoSpecifiedTests=false`
Expected: PASS.

- [ ] **Step 5: Commit** — `feat(runtime): add PropertyResolver SPI and the default resolver`

---

### Task 3: Interpreted read/write and `has…` go through the resolver; query helper

**Files:**
- Modify: `OgnlRuntime.java` — `getMethodValue` `:1678`, `setMethodValue` `:1714`, `getFieldValue` `:1813`, `setFieldValue` `:1853`, `hasField` `:1890`, `hasSettableField` `:1901`, `hasReadableField` `:1916`, `hasRecordComponentAccessor` `:1923`, `hasGetMethod` `:2154`, `hasSetMethod` `:2236`
- Modify: `ObjectPropertyAccessor.java` — `getPossibleProperty` `:43`, `setPossibleProperty` `:69`
- Modify: `PropertyResolution.java` — add `isUsable`
- Test: `ognl/src/test/java/ognl/NarrowingPropertyResolverTest.java`

**Interfaces:**
- Consumes: `PropertyResolution.candidates`, `PropertyCandidate`, `PropertyAccessKind` (Task 2)
- Produces:
  - `static <C extends OgnlContext<C>> boolean PropertyResolution.isUsable(C context, Object target, String name, PropertyCandidate c, PropertyAccessKind kind)`: method steps → `isMethodAccessible`, with LENIENT on READ also requiring `!context.isIgnoreReadMethods()`; FIELD on READ → accessible; FIELD on WRITE → non-static, non-final and accessible.
  - `public static <C extends OgnlContext<C>> Optional<PropertyCandidate> OgnlRuntime.firstUsableCandidate(C context, Object target, String propertyName, PropertyAccessKind kind)`: the first candidate (for `target.getClass()`) for which `isUsable` holds. Javadoc: this is what evaluation will use, except that a FIELD write whose value fails conversion moves on to the next candidate, and an indexed overload is chosen by runtime argument types.

Walk semantics (list order, not step order):
- READ (`getPossibleProperty`): for each usable candidate, a method is invoked with no arguments (`InvocationTargetException` → `new OgnlException(name, targetException)`, as today) and its result returned. A field that is static throws `NoSuchFieldException(name)`, wrapped by the existing catch as today. Otherwise the field is read under `MemberAccess.setup/restore`. If no candidate is usable, return `NotFound`.
- WRITE (`setPossibleProperty`): ACCESSOR → `callAppropriateMethod(context, target, target, m.getName(), name, List.of(m), new Object[]{value})`, return `null`. FIELD → today's `isTypeCompatible` / `getConvertedType` logic under `setup/restore`; on success return `null`; on conversion failure continue. LENIENT → `m.invoke(target, value)`, return its result. If no candidate is usable, return `NotFound`.
- The per-step public methods select their step from the same list: `getMethodValue` takes the first ACCESSOR, or else LENIENT unless `isIgnoreReadMethods`; `setMethodValue` takes the first ACCESSOR on WRITE; `getFieldValue` / `setFieldValue` take the FIELD on READ / WRITE. When `checkAccessAndExistence` is true they keep today's accessibility handling; when it is false they skip it. Share the invoke and field code between the walk and these methods. Don't duplicate it.
- `has…`: `hasGetMethod` → a usable ACCESSOR on READ; `hasSetMethod` → a usable ACCESSOR on WRITE; `hasField` / `hasReadableField` → the FIELD on READ that is accessible (and, for readable, non-static); `hasSettableField` → the FIELD on WRITE that is usable; `hasRecordComponentAccessor` → today's check, plus the component accessor must equal a method candidate on READ.

- [ ] **Step 1: Write the failing tests.** Each test installs a narrowing resolver built as `(c, n, k) -> DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream().filter(<predicate>).toList()` and resets it in `@AfterEach`.

```java
@Test void droppingAccessorReadFallsToField()        // BothBean, drop ACCESSOR on READ: get "value" == field value
@Test void droppingFieldMakesFieldOnlyPropertyMissing() // FieldBean, drop FIELD: get "name" throws NoSuchPropertyException; set throws NoSuchPropertyException
@Test void droppingLenientHidesLenientRead()         // LenientBean, drop LENIENT: get "label" throws NoSuchPropertyException
@Test void exactSetterOnlyStopsFieldAndLenient()     // BothBean, WRITE keeps only ACCESSOR, context denies "setValue": set throws NoSuchPropertyException; bean.value unchanged
@Test void orderIsHonoured()                         // BothBean, WRITE reordered [FIELD, ACCESSOR, LENIENT]: set "x" -> bean.value == "x" (not "m:x")
@Test void duplicateMemberInvokedOnce()              // BothBean default, set "x": setValue invoked once (counter field in BothBean)
@Test void ignoreReadMethodsWithLenientOnlyList()    // LenientBean, setIgnoreReadMethods(true): get "label" throws NoSuchPropertyException
@Test void hasPropertiesFollowNarrowing()            // FieldBean drop FIELD: hasGetProperty/hasSetProperty "name" false; BothBean drop ACCESSOR on WRITE: hasSetMethod false, hasSetProperty true (field)
@Test void recordAccessorNeedsCandidate()            // main: Point, drop LENIENT: get "x" throws NoSuchPropertyException, hasGetProperty false
@Test void firstUsableCandidateMatchesEvaluation()   // BothBean context denying "setValue": firstUsableCandidate(WRITE) == field(value); after set, bean.value == "x"
@Test void firstUsableCandidateEmptyWhenNothingUsable() // FieldBean "fixed" WRITE -> Optional.empty()
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -pl ognl -Dtest=NarrowingPropertyResolverTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: FAIL (narrowing has no effect yet; `firstUsableCandidate` doesn't compile).

- [ ] **Step 3: Implement the walk, the per-step selection, `has…`, `isUsable` and `firstUsableCandidate`** as specified above.

- [ ] **Step 4: Run the narrowing, characterization and full suites**

Run: `./mvnw test -pl ognl`
Expected: PASS, with no change to existing tests.

- [ ] **Step 5: Commit** — `feat(runtime): resolve interpreted property access through the PropertyResolver`

---

### Task 4: Indexed route

**Files:**
- Modify: `OgnlRuntime.java` — `getIndexedPropertyType(Class, String)` `~:2386`, `getIndexedProperty` `:2406`, `setIndexedProperty` `:2433`
- Modify: `ASTProperty.java:66–85` (`getIndexedPropertyType`)
- Test: add to `NarrowingPropertyResolverTest.java`

**Interfaces:**
- Consumes: `PropertyResolution.candidates` (Task 2)
- Produces: package-private `static int OgnlRuntime.getIndexedPropertyType(Class<?> descriptorClass, Class<?> targetClass, String name)`. It does today's descriptor detection on `descriptorClass` and returns `INDEXED_PROPERTY_NONE` when the resolver returns no `INDEXED_READ` and no `INDEXED_WRITE` candidates for `targetClass`. The public `getIndexedPropertyType(Class c, String n)` delegates as `(c, c, n)`. `ASTProperty` passes `(getInterfaceClass(source.getClass()), source.getClass(), property)`.

`getIndexedProperty` / `setIndexedProperty`: keep the descriptor lookup and today's "is not an indexed property" exception. Then take the `INDEXED_READ` / `INDEXED_WRITE` candidates for `source.getClass()`. If they are empty, throw the same exception. If their methods equal (same elements, any order) `getMethods(source.getClass(), m.getName(), false)`, call `callMethod` as today. Otherwise call `callAppropriateMethod(context, source, source, m.getName(), null, <candidate methods>, args)`.

- [ ] **Step 1: Write the failing tests**

```java
@Test void droppingIndexedMakesPropertyNotIndexed()  // drop all INDEXED for "items": "items[1]" no longer calls getItems(int) (assert per observed: reads getItems()[1] via array, or throws), and getIndexedPropertyType(IndexedBean.class,"items") == INDEXED_PROPERTY_NONE
@Test void narrowedOverloadIsUnreachable()           // keep only getItems(String): "items[1]" never invokes getItems(int) (invocation counter on IndexedBean stays 0)
@Test void narrowedIndexedWrite()                    // drop setAttr from INDEXED_WRITE: "attr['k']" = "v" throws OgnlException; attrs unchanged
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -pl ognl -Dtest=NarrowingPropertyResolverTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: the three new tests FAIL.

- [ ] **Step 3: Implement** as specified.

- [ ] **Step 4: Run the full module suite**

Run: `./mvnw test -pl ognl`
Expected: PASS.

- [ ] **Step 5: Commit** — `feat(runtime): resolve indexed property access through the PropertyResolver`

---

### Task 5: Compiled-mode guards (main only)

**Files:**
- Modify: `PropertyResolution.java` — add `isOffered`
- Modify: `ASTProperty.java` — `toGetSourceString` (`:226–280`), `toSetSourceString` (`:440–490`)
- Modify: `ObjectPropertyAccessor.java` — `getPropertyClass` `:128`, `getSourceAccessor` `:149`, `getSourceSetter` `:194`
- Modify: `ListPropertyAccessor.java:168`, `:210`
- Test: add to `NarrowingPropertyResolverTest.java`

**Interfaces:**
- Produces: package-private `static boolean PropertyResolution.isOffered(Class<?> targetClass, String name, PropertyAccessKind kind, Member member)`. For a `Method`: some candidate method has the same name and the same parameter types. For a `Field`: some FIELD candidate has the same name.

Rule for every listed site: each `Method` or `Field` that the generated source will reference (including `pd.getReadMethod()`, `pd.getWriteMethod()`, the indexed `m` and the `getReadMethod` / `getWriteMethod` / `getField` results) must pass `isOffered` for its kind (`READ`, `WRITE`, `INDEXED_READ`, `INDEXED_WRITE`) on the class whose member it is looked up from. Otherwise:
- in `ASTProperty`, throw `UnsupportedCompilationException("Property " + name + " is not offered by the PropertyResolver on " + cls.getName())`;
- in the accessor helpers, treat the member as not found (`null`), so their existing not-found branches apply.

- [ ] **Step 1: Write the failing tests**

```java
@Test void compiledReadFollowsNarrowing()   // BothBean, drop ACCESSOR on READ: Ognl.compileExpression(ctx, bean, "value") getValue == field value (equals interpreted)
@Test void compiledWriteFollowsNarrowing()  // BothBean, WRITE = [FIELD]: compiled setValue "x" -> bean.value == "x"
@Test void compiledIndexedFollowsNarrowing()// keep only getItems(String): compiled "items[1]" never invokes getItems(int)
@Test void defaultCompiledUnchanged()       // compiledReadMatchesInterpreted from Task 1 still passes (rerun)
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw test -pl ognl -Dtest=NarrowingPropertyResolverTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: the compiled tests FAIL.

- [ ] **Step 3: Implement** the guard and apply it at each site.

- [ ] **Step 4: Run the full module suite** (this includes the legacy compiled-mode suites under `ognl/src/test/java/ognl/test`)

Run: `./mvnw test -pl ognl`
Expected: PASS.

- [ ] **Step 5: Commit** — `feat(compiler): honour the PropertyResolver in compiled property access`

---

### Task 6: Javadoc pass and coverage

**Files:**
- Modify: `PropertyResolver.java`, `PropertyCandidate.java`, `DefaultPropertyResolver.java`, `OgnlRuntime.java` (install methods, `firstUsableCandidate`, raw lookups per Decision 1)

- [ ] **Step 1: Check that the javadoc carries the spec's text:**
  - global per classloader, shared-jar caveat, install before the first evaluation, both late-install leaks;
  - purity and caching;
  - `MemberAccess` still decides;
  - the two unpredictable cases (field-conversion fallback, indexed overload by runtime type);
  - raw lookups ignore the installed resolver.

  Keep each to the shortest form that carries it, matching the surrounding style.
- [ ] **Step 2: Run coverage.** Run `./mvnw verify -pl ognl` and check the JaCoCo report (`ognl/target/site/jacoco/index.html`) for the new classes and changed methods. Expected: >80% line coverage on the new code. Add tests for any uncovered branch in `PropertyResolution`.
- [ ] **Step 3: Commit** — `docs(runtime): document PropertyResolver installation and limits`

---

### Task 7: Performance A/B (main)

- [ ] **Step 1: Build both commits in scratch worktrees.** Use `b7d3016b`, or the branch's merge base, and the branch HEAD. In each one run `cd benchmarks && ../mvnw -q clean install -DskipTests`.
- [ ] **Step 2: Run the property benchmarks on each**

Run: `java -cp target/benchmarks.jar org.openjdk.jmh.Main 'OgnlPropertyChainPerformanceBenchmarks|OgnlPerformanceBenchmarks' -f 2 -wi 3 -i 5 -bm avgt`
Expected: interpreted property chains equal within error. If they are not, revisit the cache key (Review Focus 1) before opening the PR.
- [ ] **Step 3:** Put the A/B table in the PR body. No commit.

---

### Task 8: Main PR

- [ ] **Step 0:** If #753 (cache ownership) has merged, rebase onto main and move the candidate cache's clearing under `OgnlCache`'s registration, as #753 does for the other caches. If it hasn't merged, leave a clean conflict for it.
- [ ] **Step 1:** Run `./mvnw test` from the repo root. Expected: BUILD SUCCESS.
- [ ] **Step 2:** Run `/security-review`, and read `SECURITY.md` ("Security model"). If there is a real finding, stop and do not open a PR.
- [ ] **Step 3:** Push `feat/755-property-resolver` and open the PR against `main`. The body follows `~/.claude/pr_guideline.md` and contains: `Fixes #755`, the decisions from the spec, the planner decisions above, the A/B table and the Struts usage sketch (`OgnlRuntime.firstUsableCandidate(Ognl.createDefaultContext(target, memberAccess), target, root, WRITE)`).
- [ ] **Step 4:** Check SonarCloud for new issues (`https://sonarcloud.io/project/issues?issueStatuses=OPEN%2CCONFIRMED&sinceLeakPeriod=true&pullRequest=<N>&id=orphan-oss_ognl`) and fix them in follow-up commits.

---

### Task 9: 3.4.x backport (after the main PR merges)

**Files:** the 3.4.x equivalents under `src/main/java/ognl/` and `src/test/java/ognl/`, in the separate 3.4.x checkout.

- [ ] **Step 1:** Fetch, then branch `feat/755-property-resolver-3.4.x` from `origin/ognl-3-4-x`. Port the Task 1–4 and Task 6 commits with `git format-patch <range> --stdout | git am -3 -p2 --include='src/*'`. **Skip Task 5:** it adds compiled-only guards, and 3.4.x compiled mode is frozen.
- [ ] **Step 2: Java 8 adaptation:**
  - `PropertyCandidate` becomes a final class with `member()`, `step()`, `equals`, `hashCode` and `toString`;
  - `List.of` / `toList()` become `Collections.unmodifiableList(new ArrayList<>(…))` / `Collectors.toList()`;
  - pattern `instanceof` becomes casts;
  - `OgnlContext` is non-generic (`OgnlContext context` parameters, no `<C>`);
  - drop `recordComponentReads`, `recordAccessorNeedsCandidate` and the `Point` fixture.
- [ ] **Step 3:** Find the compiled-mode sites that escape the resolver on 3.4.x (the Task 5 sites). List them in the `setPropertyResolver` javadoc and the PR body.
- [ ] **Step 4:** Run `./mvnw test` on Temurin 8. Expected: BUILD SUCCESS. 3.4.x legacy suites also run compiled mode, so a failure seen only on 3.4.x is likely a compiled path relying on a raw lookup. Fix it by keeping the raw lookup on that path, not by adding a compiled-only hook.
- [ ] **Step 5:** Run `/security-review` rescoped to `origin/ognl-3-4-x...HEAD`. Then open the PR against `ognl-3-4-x`, with `Refs #755` (3.4.x PRs don't auto-close) and the A/B repeated on 3.4.x if the cache code differs. Sonar compares this PR against main, so pre-existing complexity may fail the gate. File a follow-up instead of refactoring in the backport.
