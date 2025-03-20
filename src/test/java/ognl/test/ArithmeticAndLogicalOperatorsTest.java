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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void doubleValuedArithmeticExpressions() throws OgnlException {
        assertEquals(-1d, Ognl.getValue("-1d", context, root));
        assertEquals(1d, Ognl.getValue("+1d", context, root));
        assertEquals(1f, Ognl.getValue("--1f", context, root));
        assertEquals(4.0d, Ognl.getValue("2*2.0", context, root));
        assertEquals(2.5d, Ognl.getValue("5/2.", context, root));
        assertEquals(7d, Ognl.getValue("5+2D", context, root));
        assertEquals(3.0f, Ognl.getValue("5f-2F", context, root));
        assertEquals(11d, Ognl.getValue("5.+2*3", context, root));
        assertEquals(21d, Ognl.getValue("(5.+2)*3", context, root));
    }

    @Test
    void bigDecimalValuedArithmeticExpressions() throws OgnlException {
        assertEquals(BigDecimal.valueOf(-1), Ognl.getValue("-1b", context, root));
        assertEquals(BigDecimal.valueOf(1), Ognl.getValue("+1b", context, root));
        assertEquals(BigDecimal.valueOf(1), Ognl.getValue("--1b", context, root));
        assertEquals(BigDecimal.valueOf(4.0), Ognl.getValue("2*2.0b", context, root));
        assertEquals(BigDecimal.valueOf(2), Ognl.getValue("5/2.B", context, root));
        assertEquals(BigDecimal.valueOf(2.5), Ognl.getValue("5.0B/2", context, root));
        assertEquals(BigDecimal.valueOf(7), Ognl.getValue("5+2b", context, root));
        assertEquals(BigDecimal.valueOf(3), Ognl.getValue("5-2B", context, root));
        assertEquals(BigDecimal.valueOf(11d), Ognl.getValue("5.+2b*3", context, root));
        assertEquals(BigDecimal.valueOf(21d), Ognl.getValue("(5.+2b)*3", context, root));
    }

    @Test
    void integerValuedArithmeticExpressions() throws OgnlException {
        assertEquals(-1, Ognl.getValue("-1", context, root));
        assertEquals(1, Ognl.getValue("+1", context, root));
        assertEquals(1, Ognl.getValue("--1", context, root));
        assertEquals(4, Ognl.getValue("2*2", context, root));
        assertEquals(2, Ognl.getValue("5/2", context, root));
        assertEquals(7, Ognl.getValue("5+2", context, root));
        assertEquals(3, Ognl.getValue("5-2", context, root));
        assertEquals(11, Ognl.getValue("5+2*3", context, root));
        assertEquals(21, Ognl.getValue("(5+2)*3", context, root));
        assertEquals(~1, Ognl.getValue("~1", context, root));
        assertEquals(1, Ognl.getValue("5%2", context, root));
        assertEquals(20, Ognl.getValue("5<<2", context, root));
        assertEquals(1, Ognl.getValue("5>>2", context, root));
        assertEquals(1, Ognl.getValue("5>>1+1", context, root));
        assertEquals(-5 >>> 2, Ognl.getValue("-5>>>2", context, root));
        assertEquals(-5L >>> 2, Ognl.getValue("-5L>>>2", context, root));
        assertEquals(1.0, Ognl.getValue("5. & 3", context, root));
        assertEquals(6, Ognl.getValue("5 ^3", context, root));
        assertEquals(7L, Ognl.getValue("5l&3|5^3", context, root));
        assertEquals(5, Ognl.getValue("5&(3|5^3)", context, root));
        assertEquals(1, Ognl.getValue("true ? 1 : 1/0", context, root));
    }

    @Test
    void bigIntegerValuedArithmeticExpressions() throws OgnlException {
        assertEquals(BigInteger.valueOf(-1), Ognl.getValue("-1h", context, root));
        assertEquals(BigInteger.valueOf(1), Ognl.getValue("+1H", context, root));
        assertEquals(BigInteger.valueOf(1), Ognl.getValue("--1h", context, root));
        assertEquals(BigInteger.valueOf(4), Ognl.getValue("2h*2", context, root));
        assertEquals(BigInteger.valueOf(2), Ognl.getValue("5/2h", context, root));
        assertEquals(BigInteger.valueOf(7), Ognl.getValue("5h+2", context, root));
        assertEquals(BigInteger.valueOf(3), Ognl.getValue("5-2h", context, root));
        assertEquals(BigInteger.valueOf(11), Ognl.getValue("5+2H*3", context, root));
        assertEquals(BigInteger.valueOf(21), Ognl.getValue("(5+2H)*3", context, root));
        assertEquals(BigInteger.valueOf(~1), Ognl.getValue("~1h", context, root));
        assertEquals(BigInteger.valueOf(1), Ognl.getValue("5h%2", context, root));
        assertEquals(BigInteger.valueOf(20), Ognl.getValue("5h<<2", context, root));
        assertEquals(BigInteger.valueOf(1), Ognl.getValue("5h>>2", context, root));
        assertEquals(BigInteger.valueOf(1), Ognl.getValue("5h>>1+1", context, root));
        assertEquals(BigInteger.valueOf(-2), Ognl.getValue("-5h>>>2", context, root));
        assertEquals(BigInteger.valueOf(1L), Ognl.getValue("5.b & 3", context, root));
        assertEquals(BigInteger.valueOf(6), Ognl.getValue("5h ^3", context, root));
        assertEquals(BigInteger.valueOf(7L), Ognl.getValue("5h&3|5^3", context, root));
        assertEquals(BigInteger.valueOf(5L), Ognl.getValue("5H&(3|5^3)", context, root));
    }

    @Test
    void logicalExpressions() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("!1", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("!null", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("5<2", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5>2", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5<=5", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5>=3", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5<-5>>>2", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5==5.0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("5!=5.0", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("null in {true,false,null}", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("null not in {true,false,null}", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("null in {true,false,null}.toArray()", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("5 in {true,false,null}", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5 not in {true,false,null}", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("5 instanceof java.lang.Integer", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("5. instanceof java.lang.Integer", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("!false || true", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("!(true && true)", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("(1 > 0 && true) || 2 > 0", context, root));
    }

    @Test
    void logicalExpressionsStringVersions() throws OgnlException {
        assertEquals(Integer.valueOf(2), Ognl.getValue("2 or 0", context, root));
        assertEquals(Integer.valueOf(0), Ognl.getValue("1 and 0", context, root));
        assertEquals(Integer.valueOf(1), Ognl.getValue("1 bor 0", context, root));
        assertEquals(Integer.valueOf(12), Ognl.getValue("true && 12", context, root));
        assertEquals(Integer.valueOf(1), Ognl.getValue("1 xor 0", context, root));
        assertEquals(0, Ognl.getValue("1 band 0", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("1 eq 1", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("1 neq 1", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("1 lt 5", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("1 lte 5", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("1 gt 5", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("1 gte 5", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("1 lt 5", context, root));
        assertEquals(Integer.valueOf(4), Ognl.getValue("1 shl 2", context, root));
        assertEquals(Integer.valueOf(1), Ognl.getValue("4 shr 2", context, root));
        assertEquals(Integer.valueOf(1), Ognl.getValue("4 ushr 2", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("not null", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("not 1", context, root));
    }

    @Test
    void equalityOnIdentity() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("#a = new java.lang.Object(), #a == #a", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("#a = new java.lang.Object(), #b = new java.lang.Object(), #a == #b", context, root));
    }

    @Test
    void comparableAndNonComparable() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("#a = new java.lang.Object(), #a == ''", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("#a = new java.lang.Object(), '' == #a", context, root));
    }

    @Test
    void expressionsWithVariables() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("#x > 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("#x < 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("#x == 0", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("#x == 1", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("0 > #x", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("0 < #x", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("0 == #x", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("1 == #x", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("\"1\" > 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("\"1\" < 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("\"1\" == 0", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("\"1\" == 1", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("0 > \"1\"", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("0 < \"1\"", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("0 == \"1\"", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("1 == \"1\"", context, root));
        assertEquals("11", Ognl.getValue("#x + 1", context, root));
        assertEquals("11", Ognl.getValue("1 + #x", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("#y == 1", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("#y == \"1\"", context, root));
        assertEquals("11", Ognl.getValue("#y + \"1\"", context, root));
        assertEquals("11", Ognl.getValue("\"1\" + #y", context, root));
    }

}
