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

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Root;
import ognl.test.util.DualModeTestUtils;
import ognl.test.util.OgnlExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArithmeticAndLogicalOperatorsTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
        context.put("x", "1");
        context.put("y", new BigDecimal(1));

        root = new Root();
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void doubleValuedArithmeticExpressions(OgnlExecutionMode mode) throws Exception {
        assertEquals(-1d, DualModeTestUtils.prepareAndEvaluate("-1d", context, root, mode));
        assertEquals(1d, DualModeTestUtils.prepareAndEvaluate("+1d", context, root, mode));
        assertEquals(1f, DualModeTestUtils.prepareAndEvaluate("--1f", context, root, mode));
        assertEquals(4.0d, DualModeTestUtils.prepareAndEvaluate("2*2.0", context, root, mode));
        assertEquals(2.5d, DualModeTestUtils.prepareAndEvaluate("5/2.", context, root, mode));
        assertEquals(7d, DualModeTestUtils.prepareAndEvaluate("5+2D", context, root, mode));
        assertEquals(3.0f, DualModeTestUtils.prepareAndEvaluate("5f-2F", context, root, mode));
        assertEquals(11d, DualModeTestUtils.prepareAndEvaluate("5.+2*3", context, root, mode));
        assertEquals(21d, DualModeTestUtils.prepareAndEvaluate("(5.+2)*3", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void bigDecimalValuedArithmeticExpressions(OgnlExecutionMode mode) throws Exception {
        assertEquals(BigDecimal.valueOf(-1), DualModeTestUtils.prepareAndEvaluate("-1b", context, root, mode));
        assertEquals(BigDecimal.valueOf(1), DualModeTestUtils.prepareAndEvaluate("+1b", context, root, mode));
        assertEquals(BigDecimal.valueOf(1), DualModeTestUtils.prepareAndEvaluate("--1b", context, root, mode));
        assertEquals(BigDecimal.valueOf(4.0), DualModeTestUtils.prepareAndEvaluate("2*2.0b", context, root, mode));
        assertEquals(BigDecimal.valueOf(2), DualModeTestUtils.prepareAndEvaluate("5/2.B", context, root, mode));
        assertEquals(BigDecimal.valueOf(2.5), DualModeTestUtils.prepareAndEvaluate("5.0B/2", context, root, mode));
        assertEquals(BigDecimal.valueOf(7), DualModeTestUtils.prepareAndEvaluate("5+2b", context, root, mode));
        assertEquals(BigDecimal.valueOf(3), DualModeTestUtils.prepareAndEvaluate("5-2B", context, root, mode));
        assertEquals(BigDecimal.valueOf(11d), DualModeTestUtils.prepareAndEvaluate("5.+2b*3", context, root, mode));
        assertEquals(BigDecimal.valueOf(21d), DualModeTestUtils.prepareAndEvaluate("(5.+2b)*3", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void integerValuedArithmeticExpressions(OgnlExecutionMode mode) throws Exception {
        assertEquals(-1, DualModeTestUtils.prepareAndEvaluate("-1", context, root, mode));
        assertEquals(1, DualModeTestUtils.prepareAndEvaluate("+1", context, root, mode));
        assertEquals(1, DualModeTestUtils.prepareAndEvaluate("--1", context, root, mode));
        assertEquals(4, DualModeTestUtils.prepareAndEvaluate("2*2", context, root, mode));
        assertEquals(2, DualModeTestUtils.prepareAndEvaluate("5/2", context, root, mode));
        assertEquals(7, DualModeTestUtils.prepareAndEvaluate("5+2", context, root, mode));
        assertEquals(3, DualModeTestUtils.prepareAndEvaluate("5-2", context, root, mode));
        assertEquals(11, DualModeTestUtils.prepareAndEvaluate("5+2*3", context, root, mode));
        assertEquals(21, DualModeTestUtils.prepareAndEvaluate("(5+2)*3", context, root, mode));
        assertEquals(~1, DualModeTestUtils.prepareAndEvaluate("~1", context, root, mode));
        assertEquals(1, DualModeTestUtils.prepareAndEvaluate("5%2", context, root, mode));
        assertEquals(20, DualModeTestUtils.prepareAndEvaluate("5<<2", context, root, mode));
        assertEquals(1, DualModeTestUtils.prepareAndEvaluate("5>>2", context, root, mode));
        assertEquals(1, DualModeTestUtils.prepareAndEvaluate("5>>1+1", context, root, mode));
        assertEquals(-5 >>> 2, DualModeTestUtils.prepareAndEvaluate("-5>>>2", context, root, mode));
        assertEquals(-5L >>> 2, DualModeTestUtils.prepareAndEvaluate("-5L>>>2", context, root, mode));
        assertEquals(1.0, DualModeTestUtils.prepareAndEvaluate("5. & 3", context, root, mode));
        assertEquals(6, DualModeTestUtils.prepareAndEvaluate("5 ^3", context, root, mode));
        assertEquals(7L, DualModeTestUtils.prepareAndEvaluate("5l&3|5^3", context, root, mode));
        assertEquals(5, DualModeTestUtils.prepareAndEvaluate("5&(3|5^3)", context, root, mode));
        assertEquals(1, DualModeTestUtils.prepareAndEvaluate("true ? 1 : 1/0", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void bigIntegerValuedArithmeticExpressions(OgnlExecutionMode mode) throws Exception {
        assertEquals(BigInteger.valueOf(-1), DualModeTestUtils.prepareAndEvaluate("-1h", context, root, mode));
        assertEquals(BigInteger.valueOf(1), DualModeTestUtils.prepareAndEvaluate("+1H", context, root, mode));
        assertEquals(BigInteger.valueOf(1), DualModeTestUtils.prepareAndEvaluate("--1h", context, root, mode));
        assertEquals(BigInteger.valueOf(4), DualModeTestUtils.prepareAndEvaluate("2h*2", context, root, mode));
        assertEquals(BigInteger.valueOf(2), DualModeTestUtils.prepareAndEvaluate("5/2h", context, root, mode));
        assertEquals(BigInteger.valueOf(7), DualModeTestUtils.prepareAndEvaluate("5h+2", context, root, mode));
        assertEquals(BigInteger.valueOf(3), DualModeTestUtils.prepareAndEvaluate("5-2h", context, root, mode));
        assertEquals(BigInteger.valueOf(11), DualModeTestUtils.prepareAndEvaluate("5+2H*3", context, root, mode));
        assertEquals(BigInteger.valueOf(21), DualModeTestUtils.prepareAndEvaluate("(5+2H)*3", context, root, mode));
        assertEquals(BigInteger.valueOf(~1), DualModeTestUtils.prepareAndEvaluate("~1h", context, root, mode));
        assertEquals(BigInteger.valueOf(1), DualModeTestUtils.prepareAndEvaluate("5h%2", context, root, mode));
        assertEquals(BigInteger.valueOf(20), DualModeTestUtils.prepareAndEvaluate("5h<<2", context, root, mode));
        assertEquals(BigInteger.valueOf(1), DualModeTestUtils.prepareAndEvaluate("5h>>2", context, root, mode));
        assertEquals(BigInteger.valueOf(1), DualModeTestUtils.prepareAndEvaluate("5h>>1+1", context, root, mode));
        assertEquals(BigInteger.valueOf(-2), DualModeTestUtils.prepareAndEvaluate("-5h>>>2", context, root, mode));
        assertEquals(BigInteger.valueOf(1L), DualModeTestUtils.prepareAndEvaluate("5.b & 3", context, root, mode));
        assertEquals(BigInteger.valueOf(6), DualModeTestUtils.prepareAndEvaluate("5h ^3", context, root, mode));
        assertEquals(BigInteger.valueOf(7L), DualModeTestUtils.prepareAndEvaluate("5h&3|5^3", context, root, mode));
        assertEquals(BigInteger.valueOf(5L), DualModeTestUtils.prepareAndEvaluate("5H&(3|5^3)", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void logicalExpressions(OgnlExecutionMode mode) throws Exception {
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("!1", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("!null", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("5<2", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5>2", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5<=5", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5>=3", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5<-5>>>2", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5==5.0", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("5!=5.0", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("null in {true,false,null}", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("null not in {true,false,null}", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("null in {true,false,null}.toArray()", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("5 in {true,false,null}", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5 not in {true,false,null}", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("5 instanceof java.lang.Integer", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("5. instanceof java.lang.Integer", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("!false || true", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("!(true && true)", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("(1 > 0 && true) || 2 > 0", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void logicalExpressionsStringVersions(OgnlExecutionMode mode) throws Exception {
        assertEquals(Integer.valueOf(2), DualModeTestUtils.prepareAndEvaluate("2 or 0", context, root, mode));
        assertEquals(Integer.valueOf(0), DualModeTestUtils.prepareAndEvaluate("1 and 0", context, root, mode));
        assertEquals(Integer.valueOf(1), DualModeTestUtils.prepareAndEvaluate("1 bor 0", context, root, mode));
        assertEquals(Integer.valueOf(12), DualModeTestUtils.prepareAndEvaluate("true && 12", context, root, mode));
        assertEquals(Integer.valueOf(1), DualModeTestUtils.prepareAndEvaluate("1 xor 0", context, root, mode));
        assertEquals(0, DualModeTestUtils.prepareAndEvaluate("1 band 0", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("1 eq 1", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("1 neq 1", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("1 lt 5", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("1 lte 5", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("1 gt 5", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("1 gte 5", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("1 lt 5", context, root, mode));
        assertEquals(Integer.valueOf(4), DualModeTestUtils.prepareAndEvaluate("1 shl 2", context, root, mode));
        assertEquals(Integer.valueOf(1), DualModeTestUtils.prepareAndEvaluate("4 shr 2", context, root, mode));
        assertEquals(Integer.valueOf(1), DualModeTestUtils.prepareAndEvaluate("4 ushr 2", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("not null", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("not 1", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void equalityOnIdentity(OgnlExecutionMode mode) throws Exception {
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("#a = new java.lang.Object(), #a == #a", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("#a = new java.lang.Object(), #b = new java.lang.Object(), #a == #b", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void comparableAndNonComparable(OgnlExecutionMode mode) throws Exception {
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("#a = new java.lang.Object(), #a == ''", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("#a = new java.lang.Object(), '' == #a", context, root, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void expressionsWithVariables(OgnlExecutionMode mode) throws Exception {
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("#x > 0", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("#x < 0", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("#x == 0", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("#x == 1", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("0 > #x", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("0 < #x", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("0 == #x", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("1 == #x", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("\"1\" > 0", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("\"1\" < 0", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("\"1\" == 0", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("\"1\" == 1", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("0 > \"1\"", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("0 < \"1\"", context, root, mode));
        assertEquals(Boolean.FALSE, DualModeTestUtils.prepareAndEvaluate("0 == \"1\"", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("1 == \"1\"", context, root, mode));
        assertEquals("11", DualModeTestUtils.prepareAndEvaluate("#x + 1", context, root, mode));
        assertEquals("11", DualModeTestUtils.prepareAndEvaluate("1 + #x", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("#y == 1", context, root, mode));
        assertEquals(Boolean.TRUE, DualModeTestUtils.prepareAndEvaluate("#y == \"1\"", context, root, mode));
        assertEquals("11", DualModeTestUtils.prepareAndEvaluate("#y + \"1\"", context, root, mode));
        assertEquals("11", DualModeTestUtils.prepareAndEvaluate("\"1\" + #y", context, root, mode));
    }

}
