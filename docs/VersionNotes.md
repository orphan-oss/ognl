# Version Notes

## Release notes - version 3.2.20, 3.1.29 (2021-04-05)
 * fixes `OgnlOps#equal`, see [#116](../../../pull/116) & [#123](../../../pull/123) -
   thanks to aleksandr-m

## Release notes - version 3.2.19 (2021-03-18)
 * uses `MemberAccess` to create a new default context to avoid NPE [#118](../../../pull/118) -
   thanks to zhuster

## Release notes - version 3.2.18 (2020-12-18)
 * un-deprecates previously deprecated API by providing missing instance of `MemberAccess` [#114](../../../issues/114) -
   thanks to lukaszlenart

## Release notes - version 3.2.17 (2020-12-05)
 * makes AST classes public [#115](../../../pull/115) -
   thanks to sebthom 
 * un-deprecates previously deprecated API by providing missing instance of `MemberAccess` [#114](../../../issues/114) -
   thanks to lukaszlenart

## Release notes - version 3.2.16 (2020-11-14)
 * adds support for null varargs [#113](../../../pull/113) -
   thanks to lukaszlenart
 * updates `isMethodCallable()` logic, re-introduce its usage for `getReadMethod()` [#110](../../../pull/110) -
   thanks to JCgH4164838Gh792C124B5
 * introduces `AbstractMemberAccess` to allow create it on-fly [#109](../../../pull/109) -
   thanks to lukaszlenart
 * bumps junit from 4.12 to 4.13.1 [#108](../../../pull/108) -
   thanks to dependabot
 * minor cleanups related to previous 3.1.x merges [#107](../../../pull/107) -
   thanks to JCgH4164838Gh792C124B5
 * fixes resolve race condition when there are to many threads since [#106](../../../pull/106) -
   thanks to rolandhe

## Release notes - version 3.2.15
 * fixes `OgnlRuntime#getReadMethod()` returns `null` if the method is a bridge method [#104](../../../pull/104) -
   thanks to harawata

## Release notes - version 3.2.14
 * deprecated constructor always throws an exception [#81](../../../issues/81),[#101](../../../pull/101) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.2.13, 3.1.28
 * fixes Enum comparison failure [#98](../../../pull/98),[#99](../../../pull/99) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.2.12
 * DefaultClassResolver should resolve classes in the default package [#93](../../../pull/93) -
   thanks to Iwao AVE!
 * Resolves problem with setting varargs parameter [#92](../../../pull/92) -
   thanks to Łukasz Lenart
 * Various minor cleanup changes [#85](../../../pull/85) -
   thanks to JCgH4164838Gh792C124B5
 * add expression max length functionality to improve security [#82](../../../pull/82) -
   thanks to Yasser Zamani
 * plus additional enhancements related to max length functionality [#87](../../../pull/87) -
   thanks to JCgH4164838Gh792C124B5
 * improves getter/setter detection algorithm [#75](../../../pull/75) -
   thanks to JCgH4164838Gh792C124B5
* enhances cache clearing [#77](../../../pull/77) -
   thanks to JCgH4164838Gh792C124B5
* does not fail on getDeclaredXXX when user has used a SecurityManager [#79](../../../pull/79) -
   thanks to Yasser Zamani

## Release notes - version 3.1.26
 * add expression max length functionality to improve security [#82](../../../pull/82) -
   thanks to Yasser Zamani
 * plus additional enhancements related to max length functionality [#87](../../../pull/87) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.1.25
 * improves getter/setter detection algorithm [#75](../../../pull/75) -
   thanks to JCgH4164838Gh792C124B5
* enhances cache clearing [#77](../../../pull/77) -
   thanks to JCgH4164838Gh792C124B5
* does not fail on getDeclaredXXX when user has used a SecurityManager [#79](../../../pull/79) -
   thanks to Yasser Zamani

## Release notes - version 3.2.11
 * Fixes to compare non-comparable objects by equals only [#78](../../../issues/78) -
   thanks to peteruhnak

## Release notes - version 3.1.24
 * Adds optional Security Manager to allow run expressions in a sandbox [#69](../../../pull/69) -
   thanks to Yasser Zamani

## Release notes - version 3.1.22
 * Restores unrestricted access to public static fields [#67](../../../pull/67) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.2.10
 * Upgrades to Javassist 3.24.1 to restore support for Java 7 [#65](../../../issues/65) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.1.21, 3.2.9
 * Code clean up [#63](../../../issues/63), [#64](../../../issues/64) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.1.20
 * Minor cleanups for `DefaultMemberAccess` `restore` method [#61](../../../pull/61) -
   thanks to JCgH4164838Gh792C124B5

## Release notes - version 3.1.19, 3.2.8
 * `MemberAccess` does not support private static field. [#59](../../../issues/59) -
   thanks to hengyunabc

## Release notes - version 3.1.18
 * `getDeclaredMethods()` searches super-interfaces recursively. [#55](../../../pull/55) -
   thanks to Iwao AVE!
 * allows override a strategy for loading a class on `DefaultClassResolve` [#50](../../../pull/50) -
   thanks to Iwao AVE!

## Release notes - version 3.1.18, 3.2.7
 * `getDeclaredMethods()` searches super-interfaces recursively. [#55](../../../pull/55) -
   thanks to Iwao AVE!

## Release notes - version 3.2.6
 * allows override a strategy for loading a class on `DefaultClassResolve` [#50](../../../pull/50) -
   thanks to kazuki43zoo

## Release notes - version 3.1.17, 3.2.5
 * supports concurrency in `DefaultClassResolver` [#46](../../../pull/46) -
   thanks to kazuki43zoo 

## Release notes - version 3.1.16, 3.2.4
 * collects only default methods when scanning interfaces [#40](../../../pull/40) -
   thanks to Iwao AVE!

## Release notes - version 3.2.3 - WIP (new changes are coming)
 * makes `OgnlContext` a bit more immutable
   * `TypeConverter` can be set only when creating a new context, the setter won't work anymore
   * Implementation of the `MemberAccess` is required when crearting a new context, you must always provide your own
   * `DefaultMemberAccess` is only available in tests, it won't be used when there was no custom `MemberAccess` provided, an exception will be thrown in such case
 * sets source and target in `pom.xml` to Java 1.7
 * makes better decisions on methods first call [#39](../../../pull/39) -
   thanks to Yasser Zamani
 * fixes access to property which reads method is Java 8 default method [#33](../../../pull/33) -
   thanks to Yanming Zhou

## Release notes - version 3.1.15, 3.0.21
 * makes better decisions on methods first call [#36](../../../pull/36), [#38](../../../pull/38) -
   thanks to Yasser Zamani

## Release notes - version 3.1.14, 3.0.20
 * drops access to `#context` and `_classResolver` via a magic keys -
   thanks to Łukasz Lenart

## Release notes - version 3.1.12
 * fixes issue with returning the `hasCode` method when looking for a field `code` [#32](../../../issues/32) -
   thanks to Łukasz Lenart

## Release notes - version 3.1.11
 * fixes issue with returning default methods from interfaces implemented by parent class [#30](../../../issues/30) -
   thanks to Vlastimil Dolejš

## Release notes - version 3.1.10, 3.0.19
 * Does not treat negative numbers as an arithmetic operation [#28](../../../issues/28) -
   thanks to Łukasz Lenart

## Release notes - version 3.1.9, 3.0.18
 * Drops access to `_memeberAccess` field via a magic key -
   thanks to Łukasz Lenart

## Release notes - version 3.1.8, 3.0.17, 3.0.6.2
 * Exposes flags to allow check if an expression is a chain or an arithmetic operation or a simple method -
   thanks to Łukasz Lenart

## Release notes - version 3.1.6
 * fixes automatic type conversion to avoid `double boxing` [#25](../../../issues/25)/[#26](../../../pull/26) - 
   thanks to Christian Niessner from [secadm GmbH](http://www.secadm.de/)

## Release notes - version 3.1.5
 * fixes issue with selecting overloaded methods [#23](../../../issues/23)/[#24](../../../pull/24) - 
   thanks to Christian Niessner from [secadm GmbH](http://www.secadm.de/)

## Release notes - version 3.1.4
 * fixes issue with executing expressions on Java 8 plus adds an `java like` method matching [#19](../../../pull/19) -
   thanks to marvkis

## Release notes - version 3.1.3, 3.0.14, 3.0.6.1
 * Exposes flag to allow check if an expression is a sequence of simple expressions -
   thanks to Łukasz Lenart

## Release notes - version 3.1.2
 * Fixes accessing statics within Enums [OGNL-158](https://issues.apache.org/jira/browse/OGNL-158) -
   thanks to Aleksandr Mashchenko

## Release notes - version 3.1.1, 3.0.13
 * OgnlRuntime.invokeMethod can throw IllegalAccessException because of hash collisions was fixed [OGNL-252](https://issues.apache.org/jira/browse/OGNL-252) - 
   thanks to Carlos Saona

## Release notes - version 3.1
 * support for boolean expression in Strings was added, this can break backward compatibility [#8](../../../issues/8) - 
   thanks to Daniel Fernández

## Release notes - version 3.0.12
 * lots of optimizations which should improve overall performance [#9](../../../pull/9), [#10](../../../pull/10), [#11](../../../pull/11), [#12](../../../pull/12) - 
   thanks to Daniel Fernández
 * OGNL supports default methods in interfaces (Java 8) [OGNL-249](https://issues.apache.org/jira/browse/OGNL-249)

## Release notes - version 3.0.11
 * fixes problem with cacheKey too expensive to create [WW-4485 ](https://issues.apache.org/jira/browse/WW-4485 ) -
   thanks to Jasper Rosenberg

## Release notes - version 3.0.10
 * regression bug in ognl for "is..." property getters [WW-4462](https://issues.apache.org/jira/browse/WW-4462) -
   if expression doesn't end with `()` is considered as a name of property a not the method itself
   thanks to Jasper Rosenberg

## Release notes - version 3.0.9
 * replaced IntHashMap with ConcurrentMap to avoid deadlocks [WW-4451](https://issues.apache.org/jira/browse/WW-4451) -
   thanks to Jasper Rosenberg

## Release notes - version 3.0.8
 * added better capitalization logic for methods [WW-3909](https://issues.apache.org/jira/browse/WW-3909) -
   thanks to Iwen.ma

## Release notes - version 3.0.7
  * uses better method to calculate method's cache key [WW-4113](https://issues.apache.org/jira/browse/WW-4113) -
    thanks to Kevin Su

## Release notes - version 3.0.6
 * important performance improvement [OGNL-224](https://issues.apache.org/jira/browse/OGNL-224) -
   thanks to Pelladi Gabor
 * race condition fix [OGNL-226](https://issues.apache.org/jira/browse/OGNL-226) - thanks to Johno Crawford

## Release notes - version 3.0.5
 * partially reverts previous changes to allow OGNL to work in environment with Security Manager enabled
   [WW-3746](https://issues.apache.org/jira/browse/WW-3746)

## Release notes - version 3.0.4
 * Adds possibility to discover eval chain

## Release notes - version 3.0.3
 * small fix to improve performance [WW-3580](https://issues.apache.org/jira/browse/WW-3580)

## Release notes - version 3.0.2
 * small fix to solve a problem with compiling under JDK5

## Release notes - version 3.0.1
 * Javassist added back as a dependency [WW-3544](https://issues.apache.org/jira/browse/WW-3544)
