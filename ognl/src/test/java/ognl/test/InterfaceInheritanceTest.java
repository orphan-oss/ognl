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
import ognl.OgnlRuntime;
import ognl.test.objects.Bean1;
import ognl.test.objects.BeanProvider;
import ognl.test.objects.BeanProviderAccessor;
import ognl.test.objects.EvenOdd;
import ognl.test.objects.ListSourceImpl;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InterfaceInheritanceTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        root.getBeans().setBean("testBean", new Bean1());
        root.getBeans().setBean("evenOdd", new EvenOdd());

        List<Object> list = new ListSourceImpl();
        list.add("test1");

        root.getMap().put("customList", list);

        context = Ognl.createDefaultContext(root);

        OgnlRuntime.setPropertyAccessor(BeanProvider.class, new BeanProviderAccessor());
    }

    @Test
    void testMyMap() throws Exception {
        Object actual = Ognl.getValue("myMap", context, root);
        assertEquals(root.getMyMap(), actual);
    }

    @Test
    void testMyMapTest() throws Exception {
        Object actual = Ognl.getValue("myMap.test", context, root);
        assertEquals(root, actual);
    }

    @Test
    void testMyMapList() throws Exception {
        Object actual = Ognl.getValue("list", context, root.getMyMap());
        assertEquals(root.getList(), actual);
    }

    @Test
    void testMyMapArray0() throws Exception {
        Object actual = Ognl.getValue("myMap.array[0]", context, root);
        assertEquals(root.getArray()[0], actual);
    }

    @Test
    void testMyMapList1() throws Exception {
        Object actual = Ognl.getValue("myMap.list[1]", context, root);
        assertEquals(root.getList().get(1), actual);
    }

    @Test
    void testMyMapCaret() throws Exception {
        Object actual = Ognl.getValue("myMap[^]", context, root);
        assertEquals(99, actual);
    }

    @Test
    void testMyMapDollar() throws Exception {
        Object actual = Ognl.getValue("myMap[$]", context, root);
        assertNull(actual);
    }

    @Test
    void testMyMapArrayDollar() throws Exception {
        Object actual = Ognl.getValue("array[$]", context, root.getMyMap());
        assertEquals(root.getArray()[root.getArray().length - 1], actual);
    }

    @Test
    void testMyMapString() throws Exception {
        Object actual = Ognl.getValue("[\"myMap\"]", context, root);
        assertEquals(root.getMyMap(), actual);
    }

    @Test
    void testMyMapNull() throws Exception {
        Object actual = Ognl.getValue("myMap[null]", context, root);
        assertNull(actual);
    }

    @Test
    void testMyMapXNull() throws Exception {
        Object actual = Ognl.getValue("myMap[#x = null]", context, root);
        assertNull(actual);
    }

    @Test
    void testMyMapNullTest() throws Exception {
        Object actual = Ognl.getValue("myMap.(null,test)", context, root);
        assertEquals(root, actual);
    }

    @Test
    void testMyMapNull25() throws Exception {
        Object actual = Ognl.getValue("myMap[null] = 25", context, root);
        assertEquals(25, actual);
    }

    @Test
    void testMyMapNull50() throws Exception {
        Ognl.setValue("myMap[null]", context, root, 50);
        Object actual = Ognl.getValue("myMap[null]", context, root);
        assertEquals(50, actual);
    }

    @Test
    void testBeansTestBean() throws Exception {
        Object actual = Ognl.getValue("beans.testBean", context, root);
        assertEquals(root.getBeans().getBean("testBean"), actual);
    }

    @Test
    void testBeansEvenOddNext() throws Exception {
        Object actual = Ognl.getValue("beans.evenOdd.next", context, root);
        assertEquals("even", actual);
    }

    @Test
    void testMapCompFormClientId() throws Exception {
        Object actual = Ognl.getValue("map.comp.form.clientId", context, root);
        assertEquals("form1", actual);
    }

    @Test
    void testMapCompGetCount() throws Exception {
        Object actual = Ognl.getValue("map.comp.getCount(genericIndex)", context, root);
        assertEquals(0, actual);
    }

    @Test
    void testMapCustomListTotal() throws Exception {
        Object actual = Ognl.getValue("map.customList.total", context, root);
        assertEquals(1, actual);
    }

    @Test
    void testMyTestTheMapKey() throws Exception {
        Object actual = Ognl.getValue("myTest.theMap['key']", context, root);
        assertEquals("value", actual);
    }

    @Test
    void testContentProviderHasChildren() throws Exception {
        Object actual = Ognl.getValue("contentProvider.hasChildren(property)", context, root);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testObjectIndexInstanceOf() throws Exception {
        Object actual = Ognl.getValue("objectIndex instanceof java.lang.Object", context, root);
        assertEquals(Boolean.TRUE, actual);
    }
}
