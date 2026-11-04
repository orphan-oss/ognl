# Pluggable property resolution (#755) — design

Status: draft for review, 2026-11-04. Issue: #755. Consumer: Apache Struts WW-5326 (Struts 8 / OGNL 3.5)
and Struts 7.5.0 (OGNL 3.4.x).

## Intent

Before OGNL evaluates a request parameter, Struts' `StrutsParameterAuthorizer.isAuthorized(parameterName, target,
action)` predicts which root member (method or field) the parameter will reach on the action or model, and checks
that member for `@StrutsParameter`. To do that, it copies OGNL's rules for turning a property name into members:
`set`/`is`/`get` prefixing, `accessorBaseNames` (copied in WW-5759), argument counts, public-field matching, and,
since WW-5820 (Struts PR #2073), the write order across the root. Every OGNL change to those rules (#643/#644,
#657) has to be mirrored there, and until it is, Struts and OGNL disagree.

The fix: OGNL owns the rules as one installable strategy, the **property resolver**. OGNL uses it on every route
from a property name to a member, and embedding applications query **the same instance**. Struts can also install
a resolver that **narrows** resolution, and OGNL then refuses what the resolver did not offer.

## Decisions (made by Łukasz)

1. **The resolver returns members, not base names.** A base-name list would leave Struts re-doing prefixing,
   argument counts, method-vs-field precedence and overload choice, which is where the drift has come from.
2. **The result is an ordered list of candidate members.** OGNL tries them in order and uses the first that
   `MemberAccess` allows. The resolver decides *which members are candidates*, and `MemberAccess` still decides
   *which may be used*. One member per access was rejected because a single answer cannot reproduce today's
   setter → field → lenient-write chain, where `MemberAccess` runs between the steps.
3. **It is installed globally**, one resolver per JVM / OGNL classloader, not per `OgnlContext`. The method caches
   are static and keyed by (class, name), so a per-context resolver would be served whatever an earlier one
   cached.
4. **Narrowing is supported.** A custom resolver that leaves a member out makes OGNL never reach it through
   property resolution.
5. **The resolver owns every property-name route**, including the indexed-property route (`foo[0]` →
   `getFoo(int)` / `setFoo(int, V)`).
6. **Ship on both lines**: main (3.5.0) and a backport to `ognl-3-4-x` (3.4.18+), so Struts 7.5.0 (on OGNL
   3.4.17 today) can switch its authorizer to the resolver without waiting for Struts 8.
7. **The default resolver reproduces today's resolution exactly** on each line. No behaviour changes for anyone
   who doesn't install a resolver.

## Goals

- One public interface, one installed instance, used by OGNL internally and queryable by callers.
- With the default resolver, every existing test passes unchanged on both lines, and characterization tests
  (written first, against today's code) pass before and after.
- A narrowing resolver leaves no hole: for each route below, a test shows that a member the resolver omits is not
  reached.
- Struts can answer "which member does a write/read of `name` on `Class` reach?" from the resolver and its own
  `MemberAccess`, with no copy of OGNL's naming rules.

## Non-goals

- Method calls (`foo.bar()`, `ASTMethod`) and static fields (`@Cls@FIELD`, `ASTStaticField`). They resolve method
  and field names given literally, not property names. *(My call.)*
- A resolver per `OgnlContext`, per action or per expression.
- Changing default resolution, including oddities it currently has (see "Known quirks the default must keep").
- Moving `MemberAccess` decisions into the resolver. `MemberAccess` stays the only access-control layer
  (SECURITY.md "Security model"). The resolver narrows candidates, it does not grant access.
- Compiled-mode-only work on 3.4.x. Compiled mode there is frozen (README policy, #739/#740).

## Anti-goals (failure even if it "works")

- The default resolver differs from today's behaviour in any observable way, on either line.
- Any property-name route inside OGNL still resolves members without the resolver. That reintroduces the
  divergence #755 exists to remove.
- Struts still has to copy a naming rule, an argument-count rule or the candidate order to get OGNL's answer.

## Current state: routes to cover

Line numbers are main at `b7d3016b`. 3.4.x has the same structure (same methods, same `setPossibleProperty`
order, Java 8, no records).

| Route | Where | Today's naming rule |
|---|---|---|
| Getter / setter | `getGetMethod` / `_getGetMethod`, `getSetMethod` / `_getSetMethod` (`OgnlRuntime.java:2084–2235`) via `getDeclaredMethods` + private `accessorBaseNames` (`:1997–2033`) | `is`/`get`/`set` + base names (`uRange` → `uRange`, `URange`); picks first public, then first public non-default interface, then first; 0 args for get, 1 for set; `_useFirstMatchGetSetLookup` flag |
| Lenient read / write | `getReadMethod` (`:2567`, cached in `cacheReadMethod`) and `getWriteMethod` (`:2662`), scored by `findBestMethod` (exact, case-insensitive, suffix) | reached from `getMethodValue` (`:1683`, only when `!context.isIgnoreReadMethods()`) and from `ObjectPropertyAccessor.setPossibleProperty` after setter and field both fail |
| Field | `getField` (`:1787`, walks superclasses), `getFieldValue`, `setFieldValue`, `hasField`, `hasSettableField`, `hasReadableField` | exact field name; static refused, final refused for write |
| Record accessor (main only) | `hasRecordComponentAccessor` (`:1923`), gated by `isIgnoreReadMethods` | component name |
| `has…Property` | `hasGetProperty` / `hasSetProperty` (`:2240–2254`), `ObjectPropertyAccessor.hasGetProperty/hasSetProperty` | combination of the above; must agree with what get/set would do |
| Property descriptors / indexed | `getPropertyDescriptor(s)`, `getIndexedPropertyType`, `getIndexedProperty`, `setIndexedProperty` (`~:2386–2445`), called from `ASTChain` (`:108–212`); descriptors from `java.beans.Introspector`, repaired via `getDeclaredMethods` in `PropertyDescriptorCacheEntryFactory`, plus OGNL's `ObjectIndexedPropertyDescriptor` | Introspector's decapitalized names, a third naming rule |
| Compiled mode (main) | `ASTProperty.toGetSourceString` (`:226–280`), `toSetSourceString` (`:440–490`), `ObjectPropertyAccessor` source helpers (`:129–210`), `ListPropertyAccessor` (`:168`, `:210`) | descriptor route + lenient read/write |

Interpreted write today (`ObjectPropertyAccessor.setPossibleProperty`): setter (`setMethodValue`, with
`MemberAccess`) → field (`setFieldValue`, with `MemberAccess`, and with conversion) → lenient `getWriteMethod` (with
`MemberAccess`).

Interpreted read (`getPossibleProperty` → `getMethodValue`, `:1681–1689`) is **not** "first allowed":
- The method step is the getter, or the lenient read *only if no getter exists* (and not `isIgnoreReadMethods`).
- `MemberAccess` is checked on that one method. If it is refused, OGNL goes to the field and never tries the
  lenient read.
- The default READ list must therefore leave the lenient read out whenever a getter exists, so that "first
  allowed" over the list gives today's result.

Record components (main) are reached on interpreted reads through the lenient `getReadMethod` (exact-name match).
`hasRecordComponentAccessor` only feeds `hasGetProperty`.

`has…Property` today is narrower than get/set:
- `hasGetProperty` = getter, record accessor or readable field. It ignores the lenient read.
- `hasSetProperty` = setter or settable field. It ignores the lenient write.
- Struts' `CompoundRootAccessor.setProperty`/`getProperty` use these to choose which root object takes a write or
  read (the WW-5820 order), so they must follow the resolver too.

Static fields on read: `getFieldValue` throws `NoSuchFieldException`, which `getPossibleProperty` wraps in a plain
`OgnlException`. The default must keep that exception type, not turn it into `NoSuchPropertyException` by leaving
static fields out of the list.

Indexed access (`getIndexedProperty` / `setIndexedProperty`, `:2406–2450`) uses the descriptor only for the method
**name**. It then calls `callMethod(context, source, m.getName(), args)`, which chooses among every overload with
that name by the runtime types of the index (and value). `ObjectIndexedPropertyDescriptor` pairs `getFoo(K)` with
`void setFoo(K, V)` for any key type `K`. It needs both methods and exactly one implementation of each name
(`indexMethodCheck`), so a read-only indexed getter produces no descriptor. `ASTProperty.getIndexedPropertyType`
looks up `getInterfaceClass(source.getClass())`, while `get/setIndexedProperty` look up `source.getClass()`.

Findings that shape the design:

- **A top-level write never picks an overload by the value's type.** `setMethodValue` calls only the one setter
  `getSetMethod` chose. The only value-dependent step: when a field write fails conversion
  (`getConvertedType` returns null), `setPossibleProperty` falls through to the lenient write. A pre-evaluation
  check cannot predict that. It stays as it is and is documented. With a narrowed list that has nothing after the
  field, it disappears.
- **Context flags are not resolver input.** `isIgnoreReadMethods` gates the lenient read and record accessors per
  context. The resolver's output depends only on (class, name, kind), so it stays cacheable. OGNL applies context
  flags to the list afterwards, as it applies `MemberAccess`.

## The shape

The names are my calls. The builder may rename them.

```java
package ognl;

