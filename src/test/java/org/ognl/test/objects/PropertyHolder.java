package org.ognl.test.objects;

/**
 * Simple class used to test various kind of property resolutions.
 */
public class PropertyHolder {

    String _value = "";
    String _search = "foo";

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }
    
    public boolean hasValue()
    {
        return _value != null && _value.length() > 0;
    }

    public void setSearch(String value)
    {
        _search = value;
    }

    public String getSearch()
    {
        return _search;
    }

    public void search()
    {
    }
}
