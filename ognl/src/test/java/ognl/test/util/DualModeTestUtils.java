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
package ognl.test.util;

import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 * Utility class for dual-mode testing of OGNL expressions.
 * Provides helper methods for preparing and evaluating expressions in both
 * interpreted and compiled modes to verify identical behavior.
 *
 * <p>The fundamental testing goal is that both execution paths should produce
 * identical results for any given expression.</p>
 */
public final class DualModeTestUtils {

    /**
     * System property to enable/disable dual-mode testing in CI/CD pipelines.
     * When set to "false", tests will only run in INTERPRETED mode.
     */
    public static final String DUAL_MODE_ENABLED_PROPERTY = "ognl.test.dualMode.enabled";

    private DualModeTestUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if dual-mode testing is enabled via system property.
     * Defaults to true if property is not set.
     *
     * @return true if dual-mode testing is enabled
     */
    public static boolean isDualModeEnabled() {
        String property = System.getProperty(DUAL_MODE_ENABLED_PROPERTY);
        return property == null || Boolean.parseBoolean(property);
    }

    /**
     * Prepares an OGNL expression for evaluation according to the specified execution mode.
     *
     * <p>For INTERPRETED mode, this simply parses the expression.
     * For COMPILED mode, this parses and compiles the expression, generating
     * optimized bytecode accessors.</p>
     *
     * @param expression the OGNL expression to prepare
     * @param context the OgnlContext to use for compilation
     * @param root the root object for compilation
     * @param mode the execution mode (INTERPRETED or COMPILED)
     * @param <C> the OgnlContext type
     * @return the prepared Node with accessors set if compiled
     * @throws Exception if parsing or compilation fails
     */
    public static <C extends OgnlContext<C>> Node<C> prepareExpression(
            String expression,
            C context,
            Object root,
            OgnlExecutionMode mode) throws Exception {

        Node<C> parsed = (Node<C>) Ognl.parseExpression(expression);

        if (mode == OgnlExecutionMode.COMPILED) {
            // Compile the expression to generate optimized accessors
            return Ognl.compileExpression(context, root, expression);
        }

        return parsed;
    }

    /**
     * Evaluates a prepared OGNL expression node.
     *
     * @param node the prepared expression node
     * @param context the OgnlContext to use for evaluation
     * @param root the root object for evaluation
     * @param <C> the OgnlContext type
     * @return the result of evaluating the expression
     * @throws OgnlException if evaluation fails
     */
    public static <C extends OgnlContext<C>> Object evaluateNode(
            Node<C> node,
            C context,
            Object root) throws OgnlException {
        return node.getValue(context, root);
    }

    /**
     * Convenience method to prepare and evaluate an expression in one step.
     *
     * @param expression the OGNL expression to evaluate
     * @param context the OgnlContext to use
     * @param root the root object
     * @param mode the execution mode
     * @param <C> the OgnlContext type
     * @return the result of evaluating the expression
     * @throws Exception if preparation or evaluation fails
     */
    public static <C extends OgnlContext<C>> Object prepareAndEvaluate(
            String expression,
            C context,
            Object root,
            OgnlExecutionMode mode) throws Exception {

        Node<C> node = prepareExpression(expression, context, root, mode);
        return evaluateNode(node, context, root);
    }

    /**
     * Sets a value using a prepared OGNL expression node.
     *
     * @param node the prepared expression node
     * @param context the OgnlContext to use for evaluation
     * @param root the root object for evaluation
     * @param value the value to set
     * @param <C> the OgnlContext type
     * @throws OgnlException if setting the value fails
     */
    public static <C extends OgnlContext<C>> void setValueOnNode(
            Node<C> node,
            C context,
            Object root,
            Object value) throws OgnlException {
        node.setValue(context, root, value);
    }

    /**
     * Convenience method to prepare an expression and set a value in one step.
     *
     * @param expression the OGNL expression
     * @param context the OgnlContext to use
     * @param root the root object
     * @param value the value to set
     * @param mode the execution mode
     * @param <C> the OgnlContext type
     * @throws Exception if preparation or setting fails
     */
    public static <C extends OgnlContext<C>> void prepareAndSetValue(
            String expression,
            C context,
            Object root,
            Object value,
            OgnlExecutionMode mode) throws Exception {

        Node<C> node = prepareExpression(expression, context, root, mode);
        setValueOnNode(node, context, root, value);
    }

    /**
     * Verifies that an expression has been compiled by checking if it has an accessor.
     *
     * @param node the expression node to check
     * @param mode the execution mode
     * @param <C> the OgnlContext type
     * @return true if the node should have an accessor and does, or if in INTERPRETED mode
     */
    public static <C extends OgnlContext<C>> boolean verifyCompilationState(
            Node<C> node,
            OgnlExecutionMode mode) {

        if (mode == OgnlExecutionMode.INTERPRETED) {
            return true; // No accessor expected in interpreted mode
        }

        // In compiled mode, the node should have an accessor
        // Note: accessor may be null for partial expressions that can't be compiled yet
        return node.getAccessor() != null;
    }

    /**
     * Gets a display name for the execution mode for test reporting.
     *
     * @param mode the execution mode
     * @return a friendly display name
     */
    public static String getModeName(OgnlExecutionMode mode) {
        return mode == OgnlExecutionMode.INTERPRETED ? "Interpreted" : "Compiled";
    }
}
