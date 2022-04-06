//--------------------------------------------------------------------------
//	Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.ognl.test.objects;

import org.ognl.DynamicSubscript;

import java.util.*;

public class Root extends Object
{
    public static final String      SIZE_STRING = "size";
    public static final int         STATIC_INT = 23;

    private int[]                   array = { 1, 2, 3, 4 };
    private Map                     map = new HashMap(23);
    private MyMap                   myMap = new MyMapImpl();
    private List                    list = Arrays.asList(new Object[] { null, this, array });
    private List                    settableList = new ArrayList(Arrays.asList(new Object[] { "foo", "bar", "baz" }));
    private int                     index = 1;
    private int                     intValue = 0;
    private String                  stringValue;
    private int                     yetAnotherIntValue = 46;
    private boolean                 privateAccessorBooleanValue = true;
    private int                     privateAccessorIntValue = 67;
    private int                     privateAccessorIntValue2 = 67;
    private int                     privateAccessorIntValue3 = 67;
    public String                   anotherStringValue = "foo";
    public int                      anotherIntValue = 123;
    public int                      six = 6;
    private boolean _disabled;
    private Locale _selected = Locale.getDefault();
    private List<List<Boolean>> _booleanValues = new ArrayList<List<Boolean>>();

    private boolean[] _booleanArray = {true, false, true, true};
    private List _list;
    private int verbosity = 87;
    private BeanProvider _beanProvider = new BeanProviderImpl();
    private boolean _render;
    private Boolean _readOnly = Boolean.FALSE;
    private Integer _objIndex = new Integer(1);
    private Object _genericObjIndex = new Integer(2);
    private Date _date = new Date();
    private boolean _openWindow = false;

    private ITreeContentProvider _contentProvider = new TreeContentProvider();
    private Indexed _indexed = new Indexed();
    private SearchTab _tab = new SearchTab();

    /*===================================================================
		Public static methods
	  ===================================================================*/
	public static int getStaticInt()
	{
	    return STATIC_INT;
	}

	/*===================================================================
		Constructors
	  ===================================================================*/
    public Root()
    {
        super();
    }

