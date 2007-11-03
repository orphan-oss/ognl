package org.ognl.test.objects;

import java.io.Serializable;

/**
 * Used to test ognl handling of java generics.
 */
public class BaseGeneric<E extends GenericObject, I extends Serializable> {

    E _value;
    GenericService _service;
    protected I[] ids;

    public BaseGeneric()
    {
        _service = new GenericServiceImpl();
    }

    public void setIds(I[] ids)
    {
        this.ids = ids;
    }

    public I[] getIds()
    {
        return this.ids;
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

    public String format(Object value)
    {
        return value.toString();
    }
}
