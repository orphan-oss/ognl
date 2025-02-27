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
import ognl.OgnlException;
import ognl.test.objects.Bean1;
import ognl.test.objects.ObjectIndexed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectIndexedPropertyTest {

    private ObjectIndexed objectIndexed;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        objectIndexed = new ObjectIndexed();
        context = Ognl.createDefaultContext(objectIndexed);
    }

    @Test
    void testGetNonIndexedPropertyThroughAttributesMap() throws OgnlException {
        Object actual = Ognl.getValue("attributes[\"bar\"]", context, objectIndexed);
        assertEquals("baz", actual);
    }

    @Test
    void testGetIndexedProperty() throws OgnlException {
        Object actual = Ognl.getValue("attribute[\"foo\"]", context, objectIndexed);
        assertEquals("bar", actual);
    }

    @Test
    void testSetIndexedProperty() throws OgnlException {
        Ognl.setValue("attribute[\"bar\"]", context, objectIndexed, "newValue");
        Object actual = Ognl.getValue("attribute[\"bar\"]", context, objectIndexed);
        assertEquals("newValue", actual);
    }

    @Test
    void testGetPropertyBackThroughMapToConfirm() throws OgnlException {
        Ognl.setValue("attribute[\"bar\"]", context, objectIndexed, "newValue");
        Object actual = Ognl.getValue("attributes[\"bar\"]", context, objectIndexed);
        assertEquals("newValue", actual);
    }

    @Test
    void testGetIndexedPropertyFromIndexedThenThroughOther() throws OgnlException {
        Object actual = Ognl.getValue("attribute[\"other\"].attribute[\"bar\"]", context, objectIndexed);
        assertEquals("baz", actual);
    }

    @Test
    void testGetPropertyBackThroughMapToConfirmFromIndexed() throws OgnlException {
        Object actual = Ognl.getValue("attribute[\"other\"].attributes[\"bar\"]", context, objectIndexed);
        assertEquals("baz", actual);
    }

    @Test
    void testIllegalDynamicSubscriptAccessToObjectIndexedProperty() {
        assertThrows(OgnlException.class, () -> Ognl.getValue("attribute[$]", context, objectIndexed));
    }

    @Test
    void testBeanIndexedValue() throws OgnlException {
        Bean1 root = new Bean1();
        Object actual = Ognl.getValue("bean2.bean3.indexedValue[25]", context, root);
        assertNull(actual);
    }
}