    /*===================================================================
		Private methods
	  ===================================================================*/
    {
        map.put( "test", this );
        map.put( "array", array );
        map.put( "list", list );
        map.put( "size", new Integer(5000) );
        map.put( DynamicSubscript.first, new Integer(99) );
        map.put( "baz", array);
        map.put("value", new Bean2());
        map.put("bar", new Bean3());
        map.put(new Long(82), "StringStuff=someValue");

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
        myMap.putAll( map );

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

    private boolean isPrivateAccessorBooleanValue()
    {
        return privateAccessorBooleanValue;
    }

    private void setPrivateAccessorBooleanValue(boolean value)
    {
        privateAccessorBooleanValue = value;
    }

    private int getPrivateAccessorIntValue()
    {
        return privateAccessorIntValue;
    }

    private void setPrivateAccessorIntValue(int value)
    {
        privateAccessorIntValue = value;
    }

	/*===================================================================
		Protected methods
	  ===================================================================*/
    protected int getPrivateAccessorIntValue2()
    {
        return privateAccessorIntValue2;
    }

    protected void setPrivateAccessorIntValue2(int value)
    {
        privateAccessorIntValue2 = value;
    }

	/*===================================================================
		Package protected methods
	  ===================================================================*/
    int getPrivateAccessorIntValue3()
    {
        return privateAccessorIntValue3;
    }

    void setPrivateAccessorIntValue3(int value)
    {
        privateAccessorIntValue3 = value;
    }

    /*===================================================================
		Public methods
	  ===================================================================*/
    public int[] getArray()
    {
        return array;
    }

    public boolean[] getBooleanArray()
    {
        return _booleanArray;
    }

    public void setArray(int[] value)
    {
        array = value;
    }

    public String format(String key, Object value)
    {
        return format(key, new Object[] { value });
    }

    public String format(String key, Object[] value)
    {
        return "formatted: "+key+" "+Arrays.toString(value);
    }

    public String getCurrentClass(String value)
    {
        return value + " stop";
    }

    public Messages getMessages()
    {
        return new Messages(map);
    }

    public Map getMap()
    {
        return map;
    }

    public MyMap getMyMap()
    {
        return myMap;
    }

    public List getList()
    {
        return list;
    }

    public Object getAsset(String key)
    {
        return key;
    }

    public List getSettableList()
    {
        return settableList;
    }

    public int getIndex()
    {
        return index;
    }

    public Integer getObjectIndex()
    {
        return _objIndex;
    }

    public Integer getNullIndex()
    {
        return null;
    }

    public Object getGenericIndex()
    {
        return _genericObjIndex;
    }

    public int getIntValue()
    {
        return intValue;
    }

    public void setIntValue(int value)
    {
        intValue = value;
    }

    public int getTheInt()
    {
        return six;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String value)
    {
        stringValue = value;
    }

    public String getIndexedStringValue()
    {
        return "array";
    }

    public Object getNullObject()
    {
        return null;
    }

    public String getTestString()
    {
        return "wiggle";
    }

    public Object getProperty()
    {
        return new Bean2();
    }

    public Bean2 getBean2()
    {
        return new Bean2();
    }

    public Object getIndexedProperty(String name)
    {
        return myMap.get(name);
    }

    public Indexed getIndexer()
    {
        return _indexed;
    }

    public BeanProvider getBeans()
    {
        return _beanProvider;
    }

    public boolean getBooleanValue()
    {
        return _disabled;
    }

    public void setBooleanValue(boolean value)
    {
        _disabled = value;
    }

    public boolean getDisabled()
    {
        return _disabled;
    }

    public void setDisabled(boolean disabled)
    {
        _disabled = disabled;
    }

    public Locale getSelected()
    {
        return _selected;
    }

    public void setSelected(Locale locale)
    {
        _selected = locale;
    }

    public Locale getCurrLocale()
    {
        return Locale.getDefault();
    }

    public int getCurrentLocaleVerbosity()
    {
        return verbosity;
    }

    public boolean getRenderNavigation()
    {
        return _render;
    }

    public void setSelectedList(List selected)
    {
        _list = selected;
    }

    public List getSelectedList()
    {
        return _list;
    }

    public Boolean getReadonly()
    {
        return _readOnly;
    }

    public void setReadonly(Boolean value)
    {
        _readOnly = value;
    }

    public Object getSelf()
    {
        return this;
    }

    public Date getTestDate()
    {
        return _date;
    }

    public String getWidth()
    {
        return "238px";
    }

    public Long getTheLong()
    {
        return new Long(4);
    }

    public boolean isSorted()
    {
        return true;
    }

    public TestClass getMyTest()
    {
        return new TestImpl();
    }

    public ITreeContentProvider getContentProvider()
    {
        return _contentProvider;
    }

    public boolean isPrintDelivery()
    {
        return true;
    }

    public Long getCurrentDeliveryId()
    {
        return 1l;
    }

    public Boolean isFlyingMonkey()
    {
        return Boolean.TRUE;
    }

    public Boolean isDumb()
    {
        return Boolean.FALSE;
    }

    public Date getExpiration()
    {
        return null;
    }

    public Long getMapKey()
    {
        return new Long(82);
    }

    public Object getArrayValue()
    {
        return new Object[] {new Integer("2"), new Integer("2")};
    }

    public List getResult()
    {
        List list = new ArrayList();
        list.add(new Object[]{new Integer("2"), new Integer("2")});
        list.add(new Object[]{new Integer("2"), new Integer("2")});
        list.add(new Object[]{new Integer("2"), new Integer("2")});

        return list;
    }

    public boolean isEditorDisabled()
    {
        return false;
    }

    public boolean isDisabled()
    {
        return true;
    }

    public boolean isOpenTransitionWin()
    {
        return _openWindow;
    }

    public void setOpenTransitionWin(boolean value)
    {
        _openWindow = value;
    }

    public boolean isOk(SimpleEnum value, String otherValue)
    {
        return true;
    }

    public List<List<Boolean>> getBooleanValues()
    {
        return _booleanValues;
    }

    public int getIndex1()
    {
        return 1;
    }

    public int getIndex2()
    {
        return 1;
    }

    public SearchTab getTab()
    {
        return _tab;
    }

    public void setTab(SearchTab tab)
    {
        _tab = tab;
    }

    public static class A
    {
        public int methodOfA(B b)
        {
            return 0;
        }

        public int getIntValue()
        {
            return 1;
        }
    }

    public static class B
    {
        public int methodOfB(int i)
        {
            return 0;
        }
    }

    public A getA()
    {
        return new A();
    }

    public B getB()
    {
        return new B();
    }
}
