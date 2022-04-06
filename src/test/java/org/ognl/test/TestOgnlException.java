package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.OgnlException;

/**
 * Tests {@link OgnlException}.
 */
public class TestOgnlException extends TestCase {

    public void test_Throwable_Reason()
    {
        try {
            throwException();
        } catch (OgnlException e) {
            assertTrue(NumberFormatException.class.isInstance(e.getReason()));
        }
    }

    void throwException()
            throws OgnlException
    {
        try {
            Integer.parseInt("45ac");
        } catch (NumberFormatException et) {
            throw new OgnlException("Unable to parse input string.", et);
        }
    }
}
