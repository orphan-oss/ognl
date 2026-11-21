# Optional javassist Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Parse-only users run OGNL with no javassist on the classpath; compiled mode becomes opt-in on `main` and `ognl-3-4-x`.

**Architecture:** Move the reflection-only interface-class lookup out of `ExpressionCompiler` into a core helper. Replace `OgnlRuntime`'s eager javassist static block with a lazily created compiler. Mark javassist `<optional>`. Prove it with a second surefire execution that runs without javassist.

**Tech Stack:** Java 17 (`main`), Java 8 (`ognl-3-4-x`), Maven, JUnit 5, javassist 3.33.0-GA.

**Spec:** `docs/superpowers/specs/2026-11-21-optional-javassist-design.md`

## Global Constraints

- No compiled-mode behaviour changes. No API removals. `JavaSource`, `PropertyAccessor` and `Node` are untouched.
- `Ognl` public methods keep their signatures (stable API).
- Missing-javassist message, used verbatim in both exceptions: `Compiled mode needs org.javassist:javassist on the classpath`.
- `setCompiler` keeps winning over the lazy default, and keeps clearing the interface-class cache (#753).
- Sonar: aim for >80% coverage on new code; do not extract or rename legacy method bodies (moved code counts as new).
- Work happens in `.worktrees/optional-javassist` (branch `optional-javassist`); every change lands via a PR.

## Review Focus

1. **Many threads trigger the first `getCompiler()` at once.** Expected: one `ExpressionCompiler` instance. Test in Task 2 (`concurrentFirstCallsShareOneCompiler`).
2. **`setCompiler(null)`.** Today that leaves a null compiler. Expected: the next `getCompiler()` lazily installs the default again. Test in Task 2 (`settingNullFallsBackToDefault`).
3. **Interface classes looked up before the compiler exists, then a compile happens.** Expected: identical answers either way. Test in Task 1 (parity test over the same classes).
4. **javassist present but unloadable (restricted runtime, `LinkageError`).** Expected: the same `IllegalStateException` / `OgnlException`, never a raw `Error`. Covered by catching `LinkageError` alongside `ClassNotFoundException` in Task 2's detection, and asserted by the no-javassist test (`NoClassDefFoundError` is a `LinkageError`).
5. **A shaded, relocated javassist (MyBatis).** Detection uses a string literal that maven-shade relocates. Expected: compiled mode works when the relocated copy is present. Not automatable here; the reviewer confirms detection uses `Class.forName("javassist.ClassPool", …)` as before.

---

### Task 1: Interface-class lookup without the compiler

**Files:**
- Create: `ognl/src/main/java/ognl/InterfaceClasses.java`
- Modify: `ognl/src/main/java/ognl/enhance/ExpressionCompiler.java:344-377` (`getInterfaceClass` and its private overload)
- Modify: `ognl/src/main/java/ognl/OgnlCache.java:181-182` (`interfaceClassCache` factory)
- Modify: `ognl/src/main/java/ognl/OgnlRuntime.java` (next to `getCompiler()`)
- Test: `ognl/src/test/java/ognl/InterfaceClassesTest.java`

**Interfaces:**
- Produces: `public final class ognl.InterfaceClasses` with `public static Class<?> of(Class<?> clazz)`.
- Produces: `static boolean OgnlRuntime.hasCompiler()` (package-private), true once a compiler is installed explicitly or lazily.
- `OgnlCache.interfaceClassCache` factory: `hasCompiler() ? getCompiler().getInterfaceClass(key) : InterfaceClasses.of(key)`.

- [ ] **Step 1: Write the failing test** `InterfaceClassesTest`
  - `defaultsMatchTheCompiler`: for each of `ArrayList.class`, `HashMap.class`, `HashSet.class`, `ArrayDeque.class`, `new ArrayList<>().iterator().getClass()`, `int.class`, `List.class`, `String.class`, `Object.class`, assert `InterfaceClasses.of(c)` equals `new ExpressionCompiler<>().getInterfaceClass(c)`.
  - `knownAnswers`: `ArrayList → List.class`, `HashMap → Map.class`, `HashSet → Set.class`, `ArrayDeque → Collection.class`, `int → int.class`, `String → String.class`.
- [ ] **Step 2: Run it and confirm it fails to compile.** Run: `./mvnw test -pl ognl -Dtest=InterfaceClassesTest -Dsurefire.failIfNoSpecifiedTests=false`. Expected: compilation error, `InterfaceClasses` not found.
- [ ] **Step 3: Implement `InterfaceClasses.of`** by moving the body of `ExpressionCompiler.getInterfaceClass(Class)` and its private overload there unchanged. `ExpressionCompiler.getInterfaceClass(Class)` keeps its signature and returns `InterfaceClasses.of(clazz)`; delete the private overload.
- [ ] **Step 4: Add `OgnlRuntime.hasCompiler()` and switch the `OgnlCache` factory** as in Interfaces. `hasCompiler()` reads the same field `getCompiler()` guards, under the same lock Task 2 will use (make it `synchronized` now).
- [ ] **Step 5: Run the test and the cache tests.** Run: `./mvnw test -pl ognl -Dtest='InterfaceClassesTest,OgnlRuntimeInterfaceClassCacheTest,OgnlCache*Test' -Dsurefire.failIfNoSpecifiedTests=false`. Expected: all PASS.
- [ ] **Step 6: Commit** `refactor(runtime): look up interface classes without the expression compiler`.

### Task 2: Lazy compiler, optional javassist, no-javassist test run

**Files:**
- Modify: `ognl/src/main/java/ognl/OgnlRuntime.java:322-334` (delete the static block), `getCompiler()`, `setCompiler()`, `compileExpression(C, Node<C>, Object)`
- Modify: `ognl/pom.xml` (javassist `<optional>true</optional>`; surefire executions)
- Modify: `benchmarks/pom.xml` (declare `org.javassist:javassist`, since its compiled benchmarks need it now that it is optional)
- Modify: `README.md` (Release lines section)
- Create: `ognl/src/test/java/ognl/WithoutJavassistTest.java`
- Create: `ognl/src/test/java/ognl/LazyCompilerTest.java`

**Interfaces:**
- Consumes: `OgnlRuntime.hasCompiler()`, `InterfaceClasses` (Task 1).
- Produces: `public static synchronized OgnlExpressionCompiler OgnlRuntime.getCompiler()`, which creates `new ExpressionCompiler()` on first call. If javassist is missing, it throws `IllegalStateException(<message>)`.
- Produces: `OgnlRuntime.compileExpression(...)` throws `OgnlException(<message>)` when javassist is missing and no compiler is set.
- Detection: `Class.forName("javassist.ClassPool", false, OgnlRuntime.class.getClassLoader())`. Catch `ClassNotFoundException | LinkageError`, and keep the existing `RuntimeException` branch's meaning (restricted runtime) with the same message.

- [ ] **Step 1: Write `WithoutJavassistTest`** (package `ognl`, public beans as nested static classes, `MemberAccess` allowing public members, like `benchmarks/.../DefaultMemberAccess.java`).
  - `javassistIsAbsent`: `assertThrows(ClassNotFoundException.class, () -> Class.forName("javassist.ClassPool"))`.
  - `readsAndWritesWithoutJavassist`: on a bean with `name = "x"` and `nums = [1, 2, 3]`:
    - `name.length() + nums.{? #this > 1}.size()` → `3`
    - `setValue("name", …, "y")`, then `name` → `"y"`
    - `#{'k': nums[0]}` → `Map.of("k", 1)` (the indexed lookup the probe crashed on)
    - `nums.{#this * 2}` → `List.of(2, 4, 6)`
    - `@java.lang.Math@max(1, 2)` → `2`
    - `new java.util.ArrayList().size()` → `0`
  - `compileFailsWithOgnlException`: `OgnlException e = assertThrows(OgnlException.class, () -> Ognl.compileExpression(ctx, bean, "name"))`, and `e.getMessage()` equals the message.
  - `getCompilerFailsWithIllegalState`: `assertThrows(IllegalStateException.class, OgnlRuntime::getCompiler)` with the message.
- [ ] **Step 2: Add the surefire executions to `ognl/pom.xml`.** `default-test` adds `<exclude>**/WithoutJavassistTest.java</exclude>`. A new execution `without-javassist` (phase `test`, goal `test`) uses `<includes><include>**/WithoutJavassistTest.java</include></includes>`, `<excludes combine.self="override"/>` and `<classpathDependencyExcludes><classpathDependencyExclude>org.javassist:javassist</classpathDependencyExclude></classpathDependencyExcludes>`, keeping the shared `argLine`.
- [ ] **Step 3: Run it and confirm it fails.** Run: `./mvnw test -pl ognl -Dtest=WithoutJavassistTest -Dsurefire.failIfNoSpecifiedTests=false`. Expected: `without-javassist` errors with `ExceptionInInitializerError` / "Javassist library is missing" from the static block.
- [ ] **Step 4: Write `LazyCompilerTest`** (runs in `default-test`, with javassist present). Save the current compiler in `@BeforeEach` and restore it in `@AfterEach`.
  - `defaultCompilerIsExpressionCompiler`: `assertInstanceOf(ExpressionCompiler.class, OgnlRuntime.getCompiler())`.
  - `concurrentFirstCallsShareOneCompiler`: `setCompiler(null)`, call `getCompiler()` from 16 threads released by a `CountDownLatch`, and assert all results are the same instance.
  - `settingNullFallsBackToDefault`: `setCompiler(null)`, then `getCompiler()` is a non-null `ExpressionCompiler`.
  - `customCompilerWins`: install a `ExpressionCompiler` subclass, and `getCompiler()` returns that same instance.
- [ ] **Step 5: Implement the lazy compiler** per Interfaces. Delete the static block. `compileExpression` calls `getCompiler()` and turns that method's `IllegalStateException` into `OgnlException` by checking availability first; do not catch every `IllegalStateException`. Mark javassist `<optional>true</optional>` in `ognl/pom.xml` and add it to `benchmarks/pom.xml`.
- [ ] **Step 6: Run the full suite (both executions).** Run: `./mvnw test -pl ognl`. Expected: `BUILD SUCCESS`, with `without-javassist` showing `Tests run: 4` (or however many test methods there are), 0 failures.
- [ ] **Step 7: Benchmarks still build.** Run: `./mvnw -q install -DskipTests && ./mvnw -q -pl benchmarks package`. Expected: exit 0.
- [ ] **Step 8: README.** In "Release lines", after the frozen-compiled-mode paragraph, add: compiled mode is opt-in and experimental; from 3.5.0 and 3.4.18 javassist is an optional dependency, and a project that calls `Ognl.compileExpression` must declare `org.javassist:javassist` itself; without it, `compileExpression` throws `OgnlException`.
- [ ] **Step 9: Commit** `feat: make javassist optional, create the expression compiler on first use`.

### Task 3: `main` PR

- [ ] **Step 1:** Run `./mvnw clean verify -pl ognl`. Expected: `BUILD SUCCESS`.
- [ ] **Step 2:** Get a code review of the branch diff against `origin/main` and address the findings (required before any push).
- [ ] **Step 3:** Run `/security-review` and read `SECURITY.md`. A real finding means stop, no PR.
- [ ] **Step 4:** Push `optional-javassist` and open a PR to `main` following `~/.claude/pr_guideline.md`. The body covers the why (spec), the probe finding about the interpreted path, and the upgrade note.
- [ ] **Step 5:** After CI, check the Sonar quality gate via the PR issues URL in `CLAUDE.md`, and fix new issues only.

### Task 4: 3.4.x backport PR

**Files (3.4.x single-module layout):** `src/main/java/ognl/{InterfaceClasses,OgnlCache,OgnlRuntime}.java`, `src/main/java/ognl/enhance/ExpressionCompiler.java`, `src/test/java/ognl/{InterfaceClassesTest,LazyCompilerTest,WithoutJavassistTest}.java`, `pom.xml` (javassist at the dependency with `<version>3.33.0-GA</version>`; surefire in `pluginManagement` and in the JDK profiles), `README.md`.

- [ ] **Step 1:** `git worktree add .worktrees/optional-javassist-3.4.x -b optional-javassist-3.4.x origin/ognl-3-4-x`.
- [ ] **Step 2:** Apply the `main` commits (excluding the docs/superpowers commits): `git format-patch --stdout origin/main..optional-javassist -- ognl/src | git -C .worktrees/optional-javassist-3.4.x am -3 -p2 --include='src/*'`. Then port the `pom.xml` and README hunks by hand.
- [ ] **Step 3:** Fix Java 9+ APIs in the ported code and tests: `List.of`/`Map.of`, `instanceof` patterns, `var`, `assertInstanceOf` (fine on JUnit 5.8+; check the pinned version). Also reconcile with 3.4.x's `OgnlCache` if the interface-class cache lives elsewhere there.
- [ ] **Step 4:** Run the full suite on Temurin 8: `JAVA_HOME=$(/usr/libexec/java_home -v 1.8) ./mvnw clean verify`. Expected: `BUILD SUCCESS`, including `without-javassist`. Those legacy tests also run compiled mode, so a failure there that `main` lacks is a finding to report, not to paper over.
- [ ] **Step 5:** Code review of the diff against `origin/ognl-3-4-x`; `/security-review` rescoped to `origin/ognl-3-4-x...HEAD`.
- [ ] **Step 6:** Push and open the PR to `ognl-3-4-x`, linking the `main` PR. Sonar compares 3.4.x PRs against `main`; pre-existing complexity failing the gate gets a follow-up issue, not a refactor in the backport.
