/*
 * Copyright 2020 OGNL Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ognl.test;

import ognl.Ognl;
import ognl.OgnlException;
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VarArgsMethodTest {

    private Simple root;

    @BeforeEach
    void setUp() {
        root = new Simple();
    }

    @Test
    void testNullVarArgs() throws OgnlException {
        Object value = Ognl.getValue("isNullVarArgs()", root);

        assertInstanceOf(String.class, value);
        assertEquals("null", value);
    }

    @Test
    void testVarArgsWithSingleArg() throws Exception {
        Object value = Ognl.getValue("isStringVarArgs(new String())", root);

        assertInstanceOf(String.class, value);
        assertEquals("args", value);
    }

    @Test
    void testVarArgsWithMultipleArgs() throws Exception {
        Object value = Ognl.getValue("isStringVarArgs(new String(), new String())", root);

        assertInstanceOf(String.class, value);
        assertEquals("args", value);
    }

    @Test
    void testNestedNullVarArgs() throws OgnlException {
        Object value = Ognl.getValue("get().request()", root);

        assertInstanceOf(String.class, value);
        assertEquals("null", value);
    }

    @Test
    void testNestedSingleVarArgs() throws OgnlException {
        Object value = Ognl.getValue("get().request(new String())", root);

        assertInstanceOf(String.class, value);
        assertEquals("args", value);
    }

    @Test
    void testNestedMultipleVarArgs() throws OgnlException {
        Object value = Ognl.getValue("get().request(new String(), new String())", root);

        assertInstanceOf(String.class, value);
        assertEquals("args", value);
    }

}
