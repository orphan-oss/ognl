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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue #646: record accessors are resolved through {@link OgnlRuntime#getReadMethod(Class, String)}.
 */
class RecordPropertyAccessTest {

    public record Customer(String name) {
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
