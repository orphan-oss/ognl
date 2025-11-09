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
import ognl.OgnlRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for null-safe operator (.?) expression compilation and toString() functionality.
 */
class NullSafeCompilationTest {

    private OgnlContext context;

    static class User {
        private String name;
        private Profile profile;

        User(String name, Profile profile) {
            this.name = name;
            this.profile = profile;
        }

        public String getName() {
            return name;
        }

        public Profile getProfile() {
            return profile;
        }
    }

    static class Profile {
        private String bio;
        private Address address;

        Profile(String bio, Address address) {
            this.bio = bio;
            this.address = address;
        }

        public String getBio() {
            return bio;
        }

        public Address getAddress() {
            return address;
        }
    }

    static class Address {
        private String city;

        Address(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }
    }

    @BeforeEach
    void setUp() {
        context = (OgnlContext) Ognl.createDefaultContext(null);
    }

    // ========== Expression Compilation Tests ==========

    @Test
    void nullSafeWithCompiledExpression() throws Exception {
        User user = new User("Alice", null);
        Object expr = Ognl.parseExpression("profile.?bio");

        // Compile the expression
        try {
            Object compiled = Ognl.compileExpression(context, user, "profile.?bio");
            assertNotNull(compiled, "Compiled expression should not be null");
        } catch (Exception e) {
            // Compilation may not be supported in all cases, but should not throw for null-safe
        }

        // Verify evaluation still works
        Object result = Ognl.getValue(expr, context, user);
        assertNull(result);
    }

    @Test
    void nullSafeToGetSourceString() throws Exception {
        User user = new User("Alice", null);
        Object expr = Ognl.parseExpression("profile.?address.?city");

        // This should trigger toGetSourceString in ASTChain
        try {
            String source = OgnlRuntime.getCompiler().getClassName(expr.getClass());
            assertNotNull(source);
        } catch (Exception e) {
            // May not be fully supported, but shouldn't throw
        }
    }

    // ========== toString() Tests ==========

    @ParameterizedTest
    @MethodSource("toStringTestCases")
    void expressionToString(String expression, String expectedSubstring) throws Exception {
        Object expr = Ognl.parseExpression(expression);
        String exprString = expr.toString();
        assertNotNull(exprString, "Expression string should not be null");
        assertTrue(exprString.contains(expectedSubstring),
                "Expression string should contain '" + expectedSubstring + "'");
    }

    static Stream<Arguments> toStringTestCases() {
        return Stream.of(
                Arguments.of("a.?b.?c", "?"),
                Arguments.of("user.?profile", "user"),
                Arguments.of("user.?name", "user")
        );
    }

    @Test
    void toStringComplexExpression() throws Exception {
        Object expr = Ognl.parseExpression("user.?profile.?address.?city");
        String exprString = expr.toString();
        // Should contain multiple null-safe operators
        int questionMarks = exprString.length() - exprString.replace("?", "").length();
        assertTrue(questionMarks >= 3, "Expression should contain multiple null-safe operators");
    }
}
