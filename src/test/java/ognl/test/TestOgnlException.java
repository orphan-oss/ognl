package ognl.test;

import ognl.OgnlException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link OgnlException}.
 */
public class TestOgnlException {

    @Test
    public void test_Throwable_Reason() {
        try {
            throwException();
        } catch (OgnlException e) {
            assertTrue(e.getReason() instanceof NumberFormatException);
        }
    }

    void throwException() throws OgnlException {
        try {
            Integer.parseInt("45ac");
        } catch (NumberFormatException et) {
            throw new OgnlException("Unable to parse input string.", et);
        }
    }
}
