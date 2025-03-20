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

import ognl.MethodFailedException;
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.IndexedSetObject;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndexAccessTest {

    private Root root;
    private IndexedSetObject indexedSet;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        indexedSet = new IndexedSetObject();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testListIndex() throws Exception {
        Object actual = Ognl.getValue("list[index]", context, root);
        assertEquals(root.getList().get(root.getIndex()), actual);
    }

    @Test
    void testListObjectIndex() throws Exception {
        Object actual = Ognl.getValue("list[objectIndex]", context, root);
        assertEquals(root.getList().get(root.getObjectIndex()), actual);
    }

    @Test
    void testArrayObjectIndex() throws Exception {
        Object actual = Ognl.getValue("array[objectIndex]", context, root);
        assertEquals(root.getArray()[root.getObjectIndex()], actual);
    }

    @Test
    void testArrayGetObjectIndex() throws Exception {
        Object actual = Ognl.getValue("array[getObjectIndex()]", context, root);
        assertEquals(root.getArray()[root.getObjectIndex()], actual);
    }

    @Test
    void testArrayGenericIndex() throws Exception {
        Object actual = Ognl.getValue("array[genericIndex]", context, root);
        assertEquals(root.getArray()[(Integer) root.getGenericIndex()], actual);
    }

    @Test
    void testBooleanArraySelfObjectIndex() throws Exception {
        Object actual = Ognl.getValue("booleanArray[self.objectIndex]", context, root);
        assertEquals(Boolean.FALSE, actual);
    }

    @Test
    void testBooleanArrayGetObjectIndex() throws Exception {
        Object actual = Ognl.getValue("booleanArray[getObjectIndex()]", context, root);
        assertEquals(Boolean.FALSE, actual);
    }

    @Test
    void testBooleanArrayNullIndex() {
        assertThrows(NoSuchPropertyException.class,
                () -> Ognl.getValue("booleanArray[nullIndex]", context, root),
                "nullIndex");
    }

    @Test
    void testListSizeMinusOne() {
        assertThrows(MethodFailedException.class,
                () -> Ognl.getValue("list[size() - 1]", context, root),
                "size()");
    }

    @Test
    void testToggleToggleSelected() throws Exception {
        Object actual = Ognl.getValue("(index == (array.length - 3)) ? 'toggle toggleSelected' : 'toggle'", context, root);
        assertEquals("toggle toggleSelected", actual);
    }

    @Test
    void testToggleDisplay() throws Exception {
        Object actual = Ognl.getValue("\"return toggleDisplay('excdisplay\"+index+\"', this)\"", context, root);
        assertEquals("return toggleDisplay('excdisplay1', this)", actual);
    }

    @Test
    void testMapMapKeySplit() throws Exception {
        Object actual = Ognl.getValue("map[mapKey].split('=')[0]", context, root);
        assertEquals("StringStuff", actual);
    }

    @Test
    void testBooleanValuesIndex1Index2() throws Exception {
        Object actual = Ognl.getValue("booleanValues[index1][index2]", context, root);
        assertEquals(Boolean.FALSE, actual);
    }

    @Test
    void testTabSearchCriteriaDisplayName() throws Exception {
        Object actual = Ognl.getValue("tab.searchCriteria[index1].displayName", context, root);
        assertEquals("Woodland creatures", actual);
    }

    @Test
    void testTabSearchCriteriaSelections() throws Exception {
        Object actual = Ognl.getValue("tab.searchCriteriaSelections[index1][index2]", context, root);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testTabSearchCriteriaSelectionsSetValue() throws Exception {
        Ognl.setValue("tab.searchCriteriaSelections[index1][index2]", context, root, Boolean.FALSE);
        Object actual = Ognl.getValue("tab.searchCriteriaSelections[index1][index2]", context, root);
        assertEquals(Boolean.FALSE, actual);
    }

    @Test
    void testMapBarValue() throws Exception {
        Ognl.setValue("map['bar'].value", context, root, 50);
        Object actual = Ognl.getValue("map['bar'].value", context, root);
        assertEquals(50, actual);
    }

    @Test
    void testIndexedSetThingXVal() throws Exception {
        Ognl.setValue("thing[\"x\"].val", context, indexedSet, 2);
        Object actual = Ognl.getValue("thing[\"x\"].val", context, indexedSet);
        assertEquals(2, actual);
    }
}
