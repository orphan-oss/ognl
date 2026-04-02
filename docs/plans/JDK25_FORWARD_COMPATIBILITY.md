# OGNL JDK 25 Forward-Compatibility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make OGNL forward-compatible with JDK 25 while keeping Java 17 as the minimum compilation and runtime target.

**Architecture:** Phased approach — first add JDK 25 to CI for visibility, then collapse the obsolete pre-JDK9/JDK9+ handler abstraction (Java 17 is now the minimum), deprecate SecurityManager-dependent class, verify Javassist compatibility, and document `--add-opens` requirements.

**Tech Stack:** Java 17+ (source/target), Maven, Javassist, JUnit Jupiter 6.x

---

## Context

JDK 25 (GA September 2025) introduces breaking changes affecting reflection-heavy libraries like OGNL:

- **JEP 486 (JDK 24):** SecurityManager permanently disabled — `BasicPermission` deprecated for removal
- **JEP 498 (JDK 23+):** `sun.misc.Unsafe` memory-access methods emit warnings, eventual removal
- **Stronger module encapsulation:** `setAccessible(true)` throws `InaccessibleObjectException` on strongly encapsulated members without `--add-opens`
- **Class file version 69:** Javassist must support JDK 25 bytecode format

OGNL is already partially prepared (module-aware `isLikelyAccessible()`, `canAccess()` usage, `--add-opens` in surefire). However, the `AccessibleObjectHandler` / `AccessibleObjectHandlerJDK9Plus` abstraction was designed for pre-JDK9 vs JDK9+ differentiation — **since OGNL now requires Java 17, this entire abstraction layer is obsolete**. It also contains dead `sun.misc.Unsafe` code that references APIs being removed.

---

### Task 1: Add JDK 25 to CI Matrix

**Files:**
- Modify: `.github/workflows/maven.yml:35`

- [ ] **Step 1: Update CI matrix**

```yaml
# Change line 35 from:
        java: [ '17', '21' ]
# To:
        java: [ '17', '21', '25' ]
```

- [ ] **Step 2: Run local build on JDK 25 to establish baseline**

Run: `./mvnw -B verify` with JDK 25
Expected: Capture any failures — this establishes the baseline for remaining tasks.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/maven.yml
git commit -m "ci: add JDK 25 to CI test matrix"
```

---

### Task 2: Collapse AccessibleObjectHandler Abstraction

**Files:**
- Modify: `ognl/src/main/java/ognl/AccessibleObjectHandler.java`
- Delete: `ognl/src/main/java/ognl/AccessibleObjectHandlerJDK9Plus.java`
- Modify: `ognl/src/main/java/ognl/OgnlRuntime.java:168-171, 654, 662`
- Modify: `ognl/src/test/java/ognl/DefaultMemberAccess.java:35-42`
- Test: `./mvnw test -pl ognl`

Since Java 17 is the minimum, the pre-JDK9 vs JDK9+ distinction is meaningless. `AccessibleObjectHandlerJDK9Plus` contains dead `sun.misc.Unsafe` code (the `instantiateClazzUnsafe()` method returns `null` at line 76, making the entire Unsafe path dead). After removing the Unsafe code, the handler just calls `accessibleObject.setAccessible(flag)` — a one-liner that doesn't warrant a separate class.

#### Approach

1. Give `AccessibleObjectHandler` interface a `default` method implementation (backward-compatible for external implementors)
2. Delete `AccessibleObjectHandlerJDK9Plus` entirely
3. Move the `unsafeOrDescendant()` security check to `OgnlRuntime` (it's used there for stricter invocation mode)
4. Simplify all initialization code

- [ ] **Step 1: Write tests verifying current accessible handler behavior**

Add to existing test class or new `AccessibleObjectHandlerTest.java`:

```java
@Test
void testAccessibleObjectHandlerDefaultSetAccessible() throws Exception {
    AccessibleObjectHandler handler = new AccessibleObjectHandler() {};  // Uses default method
    Method method = String.class.getMethod("length");
    handler.setAccessible(method, true);
    assertTrue(method.canAccess("test"));
}
```

- [ ] **Step 2: Run test to verify it fails (default method doesn't exist yet)**

Run: `./mvnw test -pl ognl -Dtest=AccessibleObjectHandlerTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: Compile error — `default` method not yet added

- [ ] **Step 3: Add default method to AccessibleObjectHandler interface**

Replace `ognl/src/main/java/ognl/AccessibleObjectHandler.java`:

