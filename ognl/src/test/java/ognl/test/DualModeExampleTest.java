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

import ognl.DefaultMemberAccess;
import ognl.Node;
import ognl.OgnlContext;
import ognl.test.objects.Root;
import ognl.test.util.DualModeTestUtils;
import ognl.test.util.OgnlExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Example test class demonstrating dual-mode testing patterns for OGNL.
 * This test serves as both documentation and verification that the dual-mode
 * infrastructure works correctly.
 *
 * <p>Each test method is executed twice - once in INTERPRETED mode and once
 * in COMPILED mode - to verify that both execution paths produce identical results.</p>
 */
class DualModeExampleTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        context = new OgnlContext(new DefaultMemberAccess(false), null, null);
        root = new Root();
    }

    /**
     * Example 1: Basic property access with dual-mode testing.
     * Tests that simple property navigation works identically in both modes.
     */
    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testSimplePropertyAccess(OgnlExecutionMode mode) throws Exception {
        // Prepare expression according to mode
        Node<OgnlContext> expr = DualModeTestUtils.prepareExpression(
                "property1", context, root, mode);

        // Evaluate
        Object result = DualModeTestUtils.evaluateNode(expr, context, root);

        // Verify result is identical regardless of mode
        assertNotNull(result, "Result should not be null in " +
                DualModeTestUtils.getModeName(mode) + " mode");
        assertEquals("value1", result, "Property value should match in " +
                DualModeTestUtils.getModeName(mode) + " mode");
    }

    /**
     * Example 2: Method invocation with dual-mode testing.
     * Tests that method calls work identically in both modes.
     */
    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testMethodInvocation(OgnlExecutionMode mode) throws Exception {
        // Using convenience method for prepare and evaluate
        Object result = DualModeTestUtils.prepareAndEvaluate(
                "getIndex()", context, root, mode);

        // Both modes should return the same value
        assertNotNull(result, "Method result should not be null in " +
                DualModeTestUtils.getModeName(mode) + " mode");
        assertEquals(1, result, "Method return value should match in " +
                DualModeTestUtils.getModeName(mode) + " mode");
    }

    /**
     * Example 3: Chained property access with dual-mode testing.
     * Tests that property chains work identically in both modes.
     */
    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testChainedPropertyAccess(OgnlExecutionMode mode) throws Exception {
        // Prepare and evaluate chained expression
        Object result = DualModeTestUtils.prepareAndEvaluate(
                "bean2.value", context, root, mode);

        // Both modes should navigate the chain identically
        assertNotNull(result, "Chained property should not be null in " +
                DualModeTestUtils.getModeName(mode) + " mode");
        assertEquals(100, result, "Chained property value should match in " +
                DualModeTestUtils.getModeName(mode) + " mode");
    }

    /**
     * Example 4: Setting values with dual-mode testing.
     * Tests that setValue operations work identically in both modes.
     */
    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testSetValue(OgnlExecutionMode mode) throws Exception {
        // Prepare expression
        Node<OgnlContext> expr = DualModeTestUtils.prepareExpression(
                "property1", context, root, mode);

        // Set new value
        String newValue = "modified_" + mode.name().toLowerCase();
        DualModeTestUtils.setValueOnNode(expr, context, root, newValue);

        // Verify the value was set
        Object result = DualModeTestUtils.evaluateNode(expr, context, root);
        assertEquals(newValue, result, "Set value should work in " +
                DualModeTestUtils.getModeName(mode) + " mode");
    }

    /**
     * Example 5: Array/Index access with dual-mode testing.
     * Tests that indexed access works identically in both modes.
     */
    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testArrayAccess(OgnlExecutionMode mode) throws Exception {
        // Access array element
        Object result = DualModeTestUtils.prepareAndEvaluate(
                "array[1]", context, root, mode);

        // Both modes should access the array identically
        assertNotNull(result, "Array element should not be null in " +
                DualModeTestUtils.getModeName(mode) + " mode");
        assertEquals("two", result, "Array element value should match in " +
                DualModeTestUtils.getModeName(mode) + " mode");
    }

    /**
     * Example 6: Demonstrating the alternative pattern using convenience method.
     * Shows how to use prepareAndEvaluate for simpler test code.
     */
    @ParameterizedTest(name = "[{index}] mode={0}")
    @EnumSource(OgnlExecutionMode.class)
    void testConvenienceMethod(OgnlExecutionMode mode) throws Exception {
        // This is equivalent to Example 1 but more concise
        Object result = DualModeTestUtils.prepareAndEvaluate(
                "property1", context, root, mode);

        assertEquals("value1", result);
    }
}
