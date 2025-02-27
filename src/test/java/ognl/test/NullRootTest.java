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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

class NullRootTest {

    @Test
    void testNullValue() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = new HashMap<>();
        root.put("key1", null);
        String expr = "key1.key2.key3";
        assertNull(Ognl.getValue(expr, context, root));
    }

    @Test
    void testEmptyRoot() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = new HashMap<>();
        String expr = "key1.key2.key3";
        assertNull(Ognl.getValue(expr, context, root));
    }

    @Test
    void testNullRoot() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = null;
        String expr = "key1.key2.key3";
        assertNull(Ognl.getValue(expr, context, root));
    }
}
