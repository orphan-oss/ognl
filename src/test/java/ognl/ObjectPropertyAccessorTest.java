/*
 * Copyright 2020 OGNL Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ognl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Tests various methods / functionality of {@link ObjectPropertyAccessor}.
 */
class ObjectPropertyAccessorTest {
    private OgnlContext context;
    private ObjectPropertyAccessor propertyAccessor;

    @BeforeEach
    void setUp() throws Exception {
        context = Ognl.createDefaultContext(null, new ExcludedObjectMemberAccess(false));
        propertyAccessor = new ObjectPropertyAccessor();
    }

    /**
     * Public class for "setPossibleProperty" method tests.
     */
    public static class SimplePublicClass {
        private String gender = "male";
        public String email = "test@test.com";
        private String name = "name";
        private String age = "18";

        @SuppressWarnings("unused")
        public void setGender(String gender) {
            this.gender = gender;
        }

        @SuppressWarnings("unused")
        private void setEmail(String email) {
            this.email = email;
        }

        @SuppressWarnings("unused")
        private void setName(String email) {
            this.email = email;
        }

        @SuppressWarnings("unused")
        public void setname(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        private void setAge(String age) {
            this.age = age;
        }

        @SuppressWarnings("unused")
        public void setage(String age) {
            this.age = age;
        }
    }

    public static class KafkaFetcher {
        private final List<Future<?>> completedFutures = Collections.emptyList();

        @SuppressWarnings("unused")
        public boolean hasCompletedFutures() {
            return !completedFutures.isEmpty();
        }
    }

    @Test
    void testGetPossibleProperty() throws OgnlException {
        KafkaFetcher fetcher = new KafkaFetcher();
        assertEquals(Boolean.FALSE, propertyAccessor.getPossibleProperty(context, fetcher, "completedFutures"));
        OgnlContext defaultContext = Ognl.createDefaultContext(null, new ExcludedObjectMemberAccess(true));
        defaultContext.setIgnoreReadMethods(true);
        assertEquals(Collections.emptyList(), new ObjectPropertyAccessor().getPossibleProperty(defaultContext,
                fetcher, "completedFutures"));
    }

    @Test
    void testSetPossibleProperty() throws OgnlException {
        SimplePublicClass simplePublic = new SimplePublicClass();

        // 1. when set method is accessible and set method
        assertNotSame(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "gender", "female"));
        assertEquals("female", simplePublic.gender);

        // 2. when set method is NOT accessible and fallback to set field (field is accessible)
        assertNotSame(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "email", "admin@admin.com"));
        assertEquals("admin@admin.com", simplePublic.email);

        // 3. when set method is NOT accessible, field is NOT accessible, fallback to write method (write method is accessible)
        Method setMethod = OgnlRuntime.getSetMethod(context, SimplePublicClass.class, "name");
        assertNotNull(setMethod);
        assertEquals("setName", setMethod.getName());
        Method writeMethod = OgnlRuntime.getWriteMethod(SimplePublicClass.class, "name", null);
        assertNotNull(writeMethod);
        assertEquals("setname", writeMethod.getName());
        assertNotSame(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "name", "new name"));
        assertEquals("new name", simplePublic.name);

        // 4. when set method is NOT accessible, field is NOT accessible, fallback to write method (write method is NOT accessible)
        Method ageWriteMethod = OgnlRuntime.getWriteMethod(SimplePublicClass.class, "age", null);
        ((ExcludedObjectMemberAccess) context.getMemberAccess()).exclude(ageWriteMethod);

        assertNotNull(ageWriteMethod);
        assertEquals("setage", ageWriteMethod.getName());
        assertFalse(context.getMemberAccess().isAccessible(context, simplePublic, ageWriteMethod, "age"));
        setMethod = OgnlRuntime.getSetMethod(context, SimplePublicClass.class, "age");
        assertNotNull(setMethod);
        assertEquals("setAge", setMethod.getName());
        assertEquals(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "age", "99"));
        assertEquals("18", simplePublic.age);
    }
}
