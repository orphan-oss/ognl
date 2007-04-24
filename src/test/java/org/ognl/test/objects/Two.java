package org.ognl.test.objects;

/**
 *
 */
public class Two {

    public String getMessage(String mes)
    {
        return "[" + mes + "]";
    }

    public boolean hasChildren(String name)
    {
        return name.length() > 2;
    }
}
