# Object-Graph Navigation Language - OGNL

[![Build Status](https://travis-ci.org/jkuhnert/ognl.svg?branch=master)](https://travis-ci.org/jkuhnert/ognl)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ognl/ognl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ognl/ognl/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

It's a simple Expression Language (EL) for Java, the code base sometime ago was migrated to the [Apache commons ognl](http://commons.apache.org/ognl/)
and this source code is used just to maintenance 3.x branch. The new version from Apache will start from 4.x

## New! Apache commons ognl project

Out of incubator and now officially found here [http://commons.apache.org/ognl/](http://commons.apache.org/ognl/)

## FAQ
 - How to run OGNL in Google AppEngine?
   - you need to tell OGNL to not do security manager permission checks, which will fail since GAE has a security manager and you don't have the ability to add the OGNL-specific permissions. Therefore, somewhere in your initialization code, add this `OgnlRuntime.setSecurityManager(null);`

## Development activity

### Release notes - version 3.1.11
 * fixes issue with returning default methods from interfaces implemented by parent class [#30](../../issues/30) -
   thanks to Vlastimil Dolejš

### Release notes - version 3.1.10, 3.0.19
 * Does not treat negative numbers as an arithmetic operation [#28](../../issues/28) -
   thanks to Łukasz Lenart

### Release notes - version 3.1.9, 3.0.18
 * Drops access to `_memeberAccess` field via a magic key -
   thanks to Łukasz Lenart

### Release notes - version 3.1.8, 3.0.17, 3.0.6.2
 * Exposes flags to allow check if an expression is a chain or an arithmetic operation or a simple method -
   thanks to Łukasz Lenart

### Release notes - version 3.1.6
 * fixes automatic type conversion to avoid `double boxing` [#25](../../issues/25)/[#26](../../pull/26) - 
   thanks to Christian Niessner from [secadm GmbH](http://www.secadm.de/)

### Release notes - version 3.1.5
 * fixes issue with selecting overloaded methods [#23](../../issues/23)/[#24](../../pull/24) - 
   thanks to Christian Niessner from [secadm GmbH](http://www.secadm.de/)

### Release notes - version 3.1.4
 * fixes issue with executing expressions on Java 8 plus adds an `java like` method matching [#19](../../pull/19) -
   thanks to marvkis

### Release notes - version 3.1.3, 3.0.14, 3.0.6.1
 * Exposes flag to allow check if an expression is a sequence of simple expressions -
   thanks to Łukasz Lenart

### Release notes - version 3.1.2
 * Fixes accessing statics within Enums [OGNL-158](https://issues.apache.org/jira/browse/OGNL-158) -
   thanks to Aleksandr Mashchenko

### Release notes - version 3.1.1, 3.0.13
 * OgnlRuntime.invokeMethod can throw IllegalAccessException because of hash collisions was fixed [OGNL-252](https://issues.apache.org/jira/browse/OGNL-252) - 
   thanks to Carlos Saona

### Release notes - version 3.1
 * support for boolean expression in Strings was added, this can break backward compatibility [#8](../../issues/8) - 
   thanks to Daniel Fernández

### Release notes - version 3.0.12
 * lots of optimizations which should improve overall performance [#9](../../pull/9), [#10](../../pull/10), [#11](../../pull/11), [#12](../../pull/12) - 
   thanks to Daniel Fernández
 * OGNL supports default methods in interfaces (Java 8) [OGNL-249](https://issues.apache.org/jira/browse/OGNL-249)

### Release notes - version 3.0.11
 * fixes problem with cacheKey too expensive to create [WW-4485 ](https://issues.apache.org/jira/browse/WW-4485 ) -
   thanks to Jasper Rosenberg

### Release notes - version 3.0.10
 * regression bug in ognl for "is..." property getters [WW-4462](https://issues.apache.org/jira/browse/WW-4462) -
   if expression doesn't end with `()` is considered as a name of property a not the method itself
   thanks to Jasper Rosenberg

### Release notes - version 3.0.9
 * replaced IntHashMap with ConcurrentMap to avoid deadlocks [WW-4451](https://issues.apache.org/jira/browse/WW-4451) -
   thanks to Jasper Rosenberg

### Release notes - version 3.0.8
 * added better capitalization logic for methods [WW-3909](https://issues.apache.org/jira/browse/WW-3909) -
   thanks to Iwen.ma

### Release notes - version 3.0.7
  * uses better method to calculate method's cache key [WW-4113](https://issues.apache.org/jira/browse/WW-4113) -
    thanks to Kevin Su

### Release notes - version 3.0.6
 * important performance improvement [OGNL-224](https://issues.apache.org/jira/browse/OGNL-224) -
   thanks to Pelladi Gabor
 * race condition fix [OGNL-226](https://issues.apache.org/jira/browse/OGNL-226) - thanks to Johno Crawford

### Release notes - version 3.0.5
 * partially reverts previous changes to allow OGNL to work in environment with Security Manager enabled
   [WW-3746](https://issues.apache.org/jira/browse/WW-3746)

### Release notes - version 3.0.4
 * Adds possibility to discover eval chain

### Release notes - version 3.0.3
 * small fix to improve performance [WW-3580](https://issues.apache.org/jira/browse/WW-3580)

### Release notes - version 3.0.2
 * small fix to solve a problem with compiling under JDK5

### Release notes - version 3.0.1
 * Javassist added back as a dependency [WW-3544](https://issues.apache.org/jira/browse/WW-3544)
