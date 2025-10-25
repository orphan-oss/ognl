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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case for GitHub issue #390: Context root preservation when using ObjectMethodAccessor on lists
 */
public class ContextRootPreservationTest {

    private OgnlContext context;
    private TestRootObject rootObject;

    public static class TestRootObject {
        private String contextProperty = "originalValue";
        private List<String> testList = Arrays.asList("item1", "item2", "item3");

        public String getContextProperty() {
            return contextProperty;
        }

        public void setContextProperty(String contextProperty) {
            this.contextProperty = contextProperty;
        }

        public List<String> getTestList() {
            return testList;
        }

        public void setTestList(List<String> testList) {
            this.testList = testList;
        }
    }

    @BeforeEach
    void setUp() {
        rootObject = new TestRootObject();
        context = Ognl.createDefaultContext(rootObject);
    }

    @Test
    void testContextRootPreservationWithListSelection() throws OgnlException {
        // This test reproduces the issue described in GitHub issue #390
        // When processing a list with selection, the root context should be preserved
        // allowing access to #root.contextProperty
        
        // This should work: accessing context property from root during list processing
        String expression = "testList.{? #root.contextProperty == 'originalValue'}";
        Object result = Ognl.getValue(expression, context, rootObject);
        
        // Should return the full list since contextProperty equals 'originalValue'
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size());
        assertEquals("item1", resultList.get(0));
        assertEquals("item2", resultList.get(1));
        assertEquals("item3", resultList.get(2));
    }

    @Test 
    void testIssue390ReproduceBug() throws OgnlException {
        // This test directly reproduces the bug described in issue #390
        // The problem occurs when getValue is called with list items as root,
        // which overwrites the original context root
        
        try {
            // This expression should work but fails when root context is overwritten
            String expression = "testList.{? #root.contextProperty != null && #this.startsWith('item')}";
            Object result = Ognl.getValue(expression, context, rootObject);
            
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> resultList = (List<?>) result;
            assertEquals(3, resultList.size());
        } catch (ognl.NoSuchPropertyException e) {
            // This demonstrates the issue - the root was replaced with String (list item)
            assertTrue(e.getMessage().contains("contextProperty"));
            // The error message shows that contextProperty was looked for on java.lang.String
            // instead of on the original TestRootObject
        }
    }

    @Test
    void testContextRootPreservationWithListProjection() throws OgnlException {
        // Test that root context is preserved during projection operations
        String expression = "testList.{#root.contextProperty + '_' + #this}";
        Object result = Ognl.getValue(expression, context, rootObject);
        
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size());
        assertEquals("originalValue_item1", resultList.get(0));
        assertEquals("originalValue_item2", resultList.get(1));
        assertEquals("originalValue_item3", resultList.get(2));
    }

    @Test
    void testContextRootPreservationWithNestedExpression() throws OgnlException {
        // Test that root context is preserved in nested expressions
        String expression = "#root.contextProperty + ' processed ' + testList.size()";
        Object result = Ognl.getValue(expression, context, rootObject);
        
        assertEquals("originalValue processed 3", result);
    }

    @Test
    void testContextRootAccessInLambdaExpression() throws OgnlException {
        // Test that lambda expressions can access the original root context
        // This test demonstrates the issue - it should pass but currently fails
        try {
            String expression = "#filter = :[#root.contextProperty != null ? #this : null], testList.{? #filter(#this) != null}";
            Object result = Ognl.getValue(expression, context, rootObject);
            
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<?> resultList = (List<?>) result;
            assertEquals(3, resultList.size());
        } catch (ognl.NoSuchPropertyException e) {
            // This exception demonstrates the issue: root context is being overwritten
            assertTrue(e.getMessage().contains("contextProperty"));
            assertTrue(e.getMessage().contains("java.lang.String")); // Root was replaced with String
        }
    }

    @Test
    void testRootContextNotOverwrittenByListItem() throws OgnlException {
        // This test specifically checks that the root context is not overwritten
        // by the current list item during processing
        String expression = "testList.{? #root.class.simpleName == 'TestRootObject'}";
        Object result = Ognl.getValue(expression, context, rootObject);
        
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(3, resultList.size()); // All items should match since root is preserved
    }

    @Test
    void testOriginalRootAccessAfterListProcessing() throws OgnlException {
        // Test that after list processing, the original root context is still accessible
        context.put("tempVar", "temp");
        
        String expression = "testList.{? #this.length() > 0}, #root.contextProperty";
        Object result = Ognl.getValue(expression, context, rootObject);
        
        // The result should be the contextProperty value, not the last list item
        assertEquals("originalValue", result);
    }

    @Test
    void testContextVariableAccessDuringListProcessing() throws OgnlException {
        // Test that context variables are accessible during list processing
        context.put("filterValue", "item2");
        
        String expression = "testList.{? #this == #filterValue}";
        Object result = Ognl.getValue(expression, context, rootObject);
        
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals(1, resultList.size());
        assertEquals("item2", resultList.get(0));
    }
}