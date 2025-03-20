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
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.Root;
import ognl.test.objects.Simple;
import ognl.test.objects.StaticInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticsAndConstructorsTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(true));
    }

    @Test
    void testClassForName() throws Exception {
        assertEquals(Object.class, Ognl.getValue("@java.lang.Class@forName(\"java.lang.Object\")", context, root));
    }

    @Test
    void testIntegerMaxValue() throws Exception {
        assertEquals(Integer.MAX_VALUE, Ognl.getValue("@java.lang.Integer@MAX_VALUE", context, root));
    }

    @Test
    void testMaxFunction() throws Exception {
        assertEquals(4, Ognl.getValue("@@max(3,4)", context, root));
    }

    @Test
    void testStringBuffer() throws Exception {
        assertEquals("55", Ognl.getValue("new java.lang.StringBuffer().append(55).toString()", context, root));
    }

    @Test
    void testClass() throws Exception {
        assertEquals(root.getClass(), Ognl.getValue("class", context, root));
    }

    @Test
    void testRootClass() throws Exception {
        assertEquals(root.getClass(), Ognl.getValue("@ognl.test.objects.Root@class", context, root));
    }

    @Test
    void testClassName() throws Exception {
        assertEquals(root.getClass().getName(), Ognl.getValue("class.getName()", context, root));
    }

    @Test
    void testRootClassName() throws Exception {
        assertEquals(root.getClass().getName(), Ognl.getValue("@ognl.test.objects.Root@class.getName()", context, root));
    }

    @Test
    void testRootClassNameProperty() throws Exception {
        assertEquals(root.getClass().getName(), Ognl.getValue("@ognl.test.objects.Root@class.name", context, root));
    }

    @Test
    void testClassSuperclass() throws Exception {
        assertEquals(root.getClass().getSuperclass(), Ognl.getValue("class.getSuperclass()", context, root));
    }

    @Test
    void testClassSuperclassProperty() throws Exception {
        assertEquals(root.getClass().getSuperclass(), Ognl.getValue("class.superclass", context, root));
    }

    @Test
    void testClassNameProperty() throws Exception {
        assertEquals(root.getClass().getName(), Ognl.getValue("class.name", context, root));
    }

    @Test
    void testStaticInt() throws Exception {
        assertEquals(Root.getStaticInt(), Ognl.getValue("getStaticInt()", context, root));
    }

    @Test
    void testRootStaticInt() throws Exception {
        assertEquals(Root.getStaticInt(), Ognl.getValue("@ognl.test.objects.Root@getStaticInt()", context, root));
    }

    @Test
    void testSimpleStringValue() throws Exception {
        assertEquals(new Simple().getStringValue(), Ognl.getValue("new ognl.test.objects.Simple(property).getStringValue()", context, root));
    }

    @Test
    void testSimpleStringValueWithMap() throws Exception {
        assertEquals(new Simple().getStringValue(), Ognl.getValue("new ognl.test.objects.Simple(map['test'].property).getStringValue()", context, root));
    }

    @Test
    void testMapCurrentClass() throws Exception {
        Object actual = Ognl.getValue("map.test.getCurrentClass(@ognl.test.StaticsAndConstructorsTest@KEY.toString())", context, root);
        assertEquals("size stop", actual);
    }

    @Test
    void testIntWrapper() throws Exception {
        Object actual = Ognl.getValue("new ognl.test.StaticsAndConstructorsTest$IntWrapper(index)", context, root);
        assertEquals(new IntWrapper(root.getIndex()), actual);
    }

    @Test
    void testIntObjectWrapper() throws Exception {
        assertEquals(new IntObjectWrapper(root.getIndex()), Ognl.getValue("new ognl.test.StaticsAndConstructorsTest$IntObjectWrapper(index)", context, root));
    }

    @Test
    void testA() throws Exception {
        assertEquals(new A(root), Ognl.getValue("new ognl.test.StaticsAndConstructorsTest$A(#root)", context, root));
    }

    @Test
    void testAnimalsValues() throws Exception {
        assertTrue((Boolean) Ognl.getValue("@ognl.test.StaticsAndConstructorsTest$Animals@values().length != 2", context, root));
    }

    @Test
    void testIsOk() throws Exception {
        assertTrue((Boolean) Ognl.getValue("isOk(@ognl.test.objects.SimpleEnum@ONE, null)", context, root));
    }

    @Test
    void testStaticMethod() throws Exception {
        String expressionString = "@ognl.test.objects.StaticInterface@staticMethod()";
        Node expression = Ognl.compileExpression(context, root, expressionString);

        Object actual = Ognl.getValue(expression, context, (Object) null);

        assertEquals("static", actual);
        assertEquals(StaticInterface.staticMethod(), actual);
    }

    static final String KEY = "size";

    public static class IntWrapper {
        private final int value;

        public IntWrapper(int value) {
            this.value = value;
        }

        public String toString() {
            return Integer.toString(value);
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            IntWrapper that = (IntWrapper) o;

            return value == that.value;
        }
    }

    public static class IntObjectWrapper {

        public IntObjectWrapper(Integer value) {
            this.value = value;
        }

        private final Integer value;

        public String toString() {
            return value.toString();
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            IntObjectWrapper that = (IntObjectWrapper) o;

            return value.equals(that.value);
        }
    }

    public static class A {
        String key = "A";

        public A(Root root) {

        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            A a = (A) o;

            if (key != null ? !key.equals(a.key) : a.key != null) return false;

            return true;
        }
    }

    public enum Animals {
        Dog, Cat, Wallabee, Bear
    }
}
