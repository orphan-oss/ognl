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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for null-safe operator (.?) including edge cases,
 * combined operators, and interaction with other OGNL features.
 */
public class NullSafeIntegrationTest {

    private OgnlContext context;

    static class User {
        private String name;
        private Profile profile;

        User(String name, Profile profile) {
            this.name = name;
            this.profile = profile;
        }

        public String getName() { return name; }
        public Profile getProfile() { return profile; }
    }

    static class Profile {
        private String bio;
        private Address address;
        private boolean verified;

        Profile(String bio, Address address) {
            this.bio = bio;
            this.address = address;
        }

        public String getBio() { return bio; }
        public Address getAddress() { return address; }
        public boolean isVerified() { return verified; }
    }

    static class Address {
        private String city;
        private String street;

        Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        public String getCity() { return city; }
        public String getStreet() { return street; }
    }

    @BeforeEach
    void setUp() {
        context = (OgnlContext) Ognl.createDefaultContext(null);
    }

    // ========== Combined Operator Tests ==========

    @Test
    void nullSafeWithNullCoalescing() throws Exception {
        User user = new User("Alice", null);
        // Use ternary operator since OGNL doesn't have ?: Elvis operator
        Object result = Ognl.getValue("profile.?bio != null ? profile.?bio : 'default'", context, user);
        assertEquals("default", result);
    }

    @Test
    void nullSafeWithConditional() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("profile.?bio != null ? 'yes' : 'no'", context, user);
        assertEquals("no", result);
    }

    @Test
    void nullSafeInAssignmentContext() throws Exception {
        User user = new User("Alice", null);
        Ognl.getValue("#result = profile.?bio", context, user);
        assertNull(context.get("result"));
    }

    @Test
    void nullSafeWithArithmetic() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("value", null);
        // Use ternary operator since OGNL doesn't have ?: Elvis operator
        Object result = Ognl.getValue("(value != null ? value : 0) + 10", context, root);
        assertEquals(10, result);
    }

    // ========== Edge Cases ==========

    @Test
    void consecutiveNullSafeOperators() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("profile.?address.?city", context, user);
        assertNull(result);
    }

    @Test
    void nullSafeOnLiterals() throws Exception {
        // Literals are never null, so this should work
        Object result = Ognl.getValue("'hello'.toString()", context, new Object());
        assertEquals("hello", result);
    }

    @Test
    void nullSafeOnStaticMethod() throws Exception {
        // Static methods combined with null-safe property access
        Object result = Ognl.getValue("@java.lang.System@getProperty('java.version').?toString()", context, new Object());
        assertNotNull(result);
    }

    @Test
    void veryDeepNullSafeChain() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue(
            "profile.?address.?city.?toString().?toLowerCase().?substring(0).?trim().?length().?toString().?isEmpty()",
            context, user);
        assertNull(result, "Very deep null-safe chain should return null if any intermediate is null");
    }

    @Test
    void nullSafeWithThis() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("obj", null);

        Object result = Ognl.getValue("#obj.?toString()", context, new Object());
        assertNull(result);
    }

    @Test
    void nullSafeWithRoot() throws Exception {
        Object result = Ognl.getValue("#root.?toString()", context, (Object) null);
        assertNull(result);
    }

    // ========== Integration Tests ==========

    @Test
    void nullSafeDoesNotAffectRegularAccess() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));

        // Regular access should still work
        Object result1 = Ognl.getValue("profile.bio", context, user);
        assertEquals("Bio", result1);

        // Null-safe access should also work
        Object result2 = Ognl.getValue("profile.?bio", context, user);
        assertEquals("Bio", result2);
    }

    @Test
    void nullSafeBackwardCompatibility() throws Exception {
        // Ensure existing expressions without .? still work
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));

        Object result = Ognl.getValue("profile.address.city", context, user);
        assertEquals("NYC", result);
    }

    @Test
    void nullSafeIndependentOfShortCircuit() throws Exception {
        // Null-safe should work regardless of short-circuit system property
        User user = new User("Alice", null);

        // With null-safe, should always return null
        Object result = Ognl.getValue("profile.?bio", context, user);
        assertNull(result);
    }

    // ========== Intermediate Null Check Tests ==========

    @Test
    void nullSafeWithIntermediateNull() throws Exception {
        // Create a chain where intermediate value becomes null
        User user = new User("Alice", new Profile(null, null));

        // Access through multiple levels where intermediate is null
        Object result = Ognl.getValue("profile.?address.?city", context, user);
        assertNull(result);
    }

    @Test
    void nullSafeChainWithMultipleNullChecks() throws Exception {
        // Test that the loop's null check (line 94-95 in ASTChain) is hit
        User user = new User("Alice", null);

        // This creates a chain that needs multiple null checks
        Object result = Ognl.getValue("profile.?address.?city.?toString()", context, user);
        assertNull(result);
    }

    @Test
    void nullSafeWithNestedPropertyAccess() throws Exception {
        // Create nested structure to test intermediate null handling
        User user = new User("Alice", new Profile("Bio", new Address(null, "Street")));

        Object result = Ognl.getValue("profile.?address.?city.?length()", context, user);
        assertNull(result);
    }
}
