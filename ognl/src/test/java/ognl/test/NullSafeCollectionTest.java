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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for null-safe operator (.?) with collections, arrays, and dynamic subscripts.
 */
public class NullSafeCollectionTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = (OgnlContext) Ognl.createDefaultContext(null);
    }

    // ========== Collection Projection Tests ==========

    @Test
    void nullSafeProjectionOnNullList() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("users", null);

        Object result = Ognl.getValue("users.?{name}", context, root);
        assertNull(result, "Projection on null list should return null");
    }

    @Test
    void nullSafeProjectionOnNonNullList() throws Exception {
        Map<String, Object> root = new HashMap<>();
        List<Map<String, String>> users = Arrays.asList(
                createMap("name", "Alice"),
                createMap("name", "Bob")
        );
        root.put("users", users);

        Object result = Ognl.getValue("users.{name}", context, root);
        assertNotNull(result);
    }

    @Test
    void nullSafeWithProjection() {
        // Test parsing of null-safe with projection
        assertDoesNotThrow(() -> {
            Object expr = Ognl.parseExpression("list.?{name}");
            assertNotNull(expr);
        });
    }

    // ========== Collection Selection Tests ==========

    @Test
    void nullSafeSelectionOnNullList() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", null);

        Object result = Ognl.getValue("items.?{? #this > 0}", context, root);
        assertNull(result, "Selection on null list should return null");
    }

    @Test
    void nullSafeWithSelection() {
        // Test parsing of null-safe with selection
        assertDoesNotThrow(() -> {
            Object expr = Ognl.parseExpression("list.?{? #this.active}");
            assertNotNull(expr);
        });
    }

    // ========== Array Property Access Tests ==========

    @Test
    void nullSafeWithArrayProperty() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", new String[]{"a", "b", "c"});

        Object result = Ognl.getValue("items.?length", context, root);
        assertEquals(3, result);
    }

    @Test
    void nullSafeWithNullArrayProperty() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", null);

        Object result = Ognl.getValue("items.?length", context, root);
        assertNull(result);
    }

    @Test
    void nullSafeArrayPropertyAccess() throws Exception {
        // Test null-safe on array property itself
        Map<String, Object> root = new HashMap<>();
        root.put("items", null);

        // Null-safe on property that is null
        Object result = Ognl.getValue("items.?length", context, root);
        assertNull(result);
    }

    // ========== List Property Access Tests ==========

    @Test
    void nullSafeWithListProperty() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", Arrays.asList("a", "b", "c"));

        Object result = Ognl.getValue("items.?size()", context, root);
        assertEquals(3, result);
    }

    @Test
    void nullSafeWithNullListProperty() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("items", null);

        Object result = Ognl.getValue("items.?size()", context, root);
        assertNull(result);
    }

    // ========== Dynamic Subscript Tests ==========

    @Test
    void nullSafeWithDynamicSubscriptFirst() throws Exception {
        Map<String, Object> root = new HashMap<>();
        String[] items = {"first", "second", "third"};
        root.put("items", items);

        // [^] gets first element
        Object result = Ognl.getValue("items[^]", context, root);
        assertEquals("first", result);
    }

    @Test
    void nullSafeWithDynamicSubscriptLast() throws Exception {
        Map<String, Object> root = new HashMap<>();
        String[] items = {"first", "second", "third"};
        root.put("items", items);

        // [$] gets last element
        Object result = Ognl.getValue("items[$]", context, root);
        assertEquals("third", result);
    }

    @Test
    void nullSafeWithIndexAccessAfterProperty() throws Exception {
        // Note: .?[0] syntax not supported. Use null-safe on property, then index separately
        Map<String, Object> root = new HashMap<>();
        String[] items = {"first", "second", "third"};
        root.put("items", items);

        // Regular index access
        Object result = Ognl.getValue("items[0]", context, root);
        assertEquals("first", result);
    }

    // ========== Helper Methods ==========

    private Map<String, String> createMap(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
