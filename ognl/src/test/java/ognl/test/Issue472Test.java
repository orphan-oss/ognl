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

import ognl.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Issue #472: Root properties not accessible from lambda
 * <p>
 * This test reproduces the exact scenario from the issue report where
 * a lambda expression in a custom method loses access to the root context
 * properties when evaluated in OGNL 3.4.5+.
 */
public class Issue472Test {

    /**
     * Exact reproduction of the issue as reported.
     * <p>
     * The issue occurs when:
     * 1. A context is created with a root containing properties
     * 2. An expression is evaluated with a DIFFERENT root parameter
     * 3. A lambda expression tries to access properties from the ORIGINAL root
     * <p>
     * Expected: Lambda should access the original root's 'test' property
     * Actual (before fix): NoSuchPropertyException because root was overwritten
     */
    @Test
    void testIssue472ExactReproduction() throws OgnlException {
        // Setup custom method accessor that accepts lambda expressions
        OgnlRuntime.setMethodAccessor(List.class, new ObjectMethodAccessor() {
            @Override
            public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
                List<?> list = (List<?>) target;
                if (methodName.equals("exists")) {
                    return exists(context, list, args);
                }
                return super.callMethod(context, target, methodName, args);
            }

            private static Object exists(OgnlContext context, List<?> list, Object[] args) {
                return list.stream()
                        .anyMatch(item -> {
                            try {
                                Object value = Ognl.getValue(args[0], context, item);
                                if (!(value instanceof Boolean)) {
                                    throw new RuntimeException("Lambda did not return boolean, returned '" + value + "' instead");
                                }
                                return (Boolean) value;
                            } catch (OgnlException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        });

        // Create context with root containing 'test' property
        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        defaultContext.setRoot(Map.of("test", "value"));

        // Evaluate expression with DIFFERENT root (myList parameter)
        // The lambda :[ #this.equals(test) ] should still access 'test' from the original root
        Object value = Ognl.getValue(
                "myList.exists(:[ #this.equals(test) ])",
                defaultContext,
                Map.of("myList", List.of("value"))
        );

        // Should return true because "value".equals("value")
        assertEquals(Boolean.TRUE, value, "Lambda should have access to original root property 'test'");
    }

    /**
     * Test that accessing root properties with #root. prefix also works
     */
    @Test
    void testIssue472WithExplicitRootReference() throws OgnlException {
        OgnlRuntime.setMethodAccessor(List.class, new ObjectMethodAccessor() {
            @Override
            public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
                List<?> list = (List<?>) target;
                if (methodName.equals("exists")) {
                    return list.stream()
                            .anyMatch(item -> {
                                try {
                                    Object value = Ognl.getValue(args[0], context, item);
                                    if (!(value instanceof Boolean)) {
                                        throw new RuntimeException("Lambda did not return boolean");
                                    }
                                    return (Boolean) value;
                                } catch (OgnlException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
                return super.callMethod(context, target, methodName, args);
            }
        });

        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        defaultContext.setRoot(Map.of("test", "value"));

        // Using explicit #root.test reference
        Object value = Ognl.getValue(
                "myList.exists(:[ #this.equals(#root.test) ])",
                defaultContext,
                Map.of("myList", List.of("value"))
        );

        assertEquals(Boolean.TRUE, value);
    }

    /**
     * Test that context variables are still accessible from lambda expressions
     */
    @Test
    void testIssue472ContextVariableAccess() throws OgnlException {
        OgnlRuntime.setMethodAccessor(List.class, new ObjectMethodAccessor() {
            @Override
            public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
                List<?> list = (List<?>) target;
                if (methodName.equals("exists")) {
                    return list.stream()
                            .anyMatch(item -> {
                                try {
                                    Object value = Ognl.getValue(args[0], context, item);
                                    if (!(value instanceof Boolean)) {
                                        throw new RuntimeException("Lambda did not return boolean");
                                    }
                                    return (Boolean) value;
                                } catch (OgnlException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
                return super.callMethod(context, target, methodName, args);
            }
        });

        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        defaultContext.put("testValue", "value");

        Object value = Ognl.getValue(
                "myList.exists(:[ #this.equals(#testValue) ])",
                defaultContext,
                Map.of("myList", List.of("value"))
        );

        assertEquals(Boolean.TRUE, value, "Lambda should have access to context variables");
    }

    /**
     * Test that normal property access still works correctly
     */
    @Test
    void testNormalPropertyAccessStillWorks() throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(null);

        Map<String, Object> root = Map.of(
                "name", "John",
                "age", 30
        );

        assertEquals("John", Ognl.getValue("name", context, root));
        assertEquals(30, Ognl.getValue("age", context, root));
    }
}
