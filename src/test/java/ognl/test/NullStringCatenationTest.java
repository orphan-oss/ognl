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

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NullStringCatenationTest {

    /**
     * It's used in test
     */
    public static final String MESSAGE = "blarney";

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(true));
    }

    @Test
    void testCatenateNullToString() throws Exception {
        Object actual = Ognl.getValue("\"bar\" + null", context, root);
        assertEquals("barnull", actual);
    }

    @Test
    void testCatenateNullObjectToString() throws Exception {
        Object actual = Ognl.getValue("\"bar\" + nullObject", context, root);
        assertEquals("barnull", actual);
    }

    @Test
    void testCatenateNullObjectToNumber() {
        assertThrows(NullPointerException.class,
                () -> Ognl.getValue("20.56 + nullObject", context, root),
                "nullObject");
    }

    @Test
    void testConditionalCatenation() throws Exception {
        Object actual = Ognl.getValue("(true ? 'tabHeader' : '') + (false ? 'tabHeader' : '')", context, root);
        assertEquals("tabHeader", actual);
    }

    @Test
    void testConditionalCatenationWithInt() throws Exception {
        Object actual = Ognl.getValue("theInt == 0 ? '5%' : theInt + '%'", context, root);
        assertEquals("6%", actual);
    }

    @Test
    void testCatenateWidth() throws Exception {
        Object actual = Ognl.getValue("'width:' + width + ';'", context, root);
        assertEquals("width:238px;", actual);
    }

    @Test
    void testCatenateLongAndIndex() throws Exception {
        Object actual = Ognl.getValue("theLong + '_' + index", context, root);
        assertEquals("4_1", actual);
    }

    @Test
    void testCatenateWithStaticField() throws Exception {
        Object actual = Ognl.getValue("'javascript:' + @ognl.test.NullStringCatenationTest@MESSAGE", context, root);
        assertEquals("javascript:blarney", actual);
    }

    @Test
    void testConditionalCatenationWithMethodCall() throws Exception {
        Object actual = Ognl.getValue("printDelivery ? '' : 'javascript:deliverySelected(' + property.carrier + ',' + currentDeliveryId + ')'", context, root);
        assertEquals("", actual);
    }

    @Test
    void testCatenateBeanIdAndInt() throws Exception {
        Object actual = Ognl.getValue("bean2.id + '_' + theInt", context, root);
        assertEquals("1_6", actual);
    }
}
