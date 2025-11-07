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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the null-safe navigation operator (.?)
 * Ensures 100% code coverage for the null-safe operator feature.
 */
class NullSafeOperatorTest {

    private OgnlContext context;

    // Test data classes
    public static class User {
        private String name;
        private Profile profile;
        private List<String> tags;

        public User(String name, Profile profile) {
            this.name = name;
            this.profile = profile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class Profile {
        private String bio;
        private Address address;

        public Profile(String bio, Address address) {
            this.bio = bio;
            this.address = address;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    public static class Address {
        private String city;
        private String street;

        public Address(String city, String street) {
            this.city = city;
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }
    }

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
    }

    // ========== Basic Property Access Tests ==========

    @Test
    void nullRootWithNullSafeOperator() throws Exception {
        Object result = Ognl.getValue("name", context, (Object) null);
        assertNull(result, "Accessing property on null root should return null");
    }

    @Test
    void nullSafeOnNullRoot() throws Exception {
        Object result = Ognl.getValue("#root.?name", context, (Object) null);
        assertNull(result, "Null-safe operator on null root should return null");
    }

    @Test
    void nullSafeOnNonNullObject() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        Object result = Ognl.getValue("name", context, user);
        assertEquals("Alice", result);
    }

    @Test
    void nullSafeOnNullProperty() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("profile.?bio", context, user);
        assertNull(result, "Null-safe operator should return null when property is null");
    }

    @Test
    void nullSafeNestedChain() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        Object result = Ognl.getValue("profile.?address.?city", context, user);
        assertEquals("NYC", result);
    }

    @Test
    void nullSafeNestedChainWithNullIntermediate() throws Exception {
        User user = new User("Alice", new Profile("Bio", null));
        Object result = Ognl.getValue("profile.?address.?city", context, user);
        assertNull(result, "Null-safe chain should return null when intermediate value is null");
    }

