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
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextVariableTest {

    private Simple root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Simple();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testRoot() throws Exception {
        Object actual = Ognl.getValue("#root", context, root);
        assertEquals(root, actual);
    }

    @Test
    void testThis() throws Exception {
        Object actual = Ognl.getValue("#this", context, root);
        assertEquals(root, actual);
    }

    @Test
    void testSumOfFiveAndSix() throws Exception {
        Object actual = Ognl.getValue("#f=5, #s=6, #f + #s", context, root);
        assertEquals(11, actual);
    }

    @Test
    void testSumOfFiveAndSixWithIntermediateAssignment() throws Exception {
        Object actual = Ognl.getValue("#six=(#five=5, 6), #five + #six", context, root);
        assertEquals(11, actual);
    }
}
