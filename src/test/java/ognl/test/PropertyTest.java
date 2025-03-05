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
import ognl.test.objects.BaseBean;
import ognl.test.objects.Bean2;
import ognl.test.objects.FirstBean;
import ognl.test.objects.PropertyHolder;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PropertyTest {

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm a 'CST'");
    /**
     * Used in tests
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    private static final String VALUE = "foo";

    private Root root;
    private BaseBean bean;
    private PropertyHolder property;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        bean = new FirstBean();
        property = new PropertyHolder();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(true));
    }

    @Test
    void testBooleanExpressions() throws Exception {
        assertEquals(Boolean.TRUE, Ognl.getValue("testString != null && !false", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("!getRenderNavigation() and !getReadonly()", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("!bean2.pageBreakAfter", context, root));
    }

    @Test
    void testMapExpressions() throws Exception {
        assertEquals(root.getMap(), Ognl.getValue("map", context, root));
        assertEquals(root, Ognl.getValue("map.test", context, root));
        assertEquals(root, Ognl.getValue("map[\"test\"]", context, root));
        assertEquals(root, Ognl.getValue("map[\"te\" + \"st\"]", context, root));
        assertEquals(root.getMap().get(Root.SIZE_STRING), Ognl.getValue("map[(\"s\" + \"i\") + \"ze\"]", context, root));
        assertEquals(root.getMap().get(Root.SIZE_STRING), Ognl.getValue("map[\"size\"]", context, root));
        assertEquals(root.getMap().get(Root.SIZE_STRING), Ognl.getValue("map[@ognl.test.objects.Root@SIZE_STRING]", context, root));
    }

    @Test
    void testStringExpressions() throws Exception {
        assertEquals(Boolean.FALSE, Ognl.getValue("stringValue != null && stringValue.length() > 0", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("indexedStringValue != null && indexedStringValue.length() > 0", context, root));
    }

    @Test
    void testArrayExpressions() throws Exception {
        assertEquals(root.getList(), Ognl.getValue("map.list", context, root));
        assertEquals(root.getArray()[0], Ognl.getValue("map.array[0]", context, root));
        assertEquals(root.getList().get(1), Ognl.getValue("map.list[1]", context, root));
        assertEquals(99, Ognl.getValue("map[^]", context, root));
        assertNull(Ognl.getValue("map[$]", context, root));
        assertEquals(root.getArray()[root.getArray().length - 1], Ognl.getValue("map.array[$]", context, root));
        assertEquals(root.getMap(), Ognl.getValue("[\"map\"]", context, root));
        assertEquals(root.getArray().length, Ognl.getValue("length", context, root.getArray()));
        assertEquals(root.getList().get(root.getList().size() / 2), Ognl.getValue("getMap().list[|]", context, root));
        assertEquals(root.getArray()[2] + root.getMap().size(), Ognl.getValue("map.(array[2] + size())", context, root));
        assertEquals(root.getMap(), Ognl.getValue("map.(#this)", context, root));
        assertEquals(root.getMap().get(Root.SIZE_STRING), Ognl.getValue("map.(#this != null ? #this['size'] : null)", context, root));
        assertEquals(99, Ognl.getValue("map[^].(#this == null ? 'empty' : #this)", context, root));
        assertEquals("empty", Ognl.getValue("map[$].(#this == null ? 'empty' : #this)", context, root));
        assertEquals(root, Ognl.getValue("map[$].(#root == null ? 'empty' : #root)", context, root));
    }

    @Test
    void testConditionalExpressions() throws Exception {
        assertEquals("first", Ognl.getValue("((selected != null) && (currLocale.toString() == selected.toString())) ? 'first' : 'second'", context, root));
        assertEquals(Arrays.asList(root.getStringValue(), root.getMap()), Ognl.getValue("{stringValue, getMap()}", context, root));
        assertEquals(Arrays.asList("stringValue", root.getMap().get("size")), Ognl.getValue("{'stringValue', map[\"test\"].map[\"size\"]}", context, root));
        assertEquals("100(this.checked)", Ognl.getValue("property.bean3.value + '(this.checked)'", context, root));
        assertEquals(root.getArray(), Ognl.getValue("getIndexedProperty(property.bean3.map[\"bar\"])", context, root));
        assertEquals(((Bean2) root.getProperty()).getBean3(), Ognl.getValue("getProperty().getBean3()", context, root));
    }

    @Test
    void testIntegerExpressions() throws Exception {
        assertEquals(0, Ognl.getValue("intValue", context, root));
        Ognl.setValue("intValue", context, root, 2);
        assertEquals(2, Ognl.getValue("intValue", context, root));
    }

    @Test
    void testBooleanExpressions2() throws Exception {
        assertEquals(Boolean.TRUE, Ognl.getValue("! booleanValue", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("booleanValue", context, root));
        Ognl.setValue("booleanValue", context, root, Boolean.TRUE);
        assertEquals(Boolean.TRUE, Ognl.getValue("booleanValue", context, root));
    }

    @Test
    void testMiscExpressions() throws Exception {
        assertEquals(Boolean.FALSE, Ognl.getValue("! disabled", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("disabled || readonly", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("property.bean3.value != null", context, root));
        assertEquals("background-color:blue; width:43px", Ognl.getValue("\"background-color:blue; width:\" + (currentLocaleVerbosity / 2) + \"px\"", context, root));
        assertEquals("noborder", Ognl.getValue("renderNavigation ? '' : 'noborder'", context, root));
        assertEquals(root.format("key", root.getArray()), Ognl.getValue("format('key', array)", context, root));
        assertEquals(root.format("key", 0), Ognl.getValue("format('key', intValue)", context, root));
        assertEquals(root.format("key", root.getMap().size()), Ognl.getValue("format('key', map.size)", context, root));
        assertEquals("disableButton(this,\"null\");clearElement(&quot;testFtpMessage&quot;)", Ognl.getValue("'disableButton(this,\"' + map.get('button-testing') + '\");clearElement(&quot;testFtpMessage&quot;)'", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("!disableWarning", context, root.getMap()));
        assertEquals(((Bean2) root.getMap().get("value")).getBean3().getValue(), Ognl.getValue("get('value').bean3.value", context, root.getMap()));
        assertEquals('p', Ognl.getValue("\"Tapestry\".toCharArray()[2]", context, root.getMap()));
        assertEquals(Boolean.TRUE, Ognl.getValue("nested.deep.last", context, root.getMap()));
        assertEquals("last foo stop", Ognl.getValue("'last ' + getCurrentClass(@ognl.test.PropertyTest@VALUE)", context, root));
        assertEquals(formatValue((int) ((Bean2) root.getProperty()).getMillis(), true, true), Ognl.getValue("@ognl.test.PropertyTest@formatValue(property.millis, true, true)", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("nullObject || !readonly", context, root));
        assertEquals(DATETIME_FORMAT.format(root.getTestDate()), Ognl.getValue("testDate == null ? '-' : @ognl.test.PropertyTest@DATETIME_FORMAT.format(testDate)", context, root));
        assertEquals("disabled", Ognl.getValue("disabled ? 'disabled' : 'othernot'", context, root));
        assertEquals("[ACT]", Ognl.getValue("two.getMessage(active ? 'ACT' : 'INA')", context, bean));
        assertEquals(Boolean.TRUE, Ognl.getValue("hasChildren('aaa')", context, bean));
        assertEquals(Boolean.FALSE, Ognl.getValue("two.hasChildren('aa')", context, bean));
        assertEquals(Boolean.FALSE, Ognl.getValue("two.hasChildren('a')", context, bean));
        assertEquals("currentSortAsc", Ognl.getValue("sorted ? (readonly ? 'currentSortDesc' : 'currentSortAsc') : 'currentSortNone'", context, root));
        assertEquals("NoIcon", Ognl.getValue("getAsset( (width?'Yes':'No')+'Icon' )", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("flyingMonkey", context, root));
        assertEquals("", Ognl.getValue("expiration == null ? '' : @ognl.test.PropertyTest@DATE_FORMAT.format(expiration)", context, root));
        assertEquals("javascript:toggle(1);", Ognl.getValue("printDelivery ? 'javascript:toggle(' + bean2.id + ');' : ''", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("openTransitionWin", context, root));
        assertEquals(0, Ognl.getValue("b.methodOfB(a.methodOfA(b)-1)", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("disabled", context, root));
        assertEquals("", Ognl.getValue("value", context, property));
        assertEquals("foo", Ognl.getValue("search", context, property));
    }

    public static String formatValue(int millis, boolean b1, boolean b2) {
        return millis + "-formatted";
    }
}
