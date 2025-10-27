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
 * Exact reproduction of the code from issue #472
 */
public class Issue472ExactReproTest {

    @Test
    void testExactCodeFromIssue472() throws OgnlException {
        // This is the EXACT code from the issue report
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
                                System.out.println("Evaluating lambda with item: " + item);
                                System.out.println("Context root before getValue: " + context.getRoot());
                                Object value = Ognl.getValue(args[0], context, item);
                                System.out.println("Context root after getValue: " + context.getRoot());
                                if (!(value instanceof Boolean)) {
                                    throw new RuntimeException("Lambda did not return boolean, returned '" + value + "' instead");
                                }
                                return (Boolean) value;
                            } catch (OgnlException e) {
                                System.out.println("Exception: " + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });
            }
        });

        OgnlContext defaultContext = Ognl.createDefaultContext(Map.of(), null, null);
        System.out.println("Initial context root: " + defaultContext.getRoot());
        defaultContext.setRoot(Map.of("test", "value"));
        System.out.println("After setRoot, context root: " + defaultContext.getRoot());

        Object value = Ognl.getValue("myList.exists(:[ #this.equals(test) ])", defaultContext, Map.of(
                "myList", List.of("value")
        ));
        System.out.println("Final result: " + value);

        // In OGNL 3.4.4, this returned true
        // In OGNL 3.4.5+, it throws NoSuchPropertyException: java.lang.String.test
        assertEquals(true, value);
    }
}
