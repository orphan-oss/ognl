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
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapCreationTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testMapCreation1() throws Exception {
        Map<String, String> expected = Map.of("foo", "bar");

        Object actual = Ognl.getValue("#{ \"foo\" : \"bar\" }", context, root);

        assertEquals(expected, actual);
    }

    @Test
    void testMapCreation2() throws Exception {
        Map<String, String> expected = Map.of(
                "foo", "bar",
                "bar", "baz"
        );

        Object actual = Ognl.getValue("#{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", context, root);

        assertEquals(expected, actual);
    }

    @Test
    void testMapCreation3() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", null);
        expected.put("bar", "baz");

        Object actual = Ognl.getValue("#{ \"foo\", \"bar\" : \"baz\"  }", context, root);

        assertEquals(expected, actual);
    }

    @Test
    void testMapCreation4() throws Exception {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("foo", "bar");
        expected.put("bar", "baz");

        Object actual = Ognl.getValue("#@java.util.LinkedHashMap@{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", context, root);

        assertEquals(expected, actual);
    }

    @Test
    void testMapCreation5() throws Exception {
        Map<String, String> expected = new TreeMap<>();
        expected.put("foo", "bar");
        expected.put("bar", "baz");

        Object actual = Ognl.getValue("#@java.util.TreeMap@{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", context, root);

        assertEquals(expected, actual);
    }
}
