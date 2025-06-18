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
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstantTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void test12345() throws Exception {
        Object actual = Ognl.getValue("12345", context, root);
        assertEquals(12345, actual);
    }

    @Test
    void test0x100() throws Exception {
        Object actual = Ognl.getValue("0x100", context, root);
        assertEquals(256, actual);
    }

    @Test
    void test0xfE() throws Exception {
        Object actual = Ognl.getValue("0xfE", context, root);
        assertEquals(254, actual);
    }

    @Test
    void test01000() throws Exception {
        assertEquals(512, Ognl.getValue("01000", context, root));
    }

    @Test
    void test1234L() throws Exception {
        Object actual = Ognl.getValue("1234L", context, root);
        assertEquals(1234L, actual);
    }

    @Test
    void test12_34() throws Exception {
        Object actual = Ognl.getValue("12.34", context, root);
        assertEquals(12.34, actual);
    }

    @Test
    void test_1234() throws Exception {
        Object actual = Ognl.getValue(".1234", context, root);
        assertEquals(0.1234, actual);
    }

    @Test
    void test12_34f() throws Exception {
        Object actual = Ognl.getValue("12.34f", context, root);
        assertEquals(12.34F, actual);
    }

    @Test
    void test12_() throws Exception {
        Object actual = Ognl.getValue("12.", context, root);
        assertEquals(12.0, actual);
    }

    @Test
    void test12e_1d() throws Exception {
        Object actual = Ognl.getValue("12e+1d", context, root);
        assertEquals(120.0, actual);
    }

    @Test
    void test_x() throws Exception {
        Object actual = Ognl.getValue("'x'", context, root);
        assertEquals('x', actual);
    }

    @Test
    void test_n() throws Exception {
        Object actual = Ognl.getValue("'\\n'", context, root);
        assertEquals('\n', actual);
    }

    @Test
    void test_u048c() throws Exception {
        Object actual = Ognl.getValue("'\\u048c'", context, root);
        assertEquals('\u048c', actual);
    }

    @Test
    void test_47() throws Exception {
        Object actual = Ognl.getValue("'\\47'", context, root);
        assertEquals('\47', actual);
    }

    @Test
    void test_367() throws Exception {
        Object actual = Ognl.getValue("'\\367'", context, root);
        assertEquals('\367', actual);
    }

    @Test
    void test_367Exception() {
        assertThrows(ExpressionSyntaxException.class,
                () -> Ognl.getValue("'\\367", context, root),
                "Invalid octal escape sequence");
    }

    @Test
    void test_xException() {
        assertThrows(ExpressionSyntaxException.class,
                () -> Ognl.getValue("'\\x'", context, root),
                "Invalid hexadecimal escape sequence");
    }

    @Test
    void testHelloWorld() throws Exception {
        Object actual = Ognl.getValue("\"hello world\"", context, root);
        assertEquals("hello world", actual);
    }

    @Test
    void testUnicodeString() throws Exception {
        Object actual = Ognl.getValue("\"\\u00a0\\u0068ell\\'o\\\\\\n\\r\\f\\t\\b\\\"\\167orld\\\"\"", context, root);
        assertEquals("\u00a0hell'o\\\n\r\f\t\b\"world\"", actual);
    }

    @Test
    void testHelloWorldException() {
        assertThrows(ExpressionSyntaxException.class,
                () -> Ognl.getValue("\"hello world", context, root),
                "Unterminated string");
    }

    @Test
    void testHelloXWorldException() {
        assertThrows(ExpressionSyntaxException.class,
                () -> Ognl.getValue("\"hello\\x world\"", context, root),
                "Invalid escape sequence");
    }

    @Test
    void testNull() throws Exception {
        Object actual = Ognl.getValue("null", context, root);
        assertNull(actual);
    }

    @Test
    void testTrue() throws Exception {
        Object actual = Ognl.getValue("true", context, root);
        assertEquals(true, actual);
    }

    @Test
    void testFalse() throws Exception {
        Object actual = Ognl.getValue("false", context, root);
        assertEquals(false, actual);
    }

    @Test
    void testArray() throws Exception {
        Object actual = Ognl.getValue("{ false, true, null, 0, 1. }", context, root);
        assertEquals(Arrays.asList(false, true, null, 0, 1.0), actual);
    }

    @Test
    void testHtmlPublic() throws Exception {
        Object actual = Ognl.getValue("'HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"'", context, root);
        assertEquals("HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"", actual);
    }

}
