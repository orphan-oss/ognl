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

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetterWithConversionTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testIntValueConversion() throws Exception {
        Ognl.setValue("intValue", context, root, 6.5);
        assertEquals(6, Ognl.getValue("intValue", context, root));

        Ognl.setValue("intValue", context, root, 1025.87645);
        assertEquals(1025, Ognl.getValue("intValue", context, root));

        Ognl.setValue("intValue", context, root, "654");
        assertEquals(654, Ognl.getValue("intValue", context, root));
    }

    @Test
    void testStringValueConversion() throws Exception {
        Ognl.setValue("stringValue", context, root, 25);
        assertEquals("25", Ognl.getValue("stringValue", context, root));

        Ognl.setValue("stringValue", context, root, 100.25f);
        assertEquals("100.25", Ognl.getValue("stringValue", context, root));
    }

    @Test
    void testAnotherStringValueConversion() throws Exception {
        Ognl.setValue("anotherStringValue", context, root, 0);
        assertEquals("0", Ognl.getValue("anotherStringValue", context, root));

        Ognl.setValue("anotherStringValue", context, root, 0.5);
        assertEquals("0.5", Ognl.getValue("anotherStringValue", context, root));
    }

    @Test
    void testAnotherIntValueConversion() throws Exception {
        Ognl.setValue("anotherIntValue", context, root, "5");
        assertEquals(5, Ognl.getValue("anotherIntValue", context, root));

        Ognl.setValue("anotherIntValue", context, root, 100.25);
        assertEquals(100, Ognl.getValue("anotherIntValue", context, root));
    }
}
