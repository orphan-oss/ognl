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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for null-safe operator (.?) with collections, arrays, and dynamic subscripts.
 */
class NullSafeCollectionTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "list.?{name}",
            "list.?{? #this.active}",
            "items.?{? #this > 0}"
    })
    void nullSafeExpressionParsing(String expression) {
        assertDoesNotThrow(() -> {
            Object expr = Ognl.parseExpression(expression);
            assertNotNull(expr);
        });
    }

    @ParameterizedTest
    @MethodSource("projectionSelectionTestCases")
    void nullSafeProjectionAndSelection(String expression, Object value, boolean expectNull) throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", value);

        Object result = Ognl.getValue(expression, context, root);
        if (expectNull) {
            assertNull(result);
        } else {
            assertNotNull(result);
        }
    }

    static Stream<Arguments> projectionSelectionTestCases() {
        List<Map<String, String>> users = Arrays.asList(
                createMap("name", "Alice"),
                createMap("name", "Bob")
        );
        return Stream.of(
                Arguments.of("items.?{name}", null, true),
                Arguments.of("items.{name}", users, false),
                Arguments.of("items.?{? #this > 0}", null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("propertyAccessTestCases")
    void nullSafePropertyAccess(String expression, Object value, Object expected) throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", value);

        Object result = Ognl.getValue(expression, context, root);
        assertEquals(expected, result);
    }

    static Stream<Arguments> propertyAccessTestCases() {
        return Stream.of(
                Arguments.of("items.?length", new String[]{"a", "b", "c"}, 3),
                Arguments.of("items.?length", null, null),
                Arguments.of("items.?size()", Arrays.asList("a", "b", "c"), 3),
                Arguments.of("items.?size()", null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("dynamicSubscriptTestCases")
    void dynamicSubscriptAccess(String expression, String expected) throws Exception {
        Map<String, Object> root = new HashMap<>();
        String[] items = {"first", "second", "third"};
        root.put("items", items);

        Object result = Ognl.getValue(expression, context, root);
        assertEquals(expected, result);
    }

    static Stream<Arguments> dynamicSubscriptTestCases() {
        return Stream.of(
                Arguments.of("items[^]", "first"),  // [^] gets first element
                Arguments.of("items[$]", "third"),  // [$] gets last element
                Arguments.of("items[0]", "first")   // Regular index access
        );
    }

    private static Map<String, String> createMap(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
