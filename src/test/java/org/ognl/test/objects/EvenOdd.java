package org.ognl.test.objects;

/**
 *
 */
public class EvenOdd {

    private boolean even = true;

    /**
     * Returns "even" or "odd". Whatever it returns on one invocation, it will
     * return the opposite on the next. By default, the first value returned is
     * "even".
     */
    public String getNext()
    {
        String result = even ? "even" : "odd";

        even = !even;

        return result;
    }

    public boolean isEven()
    {
        return even;
    }

    /**
     * Overrides the even flag.
     */

    public void setEven(boolean value)
    {
        even = value;
    }
}
