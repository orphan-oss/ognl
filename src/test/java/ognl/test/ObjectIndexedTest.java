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
import ognl.OgnlException;
import ognl.SimpleNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectIndexedTest {

    protected OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @Test
    void testObjectIndexAccess() throws OgnlException {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression("#ka.sunk[#root]");

        context.put("ka", new Test1());

        Object actual = Ognl.getValue(expression, context, "aksdj");

        assertEquals("foo", actual);
    }

    @Test
    void testObjectIndexInSubclass() throws OgnlException {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression("#ka.sunk[#root]");

        context.put("ka", new Test2());

        Object actual = Ognl.getValue(expression, context, "aksdj");

        assertEquals("foo", actual);
    }

    @Test
    void testMultipleObjectIndexGetters() throws OgnlException {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression("#ka.sunk[#root]");

        context.put("ka", new Test3());

        assertThrows(OgnlException.class, () -> Ognl.getValue(expression, context, new Test3()));
    }

    @Test
    void testMultipleObjectIndexSetters() throws OgnlException {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression("#ka.sunk[#root]");

        context.put("ka", new Test4());

        assertThrows(OgnlException.class, () -> Ognl.getValue(expression, context, "aksdj"));
    }

    @Test
    void testMultipleObjectIndexMethodPairs() throws OgnlException {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression("#ka.sunk[#root]");

        context.put("ka", new Test5());

        assertThrows(OgnlException.class, () -> Ognl.getValue(expression, context, "aksdj"));
    }

    interface TestInterface {
        String getSunk(String index);

        void setSunk(String index, String sunk);
    }

    static class Test1 implements TestInterface {
        public String getSunk(String index) {
            return "foo";
        }

        public void setSunk(String index, String sunk) {
            /* do nothing */
        }
    }

    static class Test2 extends Test1 {
        public String getSunk(String index) {
            return "foo";
        }

        public void setSunk(String index, String sunk) {
            /* do nothing */
        }
    }

    static class Test3 extends Test1 {
        public String getSunk(String index) {
            return "foo";
        }

        public void setSunk(String index, String sunk) {
            /* do nothing */
        }

        public String getSunk(Object index) {
            return null;
        }
    }

    static class Test4 extends Test1 {
        public String getSunk(String index) {
            return "foo";
        }

        public void setSunk(String index, String sunk) {
            /* do nothing */
        }

        public void setSunk(Object index, String sunk) {
            /* do nothing */
        }
    }

    static class Test5 extends Test1 {
        public String getSunk(String index) {
            return "foo";
        }

        public void setSunk(String index, String sunk) {
            /* do nothing */
        }

        public String getSunk(Object index) {
            return null;
        }

        public void setSunk(Object index, String sunk) {
            /* do nothing */
        }
    }
}
