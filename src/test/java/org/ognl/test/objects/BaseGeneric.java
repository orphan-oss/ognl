package org.ognl.test.objects;

/**
 * Used to test ognl handling of java generics.
 */
public class BaseGeneric<E extends GenericObject> {

    E _value;
    GenericService _service;

    public BaseGeneric()
    {
        _service = new GenericServiceImpl();
    }

    public String getMessage()
    {
        return "Message";
    }

    public E getValue()
    {
        return _value;
    }

    public GenericService getService()
    {
        return _service;
    }
}
