package org.ognl.test.objects;

/**
 *
 */
public class GetterMethods {

    private int theInt = 1;

    public boolean isAllowDisplay(Object something)
    {
        return true;
    }

    public int getAllowDisplay()
    {
        return theInt;
    }

    public void setAllowDisplay(int val)
    {
        theInt = val;
    }
}
