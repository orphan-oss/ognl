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

import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Simple;
import ognl.test.util.DualModeTestUtils;
import ognl.test.util.OgnlExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testFloatValueValid(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("floatValue", context, simple, 10f, mode);
        assertEquals(10f, DualModeTestUtils.prepareAndEvaluate("floatValue", context, simple, mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testFloatValueInvalid(OgnlExecutionMode mode) {
        assertThrows(Exception.class,
                () -> DualModeTestUtils.prepareAndSetValue("floatValue", context, simple, "x10x", mode)
                , "x10x");
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testIntValueValid(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("intValue", context, simple, 34, mode);
        Object actual = DualModeTestUtils.prepareAndEvaluate("intValue", context, simple, mode);
        assertEquals(34, actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testIntValueInvalidString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("intValue", context, simple, "foobar", mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testIntValueEmptyString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("intValue", context, simple, "", mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testIntValueWhitespaceString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("intValue", context, simple, "       \t", mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testIntValueValidWhitespaceString(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("intValue", context, simple, "       \t1234\t\t", mode);
        Object actual = DualModeTestUtils.prepareAndEvaluate("intValue", context, simple, mode);
        assertEquals(1234, actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigIntValueValid(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("bigIntValue", context, simple, BigInteger.valueOf(34), mode);
        Object actual = DualModeTestUtils.prepareAndEvaluate("bigIntValue", context, simple, mode);
        assertEquals(BigInteger.valueOf(34), actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigIntValueNull(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("bigIntValue", context, simple, null, mode);
        Object actual = DualModeTestUtils.prepareAndEvaluate("bigIntValue", context, simple, mode);
        assertNull(actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigIntValueEmptyString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("bigIntValue", context, simple, "", mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigIntValueInvalidString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("bigIntValue", context, simple, "foobar", mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigDecValueValid(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("bigDecValue", context, simple, BigDecimal.valueOf(34.55), mode);
        Object actual = DualModeTestUtils.prepareAndEvaluate("bigDecValue", context, simple, mode);
        assertEquals(BigDecimal.valueOf(34.55), actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigDecValueNull(OgnlExecutionMode mode) throws Exception {
        DualModeTestUtils.prepareAndSetValue("bigDecValue", context, simple, null, mode);
        Object actual = DualModeTestUtils.prepareAndEvaluate("bigDecValue", context, simple, mode);
        assertNull(actual);
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigDecValueEmptyString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("bigDecValue", context, simple, "", mode));
    }

    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testBigDecValueInvalidString(OgnlExecutionMode mode) {
        assertThrows(Exception.class, () -> DualModeTestUtils.prepareAndSetValue("bigDecValue", context, simple, "foobar", mode));
    }
}
