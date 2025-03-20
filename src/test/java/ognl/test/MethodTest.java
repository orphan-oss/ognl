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
import ognl.test.objects.BaseGeneric;
import ognl.test.objects.GameGeneric;
import ognl.test.objects.GameGenericObject;
import ognl.test.objects.ListSource;
import ognl.test.objects.ListSourceImpl;
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodTest {

    private Simple root;
    private ListSource list;
    private BaseGeneric<GameGenericObject, Long> generic;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Simple();
        list = new ListSourceImpl();
        generic = new GameGeneric();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testHashCode() throws Exception {
        Object actual = Ognl.getValue("hashCode()", context, root);
        assertEquals(root.hashCode(), actual);
    }

    @Test
    void testGetBooleanValue() throws Exception {
        Object actual = Ognl.getValue("getBooleanValue() ? \"here\" : \"\"", context, root);
        assertEquals("", actual);
    }

    @Test
    void testGetValueIsTrue() throws Exception {
        Object actual = Ognl.getValue("getValueIsTrue(!false) ? \"\" : \"here\" ", context, root);
        assertEquals("", actual);
    }

    @Test
    void testMessagesFormatShowAllCountOne() throws Exception {
        Object actual = Ognl.getValue("messages.format('ShowAllCount', one)", context, root);
        assertEquals(root.getMessages().format("ShowAllCount", root.getOne()), actual);
    }

    @Test
    void testMessagesFormatShowAllCountArrayOne() throws Exception {
        Object actual = Ognl.getValue("messages.format('ShowAllCount', {one})", context, root);
        assertEquals(root.getMessages().format("ShowAllCount", new Object[]{root.getOne()}), actual);
    }

    @Test
    void testMessagesFormatShowAllCountArrayOneTwo() throws Exception {
        Object actual = Ognl.getValue("messages.format('ShowAllCount', {one, two})", context, root);
        assertEquals(root.getMessages().format("ShowAllCount", new Object[]{root.getOne(), root.getTwo()}), actual);
    }

    @Test
    void testMessagesFormatShowAllCountOneTwo() throws Exception {
        Object actual = Ognl.getValue("messages.format('ShowAllCount', one, two)", context, root);
        assertEquals(root.getMessages().format("ShowAllCount", root.getOne(), root.getTwo()), actual);
    }

    @Test
    void testGetTestValue() throws Exception {
        Object actual = Ognl.getValue("getTestValue(@ognl.test.objects.SimpleEnum@ONE.value)", context, root);
        assertEquals(2, actual);
    }

    @Test
    void testGetAIsProperty() throws Exception {
        Object actual = Ognl.getValue("@ognl.test.MethodTest@getA().isProperty()", context, root);
        assertEquals(Boolean.FALSE, actual);
    }

    @Test
    void testIsDisabled() throws Exception {
        Object actual = Ognl.getValue("isDisabled()", context, root);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testIsTruck() throws Exception {
        assertEquals(Boolean.TRUE, Ognl.getValue("isTruck", context, root));
    }

    @Test
    void testIsEditorDisabled() throws Exception {
        Object actual = Ognl.getValue("isEditorDisabled()", context, root);
        assertEquals(Boolean.FALSE, actual);
    }

    @Test
    void testListAddValue() throws Exception {
        Object actual = Ognl.getValue("addValue(name)", context, list);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testGetDisplayValue() throws Exception {
        Object actual = Ognl.getValue("getDisplayValue(methodsTest.allowDisplay)", context, root);
        assertEquals("test", actual);
    }

    @Test
    void testIsThisVarArgsWorkingWithArgs() throws Exception {
        Object actual = Ognl.getValue("isThisVarArgsWorking(three, rootValue)", context, root);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testIsThisVarArgsWorkingWithoutArgs() throws Exception {
        Object actual = Ognl.getValue("isThisVarArgsWorking()", context, root);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testGenericServiceGetFullMessageFor() throws Exception {
        Object actual = Ognl.getValue("service.getFullMessageFor(value, null)", context, generic);
        assertEquals("Halo 3", actual);
    }

    @Test
    void testTestMethodsGetBean() throws Exception {
        Object actual = Ognl.getValue("testMethods.getBean('TestBean')", context, root);
        assertEquals(root.getTestMethods().getBean("TestBean"), actual);
    }

    @Test
    void testTestMethodsTestProperty() throws Exception {
        Object actual = Ognl.getValue("testMethods.testProperty", context, root);
        assertEquals(root.getTestMethods().testProperty(), actual);
    }

    @Test
    void testTestMethodsArgsTest1() throws Exception {
        Object actual = Ognl.getValue("testMethods.argsTest1({one})", context, root);
        assertEquals(root.getTestMethods().argsTest1(List.of(root.getOne()).toArray()), actual);
    }

    @Test
    void testTestMethodsArgsTest2() throws Exception {
        Object actual = Ognl.getValue("testMethods.argsTest2({one})", context, root);
        assertEquals(root.getTestMethods().argsTest2(List.of(root.getOne())), actual);
    }

    @Test
    void testTestMethodsArgsTest3() throws Exception {
        Object actual = Ognl.getValue("testMethods.argsTest3({one})", context, root);
        assertEquals("List: [1]", actual);
    }

    @Test
    void testTestMethodsShowListObjectList() throws Exception {
        Object actual = Ognl.getValue("testMethods.showList(testMethods.getObjectList())", context, root);
        assertEquals(root.getTestMethods().showList(root.getTestMethods().getObjectList().toArray()), actual);
    }

    @Test
    void testTestMethodsShowListStringList() throws Exception {
        Object actual = Ognl.getValue("testMethods.showList(testMethods.getStringList())", context, root);
        assertEquals(root.getTestMethods().showList(root.getTestMethods().getStringList().toArray()), actual);
    }

    @Test
    void testTestMethodsShowListStringArray() throws Exception {
        Object actual = Ognl.getValue("testMethods.showList(testMethods.getStringArray())", context, root);
        assertEquals(root.getTestMethods().showList(root.getTestMethods().getStringArray()), actual);
    }

    @Test
    void testTestMethodsShowStringList() throws Exception {
        Object actual = Ognl.getValue("testMethods.showStringList(testMethods.getStringList().toArray(new String[0]))", context, root);
        assertEquals(root.getTestMethods().showStringList(root.getTestMethods().getStringList().toArray(new String[0])), actual);
    }

    @Test
    void testTestMethodsAvg() throws Exception {
        Object actual = Ognl.getValue("testMethods.avg({ 5, 5 })", context, root);
        assertEquals(root.getTestMethods().avg(Arrays.asList(5, 5)), actual);
    }

    @Test
    void testNullVarArgs() throws OgnlException {
        Object value = Ognl.getValue("isThisVarArgsWorking()", context, root);

        assertInstanceOf(Boolean.class, value);
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
}
