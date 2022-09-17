# Object-Graph Navigation Language - OGNL

[![Java CI](https://github.com/orphan-oss/ognl/actions/workflows/maven.yml/badge.svg)](https://github.com/orphan-oss/ognl/actions/workflows/maven.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=orphan-oss_ognl&metric=coverage)](https://sonarcloud.io/summary/new_code?id=orphan-oss_ognl)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ognl/ognl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ognl/ognl/)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/6490/badge)](https://bestpractices.coreinfrastructure.org/projects/6490)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Description

OGNL stands for Object-Graph Navigation Language; it is an expression language for getting and setting 
properties of Java objects. You use the same expression for both getting and setting the value of a property.

The `ognl.Ognl` class contains convenience methods for evaluating OGNL expressions. You can do this in two stages, parsing 
an expression into an internal form and then using that internal form to either set or get the value of a property;
or you can do it in a single stage, and get or set a property using the String form of the expression directly.

OGNL started out as a way to set up associations between UI components and controllers using property names. As the desire 
for more complicated associations grew, Drew Davidson created what he called KVCL, for Key-Value Coding Language, egged 
on by Luke Blanshard. Luke then reimplemented the language using ANTLR, came up with the new name, and, egged on by Drew, 
filled it out to its current state. Later on Luke again reimplemented the language using JavaCC. Further maintenance 
on all the code is done by Drew (with spiritual guidance from Luke).

We pronounce OGNL as a word, like the last syllables of a drunken pronunciation of "orthogonal."

 - [Language Guide](docs/LanguageGuide.md)
 - [Developer Guide](docs/DeveloperGuide.md)
 - [Version Notes](docs/VersionNotes.md)

## Apache Commons OGNL project

Sometimes ago this project has been migrated to [Apache Commons](http://commons.apache.org/ognl/) 
with a plan to maintain it there. Right now that project is considered dead and not actively maintained. There is
no plans to release a new version under Apache Software Foundation umbrella and all the future development will happen here. 

## Commercial Support

The project maintaners are working with Tidelift to provide commercial support and invest paid working time in the improvement of the projects. 
For more information, visit the [Tidelift resources](https://tidelift.com/subscription/pkg/maven-ognl.ognl?utm_source=maven-ognl.ognl&utm_medium=referral&utm_campaign=enterprise) regarding OGNL.

## FAQ
 - How to define an AccessMember?
   - the best way is to implement your own `AccessMember` which will suite your project best, you can base on existing
     [DefaultAccessMember](src/test/java/ognl/DefaultMemberAccess.java) and adjust it to your needs.
     Since version 3.2.16 there is `AbstractAccessMemeber` which can be used a start point for your own implementation,
     see the example below:
     ```
        MemberAccess memberAccess = new AbstractMemberAccess() {
            @Override
            public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
                int modifiers = member.getModifiers();
                return Modifier.isPublic(modifiers);
            }
        };
     ```
 - How to run OGNL in Google AppEngine?
   - you need to tell OGNL to not do security manager permission checks, which will fail since GAE has a security manager 
     and you don't have the ability to add the OGNL-specific permissions. Therefore, somewhere in your initialization code, 
     add this `OgnlRuntime.setSecurityManager(null);`.
 - How to use the latest SNAPSHOT version?
   - Define OSS Sonatype repository in `~/.m2/settings.xml` as follows:
     ```xml
     <settings>
         <servers>
         ...
         </servers>
         <profiles>
             <profile>
                 <id>local</id>
                 <activation>
                     <activeByDefault>true</activeByDefault>
                 </activation>
                 <repositories>
                     <repository>
                         <id>oss-snapshots</id>
                         <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
                         <layout>default</layout>
                         <snapshots>
                             <enabled>true</enabled>
                         </snapshots>
                     </repository>
                 </repositories>
             </profile>
         </profiles>
     </settings>
     ```
     and now you can use SNAPSHOT version of OGNL in your project,