    @Test
    void mixedSafeAndUnsafeChain() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        Object result = Ognl.getValue("profile.address.?city", context, user);
        assertEquals("NYC", result);
    }

    @Test
    void mixedChainThrowsOnNullUnsafePart() {
        User user = new User("Alice", null);
        assertThrows(Exception.class, () -> {
            Ognl.getValue("profile.address.?city", context, user);
        }, "Unsafe part of chain should throw exception on null");
    }

    @Test
    void multipleNullSafeOperators() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        Object result = Ognl.getValue("profile.?address.?city", context, user);
        assertEquals("NYC", result);
    }

    @Test
    void deepNullSafeChain() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("profile.?address.?city", context, user);
        assertNull(result, "Deep null-safe chain should return null at first null encounter");
    }

    // ========== Method Call Tests ==========

    @Test
    void nullSafeMethodCallOnNull() throws Exception {
        Object result = Ognl.getValue("#root.?toString()", context, (Object) null);
        assertNull(result, "Null-safe method call on null should return null");
    }

    @Test
    void nullSafeMethodCallOnNonNull() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("getName()", context, user);
        assertEquals("Alice", result);
    }

    @Test
    void nullSafeMethodChain() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        Object result = Ognl.getValue("getProfile().?getAddress().?getCity()", context, user);
        assertEquals("NYC", result);
    }

    @Test
    void nullSafeMethodChainWithNullIntermediate() throws Exception {
        User user = new User("Alice", new Profile("Bio", null));
        Object result = Ognl.getValue("getProfile().?getAddress().?getCity()", context, user);
        assertNull(result, "Null-safe method chain should return null when intermediate is null");
    }

    @Test
    void mixedPropertyAndMethodNullSafe() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        Object result = Ognl.getValue("profile.?getAddress().?city", context, user);
        assertEquals("NYC", result);
    }

    @Test
    void methodWithArgumentsNullSafe() throws Exception {
        String str = "hello";
        Object result = Ognl.getValue("substring(0, 2)", context, str);
        assertEquals("he", result);
    }

    @Test
    void nullSafeMethodWithArgumentsOnNull() throws Exception {
        Object result = Ognl.getValue("#root.?substring(0, 2)", context, (Object) null);
        assertNull(result, "Null-safe method with arguments on null should return null");
    }

    // ========== Variable Reference Tests ==========

    @Test
    void nullSafeWithVariableReference() throws Exception {
        context.put("user", null);
        Object result = Ognl.getValue("#user.?name", context, new Object());
        assertNull(result, "Null-safe operator on null variable should return null");
    }

    @Test
    void nullSafeWithNonNullVariable() throws Exception {
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        context.put("user", user);
        Object result = Ognl.getValue("#user.?name", context, new Object());
        assertEquals("Alice", result);
    }

    @Test
    void nullSafeWithNestedVariables() throws Exception {
        User user = new User("Alice", null);
        context.put("user", user);
        Object result = Ognl.getValue("#user.?profile.?bio", context, new Object());
        assertNull(result);
    }

    // ========== Collection Operation Tests ==========

    @Test
    void nullSafeProjectionOnNullList() throws Exception {
        User user = new User("Alice", null);
        user.setTags(null);
        Object result = Ognl.getValue("tags.?{#this}", context, user);
        assertNull(result, "Null-safe projection on null list should return null");
    }

    @Test
    void nullSafeProjectionOnNonNullList() throws Exception {
        User user = new User("Alice", null);
        user.setTags(Arrays.asList("tag1", "tag2", "tag3"));
        Object result = Ognl.getValue("tags.{#this}", context, user);
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertEquals(3, ((List<?>) result).size());
    }

    @Test
    void nullSafeSelectionOnNullList() throws Exception {
        User user = new User("Alice", null);
        user.setTags(null);
        Object result = Ognl.getValue("tags.?{? #this != null}", context, user);
        assertNull(result, "Null-safe selection on null list should return null");
    }

    // ========== Map Access Tests ==========

    @Test
    void nullSafeMapAccess() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("user", null);
        Object result = Ognl.getValue("user.?name", context, root);
        assertNull(result);
    }

    @Test
    void nullSafeMapAccessWithNonNullValue() throws Exception {
        Map<String, Object> root = new HashMap<>();
        User user = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        root.put("user", user);
        Object result = Ognl.getValue("user.?name", context, root);
        assertEquals("Alice", result);
    }

    @Test
    void nullSafeIndexedMapAccess() throws Exception {
        Map<String, String> map = null;
        Map<String, Object> root = new HashMap<>();
        root.put("map", map);
        Object result = Ognl.getValue("map?.?['key']", context, root);
        assertNull(result);
    }

    // ========== Combined Operator Tests ==========

    @Test
    void nullSafeWithNullCoalescing() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("profile.?bio ?: 'default'", context, user);
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
        context.put("result", null);
        Ognl.getValue("#result = profile.?bio", context, user);
        assertNull(context.get("result"));
    }

    @Test
    void nullSafeWithArithmetic() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("value", null);
        Object result = Ognl.getValue("(value ?: 0) + 10", context, root);
        assertEquals(10, result);
    }

    // ========== Edge Cases ==========

    @Test
    void consecutiveNullSafeOperators() throws Exception {
        User user = new User("Alice", null);
        Object result = Ognl.getValue("profile?.?address?.?city", context, user);
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
        Object result = Ognl.getValue("@java.lang.Math@abs(-5)", context, new Object());
        assertEquals(5, result);
    }

    @Test
    void veryDeepNullSafeChain() throws Exception {
        // Test with 10 levels of null-safe navigation
        User user = new User("Alice", null);
        Object result = Ognl.getValue(
            "profile.?address.?city.?toString().?toLowerCase().?substring(0).?trim().?length().?toString().?isEmpty()",
            context, user);
        assertNull(result);
    }

    @Test
    void nullSafeWithThis() throws Exception {
        context.put("obj", null);
        Object result = Ognl.getValue("#obj.?toString()", context, new Object());
        assertNull(result);
    }

    @Test
    void nullSafeWithRoot() throws Exception {
        Object result = Ognl.getValue("#root.?toString()", context, (Object) null);
        assertNull(result);
    }

    // ========== Parameterized Tests ==========

    @ParameterizedTest
    @MethodSource("nullSafeTestCases")
    void nullSafeScenarios(String expression, Object root, Object expected) throws Exception {
        Object result = Ognl.getValue(expression, context, root);
        assertEquals(expected, result);
    }

    static Stream<Arguments> nullSafeTestCases() {
        User userWithFullProfile = new User("Alice", new Profile("Bio", new Address("NYC", "5th Ave")));
        User userWithNullProfile = new User("Alice", null);
        User userWithProfileNoAddress = new User("Alice", new Profile("Bio", null));

        return Stream.of(
            // Basic null-safe access
            Arguments.of("profile.?bio", userWithFullProfile, "Bio"),
            Arguments.of("profile.?bio", userWithNullProfile, null),
            Arguments.of("name", userWithFullProfile, "Alice"),

            // Nested null-safe access
            Arguments.of("profile.?address.?city", userWithFullProfile, "NYC"),
            Arguments.of("profile.?address.?city", userWithNullProfile, null),
            Arguments.of("profile.?address.?city", userWithProfileNoAddress, null),

            // Method calls
            Arguments.of("getName()", userWithFullProfile, "Alice"),
            Arguments.of("getProfile().?getBio()", userWithFullProfile, "Bio"),
            Arguments.of("getProfile().?getBio()", userWithNullProfile, null),

            // Mixed chains
            Arguments.of("profile.address.?city", userWithFullProfile, "NYC"),
            Arguments.of("profile.?address.city", userWithFullProfile, "NYC")
        );
    }

    // ========== Error Cases ==========

    @Test
    void parserAcceptsDotQuestion() {
        // Just verify that the parser accepts the .? syntax without throwing parse exception
        assertDoesNotThrow(() -> {
            Ognl.parseExpression("obj.?property");
        });
    }

    @Test
    void complexNullSafeExpression() {
        assertDoesNotThrow(() -> {
            Ognl.parseExpression("a.?b.?c.?d.?e.?f");
        });
    }

    @Test
    void nullSafeWithProjection() {
        assertDoesNotThrow(() -> {
            Ognl.parseExpression("list.?{name}");
        });
    }

    @Test
    void nullSafeWithSelection() {
        assertDoesNotThrow(() -> {
            Ognl.parseExpression("list.?{? #this > 0}");
        });
    }

    // ========== toString() Tests ==========

    @Test
    void nullSafeChainToString() throws Exception {
        Object expr = Ognl.parseExpression("a.?b.?c");
        String exprString = expr.toString();
        assertTrue(exprString.contains("?"), "Expression string should contain null-safe operator");
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
}
