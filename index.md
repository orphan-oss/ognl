---
layout: default
title: OGNL main page
---

# Object-Graph Navigation Language - OGNL

OGNL stands for Object-Graph Navigation Language; it is an expression language for getting and setting properties 
of Java objects. You use the same expression for both getting and setting the value of a property.

The ognl.Ognl class contains convenience methods for evaluating OGNL expressions. You can do this in two stages, 
parsing an expression into an internal form and then using that internal form to either set or get the value 
of a property; or you can do it in a single stage, and get or set a property using the String form of the expression 
directly.

OGNL started out as a way to set up associations between UI components and controllers using property names. 
As the desire for more complicated associations grew, Drew Davidson created what he called KVCL, for Key-Value 
Coding Language, egged on by Luke Blanshard. Luke then reimplemented the language using ANTLR, came up with 
the new name, and, egged on by Drew, filled it out to its current state. Later on Luke again reimplemented the language 
using JavaCC. Further maintenance on all the code is done by Drew (with spiritual guidance from Luke).

We pronounce OGNL as a word, like the last syllables of a drunken pronunciation of "orthogonal."

[Github Repository](https://github.com/orphan-oss/ognl)
[Language Guide](language-guide)
[Developer Guide](developer-guide)
