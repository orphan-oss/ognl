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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PrimitiveArrayTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testBooleanArrayCreation() throws Exception {
        boolean[] actual = (boolean[]) Ognl.getValue("new boolean[5]", context, root);
        assertArrayEquals(new boolean[5], actual);

        actual = (boolean[]) Ognl.getValue("new boolean[] { true, false }", context, root);
        assertArrayEquals(new boolean[]{true, false}, actual);

        actual = (boolean[]) Ognl.getValue("new boolean[] { 0, 1, 5.5 }", context, root);
        assertArrayEquals(new boolean[]{false, true, true}, actual);
    }

    @Test
    void testCharArrayCreation() throws Exception {
        char[] actual = (char[]) Ognl.getValue("new char[] { 'a', 'b' }", context, root);
        assertArrayEquals(new char[]{'a', 'b'}, actual);

        actual = (char[]) Ognl.getValue("new char[] { 10, 11 }", context, root);
        assertArrayEquals(new char[]{(char) 10, (char) 11}, actual);
    }

    @Test
    void testByteArrayCreation() throws Exception {
        byte[] actual = (byte[]) Ognl.getValue("new byte[] { 1, 2 }", context, root);
        assertArrayEquals(new byte[]{1, 2}, actual);
    }

    @Test
    void testShortArrayCreation() throws Exception {
        short[] actual = (short[]) Ognl.getValue("new short[] { 1, 2 }", context, root);
        assertArrayEquals(new short[]{1, 2}, actual);
    }

    @Test
    void testIntArrayCreation() throws Exception {
        int[] actual = (int[]) Ognl.getValue("new int[six]", context, root);
        assertArrayEquals(new int[root.six], actual);

        actual = (int[]) Ognl.getValue("new int[#root.six]", context, root);
        assertArrayEquals(new int[root.six], actual);

        actual = (int[]) Ognl.getValue("new int[6]", context, root);
        assertArrayEquals(new int[6], actual);

        actual = (int[]) Ognl.getValue("new int[] { 1, 2 }", context, root);
        assertArrayEquals(new int[]{1, 2}, actual);
    }

    @Test
    void testLongArrayCreation() throws Exception {
        long[] actual = (long[]) Ognl.getValue("new long[] { 1, 2 }", context, root);
        assertArrayEquals(new long[]{1, 2}, actual);
    }

    @Test
    void testFloatArrayCreation() throws Exception {
        float[] actual = (float[]) Ognl.getValue("new float[] { 1, 2 }", context, root);
        assertArrayEquals(new float[]{1, 2}, actual);
    }

    @Test
    void testDoubleArrayCreation() throws Exception {
        double[] actual = (double[]) Ognl.getValue("new double[] { 1, 2 }", context, root);
        assertArrayEquals(new double[]{1, 2}, actual);
    }
}
