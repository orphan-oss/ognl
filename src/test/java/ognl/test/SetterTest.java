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

import ognl.InappropriateExpressionException;
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SetterTest {

    private Root root;
    private OgnlContext context;
    private Set<String> list;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(null);
        list = new HashSet<>();
        list.add("Test1");
    }

    @Test
    void testSetNewValue() throws Exception {
        Ognl.setValue("newValue", context, root.getMap(), 101);
        assertEquals(101, Ognl.getValue("newValue", context, root.getMap()));
        Ognl.setValue("newValue", context, root.getMap(), 555);
        assertEquals(555, Ognl.getValue("newValue", context, root.getMap()));
    }

    @Test
    void testSettableListAbsoluteIndexes() throws Exception {
        Ognl.setValue("settableList[0]", context, root, "foo");
        assertEquals("foo", Ognl.getValue("settableList[0]", context, root));
        Ognl.setValue("settableList[0]", context, root, "quux");
        assertEquals("quux", Ognl.getValue("settableList[0]", context, root));
    }

    @Test
    void testSettableListSpecialIndexes() throws Exception {
        Ognl.setValue("settableList[$]", context, root, "quux");
        assertEquals("quux", Ognl.getValue("settableList[$]", context, root));
        Ognl.setValue("settableList[$]", context, root, "oompa");
        assertEquals("oompa", Ognl.getValue("settableList[$]", context, root));
    }

    @Test
    void testSetMapValue() throws Exception {
        Ognl.setValue("map.newValue", context, root, 555);
        assertEquals(555, Ognl.getValue("map.newValue", context, root));
    }

    @Test
    void testSetMap() throws Exception {
        try {
            Ognl.setValue("map", context, root, new HashMap<>());
            fail("Should have thrown NoSuchPropertyException");
        } catch (NoSuchPropertyException e) {
            assertEquals("ognl.test.objects.Root.map", e.getMessage());
        }
    }

    @Test
    void testSetSelectedList() {
        try {
            Ognl.setValue("selectedList", context, root, list);
            fail("Should have thrown IllegalArgumentException");
        } catch (OgnlException e) {
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
            assertEquals("Unable to convert type java.util.HashSet of [Test1] to type of java.util.List", e.getCause().getMessage());
            assertEquals("selectedList", e.getMessage());
        }
    }

    @Test
    void testSetOpenTransitionWin() throws Exception {
        Ognl.setValue("openTransitionWin", context, root, Boolean.TRUE);
        assertEquals(Boolean.TRUE, Ognl.getValue("openTransitionWin", context, root));
    }

    @Test
    void testSetInvalidExpression() throws Exception {
        try {
            Ognl.setValue("0", context, null, 0);
            fail("Should have thrown InappropriateExpressionException");
        } catch (InappropriateExpressionException e) {
            assertEquals("Inappropriate OGNL expression: 0", e.getMessage());
        }
    }
}