```java
package ognl;

import java.lang.reflect.AccessibleObject;

/**
 * Provides a mechanism for changing the accessibility of AccessibleObject instances.
 *
 * @since 3.1.24
 */
public interface AccessibleObjectHandler {
    /**
     * Changes the accessibility of the given AccessibleObject.
     *
     * @param accessibleObject the AccessibleObject upon which to apply the flag.
     * @param flag             the new accessible flag value.
     */
    default void setAccessible(AccessibleObject accessibleObject, boolean flag) {
        accessibleObject.setAccessible(flag);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -pl ognl -Dtest=AccessibleObjectHandlerTest`
Expected: PASS

- [ ] **Step 5: Move unsafeOrDescendant() to OgnlRuntime**

In `ognl/src/main/java/ognl/OgnlRuntime.java`, add a private static method (near the other security checks around line 160):

```java
/**
 * Check if a class is sun.misc.Unsafe or jdk.internal.misc.Unsafe.
 * Used by stricter invocation mode to block OGNL expressions from invoking Unsafe methods.
 */
private static boolean isUnsafeClass(final Class<?> clazz) {
    String className = clazz.getName();
    return "sun.misc.Unsafe".equals(className) || "jdk.internal.misc.Unsafe".equals(className);
}
```

Update line 662 from:
```java
AccessibleObjectHandlerJDK9Plus.unsafeOrDescendant(methodDeclaringClass)) {
```
to:
```java
isUnsafeClass(methodDeclaringClass)) {
```

- [ ] **Step 6: Simplify OgnlRuntime handler initialization**

In `OgnlRuntime.java`, change lines 168-171 from:
```java
private static final AccessibleObjectHandler _accessibleObjectHandler;

static {
    _accessibleObjectHandler = AccessibleObjectHandlerJDK9Plus.createHandler();
}
```
to:
```java
private static final AccessibleObjectHandler _accessibleObjectHandler = new AccessibleObjectHandler() {};
```

- [ ] **Step 7: Simplify DefaultMemberAccess (test class)**

In `ognl/src/test/java/ognl/DefaultMemberAccess.java`, change lines 34-42 from:
```java
/*
 * Assign an accessibility modification mechanism, based on Major Java Version.
 *   Note: Can be overridden using a Java option flag {@link OgnlRuntime#USE_PREJDK9_ACESS_HANDLER}.
 */
private static final AccessibleObjectHandler _accessibleObjectHandler;

static {
    _accessibleObjectHandler = AccessibleObjectHandlerJDK9Plus.createHandler();
}
```
to:
```java
private static final AccessibleObjectHandler _accessibleObjectHandler = new AccessibleObjectHandler() {};
```

- [ ] **Step 8: Delete AccessibleObjectHandlerJDK9Plus.java**

```bash
git rm ognl/src/main/java/ognl/AccessibleObjectHandlerJDK9Plus.java
```

- [ ] **Step 9: Run full test suite**

Run: `./mvnw test -pl ognl`
Expected: All tests PASS

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "refactor: collapse AccessibleObjectHandler abstraction, remove dead Unsafe code

Java 17 is the minimum - the pre-JDK9 vs JDK9+ distinction is obsolete.
AccessibleObjectHandlerJDK9Plus contained dead sun.misc.Unsafe code
(instantiateClazzUnsafe() returned null, making entire Unsafe path dead).
The handler now uses a default interface method that delegates to
AccessibleObject.setAccessible() directly."
```

---

### Task 3: Deprecate OgnlInvokePermission

**Files:**
- Modify: `ognl/src/main/java/ognl/OgnlInvokePermission.java`

`OgnlInvokePermission` extends `java.security.BasicPermission`, which is deprecated for removal in JDK 24+ (JEP 486). This class is NOT used anywhere in the OGNL codebase — it exists only for external consumers using SecurityManager policies.

- [ ] **Step 1: Add deprecation annotation, suppress removal warning, update Javadoc**

```java
/**
 * BasicPermission subclass that defines a permission token for invoking
 * methods within OGNL.  This does not override any methods (except
 * constructors) and does not implement actions.  It is similar in spirit
 * to the {@link java.lang.reflect.ReflectPermission} class in that it
 * guards access to methods.
 *
 * @deprecated SecurityManager has been permanently disabled in JDK 24
 * (<a href="https://openjdk.org/jeps/486">JEP 486</a>) and
 * {@link java.security.BasicPermission} is deprecated for removal.
 * This class will be removed in a future OGNL release.
 */
