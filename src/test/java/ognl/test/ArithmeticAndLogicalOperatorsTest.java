/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ognl.test;

import junit.framework.TestSuite;

import java.math.BigDecimal;

public class ArithmeticAndLogicalOperatorsTest extends OgnlTestCase {

    public enum EnumNoBody {ENUM1, ENUM2;}

    ;  // Basic enumeration

    public enum EnumEmptyBody {ENUM1 {}, ENUM2 {};}

    ;  // Enumeration whose elements have (empty) bodies

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
        };
    }

    ;  // Enumeration whose elements have bodies
    protected static final String FULLY_QUALIFIED_CLASSNAME = ArithmeticAndLogicalOperatorsTest.class.getName();

    private static Object[][] TESTS = {
            // Double-valued arithmetic expressions
            {"-1d", new Double(-1)},
            {"+1d", new Double(1)},
            {"--1f", new Double(1)},
            {"2*2.0", new Double(4.0)},
            {"5/2.", new Double(2.5)},
            {"5+2D", new Double(7)},
            {"5f-2F", new Double(3.0)},
            {"5.+2*3", new Double(11)},
            {"(5.+2)*3", new Double(21)},

            // BigDecimal-valued arithmetic expressions
            {"-1b", new Integer(-1)},
            {"+1b", new Integer(1)},
            {"--1b", new Integer(1)},
            {"2*2.0b", new Double(4.0)},
            {"5/2.B", new Integer(2)},
            {"5.0B/2", new Double(2.5)},
            {"5+2b", new Integer(7)},
            {"5-2B", new Integer(3)},
            {"5.+2b*3", new Double(11)},
            {"(5.+2b)*3", new Double(21)},

            // Integer-valued arithmetic expressions
            {"-1", new Integer(-1)},
            {"+1", new Integer(1)},
            {"--1", new Integer(1)},
            {"2*2", new Integer(4)},
            {"5/2", new Integer(2)},
            {"5+2", new Integer(7)},
            {"5-2", new Integer(3)},
            {"5+2*3", new Integer(11)},
            {"(5+2)*3", new Integer(21)},
            {"~1", new Integer(~1)},
            {"5%2", new Integer(1)},
            {"5<<2", new Integer(20)},
            {"5>>2", new Integer(1)},
            {"5>>1+1", new Integer(1)},
            {"-5>>>2", new Integer(-5 >>> 2)},
            {"-5L>>>2", new Long(-5L >>> 2)},
            {"5. & 3", new Long(1)},
            {"5 ^3", new Integer(6)},
            {"5l&3|5^3", new Long(7)},
            {"5&(3|5^3)", new Long(5)},
            {"true ? 1 : 1/0", new Integer(1)},

            // BigInteger-valued arithmetic expressions
            {"-1h", Integer.valueOf(-1)},
            {"+1H", Integer.valueOf(1)},
            {"--1h", Integer.valueOf(1)},
            {"2h*2", Integer.valueOf(4)},
            {"5/2h", Integer.valueOf(2)},
            {"5h+2", Integer.valueOf(7)},
            {"5-2h", Integer.valueOf(3)},
            {"5+2H*3", Integer.valueOf(11)},
            {"(5+2H)*3", Integer.valueOf(21)},
            {"~1h", Integer.valueOf(~1)},
            {"5h%2", Integer.valueOf(1)},
            {"5h<<2", Integer.valueOf(20)},
            {"5h>>2", Integer.valueOf(1)},
            {"5h>>1+1", Integer.valueOf(1)},
            {"-5h>>>2", Integer.valueOf(-2)},
            {"5.b & 3", new Long(1)},
            {"5h ^3", Integer.valueOf(6)},
            {"5h&3|5^3", new Long(7)},
            {"5H&(3|5^3)", new Long(5)},

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
            {"null in {true,false,null}.toArray()", Boolean.TRUE},
            {"5 in {true,false,null}", Boolean.FALSE},
            {"5 not in {true,false,null}", Boolean.TRUE},
            {"5 instanceof java.lang.Integer", Boolean.TRUE},
            {"5. instanceof java.lang.Integer", Boolean.FALSE},
            {"!false || true", Boolean.TRUE},
            {"!(true && true)", Boolean.FALSE},
            {"(1 > 0 && true) || 2 > 0", Boolean.TRUE},

            // Logical expressions (string versions)
            {"2 or 0", Integer.valueOf(2)},
            {"1 and 0", Integer.valueOf(0)},
            {"1 bor 0", new Integer(1)},
            {"true && 12", Integer.valueOf(12)},
            {"1 xor 0", new Integer(1)}, {"1 band 0", new Long(0)}, {"1 eq 1", Boolean.TRUE},
            {"1 neq 1", Boolean.FALSE}, {"1 lt 5", Boolean.TRUE}, {"1 lte 5", Boolean.TRUE},
            {"1 gt 5", Boolean.FALSE}, {"1 gte 5", Boolean.FALSE}, {"1 lt 5", Boolean.TRUE},
            {"1 shl 2", new Integer(4)}, {"4 shr 2", new Integer(1)}, {"4 ushr 2", new Integer(1)},
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

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            result.addTest(new ArithmeticAndLogicalOperatorsTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", null,
                    (String) TESTS[i][0], TESTS[i][1]));
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
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

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    protected void setUp() {
        super.setUp();
        _context.put("x", "1");
        _context.put("y", new BigDecimal(1));
    }
}
