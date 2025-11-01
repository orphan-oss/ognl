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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for GitHub issue #472: Root properties not accessible from lambda expressions.
 * <p>
 * This is a follow-up to issue #390, specifically testing the scenario where:
 * 1. Lambda expressions are used in list operations (selection, projection)
 * 2. The lambda is evaluated via Ognl.getValue() internally with list items as root
 * 3. The lambda needs to access properties from the ORIGINAL context root, not the list item
 * <p>
 * The bug occurred because Ognl.getValue() would call addDefaultContext(root, context) which
 * created a new context and overwrote the root with the current evaluation target (list item),
 * making the original root properties inaccessible.
 * <p>
 * The fix removes the addDefaultContext() wrapper from getValue(), using the context directly
 * without modification, thus preserving the original root throughout the evaluation.
 *
 * @see <a href="https://github.com/orphan-oss/ognl/issues/472">Issue #472</a>
 */
public class Issue472CustomMethodAccessorTest {

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

    @Before
    public void setUp() {
        rootObject = new TestRootObject();
        context = Ognl.createDefaultContext(rootObject);
    }

    @Test
    public void testRootPropertyAccessInListSelection() throws OgnlException {
        // Test that list selection can access root properties
        // This tests the core Issue #472: root properties must be accessible when
        // Ognl.getValue() is called with list items as the root parameter

        String expression = "testList.{? #this.equals(#root.targetValue)}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertTrue("Result should be a List", result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals("Should find one matching item", 1, resultList.size());
        assertEquals("value", resultList.get(0));
    }

    @Test
    public void testRootPropertyAccessWithNonMatchingValue() throws OgnlException {
        // Test that list selection returns empty when root property doesn't match any items
        rootObject.targetValue = "nonexistent";

        String expression = "testList.{? #this.equals(#root.targetValue)}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals("Should find no matching items", 0, resultList.size());
    }

    @Test
    public void testLambdaAccessingBothRootAndListItem() throws OgnlException {
        // Test that expressions can access both #root properties and #this (the list item)
        // This verifies that the fix preserves context root while still allowing access to the current item

        String expression = "prefixedList.{? #this.startsWith(#root.prefix)}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals("Should find two items with prefix", 2, resultList.size());
        assertEquals("test_value", resultList.get(0));
        assertEquals("test_item", resultList.get(1));
    }

    @Test
    public void testMultipleRootPropertiesInExpression() throws OgnlException {
        // Test accessing multiple root properties from within list selection expression
        // This validates that the entire root object remains accessible, not just a single property

        String expression = "lengthList.{? #this.length() >= #root.minLength && #this.length() <= #root.maxLength}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals("Should find one item within length range", 1, resultList.size());
        assertEquals("medium_size", resultList.get(0));
    }

    @Test
    public void testListProjectionWithRootAccess() throws OgnlException {
        // Test that list projection can also access root properties
        // This ensures the fix works for both selection {? ...} and full projection {...}

        String expression = "testList.{#this + '-' + #root.targetValue}";
        Object result = Ognl.getValue(expression, context, rootObject);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals("Should project all items with root value appended", 3, resultList.size());
        assertEquals("value-value", resultList.get(0));
        assertEquals("other-value", resultList.get(1));
        assertEquals("different-value", resultList.get(2));
    }
}
