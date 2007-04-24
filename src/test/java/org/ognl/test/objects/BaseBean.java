/**
 *
 */
package org.ognl.test.objects;


/**
 * Base class used to test inheritance class casting.
 */
public abstract class BaseBean {

    public abstract String getName();

    public boolean getActive()
    {
        return true;
    }

    public boolean isActive2()
    {
        return true;
    }

    public Two getTwo()
    {
        return new Two();
    }

    public String getMessage(String mes)
    {
        return "[" + mes + "]";
    }

    public boolean hasChildren(String name)
    {
        return name.length() > 2;
    }
}
