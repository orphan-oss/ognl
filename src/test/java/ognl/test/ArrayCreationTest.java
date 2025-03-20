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

import ognl.ExpressionSyntaxException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.Entry;
import ognl.test.objects.Root;
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayCreationTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
        root = new Root();
    }

    @Test
    void stringArrayCreation() throws Exception {
        assertArrayEquals(new String[]{"one", "two"}, (String[]) Ognl.getValue("new String[] { \"one\", \"two\" }", context, root));
    }

    @Test
    void stringArrayWithIntegers() throws Exception {
        assertArrayEquals(new String[]{"1", "2"}, (String[]) Ognl.getValue("new String[] { 1, 2 }", context, root));
    }

    @Test
    void testIntegerArrayCreation() throws Exception {
        assertArrayEquals(new Integer[]{1, 2, 3}, (Integer[]) Ognl.getValue("new Integer[] { 1, 2, 3 }", context, root));
    }

    @Test
    void stringArrayWithSize() throws Exception {
        assertArrayEquals(new String[10], (String[]) Ognl.getValue("new String[10]", context, root));
    }

    @Test
    void invalidObjectArrayCreation() {
        assertThrows(ExpressionSyntaxException.class, () -> {
            Ognl.getValue("new Object[4] { #root, #this }", context, root);
        });
    }

    @Test
    void objectArrayWithSize() throws Exception {
        assertArrayEquals(new Object[4], (Object[]) Ognl.getValue("new Object[4]", context, root));
    }

    @Test
    void objectArrayWithElements() throws Exception {
        assertArrayEquals(new Object[]{root, root}, (Object[]) Ognl.getValue("new Object[] { #root, #this }", context, root));
    }

    @Test
    void simpleArrayCreation() throws Exception {
        assertArrayEquals(new Simple[5], (Simple[]) Ognl.getValue("new ognl.test.objects.Simple[5]", context, root));
    }

    @Test
    void simpleObjectArrayCreation() throws Exception {
        assertEquals(new Simple(new Object[5]), Ognl.getValue("new ognl.test.objects.Simple(new Object[5])", context, root));
    }

    @Test
    void simpleStringArrayCreation() throws Exception {
        assertEquals(new Simple(new String[5]), Ognl.getValue("new ognl.test.objects.Simple(new String[5])", context, root));
    }

    @Test
    void conditionalEntryArrayCreation() throws Exception {
        assertArrayEquals(new Entry[]{new Entry(), new Entry()}, (Entry[]) Ognl.getValue("objectIndex ? new ognl.test.objects.Entry[] { new ognl.test.objects.Entry(), new ognl.test.objects.Entry()} : new ognl.test.objects.Entry[] { new ognl.test.objects.Entry(), new ognl.test.objects.Entry()}", context, root));
    }

    @Test
    void simpleArrayWithElements() throws Exception {
        assertArrayEquals(new Simple[]{new Simple(), new Simple("foo", 1.0f, 2)}, (Simple[]) Ognl.getValue("new ognl.test.objects.Simple[] { new ognl.test.objects.Simple(), new ognl.test.objects.Simple(\"foo\", 1.0f, 2) }", context, root));
    }
}
