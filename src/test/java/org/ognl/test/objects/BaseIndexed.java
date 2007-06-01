package org.ognl.test.objects;

/**
 * Class used to test inheritance.
 */
public class BaseIndexed {

    public Object getLine(int index)
    {
        return "line:" + index;
    }
}
