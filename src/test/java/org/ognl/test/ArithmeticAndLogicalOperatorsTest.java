// --------------------------------------------------------------------------
// Copyright (c) 2004, Drew Davidson and Luke Blanshard
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// Neither the name of the Drew Davidson nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
// OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
// AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
// THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
// DAMAGE.
// --------------------------------------------------------------------------
package org.ognl.test;

import junit.framework.TestSuite;

import java.math.BigDecimal;

public class ArithmeticAndLogicalOperatorsTest extends OgnlTestCase {

    // Basic enumeration
    public enum EnumNoBody {
        ENUM1, ENUM2
    }

    // Enumeration whose elements have (empty) bodies
    public enum EnumEmptyBody {
        ENUM1 {}, ENUM2 {}
    }

    // Enumeration whose elements have bodies
    public enum EnumBasicBody {
        ENUM1 {
            public final Integer value() {
                return Integer.valueOf(10);
            }
        },
        ENUM2 {
            public final Integer value() {
                return Integer.valueOf(20);
            }
        }
    }

    protected static final String FULLY_QUALIFIED_CLASSNAME = ArithmeticAndLogicalOperatorsTest.class.getName();

    private static final Object[][] TESTS = {
            // Double-valued arithmetic expressions
            {"-1d", (double) -1},
            {"+1d", 1.0},
            {"--1f", 1.0},
            {"2*2.0", 4.0},
            {"5/2.", 2.5},
            {"5+2D", 7.0},
            {"5f-2F", 3.0},
            {"5.+2*3", 11.0},
            {"(5.+2)*3", 21.0},

            // BigDecimal-valued arithmetic expressions
            {"-1b", -1},
            {"+1b", 1},
            {"--1b", 1},
            {"2*2.0b", 4.0},
            {"5/2.B", 2},
            {"5.0B/2", 2.5},
            {"5+2b", 7},
            {"5-2B", 3},
            {"5.+2b*3", 11.0},
            {"(5.+2b)*3", 21.0},

            // Integer-valued arithmetic expressions
            {"-1", -1},
            {"+1", 1},
            {"--1", 1},
            {"2*2", 4},
            {"5/2", 2},
            {"5+2", 7},
            {"5-2", 3},
            {"5+2*3", 11},
            {"(5+2)*3", 21},
            {"~1", ~1},
            {"5%2", 1},
            {"5<<2", 20},
            {"5>>2", 1},
            {"5>>1+1", 1},
            {"-5>>>2", -5 >>> 2},
            {"-5L>>>2", -5L >>> 2},
            {"5. & 3", 1L},
            {"5 ^3", 6},
            {"5l&3|5^3", 7L},
            {"5&(3|5^3)", 5L},
            {"true ? 1 : 1/0", 1},

            // BigInteger-valued arithmetic expressions
            {"-1h", -1},
            {"+1H", 1},
            {"--1h", 1},
            {"2h*2", 4},
            {"5/2h", 2},
            {"5h+2", 7},
            {"5-2h", 3},
            {"5+2H*3", 11},
            {"(5+2H)*3", 21},
            {"~1h", ~1},
            {"5h%2", 1},
            {"5h<<2", 20},
            {"5h>>2", 1},
            {"5h>>1+1", 1},
            {"-5h>>>2", -2},
            {"5.b & 3", 1L},
            {"5h ^3", 6},
            {"5h&3|5^3", 7L},
            {"5H&(3|5^3)", 5L},

            // Logical expressions
            {"!1", Boolean.FALSE},
            {"!null", Boolean.TRUE},
            {"5<2", Boolean.FALSE},
            {"5>2", Boolean.TRUE},
            {"5<=5", Boolean.TRUE},
            {"5>=3", Boolean.TRUE},
            {"5<-5>>>2", Boolean.TRUE},
            {"5==5.0", Boolean.TRUE},
            {"5!=5.0", Boolean.FALSE},
            {"null in {true,false,null}", Boolean.TRUE},
            {"null not in {true,false,null}", Boolean.FALSE},
            {"null in {true,false,null}.toArray()", Boolean.TRUE, new Object[]{}},
            {"5 in {true,false,null}", Boolean.FALSE},
            {"5 not in {true,false,null}", Boolean.TRUE},
            {"5 instanceof java.lang.Integer", Boolean.TRUE},
            {"5. instanceof java.lang.Integer", Boolean.FALSE},
            {"!false || true", Boolean.TRUE},
            {"!(true && true)", Boolean.FALSE},
            {"(1 > 0 && true) || 2 > 0", Boolean.TRUE},

            // Logical expressions (string versions)
            {"2 or 0", 2},
            {"1 and 0", 0},
            {"1 bor 0", 1},
            {"true && 12", 12},
            {"1 xor 0", 1}, {"1 band 0", 0L}, {"1 eq 1", Boolean.TRUE},
            {"1 neq 1", Boolean.FALSE}, {"1 lt 5", Boolean.TRUE}, {"1 lte 5", Boolean.TRUE},
            {"1 gt 5", Boolean.FALSE}, {"1 gte 5", Boolean.FALSE}, {"1 lt 5", Boolean.TRUE},
            {"1 shl 2", 4}, {"4 shr 2", 1}, {"4 ushr 2", 1},
            {"not null", Boolean.TRUE}, {"not 1", Boolean.FALSE},

            // Equality on identity; Object does not implement Comparable
            {"#a = new java.lang.Object(), #a == #a", Boolean.TRUE},
            {"#a = new java.lang.Object(), #b = new java.lang.Object(), #a == #b", Boolean.FALSE},

            // Comparable and non-Comparable
            {"#a = new java.lang.Object(), #a == ''", Boolean.FALSE},
            {"#a = new java.lang.Object(), '' == #a", Boolean.FALSE},

            {"#x > 0", Boolean.TRUE},
            {"#x < 0", Boolean.FALSE},
            {"#x == 0", Boolean.FALSE},
            {"#x == 1", Boolean.TRUE},
            {"0 > #x", Boolean.FALSE},
            {"0 < #x", Boolean.TRUE},
            {"0 == #x", Boolean.FALSE},
            {"1 == #x", Boolean.TRUE},
            {"\"1\" > 0", Boolean.TRUE},
            {"\"1\" < 0", Boolean.FALSE},
            {"\"1\" == 0", Boolean.FALSE},
            {"\"1\" == 1", Boolean.TRUE},
            {"0 > \"1\"", Boolean.FALSE},
            {"0 < \"1\"", Boolean.TRUE},
            {"0 == \"1\"", Boolean.FALSE},
            {"1 == \"1\"", Boolean.TRUE},
            {"#x + 1", "11"},
            {"1 + #x", "11"},
            {"#y == 1", Boolean.TRUE},
            {"#y == \"1\"", Boolean.TRUE},
            {"#y + \"1\"", "11"},
            {"\"1\" + #y", "11"},

            // Enumerated type equality and inequality comparisons (with and without element bodies, reversing order for completeness).
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", Boolean.FALSE},

            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", Boolean.FALSE},

            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", Boolean.FALSE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", Boolean.TRUE},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", Boolean.FALSE},

            // As per JDK JavaDocs it is only possible to compare Enum elements of the same type.  Attempting to compare different types
            //   will normally result in ClassCastExceptions.  However, OGNL should avoid that and produce an IllegalArgumentException instead.
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", IllegalArgumentException.class},

            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", IllegalArgumentException.class},

            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", IllegalArgumentException.class},

            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", IllegalArgumentException.class},
            {"@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", IllegalArgumentException.class}
    };

    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (Object[] test : TESTS) {
            result.addTest(
                    new ArithmeticAndLogicalOperatorsTest(
                            test[0] + " (" + test[1] + ")",
                            test.length == 3 ? test[2] : null,
                            (String) test[0],
                            test[1])
            );
        }
        return result;
    }

    public ArithmeticAndLogicalOperatorsTest() {
        super();
    }

    public ArithmeticAndLogicalOperatorsTest(String name) {
        super(name);
    }

    public ArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString, Object expectedResult,
                                             Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString, Object expectedResult,
                                             Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }

    protected void setUp() {
        super.setUp();
        _context.put("x", "1");
        _context.put("y", new BigDecimal(1));
    }
}
