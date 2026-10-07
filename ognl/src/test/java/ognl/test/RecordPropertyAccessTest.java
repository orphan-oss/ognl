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

import ognl.AbstractMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #646: record accessors are resolved through {@link OgnlRuntime#getReadMethod(Class, String)}.
 */
class RecordPropertyAccessTest {

    public record Customer(String name) {
    }

    public record RangedCustomer(String name, String uRange) {
    }

    public static class Calculator {
        public String compute() {
            return "computed";
        }
    }

    @BeforeEach
    void setUp() {
        OgnlRuntime.clearAdditionalCache();
    }

    @Test
    void readsRecordComponent() throws Exception {
        Customer customer = new Customer("Alice");
        OgnlContext context = Ognl.createDefaultContext(customer, new PublicOnlyAccess(null));

        Object tree = Ognl.parseExpression("name");

        assertEquals("Alice", Ognl.getValue(tree, context, customer));
        assertEquals("Alice", Ognl.getValue(tree, context, customer));
    }

    @Test
    void cachesReadMethodLookup() {
        Method first = OgnlRuntime.getReadMethod(Customer.class, "name");
        Method second = OgnlRuntime.getReadMethod(Customer.class, "name");

        assertEquals("name", first.getName());
        assertSame(first, second);
    }

    @Test
    void cachesMissingReadMethod() {
        assertNull(OgnlRuntime.getReadMethod(Customer.class, "missing"));
        assertNull(OgnlRuntime.getReadMethod(Customer.class, "missing"));
    }

    @Test
    void clearAdditionalCacheDropsReadMethods() {
        Method first = OgnlRuntime.getReadMethod(Customer.class, "name");

        OgnlRuntime.clearAdditionalCache();

        assertNotSame(first, OgnlRuntime.getReadMethod(Customer.class, "name"));
    }

    @Test
    void cachedReadMethodStillHonoursMemberAccess() throws Exception {
        Customer customer = new Customer("Alice");
        OgnlContext context = Ognl.createDefaultContext(customer, new PublicOnlyAccess("name"));
        Object tree = Ognl.parseExpression("name");

        OgnlRuntime.getReadMethod(Customer.class, "name");

        assertThrows(OgnlException.class, () -> Ognl.getValue(tree, context, customer));
        assertThrows(OgnlException.class, () -> Ognl.getValue(tree, context, customer));
    }

    @Test
    void hasGetPropertyAgreesWithGetValueForRecordComponents() throws Exception {
        RangedCustomer customer = new RangedCustomer("Alice", "r1");
        OgnlContext context = Ognl.createDefaultContext(customer, new PublicOnlyAccess(null));

        assertEquals("Alice", Ognl.getValue("name", context, customer));
        assertTrue(OgnlRuntime.hasGetProperty(context, customer, "name"));
        assertEquals("r1", Ognl.getValue("uRange", context, customer));
        assertTrue(OgnlRuntime.hasGetProperty(context, customer, "uRange"));
    }

    @Test
    void hasGetPropertyAgreesWithGetValueForMisCasedRecordComponents() throws Exception {
        RangedCustomer customer = new RangedCustomer("Alice", "r1");
        OgnlContext context = Ognl.createDefaultContext(customer, new PublicOnlyAccess(null));

        assertEquals("Alice", Ognl.getValue("NAME", context, customer));
        assertTrue(OgnlRuntime.hasGetProperty(context, customer, "NAME"));
        assertEquals("r1", Ognl.getValue("URange", context, customer));
        assertTrue(OgnlRuntime.hasGetProperty(context, customer, "URange"));
    }

    @Test
    void hasGetPropertyAgreesWithGetValueWhenReadMethodsAreIgnored() throws Exception {
        RangedCustomer customer = new RangedCustomer("Alice", "r1");
        OgnlContext context = Ognl.createDefaultContext(customer, new PublicOnlyAccess(null));
        context.setIgnoreReadMethods(true);

        assertThrows(OgnlException.class, () -> Ognl.getValue("name", context, customer));
        assertFalse(OgnlRuntime.hasGetProperty(context, customer, "name"));
    }

    @Test
    void hasGetPropertyHonoursMemberAccessForRecordAccessor() throws Exception {
        RangedCustomer customer = new RangedCustomer("Alice", "r1");
        OgnlContext context = Ognl.createDefaultContext(customer, new PublicOnlyAccess("name"));

        assertFalse(OgnlRuntime.hasGetProperty(context, customer, "name"));
        assertTrue(OgnlRuntime.hasGetProperty(context, customer, "uRange"));
    }

    @Test
    void hasGetPropertyStillIgnoresPrefixlessMethodsOnOrdinaryClasses() throws Exception {
        Calculator calculator = new Calculator();
        OgnlContext context = Ognl.createDefaultContext(calculator, new PublicOnlyAccess(null));

        assertFalse(OgnlRuntime.hasGetProperty(context, calculator, "compute"));
    }

    private static class PublicOnlyAccess extends AbstractMemberAccess {

        private final String deniedMember;

        PublicOnlyAccess(String deniedMember) {
            this.deniedMember = deniedMember;
        }

        @Override
        public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
            return Modifier.isPublic(member.getModifiers()) && !member.getName().equals(deniedMember);
        }
    }
}
