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

/**
 * Enumeration defining the two supported execution paths for OGNL expressions.
 * This enum serves as the basis for parameterized test differentiation to verify
 * that both interpreted and compiled execution modes produce identical results.
 *
 * <p>Usage in tests:</p>
 * <pre>
 * {@code @ParameterizedTest}
 * {@code @EnumSource(OgnlExecutionMode.class)}
 * void testExpression(OgnlExecutionMode mode) throws Exception {
 *     // Test runs once per mode
 * }
 * </pre>
 */
public enum OgnlExecutionMode {
    /**
     * Interpreted mode - expressions are evaluated by walking the AST tree.
     * This is the traditional execution path.
     */
    INTERPRETED,

    /**
     * Compiled mode - expressions are compiled to bytecode for faster execution.
     * Uses {@link ognl.Ognl#compileExpression} to generate optimized accessors.
     */
    COMPILED
}
