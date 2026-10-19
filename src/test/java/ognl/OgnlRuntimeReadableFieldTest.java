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

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OgnlRuntime#hasGetProperty} must promise exactly the field reads {@link OgnlRuntime#getFieldValue}
 * will perform: it refuses a static field, so a property backed only by one is not readable. The read-side
 * mirror of {@link OgnlRuntimeSettableFieldTest}.
 */
class OgnlRuntimeReadableFieldTest {

    public static class FieldsOnlyBean {
        public final String finalField = "final value";
        public static String staticField = "static value";
        public String plainField = "plain value";
    }

    @Test
    void hasGetPropertyIsFalseForStaticField() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertFalse(OgnlRuntime.hasGetProperty(context, bean, "staticField"));
    }

    @Test
    void hasGetPropertyIsTrueForFinalAndPlainFields() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertTrue(OgnlRuntime.hasGetProperty(context, bean, "finalField"));
        assertTrue(OgnlRuntime.hasGetProperty(context, bean, "plainField"));
    }

    @Test
    void hasGetPropertyAgreesWithGetValue() throws Exception {
        for (String property : Arrays.asList("finalField", "staticField", "plainField")) {
            FieldsOnlyBean bean = new FieldsOnlyBean();
            OgnlContext context = Ognl.createDefaultContext(bean);
            boolean promised = OgnlRuntime.hasGetProperty(context, bean, property);
            boolean performed;
            try {
                Ognl.getValue(property, context, bean);
                performed = true;
            } catch (OgnlException e) {
                performed = false;
            }
            assertEquals(performed, promised, property);
        }
    }

    @Test
    void staticFieldStaysReadableThroughStaticFieldSyntax() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertEquals("static value", Ognl.getValue("@ognl.OgnlRuntimeReadableFieldTest$FieldsOnlyBean@staticField", context, bean));
    }

    @Test
    void hasGetPropertyIsFalseForMissingField() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean);
        assertFalse(OgnlRuntime.hasGetProperty(context, bean, "noSuchField"));
    }

    @Test
    void hasGetPropertyIsFalseWhenMemberAccessDeniesTheField() throws Exception {
        FieldsOnlyBean bean = new FieldsOnlyBean();
        OgnlContext context = Ognl.createDefaultContext(bean, new DenyingAccess("plainField"));
        assertFalse(OgnlRuntime.hasGetProperty(context, bean, "plainField"));
        assertTrue(OgnlRuntime.hasGetProperty(context, bean, "finalField"));
    }

    private static class DenyingAccess extends AbstractMemberAccess {

        private final String deniedMember;

        DenyingAccess(String deniedMember) {
            this.deniedMember = deniedMember;
        }

        @Override
        public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
            return Modifier.isPublic(member.getModifiers()) && !member.getName().equals(deniedMember);
        }
    }
}
