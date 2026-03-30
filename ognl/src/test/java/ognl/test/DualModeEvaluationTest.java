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
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that interpreted and compiled evaluation modes produce identical results.
 * Addresses <a href="https://github.com/orphan-oss/ognl/issues/18">Issue #18</a>.
 *
 * <p>Tests marked {@code @Disabled} document known divergences between the interpreted and compiled
 * evaluation paths. The compiler generates Java source code (via javassist), which cannot represent
 * BigDecimal/BigInteger arithmetic using operators, and has other limitations around instanceof
 * and method calls on auto-boxed primitives.</p>
 */
class DualModeEvaluationTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
    }

    private Object getValueInterpreted(String expression) throws OgnlException {
        Object tree = Ognl.parseExpression(expression);
        return ((Node) tree).getValue(context.withRoot(root), root);
    }

    @SuppressWarnings("unchecked")
    private Object getValueCompiled(String expression, OgnlContext ctx) throws Exception {
        Node node = Ognl.compileExpression(ctx, root, expression);
        return node.getAccessor().get(ctx, root);
    }

    private OgnlContext freshCompiledContext() {
        OgnlContext ctx = Ognl.createDefaultContext(root, context.getMemberAccess());
        ctx.setValues(context.getValues());
        return ctx;
    }

    private void assertBothModes(String expression, Object expected) throws Exception {
        assertEquals(expected, getValueInterpreted(expression),
                "Interpreted mode failed for: " + expression);
        assertEquals(expected, getValueCompiled(expression, freshCompiledContext()),
                "Compiled mode failed for: " + expression);
    }

    private void assertBothModesMatch(String expression) throws Exception {
        Object interpreted = getValueInterpreted(expression);
        Object compiled = getValueCompiled(expression, freshCompiledContext());
        assertEquals(interpreted, compiled,
                "Modes diverged for: " + expression
                        + "\n  interpreted: " + interpreted + " (" + (interpreted != null ? interpreted.getClass().getName() : "null") + ")"
                        + "\n  compiled:    " + compiled + " (" + (compiled != null ? compiled.getClass().getName() : "null") + ")");
    }

    /**
     * These constant tests intentionally overlap with {@code ConstantTest} — that class only exercises
     * the interpreted path, while these validate that the compiled path produces identical results.
     */
    @Nested
    class Constants {

        @Test
        void integerConstant() throws Exception {
            assertBothModes("12345", 12345);
        }

        @Test
        void longConstant() throws Exception {
            assertBothModes("1234L", 1234L);
        }

        @Test
        void doubleConstant() throws Exception {
            assertBothModes("12.34", 12.34);
        }

        @Test
        void floatConstant() throws Exception {
            assertBothModes("12.34f", 12.34F);
        }

        @Test
        void trueConstant() throws Exception {
            assertBothModes("true", Boolean.TRUE);
        }

        @Test
        void falseConstant() throws Exception {
            assertBothModes("false", Boolean.FALSE);
        }

        @Test
        void nullConstant() throws Exception {
            assertBothModes("null", null);
        }

        @Test
        void stringConstant() throws Exception {
            assertBothModes("\"hello world\"", "hello world");
        }

        @Test
        void charConstant() throws Exception {
            assertBothModes("'x'", 'x');
        }

        @Test
        void hexConstant() throws Exception {
            assertBothModes("0x100", 256);
        }

        @Test
        void octalConstant() throws Exception {
            assertBothModes("01000", 512);
        }
    }

    @Nested
    @Disabled("Compiler does not support BigDecimal: Java operators (+, -, *, /) cannot be applied to BigDecimal objects in generated source")
    class BigDecimalArithmetic {

        @Test
        void negation() throws Exception {
            assertBothModes("-1b", BigDecimal.valueOf(-1));
        }

        @Test
        void unaryPlus() throws Exception {
            assertBothModes("+1b", BigDecimal.valueOf(1));
        }

        @Test
        void doubleNegation() throws Exception {
            assertBothModes("--1b", BigDecimal.valueOf(1));
        }

        @Test
        void multiplyWithDouble() throws Exception {
            assertBothModes("2*2.0b", BigDecimal.valueOf(4.0));
        }

        @Test
        void divideWithBigDecimalSuffix() throws Exception {
            assertBothModes("5/2.B", BigDecimal.valueOf(2));
        }

        @Test
        void divideWithBigDecimalNumerator() throws Exception {
            assertBothModes("5.0B/2", BigDecimal.valueOf(2.5));
        }

        @Test
        void addition() throws Exception {
            assertBothModes("5+2b", BigDecimal.valueOf(7));
        }

        @Test
        void subtraction() throws Exception {
            assertBothModes("5-2B", BigDecimal.valueOf(3));
        }
    }

    @Nested
    @Disabled("Compiler does not support BigInteger: Java operators cannot be applied to BigInteger objects in generated source")
    class BigIntegerArithmetic {

        @Test
        void negation() throws Exception {
            assertBothModes("-1h", BigInteger.valueOf(-1));
        }

        @Test
        void unaryPlus() throws Exception {
            assertBothModes("+1H", BigInteger.valueOf(1));
        }

        @Test
        void doubleNegation() throws Exception {
            assertBothModes("--1h", BigInteger.valueOf(1));
        }

        @Test
        void multiply() throws Exception {
            assertBothModes("2h*2", BigInteger.valueOf(4));
        }

        @Test
        void divide() throws Exception {
            assertBothModes("5/2h", BigInteger.valueOf(2));
        }

        @Test
        void addition() throws Exception {
            assertBothModes("5h+2", BigInteger.valueOf(7));
        }

        @Test
        void subtraction() throws Exception {
            assertBothModes("5-2h", BigInteger.valueOf(3));
        }

        @Test
        void modulus() throws Exception {
            assertBothModes("5h%2", BigInteger.valueOf(1));
        }
    }

    @Nested
    class IntegerArithmetic {

        @Test
        void negation() throws Exception {
            assertBothModes("-1", -1);
        }

        @Test
        void unaryPlus() throws Exception {
            assertBothModes("+1", 1);
        }

        @Test
        void doubleNegation() throws Exception {
            assertBothModes("--1", 1);
        }

        @Test
        void multiply() throws Exception {
            assertBothModes("2*2", 4);
        }

        @Test
        void divide() throws Exception {
            assertBothModes("5/2", 2);
        }

        @Test
        void addition() throws Exception {
            assertBothModes("5+2", 7);
        }

        @Test
        void subtraction() throws Exception {
            assertBothModes("5-2", 3);
        }

        @Test
        void precedence() throws Exception {
            assertBothModes("5+2*3", 11);
        }

        @Test
        void parentheses() throws Exception {
            assertBothModes("(5+2)*3", 21);
        }

        @Test
        void modulus() throws Exception {
            assertBothModes("5%2", 1);
        }
    }

    @Nested
    class DoubleArithmetic {

        @Test
        void negation() throws Exception {
            assertBothModes("-1d", -1d);
        }

        @Test
        void unaryPlus() throws Exception {
            assertBothModes("+1d", 1d);
        }

        @Test
        void doubleNegation() throws Exception {
            assertBothModes("--1d", 1d);
        }

        @Test
        void multiplyWithDouble() throws Exception {
            assertBothModes("2*2.0", 4.0d);
        }

        @Test
        void divideWithTrailingDot() throws Exception {
            assertBothModes("5/2.", 2.5d);
        }

        @Test
        void addition() throws Exception {
            assertBothModes("5+2D", 7d);
        }

        @Disabled("Compiler widens float to double in arithmetic expressions")
        @Test
        void floatSubtraction() throws Exception {
            assertBothModes("5f-2F", 3.0f);
        }
    }

    @Nested
    class BitwiseOperations {

        @Test
        void bitwiseNot() throws Exception {
            assertBothModes("~1", ~1);
        }

        @Test
        void leftShift() throws Exception {
            assertBothModes("5<<2", 20);
        }

        @Test
        void rightShift() throws Exception {
            assertBothModes("5>>2", 1);
        }

        @Test
        void unsignedRightShift() throws Exception {
            assertBothModes("-5>>>2", -5 >>> 2);
        }

        @Disabled("Compiler loses double type in bitwise AND, returns int instead of double")
        @Test
        void bitwiseAndWithDouble() throws Exception {
            assertBothModes("5. & 3", 1.0);
        }

        @Test
        void bitwiseXor() throws Exception {
            assertBothModes("5 ^3", 6);
        }

        @Test
        void bitwiseOrWithLong() throws Exception {
            assertBothModes("5l&3|5^3", 7L);
        }

        @Disabled("Compiler widens int to long in grouped bitwise expression")
        @Test
        void bitwiseAndGrouped() throws Exception {
            assertBothModes("5&(3|5^3)", 5);
        }

        @Disabled("Compiler does not support BigInteger bitwise operations")
        @Test
        void bigIntegerBitwiseNot() throws Exception {
            assertBothModes("~1h", BigInteger.valueOf(~1));
        }

        @Disabled("Compiler does not support BigInteger bitwise operations")
        @Test
        void bigIntegerLeftShift() throws Exception {
            assertBothModes("5h<<2", BigInteger.valueOf(20));
        }

        @Disabled("Compiler does not support BigInteger bitwise operations")
        @Test
        void bigIntegerRightShift() throws Exception {
            assertBothModes("5h>>2", BigInteger.valueOf(1));
        }
    }

    @Nested
    class LogicalExpressions {

        @Test
        void logicalNot() throws Exception {
            assertBothModes("!1", Boolean.FALSE);
        }

        @Test
        void logicalNotNull() throws Exception {
            assertBothModes("!null", Boolean.TRUE);
        }

        @Test
        void lessThan() throws Exception {
            assertBothModes("5<2", Boolean.FALSE);
        }

        @Test
        void greaterThan() throws Exception {
            assertBothModes("5>2", Boolean.TRUE);
        }

        @Test
        void lessOrEqual() throws Exception {
            assertBothModes("5<=5", Boolean.TRUE);
        }

        @Test
        void greaterOrEqual() throws Exception {
            assertBothModes("5>=3", Boolean.TRUE);
        }

        @Test
        void equality() throws Exception {
            assertBothModes("5==5.0", Boolean.TRUE);
        }

        @Test
        void inequality() throws Exception {
            assertBothModes("5!=5.0", Boolean.FALSE);
        }

        @Test
        void ternary() throws Exception {
            assertBothModes("true ? 1 : 0", 1);
        }

        @Test
        void ternaryShortCircuitsOnTrueBranch() throws Exception {
            assertBothModes("true ? 1 : 1/0", 1);
        }

        @Test
        void logicalAndShortCircuits() throws Exception {
            assertBothModes("false && 1/0 == 0", Boolean.FALSE);
        }

        @Test
        void logicalOrShortCircuits() throws Exception {
            assertBothModes("true || 1/0 == 0", Boolean.TRUE);
        }
    }

    @Nested
    class PropertyAccess {

        @Test
        void simpleProperty() throws Exception {
            assertBothModesMatch("index");
        }

        @Test
        void nestedProperty() throws Exception {
            assertBothModesMatch("bean2.id");
        }

        @Test
        void arrayLength() throws Exception {
            assertBothModesMatch("array.length");
        }

        @Test
        void arrayIndex() throws Exception {
            assertBothModesMatch("array[0]");
        }

        @Test
        void nullObject() throws Exception {
            assertBothModes("nullObject", null);
        }

        @Test
        void booleanProperty() throws Exception {
            assertBothModesMatch("disabled");
        }

        @Test
        void intProperty() throws Exception {
            assertBothModesMatch("anotherIntValue");
        }

        @Test
        void staticField() throws Exception {
            assertBothModesMatch("@ognl.test.objects.Root@STATIC_INT");
        }
    }

    @Nested
    class MethodCalls {

        @Test
        void noArgMethod() throws Exception {
            assertBothModesMatch("getIndex()");
        }

        @Test
        void stringArgMethod() throws Exception {
            assertBothModes("getCurrentClass(\"Test\")", "Test stop");
        }

        @Disabled("Compiler generates invalid cast for method call on auto-boxed primitive")
        @Test
        void toStringOnInteger() throws Exception {
            assertBothModes("index.toString()", "1");
        }

        @Test
        void staticMethod() throws Exception {
            assertBothModesMatch("@ognl.test.objects.Root@getStaticInt()");
        }
    }

    @Nested
    class NullHandling {

        @Test
        void nullEqualityToNull() throws Exception {
            assertBothModes("null == null", Boolean.TRUE);
        }

        @Test
        void nullInequalityToValue() throws Exception {
            assertBothModes("null != 1", Boolean.TRUE);
        }
    }

    @Nested
    @Disabled("Compiler generates invalid source for instanceof expressions: 'missing member name'")
    class InstanceOf {

        @Test
        void integerInstanceOf() throws Exception {
            assertBothModes("5 instanceof java.lang.Integer", Boolean.TRUE);
        }

        @Test
        void doubleNotInstanceOfInteger() throws Exception {
            assertBothModes("5. instanceof java.lang.Integer", Boolean.FALSE);
        }
    }
}
