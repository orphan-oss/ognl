# Object-Graph Navigation Language - OGNL

It's a simple Expression Language (EL) for Java, the code base sometime ago  was migrated to the [Apache](http://incubator.apache.org/projects/ognl.html)
and this source code is used just to maintenance 3.x branch. The new version from Apache will start from 4.x


## Development activity

### Release notes - version 3.0.1
 * Javassist added back as a dependency [WW-3544](https://issues.apache.org/jira/browse/WW-3544)

### Release notes - version 3.0.2
 * small fix to solve a problem with compiling under JDK5

### Release notes - version 3.0.3
 * small fix to improve performance [WW-3580](https://issues.apache.org/jira/browse/WW-3580 "Critical performance issue in production environment as thread dumps are leading to OGNL 3.0 thread blocking! Website could be backed out!")

### Release notes - version 3.0.4
 * Adds possibility to discover eval chain

### Release notes - version 3.0.5
 * partially reverts previous changes to allow OGNL to work in environment with Security Manager enabled [WW-3746](https://issues.apache.org/jira/browse/WW-3746 "Struts 2.3.1.1 OGNL crashes on WebSphere 7")
