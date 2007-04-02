package org.ognl.test.objects;

/**
 *
 */
public class ComponentSubclass extends ComponentImpl {

    int _count = 0;

    public int getCount()
    {
        return _count;
    }

    public void setCount(int count)
    {
        _count = count;
    }
}
