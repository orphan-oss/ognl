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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue #657: the lenient accessor lookup ({@link OgnlRuntime#getReadMethod(Class, String)},
 * {@link OgnlRuntime#getWriteMethod(Class, String)}) must bind the same property names as the
 * strict one, so a {@code MemberAccess} or a framework that reasons about the name sees the
 * member evaluation actually touches.
 */
class OgnlRuntimeStrictPropertyNameTest {

    public static class Bean {
        private String uRange;

        public void setuRange(String uRange) {
            this.uRange = uRange;
        }

        public String getuRange() {
            return uRange;
        }

        public boolean isPageBreakAfter() {
            return true;
        }

        public boolean hasChildren() {
            return false;
        }

        public String describe() {
            return "bean";
        }
    }

    public static class LombokStyleBean {
        private String uRange;

        public void setURange(String uRange) {
            this.uRange = uRange;
        }

        public String getURange() {
            return uRange;
        }
    }

    public static class DescriptorBean {
        public String getURL() {
            return "the-url";
        }

        public String getCustomerName() {
            return "the-name";
        }
    }

    private static class PublicOnly extends AbstractMemberAccess {
        @Override
        public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
            return Modifier.isPublic(member.getModifiers());
        }
    }

    @BeforeEach
    void clearCaches() {
        OgnlRuntime.clearAdditionalCache();
    }

    @Test
    void lenientLookupBindsTheExactName() {
        assertEquals("setuRange", OgnlRuntime.getWriteMethod(Bean.class, "uRange").getName());
        assertEquals("getuRange", OgnlRuntime.getReadMethod(Bean.class, "uRange").getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"URange", "urange", "URANGE", "uRANGE"})
    void lenientLookupRejectsMisCasedNames(String name) {
        assertNull(OgnlRuntime.getWriteMethod(Bean.class, name));
        assertNull(OgnlRuntime.getReadMethod(Bean.class, name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"URange", "urange", "URANGE", "uRANGE"})
    void evaluationRejectsMisCasedNames(String name) throws Exception {
        Bean bean = new Bean();
        OgnlContext context = Ognl.createDefaultContext(bean, new PublicOnly());

        assertThrows(NoSuchPropertyException.class, () -> Ognl.setValue(name, context, bean, "x"));
        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue(name, context, bean));
        assertNull(bean.uRange);
    }

    @Test
    void evaluationBindsTheExactName() throws Exception {
        Bean bean = new Bean();
        OgnlContext context = Ognl.createDefaultContext(bean, new PublicOnly());

        Ognl.setValue("uRange", context, bean, "x");

        assertEquals("x", bean.uRange);
        assertEquals("x", Ognl.getValue("uRange", context, bean));
    }

    @Test
    void lenientLookupAcceptsBothAccessorCapitalizations() {
        assertEquals("setuRange", OgnlRuntime.getWriteMethod(Bean.class, "uRange").getName());
        assertEquals("setURange", OgnlRuntime.getWriteMethod(LombokStyleBean.class, "uRange").getName());
        assertEquals("setURange", OgnlRuntime.getWriteMethod(LombokStyleBean.class, "URange").getName());
        assertEquals("getURange", OgnlRuntime.getReadMethod(LombokStyleBean.class, "URange").getName());
    }

    @Test
    void lenientLookupKeepsBooleanAndPlainMethodNames() {
        assertEquals("isPageBreakAfter", OgnlRuntime.getReadMethod(Bean.class, "pageBreakAfter").getName());
        assertEquals("hasChildren", OgnlRuntime.getReadMethod(Bean.class, "children").getName());
        assertEquals("describe", OgnlRuntime.getReadMethod(Bean.class, "describe").getName());
        assertEquals("getuRange", OgnlRuntime.getReadMethod(Bean.class, "getuRange").getName());
        assertEquals("setuRange", OgnlRuntime.getWriteMethod(Bean.class, "setuRange").getName());

        assertNull(OgnlRuntime.getReadMethod(Bean.class, "PAGEBREAKAFTER"));
        assertNull(OgnlRuntime.getReadMethod(Bean.class, "DESCRIBE"));
        assertNull(OgnlRuntime.getReadMethod(Bean.class, "GETURANGE"));
    }

    @Test
    void propertyDescriptorLookupMatchesTheWholeNameExactly() {
        PropertyDescriptor url = OgnlRuntime.getProperty(DescriptorBean.class, "URL");
        PropertyDescriptor customerName = OgnlRuntime.getProperty(DescriptorBean.class, "customerName");

        assertNotNull(url);
        assertEquals("URL", url.getName());
        assertNotNull(customerName);
        assertEquals("customerName", customerName.getName());

        assertNull(OgnlRuntime.getProperty(DescriptorBean.class, "url"));
        assertNull(OgnlRuntime.getProperty(DescriptorBean.class, "RL"));
        assertNull(OgnlRuntime.getProperty(DescriptorBean.class, "L"));
        assertNull(OgnlRuntime.getProperty(DescriptorBean.class, "Name"));
    }
}
