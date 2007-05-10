package org.ognl.test.objects;

/**
 *
 */
public enum SimpleEnum {

    ONE (1);

    private int _value;

    private SimpleEnum(int value)
    {
        _value = value;
    }

    public int getValue()
    {
        return _value;
    }
}
