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

class MethodWithConversionTest {

    private Simple simple;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        simple = new Simple();
        context = Ognl.createDefaultContext(simple);
    }

    @Test
    void testSetValues() throws Exception {
        Ognl.getValue("setValues(10, \"10.56\", 34.225D)", context, simple);
        assertEquals("10", simple.getStringValue());
        assertEquals(10.56F, simple.getFloatValue());
        assertEquals(34, simple.getIntValue());
    }

    @Test
    void testStringValue() throws Exception {
        Ognl.getValue("setValues(10, \"10.56\", 34.225D)", context, simple);
        assertEquals("10", Ognl.getValue("stringValue", context, simple));
        assertEquals(10.56F, Ognl.getValue("floatValue", context, simple));
        assertEquals(34, Ognl.getValue("intValue", context, simple));
    }

    @Test
    void testStringValueWithChar() throws Exception {
        Ognl.setValue("stringValue", context, simple, 'x');
        assertEquals("x", Ognl.getValue("stringValue", context, simple));
    }

    @Test
    void testSetStringValue() throws Exception {
        Ognl.getValue("setStringValue('x')", context, simple);
        assertEquals("x", Ognl.getValue("stringValue", context, simple));
    }

    @Test
    void testFloatValue() throws Exception {
        Ognl.getValue("setValues(10, \"10.56\", 34.225D)", context, simple);
        assertEquals(10.56f, Ognl.getValue("floatValue", context, simple));
    }

    @Test
    void testGetValueIsTrue() throws Exception {
        Object actual = Ognl.getValue("getValueIsTrue(rootValue)", context, simple);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testMessagesFormat() throws Exception {
        Object actual = Ognl.getValue("messages.format('Testing', one, two, three)", context, simple);
        assertEquals("blah", actual);
    }
}
