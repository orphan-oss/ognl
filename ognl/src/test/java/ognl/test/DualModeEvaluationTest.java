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
import ognl.test.objects.Simple;
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

    private void setValueInterpreted(String expression, Object value) throws OgnlException {
        Object tree = Ognl.parseExpression(expression);
        ((Node) tree).setValue(context.withRoot(root), root, value);
    }

    private void setValueCompiled(String expression, Object value, OgnlContext ctx) throws Exception {
        Node node = Ognl.compileExpression(ctx, root, expression);
        node.getAccessor().set(ctx, root, value);
    }

    private void assertSetThenGetBothModes(String expression, Object setValue, Object expectedGet) throws Exception {
        // Interpreted: set then get
        setValueInterpreted(expression, setValue);
        Object interpretedResult = getValueInterpreted(expression);
        assertEquals(expectedGet, interpretedResult,
                "Interpreted set+get failed for: " + expression);

        // Compiled: set then get (fresh context each time)
        OgnlContext compiledCtx = freshCompiledContext();
        setValueCompiled(expression, setValue, compiledCtx);
        OgnlContext compiledCtx2 = freshCompiledContext();
        Object compiledResult = getValueCompiled(expression, compiledCtx2);
        assertEquals(expectedGet, compiledResult,
                "Compiled set+get failed for: " + expression);
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

        @Test
        void mapProperty() throws Exception {
            assertBothModesMatch("map");
        }

        @Test
        void mapDotAccess() throws Exception {
            assertBothModesMatch("map.test");
        }

        @Test
        void mapBracketAccess() throws Exception {
            assertBothModesMatch("map[\"test\"]");
        }

        @Test
        void mapConcatKeyAccess() throws Exception {
            assertBothModesMatch("map[\"te\" + \"st\"]");
        }

        @Test
        void negatedBooleanProperty() throws Exception {
            assertBothModes("! booleanValue", Boolean.TRUE);
        }

        @Test
        void negatedBeanProperty() throws Exception {
            assertBothModes("!bean2.pageBreakAfter", Boolean.TRUE);
        }

        @Test
        void stringLengthCheck() throws Exception {
            assertBothModesMatch("indexedStringValue != null && indexedStringValue.length() > 0");
        }

        @Test
        void disabledProperty() throws Exception {
            assertBothModes("disabled", Boolean.TRUE);
        }

        @Test
        void ternaryWithBooleanProperty() throws Exception {
            assertBothModes("disabled ? 'disabled' : 'othernot'", "disabled");
        }

        @Test
        void nullOrBooleanExpression() throws Exception {
            assertBothModes("nullObject || !readonly", Boolean.TRUE);
        }

        @Test
        void disabledOrReadonly() throws Exception {
            assertBothModes("disabled || readonly", Boolean.TRUE);
        }

        @Test
        void renderNavigationTernary() throws Exception {
            assertBothModes("renderNavigation ? '' : 'noborder'", "noborder");
        }

        @Test
        void stringConcatenationWithProperty() throws Exception {
            assertBothModes("\"background-color:blue; width:\" + (currentLocaleVerbosity / 2) + \"px\"",
                    "background-color:blue; width:43px");
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

    @Nested
    class SetterPaths {

        @Test
        void setMapNewValue() throws Exception {
            assertSetThenGetBothModes("map.newValue", 101, 101);
        }

        @Test
        void setMapBracketKey() throws Exception {
            assertSetThenGetBothModes("map[\"testKey\"]", "testVal", "testVal");
        }

        @Test
        void setSettableListIndex() throws Exception {
            assertSetThenGetBothModes("settableList[0]", "foo", "foo");
        }

        @Test
        void setIntValueProperty() throws Exception {
            assertSetThenGetBothModes("intValue", 42, 42);
        }

        @Test
        void setOpenTransitionWin() throws Exception {
            assertSetThenGetBothModes("openTransitionWin", Boolean.TRUE, Boolean.TRUE);
        }

        @Test
        void setStringValue() throws Exception {
            assertSetThenGetBothModes("stringValue", "hello", "hello");
        }
    }

    @Nested
    class SetterWithConversion {

        @Test
        void setIntFromDouble() throws Exception {
            assertSetThenGetBothModes("intValue", 6.5, 6);
        }

        @Test
        void setIntFromString() throws Exception {
            assertSetThenGetBothModes("intValue", "654", 654);
        }

        @Test
        void setStringFromInt() throws Exception {
            assertSetThenGetBothModes("stringValue", 25, "25");
        }
    }

    @Nested
    class IndexAccess {

        @Test
        void listWithIndexVariable() throws Exception {
            assertBothModesMatch("list[index]");
        }

        @Test
        void listWithObjectIndex() throws Exception {
            assertBothModesMatch("list[objectIndex]");
        }

        @Test
        void arrayWithObjectIndex() throws Exception {
            assertBothModesMatch("array[objectIndex]");
        }

        @Test
        void arrayWithMethodIndex() throws Exception {
            assertBothModesMatch("array[getObjectIndex()]");
        }

        @Test
        void ternaryWithArrayLength() throws Exception {
            assertBothModes("(index == (array.length - 3)) ? 'toggle toggleSelected' : 'toggle'",
                    "toggle toggleSelected");
        }

        @Test
        void stringConcatWithIndex() throws Exception {
            assertBothModes("\"return toggleDisplay('excdisplay\" + index + \"', this)\"",
                    "return toggleDisplay('excdisplay1', this)");
        }

        @Test
        void mapSplitAccess() throws Exception {
            assertBothModes("map[mapKey].split('=')[0]", "StringStuff");
        }

        @Test
        void nestedListAccess() throws Exception {
            assertBothModesMatch("booleanValues[index1][index2]");
        }
    }

    @Nested
    class ArrayElements {

        @Test
        void charArrayAccess() throws Exception {
            assertBothModes("\"{Hello}\".toCharArray()[6]", '}');
        }

        @Test
        void tapestryCharArray() throws Exception {
            assertBothModes("\"Tapestry\".toCharArray()[2]", 'p');
        }

        @Test
        void listLiteral() throws Exception {
            assertBothModesMatch("{'1','2','3'}");
        }

        @Test
        void booleanListLiteral() throws Exception {
            assertBothModesMatch("{ true, !false }");
        }
    }

    @Nested
    class MethodCallsExtended {

        @Test
        void formatMethodWithProperty() throws Exception {
            assertBothModesMatch("getCurrentClass(\"Test\")");
        }

        @Test
        void ternaryWithMethodResult() throws Exception {
            assertBothModes("disabled ? 'disabled' : 'othernot'", "disabled");
        }

        @Test
        void printDeliveryConcat() throws Exception {
            assertBothModes("printDelivery ? 'javascript:toggle(' + bean2.id + ');' : ''",
                    "javascript:toggle(1);");
        }

        @Test
        void nestedMethodCall() throws Exception {
            assertBothModes("b.methodOfB(a.methodOfA(b)-1)", 0);
        }
    }

    @Nested
    class InterfaceInheritance {

        @Test
        void myMap() throws Exception {
            assertBothModesMatch("myMap");
        }

        @Test
        void myMapDotTest() throws Exception {
            assertBothModesMatch("myMap.test");
        }

        @Test
        void myMapArrayAccess() throws Exception {
            assertBothModesMatch("myMap.array[0]");
        }

        @Test
        void myMapListAccess() throws Exception {
            assertBothModesMatch("myMap.list[1]");
        }

        @Test
        void myMapFirstElement() throws Exception {
            assertBothModes("myMap[^]", 99);
        }

        @Test
        void myMapLastElement() throws Exception {
            assertBothModes("myMap[$]", null);
        }

        @Test
        void mapCompFormClientId() throws Exception {
            assertBothModes("map.comp.form.clientId", "form1");
        }

        @Test
        void myTestTheMapKey() throws Exception {
            assertBothModes("myTest.theMap['key']", "value");
        }
    }

    @Nested
    class DynamicSubscripts {

        @Test
        void mapFirstElement() throws Exception {
            assertBothModes("map[^]", 99);
        }

        @Test
        void mapLastElement() throws Exception {
            assertBothModes("map[$]", null);
        }

        @Test
        void listMidElement() throws Exception {
            assertBothModesMatch("getMap().list[|]");
        }

        @Test
        void arrayLastElement() throws Exception {
            assertBothModesMatch("map.array[$]");
        }
    }

    @Nested
    class ComplexExpressions {

        @Test
        void subExpressionWithThis() throws Exception {
            assertBothModesMatch("map.(#this)");
        }

        @Test
        void subExpressionWithTernary() throws Exception {
            assertBothModesMatch("map.(#this != null ? #this['size'] : null)");
        }

        @Test
        void firstElementSubExpression() throws Exception {
            assertBothModes("map[^].(#this == null ? 'empty' : #this)", 99);
        }

        @Test
        void lastElementSubExpression() throws Exception {
            assertBothModes("map[$].(#this == null ? 'empty' : #this)", "empty");
        }

        @Test
        void lastElementWithRootRef() throws Exception {
            assertBothModesMatch("map[$].(#root == null ? 'empty' : #root)");
        }

        @Test
        void arrayPlusMapSize() throws Exception {
            assertBothModesMatch("map.(array[2] + size())");
        }

        @Test
        void nestedTernary() throws Exception {
            assertBothModes("sorted ? (readonly ? 'currentSortDesc' : 'currentSortAsc') : 'currentSortNone'",
                    "currentSortAsc");
        }

        @Test
        void selectedLocaleTernary() throws Exception {
            assertBothModes("((selected != null) && (currLocale.toString() == selected.toString())) ? 'first' : 'second'",
                    "first");
        }

        @Test
        void listLiteralWithProperties() throws Exception {
            assertBothModesMatch("{stringValue, getMap()}");
        }

        @Test
        void getAssetWithTernary() throws Exception {
            assertBothModes("getAsset( (width?'Yes':'No')+'Icon' )", "NoIcon");
        }
    }

    @Nested
    class PrimitiveNullHandling {

        @Test
        void setNullOnIntProperty() throws Exception {
            assertSetThenGetBothModes("intValue", null, 0);
        }

        @Test
        void setNullOnBooleanProperty() throws Exception {
            assertSetThenGetBothModes("booleanValue", null, false);
        }

        @Test
        void setValueOnIntProperty() throws Exception {
            assertSetThenGetBothModes("intValue", 42, 42);
        }

        @Test
        void setValueOnBooleanProperty() throws Exception {
            assertSetThenGetBothModes("booleanValue", true, true);
        }
    }

    @Nested
    class MethodCallsWithSimple {

        private Simple simpleRoot;
        private OgnlContext simpleContext;

        @BeforeEach
        void setUp() {
            simpleRoot = new Simple();
            simpleContext = Ognl.createDefaultContext(simpleRoot, new DefaultMemberAccess(false));
        }

        private void assertSimpleBothModes(String expression, Object expected) throws Exception {
            Object tree = Ognl.parseExpression(expression);
            Object interpreted = ((Node) tree).getValue(simpleContext.withRoot(simpleRoot), simpleRoot);
            assertEquals(expected, interpreted, "Interpreted failed for: " + expression);

            OgnlContext compiledCtx = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node compiled = Ognl.compileExpression(compiledCtx, simpleRoot, expression);
            Object compiledResult = compiled.getAccessor().get(compiledCtx, simpleRoot);
            assertEquals(expected, compiledResult, "Compiled failed for: " + expression);
        }

        private void assertSimpleBothModesMatch(String expression) throws Exception {
            Object tree = Ognl.parseExpression(expression);
            Object interpreted = ((Node) tree).getValue(simpleContext.withRoot(simpleRoot), simpleRoot);

            OgnlContext compiledCtx = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node compiled = Ognl.compileExpression(compiledCtx, simpleRoot, expression);
            Object compiledResult = compiled.getAccessor().get(compiledCtx, simpleRoot);
            assertEquals(interpreted, compiledResult,
                    "Modes diverged for: " + expression
                            + "\n  interpreted: " + interpreted
                            + "\n  compiled:    " + compiledResult);
        }

        @Test
        void hashCode_() throws Exception {
            assertSimpleBothModesMatch("hashCode()");
        }

        @Test
        void booleanTernary() throws Exception {
            assertSimpleBothModes("getBooleanValue() ? \"here\" : \"\"", "");
        }

        @Test
        void isDisabled() throws Exception {
            assertSimpleBothModes("isDisabled()", Boolean.TRUE);
        }

        @Test
        void isTruck() throws Exception {
            assertSimpleBothModes("isTruck", Boolean.TRUE);
        }

        @Test
        void isEditorDisabled() throws Exception {
            assertSimpleBothModes("isEditorDisabled()", Boolean.FALSE);
        }

        @Test
        void messagesFormat() throws Exception {
            assertSimpleBothModesMatch("messages.format('ShowAllCount', one)");
        }

        @Test
        void varArgsNoArgs() throws Exception {
            assertSimpleBothModes("isThisVarArgsWorking()", Boolean.TRUE);
        }

        @Test
        void varArgsWithArgs() throws Exception {
            assertSimpleBothModes("isThisVarArgsWorking(three, rootValue)", Boolean.TRUE);
        }

        @Test
        void enumMethodArg() throws Exception {
            assertSimpleBothModes("getTestValue(@ognl.test.objects.SimpleEnum@ONE.value)", 2);
        }
    }

    @Nested
    class RootArrayAccess {

        private OgnlContext arrayContext;
        private String[] stringArray;
        private int[] intArray;

        @BeforeEach
        void setUp() {
            stringArray = new String[]{"hello", "world"};
            intArray = new int[]{10, 20, 30};
        }

        @Test
        void stringArrayLength() throws Exception {
            arrayContext = Ognl.createDefaultContext(stringArray, context.getMemberAccess());
            Object tree = Ognl.parseExpression("length");
            Object interpreted = ((Node) tree).getValue(arrayContext.withRoot(stringArray), stringArray);
            assertEquals(2, interpreted, "Interpreted failed");

            OgnlContext compiledCtx = Ognl.createDefaultContext(stringArray, context.getMemberAccess());
            Node compiled = Ognl.compileExpression(compiledCtx, stringArray, "length");
            Object compiledResult = compiled.getAccessor().get(compiledCtx, stringArray);
            assertEquals(2, compiledResult, "Compiled failed");
        }

        @Test
        void stringArrayRootElement() throws Exception {
            arrayContext = Ognl.createDefaultContext(stringArray, context.getMemberAccess());
            Object tree = Ognl.parseExpression("#root[1]");
            Object interpreted = ((Node) tree).getValue(arrayContext.withRoot(stringArray), stringArray);
            assertEquals("world", interpreted, "Interpreted failed");

            OgnlContext compiledCtx = Ognl.createDefaultContext(stringArray, context.getMemberAccess());
            Node compiled = Ognl.compileExpression(compiledCtx, stringArray, "#root[1]");
            Object compiledResult = compiled.getAccessor().get(compiledCtx, stringArray);
            assertEquals("world", compiledResult, "Compiled failed");
        }

        @Test
        void intArrayRootElement() throws Exception {
            arrayContext = Ognl.createDefaultContext(intArray, context.getMemberAccess());
            Object tree = Ognl.parseExpression("#root[1]");
            Object interpreted = ((Node) tree).getValue(arrayContext.withRoot(intArray), intArray);
            assertEquals(20, interpreted, "Interpreted failed");

            OgnlContext compiledCtx = Ognl.createDefaultContext(intArray, context.getMemberAccess());
            Node compiled = Ognl.compileExpression(compiledCtx, intArray, "#root[1]");
            Object compiledResult = compiled.getAccessor().get(compiledCtx, intArray);
            assertEquals(20, compiledResult, "Compiled failed");
        }
    }

    @Nested
    class MethodCallsWithConversion {

        @Test
        void formatWithArray() throws Exception {
            assertBothModesMatch("format('key', array)");
        }

        @Test
        void formatWithIntValue() throws Exception {
            assertBothModesMatch("format('key', intValue)");
        }

        @Test
        void formatWithMapSize() throws Exception {
            assertBothModesMatch("format('key', map.size)");
        }
    }

    @Nested
    class PrimitiveNullHandlingWithSimple {

        private Simple simpleRoot;
        private OgnlContext simpleContext;

        @BeforeEach
        void setUp() {
            simpleRoot = new Simple();
            simpleRoot.setFloatValue(10.56f);
            simpleRoot.setIntValue(34);
            simpleContext = Ognl.createDefaultContext(simpleRoot, new DefaultMemberAccess(false));
        }

        @Test
        void setNullOnFloatValue() throws Exception {
            // Interpreted: set null then get
            Object tree = Ognl.parseExpression("floatValue");
            ((Node) tree).setValue(simpleContext.withRoot(simpleRoot), simpleRoot, null);
            Object interpreted = ((Node) Ognl.parseExpression("floatValue")).getValue(
                    simpleContext.withRoot(simpleRoot), simpleRoot);
            assertEquals(0f, interpreted, "Interpreted set+get failed for: floatValue");

            // Reset for compiled test
            simpleRoot.setFloatValue(10.56f);

            // Compiled: set null then get
            OgnlContext compiledCtx = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node setNode = Ognl.compileExpression(compiledCtx, simpleRoot, "floatValue");
            setNode.getAccessor().set(compiledCtx, simpleRoot, null);

            OgnlContext compiledCtx2 = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node getNode = Ognl.compileExpression(compiledCtx2, simpleRoot, "floatValue");
            Object compiled = getNode.getAccessor().get(compiledCtx2, simpleRoot);
            assertEquals(0f, compiled, "Compiled set+get failed for: floatValue");
        }

        @Test
        void setNullOnIntValue() throws Exception {
            Object tree = Ognl.parseExpression("intValue");
            ((Node) tree).setValue(simpleContext.withRoot(simpleRoot), simpleRoot, null);
            Object interpreted = ((Node) Ognl.parseExpression("intValue")).getValue(
                    simpleContext.withRoot(simpleRoot), simpleRoot);
            assertEquals(0, interpreted, "Interpreted set+get failed for: intValue");

            simpleRoot.setIntValue(34);

            OgnlContext compiledCtx = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node setNode = Ognl.compileExpression(compiledCtx, simpleRoot, "intValue");
            setNode.getAccessor().set(compiledCtx, simpleRoot, null);

            OgnlContext compiledCtx2 = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node getNode = Ognl.compileExpression(compiledCtx2, simpleRoot, "intValue");
            Object compiled = getNode.getAccessor().get(compiledCtx2, simpleRoot);
            assertEquals(0, compiled, "Compiled set+get failed for: intValue");
        }

        @Test
        void setNullOnBooleanValue() throws Exception {
            Object tree = Ognl.parseExpression("booleanValue");
            ((Node) tree).setValue(simpleContext.withRoot(simpleRoot), simpleRoot, true);
            ((Node) Ognl.parseExpression("booleanValue")).setValue(
                    simpleContext.withRoot(simpleRoot), simpleRoot, null);
            Object interpreted = ((Node) Ognl.parseExpression("booleanValue")).getValue(
                    simpleContext.withRoot(simpleRoot), simpleRoot);
            assertEquals(false, interpreted, "Interpreted set+get failed for: booleanValue");

            simpleRoot.setBooleanValue(true);

            OgnlContext compiledCtx = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node setNode = Ognl.compileExpression(compiledCtx, simpleRoot, "booleanValue");
            setNode.getAccessor().set(compiledCtx, simpleRoot, null);

            OgnlContext compiledCtx2 = Ognl.createDefaultContext(simpleRoot, simpleContext.getMemberAccess());
            Node getNode = Ognl.compileExpression(compiledCtx2, simpleRoot, "booleanValue");
            Object compiled = getNode.getAccessor().get(compiledCtx2, simpleRoot);
            assertEquals(false, compiled, "Compiled set+get failed for: booleanValue");
        }
    }
}
