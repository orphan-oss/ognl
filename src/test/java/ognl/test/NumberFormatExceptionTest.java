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
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberFormatExceptionTest {

    private Simple simple;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        simple = new Simple();
        context = Ognl.createDefaultContext(simple);
    }

    @Test
    void testFloatValueValid() throws Exception {
        Ognl.setValue("floatValue", context, simple, 10f);
        assertEquals(10f, Ognl.getValue("floatValue", context, simple));
    }

    @Test
    void testFloatValueInvalid() {
        assertThrows(OgnlException.class,
                () -> Ognl.setValue("floatValue", context, simple, "x10x")
                , "x10x");
    }

    @Test
    void testIntValueValid() throws Exception {
        Ognl.setValue("intValue", context, simple, 34);
        Object actual = Ognl.getValue("intValue", context, simple);
        assertEquals(34, actual);
    }

    @Test
    void testIntValueInvalidString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("intValue", context, simple, "foobar"));
    }

    @Test
    void testIntValueEmptyString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("intValue", context, simple, ""));
    }

    @Test
    void testIntValueWhitespaceString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("intValue", context, simple, "       \t"));
    }

    @Test
    void testIntValueValidWhitespaceString() throws Exception {
        Ognl.setValue("intValue", context, simple, "       \t1234\t\t");
        Object actual = Ognl.getValue("intValue", context, simple);
        assertEquals(1234, actual);
    }

    @Test
    void testBigIntValueValid() throws Exception {
        Ognl.setValue("bigIntValue", context, simple, BigInteger.valueOf(34));
        Object actual = Ognl.getValue("bigIntValue", context, simple);
        assertEquals(BigInteger.valueOf(34), actual);
    }

    @Test
    void testBigIntValueNull() throws Exception {
        Ognl.setValue("bigIntValue", context, simple, null);
        Object actual = Ognl.getValue("bigIntValue", context, simple);
        assertNull(actual);
    }

    @Test
    void testBigIntValueEmptyString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("bigIntValue", context, simple, ""));
    }

    @Test
    void testBigIntValueInvalidString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("bigIntValue", context, simple, "foobar"));
    }

    @Test
    void testBigDecValueValid() throws Exception {
        Ognl.setValue("bigDecValue", context, simple, BigDecimal.valueOf(34.55));
        Object actual = Ognl.getValue("bigDecValue", context, simple);
        assertEquals(BigDecimal.valueOf(34.55), actual);
    }

    @Test
    void testBigDecValueNull() throws Exception {
        Ognl.setValue("bigDecValue", context, simple, null);
        Object actual = Ognl.getValue("bigDecValue", context, simple);
        assertNull(actual);
    }

    @Test
    void testBigDecValueEmptyString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("bigDecValue", context, simple, ""));
    }

    @Test
    void testBigDecValueInvalidString() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("bigDecValue", context, simple, "foobar"));
    }
}
