# Version Notes

## Release notes - version 3.1.17, 3.2.5
 * supports concurrency in `DefaultClassResolver` [#46](../../../pull/46) -
   thanks to kazuki43zoo! 

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