public interface PropertyResolver {
    /** Candidate members for this access, in the order OGNL tries them; empty when none. */
    List<Member> candidates(Class<?> targetClass, String propertyName, PropertyAccessKind kind);
}

public enum PropertyAccessKind { READ, WRITE, INDEXED_READ, INDEXED_WRITE }
```

- **Inputs are only (class, name, kind)**, with no context, target or value. Struts has none of those at decision
  time, and the caches are keyed this way. A resolver must be a pure function of its inputs. The javadoc says so,
  because results are cached.
- **Each element carries its member and its step**, with the step one of accessor, field, lenient or indexed.
  *(My call, settling the reviewer's question: OGNL needs the step for the `isIgnoreReadMethods` gate and for
  `has…Property`, and Struts needs it to mirror `has…Property`.)* So the list holds a small value type, for
  example `PropertyCandidate(Member member, Step step)`, not raw `Member`. In the default `WRITE` list the order is:
  the chosen setter, then the field (if it exists), then the lenient write method. In the default `READ` list it
  is: the getter, *or* the lenient read only when no getter exists, then the field. The order follows "Current
  state".
- **`has…Property` derives from the same list** *(my call)*: "some non-lenient candidate that passes the same
  checks get/set apply". With the default resolver, that is exactly today's narrower rule. With a narrowing
  resolver, it follows the narrowing. Struts' `CompoundRootAccessor` already calls `OgnlRuntime.hasSetProperty` /
  `hasGetProperty`, so it gets this without copying anything.
- **`INDEXED_*` returns every overload OGNL may call** *(my call, settling the reviewer's conflict between
  "narrowing holds" and "default exact")*. For the default, that is the same set `callMethod` searches today:
  every non-static method with the indexed accessor's name. OGNL then chooses among *the candidates only*, using
  the same runtime-type scoring as today (`callAppropriateMethod` over the list). The default is therefore
  identical, and a narrowing resolver that leaves out `getFoo(int)` makes it unreachable even for an `Integer`
  index. Descriptor detection (`IndexedPropertyDescriptor`, and `ObjectIndexedPropertyDescriptor` with any key
  type `K`) stays as today and decides *whether* the property is indexed. `getIndexedPropertyType` must agree with
  the resolver: no indexed candidates means not indexed. The builder settles the `getInterfaceClass` /
  `getClass` mismatch so that one access asks the resolver about one class, with the default results unchanged.
- **Predictability for Struts:** the chosen indexed overload depends on the index and value runtime types. The
  index type is visible in the parameter name (`[0]` versus `['k']`). The value type is not known before
  conversion. Javadoc states this. It is the second documented case a pre-evaluation check cannot predict, the
  first being the field-conversion fallback.
- **Installation:** static `OgnlRuntime.setPropertyResolver(PropertyResolver)` (null resets to the default) and
  `OgnlRuntime.getPropertyResolver()`. Setting it clears every cache derived from resolution: `cacheGetMethod`,
  `cacheSetMethod`, `cacheReadMethod`, the property-descriptor cache, and anything else the builder finds that
  caches per (class, name). The field is `volatile`. Javadoc: global per OGNL classloader. Several applications
  that share one OGNL jar (one servlet container, shared lib) share the resolver. Install it once at startup.
  Installing under load causes one cold-cache spike.
- **Install before the first evaluation: documented, not enforced** *(Łukasz's decision)*. The javadoc names both
  leaks of a late install:
  - expressions compiled earlier keep the members they captured, and no cache clear reaches them;
  - a lookup that was in flight during the install can write an old-resolver result back after the clear.

  `setPropertyResolver` swaps the field first and clears afterwards. It never throws because resolution has already
  happened.
- **Public default:** `DefaultPropertyResolver` (public, non-final or with public building blocks) so a custom
  resolver can delegate to it and filter its output. Narrowing is then
  `default.candidates(...).stream().filter(...)`.
- **Query helper (my guess, the builder may drop it):** a static method that walks the candidates exactly as OGNL
  does and returns the first member the given `MemberAccess` allows, so Struts doesn't reimplement the loop. Struts
  has no `OgnlContext` at decision time, so the helper should accept what Struts can supply (a `MemberAccess` and
  the target, or a minimal context the caller builds). The builder chooses the signature after reading
  `StrutsParameterAuthorizer`.

## Constraints

- **Behaviour-preserving default on both lines.** Write characterization tests first, against today's code, for
  each route in the table, including: `uRange`/`URange` base names; `_useFirstMatchGetSetLookup` on and off;
  public-vs-interface-vs-first preference; field superclass walk; final/static refusal; lenient case-insensitive
  and suffix matches; `isIgnoreReadMethods`; indexed `int` and `String` descriptors; the setter-refused-by-
  `MemberAccess` → field fallback; field-conversion-failure → lenient fallback.
- **Known quirks the default must keep:** `getWriteMethod`'s last fallback calls `getReadMethod(target, "set" +
  name, …)`; `getReadMethod` strips `"` from names; bridge methods count as callable for lenient read and write.
  Fixing any of these is a separate issue.
