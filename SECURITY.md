# Security Policy

## Supported Versions

Use only the supported versions

| Version | Supported          |
| ------- | ------------------ |
| 3.4.x   | :white_check_mark: |
| 3.3.x   | :x:                |
| 3.2.x   | :x:                |
| 3.1.x   | :x:                |
| 3.0.x   | :x:                |

## Security model

OGNL evaluates expressions with the privileges of the application that embeds it. The only access-control
mechanism OGNL offers is the `MemberAccess` implementation the application installs in its `OgnlContext`.
It is consulted for every property, method and constructor access with the target, the member and the context,
so it is the one place that can decide what an expression may touch. Applications that evaluate untrusted
expressions must install a restrictive `MemberAccess`, as Apache Struts does with its `SecurityMemberAccess`.

`Ognl.createDefaultContext` and `DefaultMemberAccess` allow every public member. They are meant for trusted
expressions and are not a sandbox.

Stricter invocation mode (`-Dognl.UseStricterInvocation`, on by default) refuses direct calls to a short, fixed
list of JDK members such as `Runtime`, `ProcessBuilder` and `System.exit`. It is a convenience guard against
accidents, not a security boundary. It keys on the declaring class of the method OGNL invokes, so it cannot see
a method the JDK invokes on the expression's behalf (reflection proxies, `java.beans`, and similar), and the
list will not be extended to chase such paths.

What counts as a vulnerability in OGNL:

- A member that a restrictive `MemberAccess` denies is nevertheless reachable from an expression, in
  interpreted or compiled mode. Please report it.
- OGNL crashes, loops or exhausts resources on attacker-controlled input in a way `expressionMaxLength`
  does not prevent. Please report it.

What does not:

- Circumventing the stricter-invocation list while a permissive `MemberAccess` (including the default one)
  is in place. That configuration already grants an expression everything public, and the list is not the layer
  that is supposed to stop it.

## Reporting a Vulnerability

Do not report security vulnerabilities using Github Issues, please contact [security@orphan.software](mailto:security@orphan.software) or contact [Tidelift Security](https://tidelift.com/security) directly before taking any action .
