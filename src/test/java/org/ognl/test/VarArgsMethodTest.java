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
package org.ognl.test;

import junit.framework.TestCase;
import ognl.Ognl;
import ognl.OgnlException;
import org.ognl.test.objects.Simple;

import java.util.HashMap;

public class VarArgsMethodTest extends TestCase {

    private static Simple ROOT = new Simple();

    public void testNullVarArgs() throws OgnlException {
        Object value = Ognl.getValue("isNullVarArgs()", new HashMap(), ROOT);

        assertTrue(value instanceof String);
        assertEquals("null", value);
    }

    public void testVarArgsWithSingleArg() throws Exception {
        Object value = Ognl.getValue("isStringVarArgs(new String())", new HashMap(), ROOT);

        assertTrue(value instanceof String);
        assertEquals("args", value);
    }

    public void testVarArgsWithMultipleArgs() throws Exception {
        Object value = Ognl.getValue("isStringVarArgs(new String(), new String())", new HashMap(), ROOT);

        assertTrue(value instanceof String);
        assertEquals("args", value);
    }

    public void testNestedNullVarArgs() throws OgnlException {
        Object value = Ognl.getValue("get().request()", new HashMap(), ROOT);

        assertTrue(value instanceof String);
        assertEquals("null", value);
    }

    public void testNestedSingleVarArgs() throws OgnlException {
        Object value = Ognl.getValue("get().request(new String())", new HashMap(), ROOT);

        assertTrue(value instanceof String);
        assertEquals("args", value);
    }

    public void testNestedMultipleVarArgs() throws OgnlException {
        Object value = Ognl.getValue("get().request(new String(), new String())", new HashMap(), ROOT);

        assertTrue(value instanceof String);
        assertEquals("args", value);
    }

}
