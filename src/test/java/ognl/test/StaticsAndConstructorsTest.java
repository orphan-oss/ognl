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

import junit.framework.TestSuite;
import ognl.test.objects.Root;
import ognl.test.objects.Simple;

public class StaticsAndConstructorsTest extends OgnlTestCase {
    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            {"@java.lang.Class@forName(\"java.lang.Object\")", Object.class},
            {"@java.lang.Integer@MAX_VALUE", new Integer(Integer.MAX_VALUE)},
            {"@@max(3,4)", new Integer(4)},
            {"new java.lang.StringBuffer().append(55).toString()", "55"},
            {"class", ROOT.getClass()},
            {"@ognl.test.objects.Root@class", ROOT.getClass()},
            {"class.getName()", ROOT.getClass().getName()},
            {"@ognl.test.objects.Root@class.getName()", ROOT.getClass().getName()},
            {"@ognl.test.objects.Root@class.name", ROOT.getClass().getName()},
            {"class.getSuperclass()", ROOT.getClass().getSuperclass()},
            {"class.superclass", ROOT.getClass().getSuperclass()},
            {"class.name", ROOT.getClass().getName()},
            {"getStaticInt()", new Integer(Root.getStaticInt())},
            {"@ognl.test.objects.Root@getStaticInt()", new Integer(Root.getStaticInt())},
            {"new ognl.test.objects.Simple(property).getStringValue()", new Simple().getStringValue()},
            {"new ognl.test.objects.Simple(map['test'].property).getStringValue()", new Simple().getStringValue()},
            {"map.test.getCurrentClass(@ognl.test.StaticsAndConstructorsTest@KEY.toString())", "size stop"},
            {"new ognl.test.StaticsAndConstructorsTest$IntWrapper(index)", new IntWrapper(ROOT.getIndex())},
            {"new ognl.test.StaticsAndConstructorsTest$IntObjectWrapper(index)", new IntObjectWrapper(ROOT.getIndex())},
            {"new ognl.test.StaticsAndConstructorsTest$A(#root)", new A(ROOT)},
            {"@ognl.test.StaticsAndConstructorsTest$Animals@values().length != 2", Boolean.TRUE},
            {"isOk(@ognl.test.objects.SimpleEnum@ONE, null)", Boolean.TRUE},
            {"@ognl.test.objects.StaticInterface@staticMethod()", "static"}
    };

    public static final String KEY = "size";

    public static class IntWrapper {
        public IntWrapper(int value) {
            this.value = value;
        }

        private final int value;

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

    /*===================================================================
         Public static methods
       ===================================================================*/
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            result.addTest(new StaticsAndConstructorsTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", ROOT, (String) TESTS[i][0], TESTS[i][1]));
        }
        return result;
    }

    /*===================================================================
         Constructors
       ===================================================================*/
    public StaticsAndConstructorsTest() {
        super();
    }

    public StaticsAndConstructorsTest(String name) {
        super(name);
    }

    public StaticsAndConstructorsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public StaticsAndConstructorsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public StaticsAndConstructorsTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
