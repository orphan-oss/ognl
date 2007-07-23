package org.ognl.test.objects;

import java.util.ArrayList;

/**
 *
 */
public class ListSourceImpl extends ArrayList implements ListSource {

    public ListSourceImpl()
    {
    }

    public int getTotal()
    {
        return super.size();
    }

    public Object addValue(Object value)
    {
        return super.add(value);
    }

    public Object getName()
    {
        return null;
    }
}
