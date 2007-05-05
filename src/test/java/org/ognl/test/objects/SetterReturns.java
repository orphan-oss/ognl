package org.ognl.test.objects;

/**
 *
 */
public class SetterReturns {

    private String _value = "";

    public String getValue()
    {
        return _value;
    }

    public SetterReturns setValue(String value)
    {
        _value += value;
        return this;
    }
}
