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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case for GitHub issue #472: Root properties not accessible from lambda
 * in custom ObjectMethodAccessor implementations.
 */
public class Issue472Test {

    private ObjectMethodAccessor customListAccessor;

    @BeforeEach
    void setUp() {
        // Set up custom method accessor that provides an exists() method
        customListAccessor = new ObjectMethodAccessor() {
            @Override
            public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
                List<?> list = (List<?>) target;
                if (methodName.equals("exists")) {
                    return exists(context, list, args);
                }
                return super.callMethod(context, target, methodName, args);
            }

            private Object exists(OgnlContext context, List<?> list, Object[] args) {
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
        };

        OgnlRuntime.setMethodAccessor(List.class, customListAccessor);
    }

    @Test
    void testIssue472RootPropertyAccessFromLambda() throws OgnlException {
        // Create root object with "test" property
        Map<String, Object> rootObject = new java.util.HashMap<>();
        rootObject.put("test", "value");
        rootObject.put("myList", List.of("value"));

        OgnlContext defaultContext = Ognl.createDefaultContext(rootObject, null, null);

        // Expression uses lambda to check if list item equals root property "test"
        Object value = Ognl.getValue("myList.exists(:[ #this.equals(#root.test) ])", defaultContext, rootObject);

        // Should return true since "value" in list equals "value" from root.test
        assertEquals(true, value, "Lambda should be able to access root property 'test'");
    }

    @Test
    void testIssue472RootPropertyAccessFromLambdaWithRootReference() throws OgnlException {
        // Similar test but using explicit #root reference
        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        defaultContext.setRoot(Map.of("test", "value"));

        Object value = Ognl.getValue("myList.exists(:[ #this.equals(#root.test) ])", defaultContext, Map.of(
                "myList", List.of("value")
        ));

        assertEquals(true, value, "Lambda should be able to access root property via #root.test");
    }

    @Test
    void testIssue472ContextVariableAccessFromLambda() throws OgnlException {
        // Test that context variables are also accessible
        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        defaultContext.setRoot(Map.of("test", "value"));
        defaultContext.put("filterValue", "value");

        Object value = Ognl.getValue("myList.exists(:[ #this.equals(#filterValue) ])", defaultContext, Map.of(
                "myList", List.of("value")
        ));

        assertEquals(true, value, "Lambda should be able to access context variables");
    }
}
