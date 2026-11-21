# Optional javassist: compiled mode becomes opt-in

Status: proposed, 2026-11-21. Targets `main` (3.5.0) and `ognl-3-4-x` (3.4.18).

## Why

Compiled mode (`Ognl.compileExpression`, `ognl.enhance`) still disagrees with interpreted mode
in places (13 open issues), and no known consumer uses it. Struts, Thymeleaf and MyBatis only
parse. Yet every user must ship javassist, because `OgnlRuntime`'s static initializer refuses to
load without it. MyBatis even shades and relocates javassist into its own jar for nothing.

Compiled mode stays because it is 40–80× faster on property navigation (5 ns vs 410 ns,
`benchmarks/etc/ognl-runtime-benchmark-baseline.json`), and Struts or MyBatis may want it as an
opt-in later. This change makes it opt-in today. The real fix for the mismatches is a later 4.0
project (see Approaches).

## Goals

- A parse-only application runs with no javassist on the classpath, on both release lines.
- Applications that compile keep working once they declare javassist themselves.
- Compiling without javassist fails with a clear, catchable `OgnlException`, not a
  `NoClassDefFoundError`.

## Non-goals

- No compiled-mode fixes. The 3.4.x freeze on compiled-mode behaviour still holds; this change
  is packaging only.
- No API removal, no module split, no change to `JavaSource`, `PropertyAccessor` or `Node`.

## Approaches

1. **Opt-in compiled mode in core (picked, now).** javassist becomes optional and the compiler is
   created on first use. Cheap, no API break, and it is a prerequisite for both other paths.
2. **Split the current code generator into an `ognl-compiler` module.** Rejected. It breaks the
   API (4.0), it is a large refactor that Sonar scores as all-new code, and the mismatches move
   with the code.
3. **Replace the code generator with a compiled mode built on interpreted semantics (picked,
   4.0).** It would cache the resolution interpreted mode performs (the chosen `Method`,
   accessor and conversion) as per-node method handles with guards, falling back to the
   interpreted path when a guard fails, so the two modes agree by construction. This is a
   separate project with its own brainstorm and design, starting once this change has shipped.

## Design

Verified by a throwaway probe on `main`. With only the first two changes below, a parse-only
program ran without javassist, and the full test suite ran without javassist with no failures
outside code-generation paths.

- **Dependency.** `ognl/pom.xml` marks `org.javassist:javassist` as `<optional>true</optional>`.
  Only the dependency's scope changes; the managed version stays.
- **Lazy compiler.** Remove the `OgnlRuntime` static block that loads `javassist.ClassPool`.
  `getCompiler()` creates the default `ExpressionCompiler` on first call, in a thread-safe way.
  If javassist is absent it throws `IllegalStateException` saying compiled mode needs
  `org.javassist:javassist` on the classpath. `OgnlRuntime.compileExpression` goes through
  `getCompiler()` instead of reading the field. `setCompiler` behaves as today, and a custom
  compiler needs no javassist.
- **Interface-class lookup leaves the compiler.** The probe found that interpreted evaluation
  reaches the compiler: `ASTProperty.getIndexedPropertyType` → `OgnlRuntime.getInterfaceClass` →
  `OgnlCache.interfaceClassCache` → `getCompiler().getInterfaceClass`. The default logic
  (`ExpressionCompiler.getInterfaceClass`) is plain reflection, so move it to a core helper in
  `ognl` that `ExpressionCompiler` delegates to. `OgnlCache` asks the compiler only when one is
  already installed, explicitly or lazily, and otherwise asks the helper. This preserves #753's
  contract that `setCompiler` answers drive the cache and that `setCompiler` clears it.
- **Clear failure.** `Ognl.compileExpression` throws `OgnlException` (its signature already
  declares `throws Exception`) when javassist is missing, so a `catch (Exception)` fallback like
  caipirinha's keeps working.
- **Tests.** A new test class covers parse/get/set, chains, indexed access, projection/selection,
  method and constructor calls, and the `compileExpression` failure. It runs in a second
  surefire execution with `classpathDependencyExcludes` set to `org.javassist:javassist`. The
  default execution is unchanged. Builder's call: which existing interpreted-only test classes
  also join that execution.
- **Docs.** README: compiled mode is opt-in and experimental, and requires declaring javassist.
  Add an upgrade note for 3.5.0 and 3.4.18 in `docs/VersionNotes.md` (or wherever release notes
  live on each branch).
- **3.4.x.** Ported via the usual backport flow, as a separate PR from a `-3.4.x` branch. That
  branch has the same static initializer. The builder repeats the no-javassist test run there,
  because 3.4.x legacy tests also run compiled mode.

## Left to the builder

- Name and location of the interface-class helper.
- Lazy-init idiom (synchronized accessor or holder class), as long as `setCompiler` still wins.
- Exact exception messages.
