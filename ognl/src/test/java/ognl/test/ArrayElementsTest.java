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
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrayElementsTest {

    private Root root;
    private int[] intArray;
    private String[] stringArray;

    private OgnlContext rootContext;
    private OgnlContext intArrayContext;
    private OgnlContext stringArrayContext;

    @BeforeEach
    void setUp() {
        root = new Root();
        rootContext = Ognl.createDefaultContext(root);

        intArray = new int[]{10, 20};
        intArrayContext = Ognl.createDefaultContext(intArray);

        stringArray = new String[]{"hello", "world"};
        stringArrayContext = Ognl.createDefaultContext(stringArray);
    }

    @Test
    void stringArrayLength() throws OgnlException {
        assertEquals(2, Ognl.getValue("length", stringArrayContext, stringArray));
    }

    @Test
    void stringArrayElement() throws OgnlException {
        assertEquals("world", Ognl.getValue("#root[1]", stringArrayContext, stringArray));
    }

    @Test
    void intArrayElement() throws OgnlException {
        assertEquals(20, Ognl.getValue("#root[1]", intArrayContext, intArray));
    }

    @Test
    void intArrayElementAfterSet() throws OgnlException {
        Ognl.setValue("#root[1]", intArrayContext, intArray, 50);
        assertEquals(50, Ognl.getValue("#root[1]", intArrayContext, intArray));
    }

    @Test
    void intArrayElementAfterSetWithString() throws OgnlException {
        Ognl.setValue("#root[1]", intArrayContext, intArray, "50");
        assertEquals(50, Ognl.getValue("#root[1]", intArrayContext, intArray));
    }

    @Test
    void rootIntValueAfterSetWithString() throws OgnlException {
        Ognl.setValue("intValue", rootContext, root, "50");
        assertEquals(50, Ognl.getValue("intValue", rootContext, root));
    }

    @Test
    void rootArrayAfterSetWithStringArray() throws OgnlException {
        Ognl.setValue("array", rootContext, root, new String[]{"50", "100"});
        assertEquals(Arrays.toString(new int[]{50, 100}), Arrays.toString((int[]) Ognl.getValue("array", rootContext, root)));
    }

    @Test
    void charArrayElement() throws OgnlException {
        assertEquals('}', Ognl.getValue("\"{Hello}\".toCharArray()[6]", rootContext, root));
    }

    @Test
    void charArrayElementFromString() throws OgnlException {
        assertEquals('p', Ognl.getValue("\"Tapestry\".toCharArray()[2]", rootContext, root));
    }

    @Test
    void charArray() throws OgnlException {
        assertEquals(Arrays.asList('1', '2', '3'), Ognl.getValue("{'1','2','3'}", rootContext, root));
    }

    @Test
    void booleanArray() throws OgnlException {
        assertEquals(Arrays.asList(true, true), Ognl.getValue("{ true, !false }", rootContext, root));
    }
}
