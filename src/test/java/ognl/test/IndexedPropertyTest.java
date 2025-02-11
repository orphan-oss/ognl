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
import ognl.test.objects.Indexed;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexedPropertyTest {

    private Indexed indexed;
    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        indexed = new Indexed();
        root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testGetValues() throws Exception {
        Object actual = Ognl.getValue("getValues", context, indexed);
        assertEquals(indexed.getValues(), actual);
    }

    @Test
    void testValues() throws Exception {
        Object actual = Ognl.getValue("[\"values\"]", context, indexed);
        assertEquals(indexed.getValues(), actual);
    }

    @Test
    void testValuesIndex0() throws Exception {
        Object actual = Ognl.getValue("[0]", context, indexed.getValues());
        assertEquals(indexed.getValues()[0], actual);
    }

    @Test
    void testGetValuesIndex0() throws Exception {
        Object actual = Ognl.getValue("getValues()[0]", context, indexed);
        assertEquals(indexed.getValues()[0], actual);
    }

    @Test
    void testValuesIndex0Direct() throws Exception {
        Object actual = Ognl.getValue("values[0]", context, indexed);
        assertEquals(indexed.getValues(0), actual);
    }

    @Test
    void testValuesCaret() throws Exception {
        Object actual = Ognl.getValue("values[^]", context, indexed);
        assertEquals(indexed.getValues(0), actual);
    }

    @Test
    void testValuesPipe() throws Exception {
        Object actual = Ognl.getValue("values[|]", context, indexed);
        assertEquals(indexed.getValues(1), actual);
    }

    @Test
    void testValuesDollar() throws Exception {
        Object actual = Ognl.getValue("values[$]", context, indexed);
        assertEquals(indexed.getValues(2), actual);
    }

    @Test
    void testSetValuesIndex1() throws Exception {
        Ognl.setValue("values[1]", context, indexed, "xxxx" + "xxx");
        Object actual = Ognl.getValue("values[1]", context, indexed);
        assertEquals("xxxxxxx", actual);
    }

    @Test
    void testSetValuesIndex2() throws Exception {
        Ognl.getValue("setValues(2, \"xxxx\")", context, indexed);
        Object actual = Ognl.getValue("values[2]", context, indexed);
        assertEquals("xxxx", actual);
    }

    @Test
    void testGetTitle() throws Exception {
        Object actual = Ognl.getValue("getTitle(list.size)", context, indexed);
        assertEquals("Title count 3", actual);
    }

    @Test
    void testSourceTotal() throws Exception {
        Object actual = Ognl.getValue("source.total", context, indexed);
        assertEquals(1, actual);
    }

    @Test
    void testIndexerLine() throws Exception {
        Object actual = Ognl.getValue("indexer.line[index]", context, root);
        assertEquals("line:1", actual);
    }

    @Test
    void testListLongValue() throws Exception {
        Object actual = Ognl.getValue("list[2].longValue()", context, indexed);
        assertEquals(3L, actual);
    }

    @Test
    void testMapValueId() throws Exception {
        Object actual = Ognl.getValue("map.value.id", context, root);
        assertEquals(1L, actual);
    }

    @Test
    void testPropertyHoodak() throws Exception {
        Ognl.setValue("property['hoodak']", context, indexed, "random string");
        Object actual = Ognl.getValue("property['hoodak']", context, indexed);
        assertEquals("random string", actual);
    }
}
