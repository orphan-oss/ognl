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

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the 3.4.x rewrite of {@code getDeclaredMethods}: for a property
 * like {@code uRange} both the JavaBeans-compliant accessors ({@code setuRange}) and the
 * naively capitalized ones ({@code setURange}) must resolve, the spec-compliant accessor
 * taking precedence when a class declares both.
 */
class OgnlRuntimeBeanCapitalizationTest {

    public static class Bean {
        private String uRange;

        public void setuRange(String uRange) {
            this.uRange = uRange;
        }

        public String getuRange() {
            return uRange;
        }
    }

    /** Lombok-style accessors: naive capitalization of the field name. */
    public static class LombokStyleBean {
        private String uRange;

        public void setURange(String uRange) {
            this.uRange = uRange;
        }

        public String getURange() {
            return uRange;
        }
    }

    /** Declares both styles; the spec-compliant accessor must win. */
    public static class BothStylesBean {
        private String uRange;
        String lastSetterUsed;
        String lastGetterUsed;

        public void setuRange(String uRange) {
            this.uRange = uRange;
            this.lastSetterUsed = "setuRange";
        }

        public String getuRange() {
            this.lastGetterUsed = "getuRange";
            return uRange;
        }

        public void setURange(String uRange) {
            this.uRange = uRange;
            this.lastSetterUsed = "setURange";
        }

        public String getURange() {
            this.lastGetterUsed = "getURange";
            return uRange;
        }
    }

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(new Bean());
    }

    @Test
    void getDeclaredMethodsFindsSpecCompliantSetter() {
        List<Method> methods = OgnlRuntime.getDeclaredMethods(Bean.class, "uRange", true);
        assertEquals(1, methods.size());
        assertEquals("setuRange", methods.get(0).getName());
    }

    @Test
    void getDeclaredMethodsFindsSpecCompliantGetter() {
        List<Method> methods = OgnlRuntime.getDeclaredMethods(Bean.class, "uRange", false);
        assertEquals(1, methods.size());
        assertEquals("getuRange", methods.get(0).getName());
    }

    @Test
    void getSetMethodResolvesSpecCompliantSetter() throws Exception {
        Method method = OgnlRuntime.getSetMethod(context, Bean.class, "uRange");
        assertNotNull(method);
        assertEquals("setuRange", method.getName());
    }

    @Test
    void hasSetMethodResolvesSpecCompliantSetter() throws Exception {
        assertTrue(OgnlRuntime.hasSetMethod(context, new Bean(), Bean.class, "uRange"));
    }

    @Test
    void setAndGetValueUseSpecCompliantAccessors() throws Exception {
        Bean bean = new Bean();
        Ognl.setValue("uRange", context, bean, "0221123");
        assertEquals("0221123", bean.getuRange());
        assertEquals("0221123", Ognl.getValue("uRange", context, bean));
    }

    @Test
    void ordinaryPropertiesKeepWorking() throws Exception {
        // control group: normal capitalization is unaffected
        assertFalse(OgnlRuntime.getDeclaredMethods(Bean.class, "class", false).isEmpty());
    }

    @Test
    void getSetMethodResolvesLombokStyleSetter() throws Exception {
        Method method = OgnlRuntime.getSetMethod(context, LombokStyleBean.class, "uRange");
        assertNotNull(method);
        assertEquals("setURange", method.getName());
    }

    @Test
    void setAndGetValueUseLombokStyleAccessors() throws Exception {
        LombokStyleBean bean = new LombokStyleBean();
        Ognl.setValue("uRange", context, bean, "0221123");
        assertEquals("0221123", bean.getURange());
        assertEquals("0221123", Ognl.getValue("uRange", context, bean));
    }

    @Test
    void specCompliantSetterTakesPrecedenceWhenBothStylesExist() throws Exception {
        BothStylesBean bean = new BothStylesBean();
        Ognl.setValue("uRange", context, bean, "0221123");
        assertEquals("setuRange", bean.lastSetterUsed);
    }

    @Test
    void specCompliantGetterTakesPrecedenceWhenBothStylesExist() throws Exception {
        BothStylesBean bean = new BothStylesBean();
        bean.setuRange("0221123");
        assertEquals("0221123", Ognl.getValue("uRange", context, bean));
        assertEquals("getuRange", bean.lastGetterUsed);
    }
}
