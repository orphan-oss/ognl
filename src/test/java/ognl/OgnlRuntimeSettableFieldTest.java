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
package ognl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OgnlRuntime#hasSetProperty} must promise exactly the field writes
 * {@link OgnlRuntime#setFieldValue} will perform: it refuses a static or final field, so a property
 * backed only by one of those is not settable and must not be reported as such. Reads are a separate
 * question - a final field is readable - so narrowing this must not narrow
 * {@link OgnlRuntime#hasGetProperty}.
 */
class OgnlRuntimeSettableFieldTest {

    public static class FieldsOnlyBean {
        public final String finalField = "final value";
        public static String staticField = "static value";
        public String plainField = "plain value";
    }

    public static class FinalFieldWithSetterBean {
        public final String uRange = "final value";

        public void setuRange(String uRange) {
            // a real setter alongside the final field
        }
    }

    @Test
    void hasSetPropertyIsFalseForFinalField() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertFalse(OgnlRuntime.hasSetProperty(context, bean, "finalField"));
    }

    @Test
    void hasSetPropertyIsFalseForStaticField() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertFalse(OgnlRuntime.hasSetProperty(context, bean, "staticField"));
    }

    @Test
    void hasSetPropertyIsTrueForPlainField() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertTrue(OgnlRuntime.hasSetProperty(context, bean, "plainField"));
    }

    @Test
    void hasSetPropertyIsTrueWhenASetterExistsAlongsideAFinalField() throws Exception {
        FinalFieldWithSetterBean bean = new FinalFieldWithSetterBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertTrue(OgnlRuntime.hasSetProperty(context, bean, "uRange"));
    }

    @Test
    void hasSetPropertyAgreesWithSetFieldValue() throws Exception {
        for (String property : Arrays.asList("finalField", "staticField", "plainField")) {
            FieldsOnlyBean bean = new FieldsOnlyBean();
            OgnlContext context = Ognl.createDefaultContext(bean);
            boolean promised = OgnlRuntime.hasSetProperty(context, bean, property);
            boolean performed = OgnlRuntime.setFieldValue(context, bean, property, "written", true);
            assertEquals(performed, promised, property);
        }
    }

    @Test
    void finalFieldStaysReadable() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertTrue(OgnlRuntime.hasGetProperty(context, bean, "finalField"));
        assertEquals("final value", Ognl.getValue("finalField", context, bean));
    }
}
