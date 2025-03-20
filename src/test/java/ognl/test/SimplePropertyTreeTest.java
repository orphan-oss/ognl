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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimplePropertyTreeTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
    }

    @Test
    void testName() throws Exception {
        assertTrue(Ognl.isSimpleProperty(Ognl.parseExpression("name"), context));
    }

    @Test
    void testFoo() throws Exception {
        assertTrue(Ognl.isSimpleProperty(Ognl.parseExpression("foo"), context));
    }

    @Test
    void testNameWithIndex() throws Exception {
        assertFalse(Ognl.isSimpleProperty(Ognl.parseExpression("name[i]"), context));
    }

    @Test
    void testNameWithAddition() throws Exception {
        assertFalse(Ognl.isSimpleProperty(Ognl.parseExpression("name + foo"), context));
    }

    @Test
    void testNameWithProperty() throws Exception {
        assertFalse(Ognl.isSimpleProperty(Ognl.parseExpression("name.foo"), context));
    }

    @Test
    void testNameWithPropertyChain() throws Exception {
        assertFalse(Ognl.isSimpleProperty(Ognl.parseExpression("name.foo.bar"), context));
    }

    @Test
    void testNameWithFilter() throws Exception {
        assertFalse(Ognl.isSimpleProperty(Ognl.parseExpression("name.{? foo }"), context));
    }

    @Test
    void testNameWithProjection() throws Exception {
        assertFalse(Ognl.isSimpleProperty(Ognl.parseExpression("name.( foo )"), context));
    }
}
