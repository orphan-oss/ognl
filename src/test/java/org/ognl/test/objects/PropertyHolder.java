package org.ognl.test.objects;

/**
 * Simple class used to test various kind of property resolutions.
 */
public class PropertyHolder {

    String _value = "";

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
}
