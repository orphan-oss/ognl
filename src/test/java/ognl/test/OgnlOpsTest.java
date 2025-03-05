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

import ognl.OgnlException;
import ognl.OgnlOps;
import ognl.enhance.UnsupportedCompilationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class OgnlOpsTest {

    @Test
    void testEqualStringsEqual() {
        final String v1 = "a";
        final String v2 = "a";
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    void testEqualStringsNotEqual() {
        final String v1 = "a";
        final String v2 = "b";
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    void testEqualFloatsEqual() {
        final Float v1 = 0.1f;
        final Float v2 = 0.1f;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    void testEqualFloatsNotEqual() {
        final Float v1 = 0.1f;
        final Float v2 = 0.2f;
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    void testEqualLongsEqual() {
        final Long v1 = 1L;
        final Long v2 = 1L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    void testEqualLongsNotEqual() {
        final Long v1 = 1L;
        final Long v2 = 2L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    void testEqualBigLongsEqual() {
        final Long v1 = 1000000000000000001L;
        final Long v2 = 1000000000000000001L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    void testEqualBigLongsNotEqual() {
        final Long v1 = 1000000000000000001L;
        final Long v2 = 1000000000000000002L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    void testEqualNullsEqual() {
        assertTrue(OgnlOps.equal(null, null));
    }

    @Test
    void testEqualNullsNotEqual() {
        final Object v2 = "b";
        assertFalse(OgnlOps.equal(null, v2));
        assertFalse(OgnlOps.equal(v2, null));
    }

    @Test
    void testShiftLeft() {
        assertEquals(8, OgnlOps.shiftLeft(1, 3));
        assertEquals(new BigInteger("8"), OgnlOps.shiftLeft(new BigInteger("1"), 3));
    }

    @Test
    void testShiftRight() {
        assertEquals(1, OgnlOps.shiftRight(8, 3));
        assertEquals(new BigInteger("1"), OgnlOps.shiftRight(new BigInteger("8"), 3));
    }

    @Test
    void testUnsignedShiftRight() {
        assertEquals(1, OgnlOps.unsignedShiftRight(8, 3));
        assertEquals(new BigInteger("1"), OgnlOps.unsignedShiftRight(new BigInteger("8"), 3));
    }

    @Test
    void testAdd() {
        assertEquals(5, OgnlOps.add(2, 3));
        assertEquals(new BigInteger("5"), OgnlOps.add(new BigInteger("2"), new BigInteger("3")));
        assertEquals(new BigDecimal("5.0"), OgnlOps.add(new BigDecimal("2.0"), new BigDecimal("3.0")));
    }

    @Test
    void testSubtract() {
        assertEquals(1, OgnlOps.subtract(3, 2));
        assertEquals(new BigInteger("1"), OgnlOps.subtract(new BigInteger("3"), new BigInteger("2")));
        assertEquals(new BigDecimal("1.0"), OgnlOps.subtract(new BigDecimal("3.0"), new BigDecimal("2.0")));
    }

    @Test
    void testMultiply() {
        assertEquals(6, OgnlOps.multiply(2, 3));
        assertEquals(new BigInteger("6"), OgnlOps.multiply(new BigInteger("2"), new BigInteger("3")));
        assertEquals(new BigDecimal("6.00"), OgnlOps.multiply(new BigDecimal("2.0"), new BigDecimal("3.0")));
    }

    @Test
    void testDivide() {
        assertEquals(2, OgnlOps.divide(6, 3));
        assertEquals(new BigInteger("2"), OgnlOps.divide(new BigInteger("6"), new BigInteger("3")));
        assertEquals(new BigDecimal("2.0"), OgnlOps.divide(new BigDecimal("6.0"), new BigDecimal("3.0")));
    }

    @Test
    void testRemainder() {
        assertEquals(1, OgnlOps.remainder(7, 3));
        assertEquals(new BigInteger("1"), OgnlOps.remainder(new BigInteger("7"), new BigInteger("3")));
    }

    @Test
    void testNegate() {
        assertEquals(-1, OgnlOps.negate(1));
        assertEquals(new BigInteger("-1"), OgnlOps.negate(new BigInteger("1")));
        assertEquals(new BigDecimal("-1.0"), OgnlOps.negate(new BigDecimal("1.0")));
    }

    @Test
    void testBitNegate() {
        assertEquals(~1, OgnlOps.bitNegate(1));
        assertEquals(new BigInteger("-2"), OgnlOps.bitNegate(new BigInteger("1")));
    }

    @Test
    void testGetEscapeString() {
        assertEquals("\\t", OgnlOps.getEscapeString("\t"));
        assertEquals("\\n", OgnlOps.getEscapeString("\n"));
    }

    @Test
    void testGetEscapedChar() {
        assertEquals("\\t", OgnlOps.getEscapedChar('\t'));
        assertEquals("\\n", OgnlOps.getEscapedChar('\n'));
    }

    @Test
    void testReturnValue() {
        assertEquals("test", OgnlOps.returnValue(null, "test"));
    }

    @Test
    void testCastToRuntime() {
        RuntimeException ex = new RuntimeException("test");
        assertEquals(ex, OgnlOps.castToRuntime(ex));
    }
}
