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

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.Root;
import ognl.test.util.OgnlTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Tests that interpreted and compiled evaluation modes produce identical results.
 * Addresses <a href="https://github.com/orphan-oss/ognl/issues/18">Issue #18</a>.
 */
class DualModeEvaluationTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
    }

    @Nested
    class Constants {

        @Test
        void integerConstant() throws Exception {
            OgnlTestUtil.assertBothModes("12345", context, root, 12345);
        }

        @Test
        void longConstant() throws Exception {
            OgnlTestUtil.assertBothModes("1234L", context, root, 1234L);
        }

        @Test
        void doubleConstant() throws Exception {
            OgnlTestUtil.assertBothModes("12.34", context, root, 12.34);
        }

        @Test
        void floatConstant() throws Exception {
            OgnlTestUtil.assertBothModes("12.34f", context, root, 12.34F);
        }

        @Test
        void trueConstant() throws Exception {
            OgnlTestUtil.assertBothModes("true", context, root, Boolean.TRUE);
        }

        @Test
        void falseConstant() throws Exception {
            OgnlTestUtil.assertBothModes("false", context, root, Boolean.FALSE);
        }

        @Test
        void nullConstant() throws Exception {
            OgnlTestUtil.assertBothModes("null", context, root, null);
        }

        @Test
        void stringConstant() throws Exception {
            OgnlTestUtil.assertBothModes("\"hello world\"", context, root, "hello world");
        }

        @Test
        void charConstant() throws Exception {
            OgnlTestUtil.assertBothModes("'x'", context, root, 'x');
        }

        @Test
        void hexConstant() throws Exception {
            OgnlTestUtil.assertBothModes("0x100", context, root, 256);
        }

        @Test
        void octalConstant() throws Exception {
            OgnlTestUtil.assertBothModes("01000", context, root, 512);
        }
    }

    @Nested
    class BigDecimalArithmetic {

        @Test
        void negation() throws Exception {
            OgnlTestUtil.assertBothModes("-1b", context, root, BigDecimal.valueOf(-1));
        }

        @Test
        void unaryPlus() throws Exception {
            OgnlTestUtil.assertBothModes("+1b", context, root, BigDecimal.valueOf(1));
        }

        @Test
        void doubleNegation() throws Exception {
            OgnlTestUtil.assertBothModes("--1b", context, root, BigDecimal.valueOf(1));
        }

        @Test
        void multiplyWithDouble() throws Exception {
            OgnlTestUtil.assertBothModes("2*2.0b", context, root, BigDecimal.valueOf(4.0));
        }

        @Test
        void divideWithBigDecimalSuffix() throws Exception {
            OgnlTestUtil.assertBothModes("5/2.B", context, root, BigDecimal.valueOf(2));
        }

        @Test
        void divideWithBigDecimalNumerator() throws Exception {
            OgnlTestUtil.assertBothModes("5.0B/2", context, root, BigDecimal.valueOf(2.5));
        }

        @Test
        void addition() throws Exception {
            OgnlTestUtil.assertBothModes("5+2b", context, root, BigDecimal.valueOf(7));
        }

        @Test
        void subtraction() throws Exception {
            OgnlTestUtil.assertBothModes("5-2B", context, root, BigDecimal.valueOf(3));
        }
    }

    @Nested
    class BigIntegerArithmetic {

        @Test
        void negation() throws Exception {
            OgnlTestUtil.assertBothModes("-1h", context, root, BigInteger.valueOf(-1));
        }

        @Test
        void unaryPlus() throws Exception {
            OgnlTestUtil.assertBothModes("+1H", context, root, BigInteger.valueOf(1));
        }

        @Test
        void doubleNegation() throws Exception {
            OgnlTestUtil.assertBothModes("--1h", context, root, BigInteger.valueOf(1));
        }

        @Test
        void multiply() throws Exception {
            OgnlTestUtil.assertBothModes("2h*2", context, root, BigInteger.valueOf(4));
        }

        @Test
        void divide() throws Exception {
            OgnlTestUtil.assertBothModes("5/2h", context, root, BigInteger.valueOf(2));
        }

        @Test
        void addition() throws Exception {
            OgnlTestUtil.assertBothModes("5h+2", context, root, BigInteger.valueOf(7));
        }

        @Test
        void subtraction() throws Exception {
            OgnlTestUtil.assertBothModes("5-2h", context, root, BigInteger.valueOf(3));
        }
    }

    @Nested
    class IntegerArithmetic {

        @Test
        void negation() throws Exception {
            OgnlTestUtil.assertBothModes("-1", context, root, -1);
        }

        @Test
        void unaryPlus() throws Exception {
            OgnlTestUtil.assertBothModes("+1", context, root, 1);
        }

        @Test
        void doubleNegation() throws Exception {
            OgnlTestUtil.assertBothModes("--1", context, root, 1);
        }

        @Test
        void multiply() throws Exception {
            OgnlTestUtil.assertBothModes("2*2", context, root, 4);
        }

        @Test
        void divide() throws Exception {
            OgnlTestUtil.assertBothModes("5/2", context, root, 2);
        }

        @Test
        void addition() throws Exception {
            OgnlTestUtil.assertBothModes("5+2", context, root, 7);
        }

        @Test
        void subtraction() throws Exception {
            OgnlTestUtil.assertBothModes("5-2", context, root, 3);
        }

        @Test
        void precedence() throws Exception {
            OgnlTestUtil.assertBothModes("5+2*3", context, root, 11);
        }

        @Test
        void parentheses() throws Exception {
            OgnlTestUtil.assertBothModes("(5+2)*3", context, root, 21);
        }

        @Test
        void modulus() throws Exception {
            OgnlTestUtil.assertBothModes("5%2", context, root, 1);
        }
    }

    @Nested
    class DoubleArithmetic {

        @Test
        void negation() throws Exception {
            OgnlTestUtil.assertBothModes("-1d", context, root, -1d);
        }

        @Test
        void unaryPlus() throws Exception {
            OgnlTestUtil.assertBothModes("+1d", context, root, 1d);
        }

        @Test
        void multiplyWithDouble() throws Exception {
            OgnlTestUtil.assertBothModes("2*2.0", context, root, 4.0d);
        }

        @Test
        void divideWithTrailingDot() throws Exception {
            OgnlTestUtil.assertBothModes("5/2.", context, root, 2.5d);
        }

        @Test
        void addition() throws Exception {
            OgnlTestUtil.assertBothModes("5+2D", context, root, 7d);
        }

        @Test
        void floatSubtraction() throws Exception {
            OgnlTestUtil.assertBothModes("5f-2F", context, root, 3.0f);
        }
    }

    @Nested
    class BitwiseOperations {

        @Test
        void bitwiseNot() throws Exception {
            OgnlTestUtil.assertBothModes("~1", context, root, ~1);
        }

        @Test
        void leftShift() throws Exception {
            OgnlTestUtil.assertBothModes("5<<2", context, root, 20);
        }

        @Test
        void rightShift() throws Exception {
            OgnlTestUtil.assertBothModes("5>>2", context, root, 1);
        }

        @Test
        void unsignedRightShift() throws Exception {
            OgnlTestUtil.assertBothModes("-5>>>2", context, root, -5 >>> 2);
        }

        @Test
        void bitwiseAndWithDouble() throws Exception {
            OgnlTestUtil.assertBothModes("5. & 3", context, root, 1.0);
        }

        @Test
        void bitwiseXor() throws Exception {
            OgnlTestUtil.assertBothModes("5 ^3", context, root, 6);
        }

        @Test
        void bitwiseOrWithLong() throws Exception {
            OgnlTestUtil.assertBothModes("5l&3|5^3", context, root, 7L);
        }

        @Test
        void bitwiseAndGrouped() throws Exception {
            OgnlTestUtil.assertBothModes("5&(3|5^3)", context, root, 5);
        }

        @Test
        void bigIntegerBitwiseNot() throws Exception {
            OgnlTestUtil.assertBothModes("~1h", context, root, BigInteger.valueOf(~1));
        }

        @Test
        void bigIntegerLeftShift() throws Exception {
            OgnlTestUtil.assertBothModes("5h<<2", context, root, BigInteger.valueOf(20));
        }

        @Test
        void bigIntegerRightShift() throws Exception {
            OgnlTestUtil.assertBothModes("5h>>2", context, root, BigInteger.valueOf(1));
        }
    }

    @Nested
    class LogicalExpressions {

        @Test
        void logicalNot() throws Exception {
            OgnlTestUtil.assertBothModes("!1", context, root, Boolean.FALSE);
        }

        @Test
        void logicalNotNull() throws Exception {
            OgnlTestUtil.assertBothModes("!null", context, root, Boolean.TRUE);
        }

        @Test
        void lessThan() throws Exception {
            OgnlTestUtil.assertBothModes("5<2", context, root, Boolean.FALSE);
        }

        @Test
        void greaterThan() throws Exception {
            OgnlTestUtil.assertBothModes("5>2", context, root, Boolean.TRUE);
        }

        @Test
        void lessOrEqual() throws Exception {
            OgnlTestUtil.assertBothModes("5<=5", context, root, Boolean.TRUE);
        }

        @Test
        void greaterOrEqual() throws Exception {
            OgnlTestUtil.assertBothModes("5>=3", context, root, Boolean.TRUE);
        }

        @Test
        void equality() throws Exception {
            OgnlTestUtil.assertBothModes("5==5.0", context, root, Boolean.TRUE);
        }

        @Test
        void inequality() throws Exception {
            OgnlTestUtil.assertBothModes("5!=5.0", context, root, Boolean.FALSE);
        }

        @Test
        void ternary() throws Exception {
            OgnlTestUtil.assertBothModes("true ? 1 : 1/0", context, root, 1);
        }
    }

    @Nested
    class PropertyAccess {

        @Test
        void simpleProperty() throws Exception {
            OgnlTestUtil.assertBothModesMatch("index", context, root);
        }

        @Test
        void nestedProperty() throws Exception {
            OgnlTestUtil.assertBothModesMatch("bean2.id", context, root);
        }

        @Test
        void arrayLength() throws Exception {
            OgnlTestUtil.assertBothModesMatch("array.length", context, root);
        }

        @Test
        void arrayIndex() throws Exception {
            OgnlTestUtil.assertBothModesMatch("array[0]", context, root);
        }

        @Test
        void nullObject() throws Exception {
            OgnlTestUtil.assertBothModes("nullObject", context, root, null);
        }

        @Test
        void booleanProperty() throws Exception {
            OgnlTestUtil.assertBothModesMatch("disabled", context, root);
        }

        @Test
        void intProperty() throws Exception {
            OgnlTestUtil.assertBothModesMatch("intValue", context, root);
        }

        @Test
        void staticField() throws Exception {
            OgnlTestUtil.assertBothModesMatch("@ognl.test.objects.Root@STATIC_INT", context, root);
        }
    }

    @Nested
    class MethodCalls {

        @Test
        void noArgMethod() throws Exception {
            OgnlTestUtil.assertBothModesMatch("getIndex()", context, root);
        }

        @Test
        void stringArgMethod() throws Exception {
            OgnlTestUtil.assertBothModes("getCurrentClass(\"Test\")", context, root, "Test stop");
        }

        @Test
        void toStringOnInteger() throws Exception {
            OgnlTestUtil.assertBothModes("index.toString()", context, root, "1");
        }

        @Test
        void staticMethod() throws Exception {
            OgnlTestUtil.assertBothModesMatch("@ognl.test.objects.Root@getStaticInt()", context, root);
        }
    }

    @Nested
    class NullHandling {

        @Test
        void nullEqualityToNull() throws Exception {
            OgnlTestUtil.assertBothModes("null == null", context, root, Boolean.TRUE);
        }

        @Test
        void nullInequalityToValue() throws Exception {
            OgnlTestUtil.assertBothModes("null != 1", context, root, Boolean.TRUE);
        }

        @Test
        void nullObjectProperty() throws Exception {
            OgnlTestUtil.assertBothModes("nullObject", context, root, null);
        }
    }

    @Nested
    class InstanceOf {

        @Test
        void integerInstanceOf() throws Exception {
            OgnlTestUtil.assertBothModes("5 instanceof java.lang.Integer", context, root, Boolean.TRUE);
        }

        @Test
        void doubleNotInstanceOfInteger() throws Exception {
            OgnlTestUtil.assertBothModes("5. instanceof java.lang.Integer", context, root, Boolean.FALSE);
        }
    }
}
