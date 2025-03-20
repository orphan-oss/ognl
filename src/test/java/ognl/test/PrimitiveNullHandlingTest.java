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
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimitiveNullHandlingTest {

    private Simple simple;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        simple = new Simple();
        simple.setFloatValue(10.56f);
        simple.setIntValue(34);
        context = Ognl.createDefaultContext(simple);
    }

    @Test
    void testFloatValue() throws Exception {
        Object actual = Ognl.getValue("floatValue", context, simple);
        assertEquals(10.56f, actual);

        Ognl.setValue("floatValue", context, simple, null);

        actual = Ognl.getValue("floatValue", context, simple);
        assertEquals(0f, actual);
    }

    @Test
    void testIntValue() throws Exception {
        Object actual = Ognl.getValue("intValue", context, simple);
        assertEquals(34, actual);

        Ognl.setValue("intValue", context, simple, null);

        actual = Ognl.getValue("intValue", context, simple);
        assertEquals(0, actual);
    }

    @Test
    void testBooleanValue() throws Exception {
        Object actual = Ognl.getValue("booleanValue", context, simple);
        assertEquals(false, actual);

        Ognl.setValue("booleanValue", context, simple, true);
        actual = Ognl.getValue("booleanValue", context, simple);
        assertEquals(true, actual);

        Ognl.setValue("booleanValue", context, simple, null);
        actual = Ognl.getValue("booleanValue", context, simple);
        assertEquals(false, actual);
    }
}
