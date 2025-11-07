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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for GitHub issue #472: Root properties not accessible from lambda expressions.
 * <p>
 * This is a follow-up to issue #390, specifically testing the scenario where:
 * 1. Lambda expressions are used in list operations (selection, projection)
 * 2. The lambda is evaluated via Ognl.getValue() internally with list items as root
 * 3. The lambda needs to access properties from the ORIGINAL context root, not the list item
 * <p>
 * The bug occurred because Ognl.getValue() would call context.withRoot(root) which
 * created a new context and overwrote the root with the current evaluation target (list item),
 * making the original root properties inaccessible.
 * <p>
 * The fix removes the context.withRoot(root) call from getValue(), using the context directly
 * without modification, thus preserving the original root throughout the evaluation.
 *
 * @see <a href="https://github.com/orphan-oss/ognl/issues/472">Issue #472</a>
 */
class Issue472CustomMethodAccessorTest {

    private OgnlContext context;
    private TestRootObject rootObject;

    public static class TestRootObject {
        private String targetValue = "value";
        private final String prefix = "test_";
        private final int minLength = 6;
        private final int maxLength = 11;
        private final List<String> testList = Arrays.asList("value", "other", "different");
        private final List<String> prefixedList = Arrays.asList("test_value", "other", "test_item");
        private final List<String> lengthList = Arrays.asList("short", "medium_size", "very_long_string");

        public String getTargetValue() {
            return targetValue;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getMinLength() {
            return minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public List<String> getTestList() {
            return testList;
        }

        public List<String> getPrefixedList() {
            return prefixedList;
        }

        public List<String> getLengthList() {
            return lengthList;
        }
    }

    @BeforeEach
    void setUp() {
        rootObject = new TestRootObject();
        context = Ognl.createDefaultContext(rootObject);
    }

    @Test
    void testRootPropertyAccessInListSelection() throws OgnlException {
        // Test that list selection can access root properties
        // This tests the core Issue #472: root properties must be accessible when
        // Ognl.getValue() is called with list items as the root parameter

        String expression = "testList.{? #this.equals(#root.targetValue)}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertInstanceOf(List.class, result, "Result should be a List");
        List<?> resultList = (List<?>) result;
        assertEquals(1, resultList.size(), "Should find one matching item");
        assertEquals("value", resultList.get(0));
    }

    @Test
    void testRootPropertyAccessWithNonMatchingValue() throws OgnlException {
        // Test that list selection returns empty when root property doesn't match any items
        rootObject.targetValue = "nonexistent";

        String expression = "testList.{? #this.equals(#root.targetValue)}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertInstanceOf(List.class, result);
        List<?> resultList = (List<?>) result;
        assertEquals(0, resultList.size(), "Should find no matching items");
    }

    @Test
    void testLambdaAccessingBothRootAndListItem() throws OgnlException {
        // Test that expressions can access both #root properties and #this (the list item)
        // This verifies that the fix preserves context root while still allowing access to the current item

        String expression = "prefixedList.{? #this.startsWith(#root.prefix)}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertInstanceOf(List.class, result);
        List<?> resultList = (List<?>) result;
        assertEquals(2, resultList.size(), "Should find two items with prefix");
        assertEquals("test_value", resultList.get(0));
        assertEquals("test_item", resultList.get(1));
    }

    @Test
    void testMultipleRootPropertiesInExpression() throws OgnlException {
        // Test accessing multiple root properties from within list selection expression
        // This validates that the entire root object remains accessible, not just a single property

        String expression = "lengthList.{? #this.length() >= #root.minLength && #this.length() <= #root.maxLength}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertInstanceOf(List.class, result);
        List<?> resultList = (List<?>) result;
        assertEquals(1, resultList.size(), "Should find one item within length range");
        assertEquals("medium_size", resultList.get(0));
    }

    @Test
    void testListProjectionWithRootAccess() throws OgnlException {
        // Test that list projection can also access root properties
        String expression = "testList.{#this + '-' + #root.targetValue}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertInstanceOf(List.class, result);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size(), "Should project all items with root value appended");
        assertEquals("value-value", resultList.get(0));
        assertEquals("other-value", resultList.get(1));
        assertEquals("different-value", resultList.get(2));
    }
}
