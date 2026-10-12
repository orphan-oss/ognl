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

import ognl.DefaultMemberAccess;
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.beans.PropertyDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue #657: opt-in strict property-name mode matches accessor names exactly; the default lookup stays
 * case-insensitive.
 */
class PropertyNameMatchingTest {

    public static class RangeBean {
        private String uRange = "r0";

        public String getuRange() {
            return uRange;
        }

        public void setuRange(String uRange) {
            this.uRange = uRange;
        }
    }

    public static class UrlBean {
        public String getURL() {
            return "the-url";
        }

        public String getCustomerName() {
            return "the-name";
        }
    }

    public record Customer(String name, String uRange) {
    }

    public static class FlagBean {
        public boolean isActive() {
            return true;
        }

        public boolean hasChildren() {
            return true;
        }
    }

    private OgnlContext context;
    private Boolean originalStrictPropertyNames;

    @BeforeEach
    void setUp() {
        OgnlRuntime.clearCache();
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (originalStrictPropertyNames != null) {
            setUseStrictPropertyNames(originalStrictPropertyNames);
        }
        OgnlRuntime.clearCache();
    }

    @Test
    void strictPropertyNamesIsOffByDefault() throws Exception {
        assertFalse(OgnlRuntime.getUseStrictPropertyNamesValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"uRange", "URange", "urange", "URANGE", "uRANGE"})
    void lenientModeBindsMisCasedPropertyName(String name) throws Exception {
        setUseStrictPropertyNames(false);
        RangeBean bean = new RangeBean();

        Ognl.setValue(name, context, bean, "r1");

        assertEquals("r1", bean.getuRange());
        assertEquals("r1", Ognl.getValue(name, context, bean));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "NAME"})
    void lenientModeResolvesMisCasedRecordComponent(String name) throws Exception {
        setUseStrictPropertyNames(false);

        assertEquals("Alice", Ognl.getValue(name, context, new Customer("Alice", "r1")));
    }

    @Test
    void strictModeBindsExactPropertyName() throws Exception {
        setUseStrictPropertyNames(true);
        RangeBean bean = new RangeBean();

        Ognl.setValue("uRange", context, bean, "r1");

        assertEquals("r1", bean.getuRange());
        assertEquals("r1", Ognl.getValue("uRange", context, bean));
    }

    @ParameterizedTest
    @ValueSource(strings = {"URange", "urange", "URANGE", "uRANGE"})
    void strictModeDoesNotSetMisCasedPropertyName(String name) throws Exception {
        setUseStrictPropertyNames(true);
        RangeBean bean = new RangeBean();

        assertNull(OgnlRuntime.getSetMethod(context, RangeBean.class, name));
        assertThrows(NoSuchPropertyException.class, () -> Ognl.setValue(name, context, bean, "r1"));
        assertEquals("r0", bean.getuRange());
    }

    @ParameterizedTest
    @ValueSource(strings = {"URange", "urange", "URANGE", "uRANGE"})
    void strictModeDoesNotGetMisCasedPropertyName(String name) throws Exception {
        setUseStrictPropertyNames(true);

        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue(name, context, new RangeBean()));
    }

    @Test
    void strictModeResolvesRecordComponentByExactName() throws Exception {
        setUseStrictPropertyNames(true);
        Customer customer = new Customer("Alice", "r1");

        assertEquals("Alice", Ognl.getValue("name", context, customer));
        assertEquals("r1", Ognl.getValue("uRange", context, customer));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NAME", "Name", "URange"})
    void strictModeDoesNotResolveMisCasedRecordComponent(String name) throws Exception {
        setUseStrictPropertyNames(true);

        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue(name, context, new Customer("Alice", "r1")));
    }

    @Test
    void switchingToStrictModeDropsLenientLookups() throws Exception {
        setUseStrictPropertyNames(false);
        RangeBean bean = new RangeBean();
        Ognl.setValue("URange", context, bean, "r1");
        assertEquals("r1", Ognl.getValue("URange", context, bean));

        setUseStrictPropertyNames(true);

        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue("URange", context, bean));
        assertThrows(NoSuchPropertyException.class, () -> Ognl.setValue("URange", context, bean, "r2"));
    }

    @ParameterizedTest
    @CsvSource({"URL, URL", "url, URL", "customerName, customerName"})
    void getPropertyResolvesPropertyName(String name, String expected) {
        PropertyDescriptor descriptor = OgnlRuntime.getProperty(UrlBean.class, name);

        assertNotNull(descriptor);
        assertEquals(expected, descriptor.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RL", "L", "Name", "erName"})
    void getPropertyDoesNotMatchBySuffix(String name) {
        assertNull(OgnlRuntime.getProperty(UrlBean.class, name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"URange", "urange", "URANGE", "uRANGE"})
    void writeMethodMatchesMisCasedNameOnlyInLenientMode(String name) throws Exception {
        setUseStrictPropertyNames(false);
        assertNotNull(OgnlRuntime.getWriteMethod(RangeBean.class, name));

        setUseStrictPropertyNames(true);
        assertNull(OgnlRuntime.getWriteMethod(RangeBean.class, name));
    }

    @Test
    void strictModeReadMethodResolvesIsAndHasPrefixesByExactName() throws Exception {
        setUseStrictPropertyNames(true);

        assertEquals("isActive", OgnlRuntime.getReadMethod(FlagBean.class, "active").getName());
        assertEquals("hasChildren", OgnlRuntime.getReadMethod(FlagBean.class, "children").getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "aCTIVE", "CHILDREN", "cHILDREN"})
    void readMethodMatchesMisCasedIsAndHasPrefixesOnlyInLenientMode(String name) throws Exception {
        setUseStrictPropertyNames(false);
        assertNotNull(OgnlRuntime.getReadMethod(FlagBean.class, name));

        setUseStrictPropertyNames(true);
        assertNull(OgnlRuntime.getReadMethod(FlagBean.class, name));
    }

    @Test
    void strictModeEmptyNameMatchesNothing() throws Exception {
        setUseStrictPropertyNames(true);

        assertNull(OgnlRuntime.getReadMethod(RangeBean.class, ""));
        assertNull(OgnlRuntime.getWriteMethod(RangeBean.class, ""));
    }

    private void setUseStrictPropertyNames(boolean strict) {
        if (originalStrictPropertyNames == null) {
            originalStrictPropertyNames = OgnlRuntime.getUseStrictPropertyNamesValue();
        }
        OgnlRuntime.setUseStrictPropertyNames(strict);
    }
}
