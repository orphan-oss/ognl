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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionDirectPropertyTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testSize() throws Exception {
        assertEquals(2, Ognl.getValue("size", context, Arrays.asList("hello", "world")));
    }

    @Test
    void testIsEmptyFalse() throws Exception {
        assertEquals(false, Ognl.getValue("isEmpty", context, Arrays.asList("hello", "world")));
    }

    @Test
    void testIsEmptyTrue() throws Exception {
        assertEquals(true, Ognl.getValue("isEmpty", context, Arrays.asList()));
    }

    @Test
    void testIteratorNext() throws Exception {
        assertEquals("hello", Ognl.getValue("iterator.next", context, Arrays.asList("hello", "world")));
    }

    @Test
    void testIteratorHasNext() throws Exception {
        assertEquals(true, Ognl.getValue("iterator.hasNext", context, Arrays.asList("hello", "world")));
    }

    @Test
    void testIteratorHasNextAfterTwoNexts() throws Exception {
        assertEquals(false, Ognl.getValue("#it = iterator, #it.next, #it.next, #it.hasNext", context, Arrays.asList("hello", "world")));
    }

    @Test
    void testIteratorNextAfterTwoNexts() throws Exception {
        assertEquals("world", Ognl.getValue("#it = iterator, #it.next, #it.next", context, Arrays.asList("hello", "world")));
    }

    @Test
    void testRootMapTest() throws Exception {
        assertEquals(root, Ognl.getValue("map[\"test\"]", context, root));
    }

    @Test
    void testRootMapSize() throws Exception {
        assertEquals(root.getMap().size(), Ognl.getValue("map.size", context, root));
    }

    @Test
    void testRootMapKeySet() throws Exception {
        assertEquals(root.getMap().keySet(), Ognl.getValue("map.keySet", context, root));
    }

    @Test
    void testRootMapValues() throws Exception {
        assertEquals(root.getMap().values(), Ognl.getValue("map.values", context, root));
    }

    @Test
    void testRootMapKeysSize() throws Exception {
        assertEquals(root.getMap().keySet().size(), Ognl.getValue("map.keys.size", context, root));
    }

    @Test
    void testRootMapSizeValue() throws Exception {
        assertEquals(root.getMap().get("size"), Ognl.getValue("map[\"size\"]", context, root));
    }

    @Test
    void testRootMapIsEmpty() throws Exception {
        assertEquals(root.getMap().isEmpty() ? Boolean.TRUE : Boolean.FALSE, Ognl.getValue("map.isEmpty", context, root));
    }

    @Test
    void testRootMapIsEmptyKey() throws Exception {
        assertEquals(null, Ognl.getValue("map[\"isEmpty\"]", context, root));
    }
}
