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
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.BaseGeneric;
import ognl.test.objects.GameGeneric;
import ognl.test.objects.GameGenericObject;
import ognl.test.objects.ListSource;
import ognl.test.objects.ListSourceImpl;
import ognl.test.objects.Simple;

import java.util.Arrays;
import java.util.List;

public class MethodTest extends OgnlTestCase {

    private static Simple ROOT = new Simple();
    private static ListSource LIST = new ListSourceImpl();
    private static BaseGeneric<GameGenericObject, Long> GENERIC = new GameGeneric();

    private static Object[][] TESTS = {
            {"hashCode()", new Integer(ROOT.hashCode())},
            {"getBooleanValue() ? \"here\" : \"\"", ""},
            {"getValueIsTrue(!false) ? \"\" : \"here\" ", ""},
            {"messages.format('ShowAllCount', one)", ROOT.getMessages().format("ShowAllCount", ROOT.getOne())},
            {"messages.format('ShowAllCount', {one})", ROOT.getMessages().format("ShowAllCount", new Object[]{ROOT.getOne()})},
            {"messages.format('ShowAllCount', {one, two})", ROOT.getMessages().format("ShowAllCount", new Object[]{ROOT.getOne(), ROOT.getTwo()})},
            {"messages.format('ShowAllCount', one, two)", ROOT.getMessages().format("ShowAllCount", ROOT.getOne(), ROOT.getTwo())},
            {"getTestValue(@ognl.test.objects.SimpleEnum@ONE.value)", new Integer(2)},
            {"@ognl.test.MethodTest@getA().isProperty()", Boolean.FALSE},
            {"isDisabled()", Boolean.TRUE},
            {"isTruck", Boolean.TRUE},
            {"isEditorDisabled()", Boolean.FALSE},
            {LIST, "addValue(name)", Boolean.TRUE},
            {"getDisplayValue(methodsTest.allowDisplay)", "test"},
            {"isThisVarArgsWorking(three, rootValue)", Boolean.TRUE},
            {"isThisVarArgsWorking()", Boolean.TRUE},
            {GENERIC, "service.getFullMessageFor(value, null)", "Halo 3"},
            // TestCase for https://github.com/jkuhnert/ognl/issues/17 -  ArrayIndexOutOfBoundsException when trying to access BeanFactory
            {"testMethods.getBean('TestBean')", ROOT.getTestMethods().getBean("TestBean")},
            // https://issues.apache.org/jira/browse/OGNL-250 -  OnglRuntime getMethodValue fails to find method matching propertyName
            {"testMethods.testProperty", ROOT.getTestMethods().testProperty()},
            {"testMethods.argsTest1({one})", ROOT.getTestMethods().argsTest1(Arrays.asList(ROOT.getOne()).toArray())},    // toArray() is automatically done by OGNL type conversion
            // we need to cast out generics (insert "Object")
            {"testMethods.argsTest2({one})", ROOT.getTestMethods().argsTest2(Arrays.asList((Object) ROOT.getOne()))},
            //   Java 'ROOT.getTestMethods().argsTest1(Arrays.asList( ROOT.getOne() )' doesn't compile:
            //		--> The method argsTest(Object[]) in the type MethodTestMethods is not applicable for the arguments (List<Integer>)
            {"testMethods.argsTest3({one})", "List: [1]"},
            {"testMethods.showList(testMethods.getObjectList())", ROOT.getTestMethods().showList(ROOT.getTestMethods().getObjectList().toArray())},
            {"testMethods.showList(testMethods.getStringList())", ROOT.getTestMethods().showList(ROOT.getTestMethods().getStringList().toArray())},
            {"testMethods.showList(testMethods.getStringArray())", ROOT.getTestMethods().showList(ROOT.getTestMethods().getStringArray())},
            // TODO This one doesn't work - even 'toArray(new String[0]) returns Object[] and so the wrong method is called - currently no idea how to handle this...
            // { "testMethods.showList(testMethods.getStringList().toArray(new String[0]))", ROOT.getTestMethods().showList(ROOT.getTestMethods().getStringList().toArray(new String[0])) },
            // but this one works - at least in interpretation mode...
            {"testMethods.showStringList(testMethods.getStringList().toArray(new String[0]))", ROOT.getTestMethods().showStringList(ROOT.getTestMethods().getStringList().toArray(new String[0]))},

            //	https://github.com/jkuhnert/ognl/issues/23 - Exception selecting overloaded method in 3.1.4
            {"testMethods.avg({ 5, 5 })", ROOT.getTestMethods().avg((List) Arrays.asList(5, 5))},
    };

    public void testNullVarArgs() throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(ROOT);

        Object value = Ognl.getValue("isThisVarArgsWorking()", context, ROOT);

        assertTrue(value instanceof Boolean);
        assertTrue((Boolean) value);
    }

    public static class A {
        public boolean isProperty() {
            return false;
        }
    }

    public static A getA() {
        return new A();
    }

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new MethodTest((String) TESTS[i][1] + " (" + TESTS[i][2] + ")", TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            } else {
                result.addTest(new MethodTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", ROOT, (String) TESTS[i][0], TESTS[i][1]));
            }
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public MethodTest() {
        super();
    }

    public MethodTest(String name) {
        super(name);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                      Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
