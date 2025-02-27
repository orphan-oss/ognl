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
package ognl.test.objects;

import ognl.DynamicSubscript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Root {

    public static final String SIZE_STRING = "size";
    public static final int STATIC_INT = 23;

    private int[] array = {1, 2, 3, 4};
    private final Map<Object, Object> map = new HashMap<>(23);
    private final MyMap myMap = new MyMapImpl();
    private final List<Object> list = Arrays.asList(null, this, array);
    private final List<Object> settableList = new ArrayList<>(Arrays.asList("foo", "bar", "baz"));
    private final int index = 1;
    private int intValue = 0;
    private String stringValue;
    private int yetAnotherIntValue = 46;
    private boolean privateAccessorBooleanValue = true;
    private int privateAccessorIntValue = 67;
    private int privateAccessorIntValue2 = 67;
    private int privateAccessorIntValue3 = 67;
    public String anotherStringValue = "foo";
    public int anotherIntValue = 123;
    public int six = 6;
    private boolean _disabled;
    private Locale _selected = Locale.getDefault();
    private final List<List<Boolean>> _booleanValues = new ArrayList<>();

    private final boolean[] _booleanArray = {true, false, true, true};
    private List<Object> _list;
    private final int verbosity = 87;
    private final BeanProvider _beanProvider = new BeanProviderImpl();
    private boolean _render;
    private Boolean _readOnly = Boolean.FALSE;
    private final Integer _objIndex = 1;
    private final Object _genericObjIndex = 2;
    private final Date _date = new Date();
    private boolean _openWindow = false;

    private final ITreeContentProvider _contentProvider = new TreeContentProvider();
    private final Indexed _indexed = new Indexed();
    private SearchTab _tab = new SearchTab();

    /*===================================================================
        Public static methods
    ===================================================================*/
    public static int getStaticInt() {
        return STATIC_INT;
    }

    /*===================================================================
        Constructors
      ===================================================================*/
    public Root() {
        map.put("test", this);
        map.put("array", array);
        map.put("list", list);
        map.put("size", 5000);
        map.put(DynamicSubscript.first, 99);
        map.put("baz", array);
        map.put("value", new Bean2());
        map.put("bar", new Bean3());
        map.put(82L, "StringStuff=someValue");

        IFormComponent comp = new FormComponentImpl();
        comp.setClientId("formComponent");

        IForm form = new FormImpl();
        form.setClientId("form1");
        comp.setForm(form);

        map.put("comp", comp);

        Map newMap = new HashMap();
        Map chain = new HashMap();
        newMap.put("deep", chain);

        chain.put("last", Boolean.TRUE);

        map.put("nested", newMap);

        /* make myMap identical */
        myMap.putAll(map);

        List<Boolean> bool1 = new ArrayList<Boolean>();
        bool1.add(Boolean.TRUE);
        bool1.add(Boolean.FALSE);
        bool1.add(Boolean.TRUE);

        _booleanValues.add(bool1);

        List<Boolean> bool2 = new ArrayList<Boolean>();
        bool2.add(Boolean.TRUE);
        bool2.add(Boolean.FALSE);
        bool2.add(Boolean.TRUE);

        _booleanValues.add(bool2);
    }

    private boolean isPrivateAccessorBooleanValue() {
        return privateAccessorBooleanValue;
    }

    private void setPrivateAccessorBooleanValue(boolean value) {
        privateAccessorBooleanValue = value;
    }

    private int getPrivateAccessorIntValue() {
        return privateAccessorIntValue;
    }

    private void setPrivateAccessorIntValue(int value) {
        privateAccessorIntValue = value;
    }

    /*===================================================================
        Protected methods
      ===================================================================*/
    protected int getPrivateAccessorIntValue2() {
        return privateAccessorIntValue2;
    }

    protected void setPrivateAccessorIntValue2(int value) {
        privateAccessorIntValue2 = value;
    }

    /*===================================================================
        Package protected methods
      ===================================================================*/
    int getPrivateAccessorIntValue3() {
        return privateAccessorIntValue3;
    }

    void setPrivateAccessorIntValue3(int value) {
        privateAccessorIntValue3 = value;
    }

    /*===================================================================
		Public methods
	  ===================================================================*/
    public int[] getArray() {
        return array;
    }

    public boolean[] getBooleanArray() {
        return _booleanArray;
    }

    public void setArray(int[] value) {
        array = value;
    }

    public String format(String key, Object value) {
        return format(key, new Object[]{value});
    }

    public String format(String key, Object[] value) {
        return "formatted: " + key + " " + Arrays.toString(value);
    }

    public String getCurrentClass(String value) {
        return value + " stop";
    }

    public Messages getMessages() {
        return new Messages(map);
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    public MyMap getMyMap() {
        return myMap;
    }

    public List getList() {
        return list;
    }

    public Object getAsset(String key) {
        return key;
    }

    public List getSettableList() {
        return settableList;
    }

    public int getIndex() {
        return index;
    }

    public Integer getObjectIndex() {
        return _objIndex;
    }

    public Integer getNullIndex() {
        return null;
    }

    public Object getGenericIndex() {
        return _genericObjIndex;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int value) {
        intValue = value;
    }

    public int getTheInt() {
        return six;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String value) {
        stringValue = value;
    }

    public String getIndexedStringValue() {
        return "array";
    }

    public Object getNullObject() {
        return null;
    }

    public String getTestString() {
        return "wiggle";
    }

    public Object getProperty() {
        return new Bean2();
    }

    public Bean2 getBean2() {
        return new Bean2();
    }

    public Object getIndexedProperty(String name) {
        return myMap.get(name);
    }

    public Indexed getIndexer() {
        return _indexed;
    }

    public BeanProvider getBeans() {
        return _beanProvider;
    }

    public boolean getBooleanValue() {
        return _disabled;
    }

    public void setBooleanValue(boolean value) {
        _disabled = value;
    }

    public boolean getDisabled() {
        return _disabled;
    }

    public void setDisabled(boolean disabled) {
        _disabled = disabled;
    }

    public Locale getSelected() {
        return _selected;
    }

    public void setSelected(Locale locale) {
        _selected = locale;
    }

    public Locale getCurrLocale() {
        return Locale.getDefault();
    }

    public int getCurrentLocaleVerbosity() {
        return verbosity;
    }

    public boolean getRenderNavigation() {
        return _render;
    }

    public void setSelectedList(List selected) {
        _list = selected;
    }

    public List getSelectedList() {
        return _list;
    }

    public Boolean getReadonly() {
        return _readOnly;
    }

    public void setReadonly(Boolean value) {
        _readOnly = value;
    }

    public Object getSelf() {
        return this;
    }

    public Date getTestDate() {
        return _date;
    }

    public String getWidth() {
        return "238px";
    }

    public Long getTheLong() {
        return 4L;
    }

    public boolean isSorted() {
        return true;
    }

    public TestClass getMyTest() {
        return new TestImpl();
    }

    public ITreeContentProvider getContentProvider() {
        return _contentProvider;
    }

    public boolean isPrintDelivery() {
        return true;
    }

    public Long getCurrentDeliveryId() {
        return 1l;
    }

    public Boolean isFlyingMonkey() {
        return Boolean.TRUE;
    }

    public Boolean isDumb() {
        return Boolean.FALSE;
    }

    public Date getExpiration() {
        return null;
    }

    public Long getMapKey() {
        return 82L;
    }

    public Object getArrayValue() {
        return new Object[]{Integer.valueOf("2"), Integer.valueOf("2")};
    }

    public List<Object> getResult() {
        List<Object> list = new ArrayList<>();
        list.add(new Object[]{Integer.valueOf("2"), Integer.valueOf("2")});
        list.add(new Object[]{Integer.valueOf("2"), Integer.valueOf("2")});
        list.add(new Object[]{Integer.valueOf("2"), Integer.valueOf("2")});

        return list;
    }

    public boolean isEditorDisabled() {
        return false;
    }

    public boolean isDisabled() {
        return true;
    }

    public boolean isOpenTransitionWin() {
        return _openWindow;
    }

    public void setOpenTransitionWin(boolean value) {
        _openWindow = value;
    }

    public boolean isOk(SimpleEnum value, String otherValue) {
        return true;
    }

    public List<List<Boolean>> getBooleanValues() {
        return _booleanValues;
    }

    public int getIndex1() {
        return 1;
    }

    public int getIndex2() {
        return 1;
    }

    public SearchTab getTab() {
        return _tab;
    }

    public void setTab(SearchTab tab) {
        _tab = tab;
    }

    public static class A {
        public int methodOfA(B b) {
            return 0;
        }

        public int getIntValue() {
            return 1;
        }
    }

    public static class B {
        public int methodOfB(int i) {
            return 0;
        }
    }

    public A getA() {
        return new A();
    }

    public B getB() {
        return new B();
    }
}
