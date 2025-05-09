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

class PrivateAccessorTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(true));
    }

    @Test
    void testPrivateAccessorIntValue() throws Exception {
        Object actual = Ognl.getValue("getPrivateAccessorIntValue()", context, root);
        assertEquals(67, actual);

        actual = Ognl.getValue("privateAccessorIntValue", context, root);
        assertEquals(67, actual);

        Ognl.setValue("privateAccessorIntValue", context, root, 100);
        actual = Ognl.getValue("privateAccessorIntValue", context, root);
        assertEquals(100, actual);
    }

    @Test
    void testPrivateAccessorIntValue2() throws Exception {
        Object actual = Ognl.getValue("privateAccessorIntValue2", context, root);
        assertEquals(67, actual);

        Ognl.setValue("privateAccessorIntValue2", context, root, 100);
        actual = Ognl.getValue("privateAccessorIntValue2", context, root);
        assertEquals(100, actual);
    }

    @Test
    void testPrivateAccessorIntValue3() throws Exception {
        Object actual = Ognl.getValue("privateAccessorIntValue3", context, root);
        assertEquals(67, actual);

        Ognl.setValue("privateAccessorIntValue3", context, root, 100);

        actual = Ognl.getValue("privateAccessorIntValue3", context, root);
        assertEquals(100, actual);
    }

    @Test
    void testPrivateAccessorBooleanValue() throws Exception {
        Object actual = Ognl.getValue("privateAccessorBooleanValue", context, root);
        assertEquals(true, actual);

        Ognl.setValue("privateAccessorBooleanValue", context, root, false);

        actual = Ognl.getValue("privateAccessorBooleanValue", context, root);
        assertEquals(false, actual);
    }
}