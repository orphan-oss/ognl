# Object-Graph Navigation Language - OGNL

[![Build Status](https://travis-ci.org/jkuhnert/ognl.svg?branch=master)](https://travis-ci.org/jkuhnert/ognl)

It's a simple Expression Language (EL) for Java, the code base sometime ago was migrated to the [Apache commons ognl](http://commons.apache.org/ognl/)
and this source code is used just to maintenance 3.x branch. The new version from Apache will start from 4.x

## New! Apache commons ognl project

Out of incubator and now officially found here [http://commons.apache.org/ognl/](http://commons.apache.org/ognl/)

## Development activity

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
