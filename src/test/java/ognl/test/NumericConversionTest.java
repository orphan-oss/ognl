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

import ognl.OgnlOps;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumericConversionTest {

    private void runTest(Object value, Class<?> toClass, Object expectedValue, int scale) {
        Object result = OgnlOps.convertValue(value, toClass);

        if (scale >= 0) {
            double scalingFactor = Math.pow(10, scale);
            double v1 = ((Number) result).doubleValue() * scalingFactor;
            double v2 = ((Number) expectedValue).doubleValue() * scalingFactor;

            assertEquals((int) v1, (int) v2);
        } else {
            assertEquals(result, expectedValue);
        }
    }

    @Test
    void testIntegerConversions() {
        runTest("55", Integer.class, 55, -1);
        runTest(55, Integer.class, 55, -1);
        runTest(55.0, Integer.class, 55, -1);
        runTest(true, Integer.class, 1, -1);
        runTest((byte) 55, Integer.class, 55, -1);
        runTest((char) 55, Integer.class, 55, -1);
        runTest((short) 55, Integer.class, 55, -1);
        runTest(55L, Integer.class, 55, -1);
        runTest(55.0f, Integer.class, 55, -1);
        runTest(new BigInteger("55"), Integer.class, 55, -1);
        runTest(new BigDecimal("55"), Integer.class, 55, -1);
    }

    @Test
    void testDoubleConversions() {
        runTest("55.1234", Double.class, 55.1234, -1);
        runTest(55, Double.class, 55.0, -1);
        runTest(55.1234, Double.class, 55.1234, -1);
        runTest(true, Double.class, 1.0, -1);
        runTest((byte) 55, Double.class, 55.0, -1);
        runTest((char) 55, Double.class, 55.0, -1);
        runTest((short) 55, Double.class, 55.0, -1);
        runTest(55L, Double.class, 55.0, -1);
        runTest(55.1234f, Double.class, 55.1234, 4);
        runTest(new BigInteger("55"), Double.class, 55.0, -1);
        runTest(new BigDecimal("55.1234"), Double.class, 55.1234, -1);
    }

    @Test
    void testBooleanConversions() {
        runTest("true", Boolean.class, true, -1);
        runTest(55, Boolean.class, true, -1);
        runTest(55.0, Boolean.class, true, -1);
        runTest(true, Boolean.class, true, -1);
        runTest((byte) 55, Boolean.class, true, -1);
        runTest((char) 55, Boolean.class, true, -1);
        runTest((short) 55, Boolean.class, true, -1);
        runTest(55L, Boolean.class, true, -1);
        runTest(55.0f, Boolean.class, true, -1);
        runTest(new BigInteger("55"), Boolean.class, true, -1);
        runTest(new BigDecimal("55"), Boolean.class, true, -1);
    }

    @Test
    void testByteConversions() {
        runTest("55", Byte.class, (byte) 55, -1);
        runTest(55, Byte.class, (byte) 55, -1);
        runTest(55.0, Byte.class, (byte) 55, -1);
        runTest(true, Byte.class, (byte) 1, -1);
        runTest((byte) 55, Byte.class, (byte) 55, -1);
        runTest((char) 55, Byte.class, (byte) 55, -1);
        runTest((short) 55, Byte.class, (byte) 55, -1);
        runTest(55L, Byte.class, (byte) 55, -1);
        runTest(55.0f, Byte.class, (byte) 55, -1);
        runTest(new BigInteger("55"), Byte.class, (byte) 55, -1);
        runTest(new BigDecimal("55"), Byte.class, (byte) 55, -1);
    }

    @Test
    void testCharacterConversions() {
        runTest("55", Character.class, (char) 55, -1);
        runTest(55, Character.class, (char) 55, -1);
        runTest(55.0, Character.class, (char) 55, -1);
        runTest(true, Character.class, (char) 1, -1);
        runTest((byte) 55, Character.class, (char) 55, -1);
        runTest((char) 55, Character.class, (char) 55, -1);
        runTest((short) 55, Character.class, (char) 55, -1);
        runTest(55L, Character.class, (char) 55, -1);
        runTest(55.0f, Character.class, (char) 55, -1);
        runTest(new BigInteger("55"), Character.class, (char) 55, -1);
        runTest(new BigDecimal("55"), Character.class, (char) 55, -1);
    }

    @Test
    void testShortConversions() {
        runTest("55", Short.class, (short) 55, -1);
        runTest(55, Short.class, (short) 55, -1);
        runTest(55.0, Short.class, (short) 55, -1);
        runTest(true, Short.class, (short) 1, -1);
        runTest((byte) 55, Short.class, (short) 55, -1);
        runTest((char) 55, Short.class, (short) 55, -1);
        runTest((short) 55, Short.class, (short) 55, -1);
        runTest(55L, Short.class, (short) 55, -1);
        runTest(55.0f, Short.class, (short) 55, -1);
        runTest(new BigInteger("55"), Short.class, (short) 55, -1);
        runTest(new BigDecimal("55"), Short.class, (short) 55, -1);
    }

    @Test
    void testLongConversions() {
        runTest("55", Long.class, 55L, -1);
        runTest(55, Long.class, 55L, -1);
        runTest(55.0, Long.class, 55L, -1);
        runTest(true, Long.class, 1L, -1);
        runTest((byte) 55, Long.class, 55L, -1);
        runTest((char) 55, Long.class, 55L, -1);
        runTest((short) 55, Long.class, 55L, -1);
        runTest(55L, Long.class, 55L, -1);
        runTest(55.0f, Long.class, 55L, -1);
        runTest(new BigInteger("55"), Long.class, 55L, -1);
        runTest(new BigDecimal("55"), Long.class, 55L, -1);
    }

    @Test
    void testFloatConversions() {
        runTest("55.1234", Float.class, 55.1234f, -1);
        runTest(55, Float.class, 55.0f, -1);
        runTest(55.1234, Float.class, 55.1234f, -1);
        runTest(true, Float.class, 1.0f, -1);
        runTest((byte) 55, Float.class, 55.0f, -1);
        runTest((char) 55, Float.class, 55.0f, -1);
        runTest((short) 55, Float.class, 55.0f, -1);
        runTest(55L, Float.class, 55.0f, -1);
        runTest(55.1234f, Float.class, 55.1234f, 4);
        runTest(new BigInteger("55"), Float.class, 55.0f, -1);
        runTest(new BigDecimal("55.1234"), Float.class, 55.1234f, -1);
    }

    @Test
    void testBigIntegerConversions() {
        runTest("55", BigInteger.class, new BigInteger("55"), -1);
        runTest(55, BigInteger.class, new BigInteger("55"), -1);
        runTest(55.0, BigInteger.class, new BigInteger("55"), -1);
        runTest(true, BigInteger.class, new BigInteger("1"), -1);
        runTest((byte) 55, BigInteger.class, new BigInteger("55"), -1);
        runTest((char) 55, BigInteger.class, new BigInteger("55"), -1);
        runTest((short) 55, BigInteger.class, new BigInteger("55"), -1);
        runTest(55L, BigInteger.class, new BigInteger("55"), -1);
        runTest(55.0f, BigInteger.class, new BigInteger("55"), -1);
        runTest(new BigInteger("55"), BigInteger.class, new BigInteger("55"), -1);
        runTest(new BigDecimal("55"), BigInteger.class, new BigInteger("55"), -1);
    }

    @Test
    void testBigDecimalConversions() {
        runTest("55.1234", BigDecimal.class, new BigDecimal("55.1234"), -1);
        runTest(55, BigDecimal.class, new BigDecimal("55"), -1);
        runTest(55.1234, BigDecimal.class, new BigDecimal("55.1234"), 4);
        runTest(true, BigDecimal.class, new BigDecimal("1"), -1);
        runTest((byte) 55, BigDecimal.class, new BigDecimal("55"), -1);
        runTest((char) 55, BigDecimal.class, new BigDecimal("55"), -1);
        runTest((short) 55, BigDecimal.class, new BigDecimal("55"), -1);
        runTest(55L, BigDecimal.class, new BigDecimal("55"), -1);
        runTest(55.1234f, BigDecimal.class, new BigDecimal("55.1234"), 4);
        runTest(new BigInteger("55"), BigDecimal.class, new BigDecimal("55"), -1);
        runTest(new BigDecimal("55.1234"), BigDecimal.class, new BigDecimal("55.1234"), -1);
    }
}
