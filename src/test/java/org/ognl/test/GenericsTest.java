package org.ognl.test;

import junit.framework.TestSuite;
import org.ognl.test.objects.BaseGeneric;
import org.ognl.test.objects.GameGeneric;
import org.ognl.test.objects.GameGenericObject;
import org.ognl.test.objects.GenericRoot;

/**
 * Tests java >= 1.5 generics support in ognl.
 */
public class GenericsTest extends OgnlTestCase
{
    static GenericRoot ROOT = new GenericRoot();
    static BaseGeneric<GameGenericObject, Long> GENERIC = new GameGeneric();

    static Object[][] TESTS = {
            /* { ROOT, "cracker.param", null, new Integer(2), new Integer(2)}, */
            { GENERIC, "ids", null, new Long[] {1l, 101l}, new Long[] {1l, 101l}},
            /* { GENERIC, "ids", new Long[] {1l, 101l}, new String[] {"2", "34"}, new Long[]{2l, 34l}}, */
    };

    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for(int i = 0; i < TESTS.length; i++)
        {
            if (TESTS[i].length == 5)
            {
                result.addTest(new GenericsTest((String) TESTS[i][1] + " (" + TESTS[i][2] + ")", TESTS[i][0], (String) TESTS[i][1],
                                                TESTS[i][2], TESTS[i][3], TESTS[i][4]));
            }
        }

        return result;
    }

    public GenericsTest(String name, Object root, String expressionString,
                        Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }
}