- **Public API stability:** existing public methods on `OgnlRuntime` (`getGetMethod`, `getSetMethod`,
  `getReadMethod`, `getWriteMethod`, `getDeclaredMethods`, `getField`, `hasGetProperty`, …) keep their signatures
  and their default results. They become views over the installed resolver where that's what they mean.
- **Sonar treats moved code as new code** (memory, #713-style). Prefer having `DefaultPropertyResolver` call the
  existing lookups over moving their bodies, if that keeps the default exact.
- **3.4.x:** Java 8 (no `List.of`, records or `var`), tested on Temurin 8. Port with the documented
  `format-patch | am -3 -p2 --include='src/*'` flow, on a branch suffixed `-3.4.x`. On 3.4.x, compiled mode is
  covered only where it already shares the code being changed (the descriptor route and the lenient lookups). Do
  not add compiled-only hooks there. List any compiled-mode route that escapes the resolver on 3.4.x in the
  javadoc / release notes.
- **Main:** compiled mode must not escape the resolver. `ASTProperty`'s compiled getter/setter paths and the
  `ObjectPropertyAccessor` / `ListPropertyAccessor` source helpers go through it, through the descriptor route or
  directly.
- **Order of work:** main first (one PR), then the 3.4.x backport PR. Each needs `/security-review` before
  `gh pr create`, and SonarCloud's gate on main.
- **Cache ownership PR #753** (open, contributor) touches the same caches. Don't wait on it. Rebase onto it if it
  merges first, or leave a clean conflict for it.

## Left to the builder

- Final type and method names, including the candidate value type and its step names.
- How the descriptor route is made resolver-driven: build `PropertyDescriptor`s from the resolver, or filter
  Introspector's output through it. Either way, the default must yield the same descriptors.
- Whether the lenient lookups stay as separate public methods behind the default resolver or fold into it.
- The query helper's existence and signature.
- How to split commits; test placement (JUnit 5 on main; on 3.4.x, check whether legacy compiled-mode suites also
  run the new tests).
- Release notes (`docs/VersionNotes.md`) and the javadoc caveat about shared jars.

## Out of scope, follow-ups

- Struts side: switching `StrutsParameterAuthorizer` (7.5.0 and 8.0.0) to the resolver, and deciding the narrowed
  Struts resolver (for example, refusing the lenient case-insensitive fallback). Tracked under WW-5326 in Struts.