@SuppressWarnings("removal")
@Deprecated(since = "3.5.0", forRemoval = true)
public class OgnlInvokePermission extends BasicPermission {
```

- [ ] **Step 2: Compile**

Run: `./mvnw compile -pl ognl`
Expected: Compiles cleanly (the `@SuppressWarnings("removal")` suppresses `BasicPermission` deprecation warnings on JDK 24+)

- [ ] **Step 3: Commit**

```bash
git add ognl/src/main/java/ognl/OgnlInvokePermission.java
git commit -m "deprecate: mark OgnlInvokePermission for removal (JEP 486)"
```

---

### Task 4: Fix detectMajorJavaVersion Fallback

**Files:**
- Modify: `ognl/src/main/java/ognl/OgnlRuntime.java:2702, 2713`
- Test: existing tests or new test

The `detectMajorJavaVersion()` Javadoc says fallback is 5 and the code returns 5 (line 2713), but the companion `parseMajorJavaVersion()` Javadoc says 17. Since Java 17 is the minimum, the fallback should be 17.

- [ ] **Step 1: Update fallback value and Javadoc**

Change line 2702:
```java
 * @return Detected Major Java Version, or 17 (minimum supported version for OGNL) if unable to detect.
```

Change line 2713:
```java
    majorVersion = 17;  // Return minimum supported Java version for OGNL
```

- [ ] **Step 2: Run tests**

Run: `./mvnw test -pl ognl`
Expected: All PASS

- [ ] **Step 3: Commit**

```bash
git add ognl/src/main/java/ognl/OgnlRuntime.java
git commit -m "fix: update detectMajorJavaVersion fallback from 5 to 17"
```

---

### Task 5: Verify and Update Javassist Dependency

**Files:**
- Modify (if needed): `pom.xml:20` (`javassist.version` property, currently `3.30.2-GA`)

Javassist 3.30.2-GA supports up to JDK 22 class file format. JDK 25 uses class file version 69.

- [ ] **Step 1: Check latest Javassist version**

Check Maven Central for latest `org.javassist:javassist` version.

- [ ] **Step 2: Run test suite on JDK 25**

Run (with JDK 25): `./mvnw test -pl ognl`
Look for: `javassist.CannotCompileException`, `UnsupportedClassVersionError`, or class format errors.

- [ ] **Step 3: Update Javassist if needed**

```xml
<javassist.version>3.31.0-GA</javassist.version>  <!-- or latest -->
```

- [ ] **Step 4: Verify on both JDK 17 and 25**

Run: `./mvnw test -pl ognl` on both JDK 17 and JDK 25
Expected: All PASS on both

- [ ] **Step 5: Commit if changed**

```bash
git add pom.xml
git commit -m "build: update Javassist to support JDK 25 class file format"
```

---

### Task 6: Audit --add-opens and Update Documentation

**Files:**
- Modify (if needed): `ognl/pom.xml:52-55` (surefire argLine)
- Modify: `CLAUDE.md`

- [ ] **Step 1: Run full test suite on JDK 25 and check for access errors**

Run (with JDK 25): `./mvnw test -pl ognl 2>&1 | grep -i "InaccessibleObjectException\|IllegalAccessError\|add-opens"`

Current `--add-opens` in surefire:
- `java.base/java.lang=ALL-UNNAMED`
- `java.base/java.util=ALL-UNNAMED`

Add any additional opens only if needed based on actual test failures.

- [ ] **Step 2: Update CLAUDE.md**

Change the language line:
```
- **Language**: Java 17 (CI also tests against Java 21 and 25)
```

- [ ] **Step 3: Run full test suite on JDK 17, 21, and 25**

Expected: All PASS on all three

- [ ] **Step 4: Commit**

```bash
git add ognl/pom.xml CLAUDE.md
git commit -m "build: audit --add-opens for JDK 25, update docs"
```

---

## Verification

After all tasks complete:

1. **JDK 17:** `./mvnw clean verify` — must pass (backward compat)
2. **JDK 21:** `./mvnw clean verify` — must pass (current CI)
3. **JDK 25:** `./mvnw clean verify` — must pass (forward compat)
4. **CI green** on all matrix entries
5. **No SonarCloud regressions** on the PR
6. **Compiled expressions work on JDK 25** — tests exercising `ExpressionCompiler` pass

## Out of Scope (Track Separately)

- **`module-info.java`**: Adding a proper module descriptor is a larger effort. `Automatic-Module-Name: ognl` is sufficient for now.
- **Removing `OgnlInvokePermission`**: Deprecated now, remove in OGNL 4.0.
- **`AccessibleObject.setAccessible(AccessibleObject[], boolean)` static method**: Deprecated since JDK 9 but the reflective lookup at `OgnlRuntime.java:203` already handles `NoSuchMethodException` gracefully — no change needed.