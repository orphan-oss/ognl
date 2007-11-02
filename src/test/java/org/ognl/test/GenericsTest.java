package org.ognl.test;

import org.ognl.test.objects.BaseGeneric;
import org.ognl.test.objects.GameGeneric;

/**
 * Tests java >= 1.5 generics support in ognl.
 */
public class GenericsTest 
{
    private static BaseGeneric ROOT = new GameGeneric();

    private static Object[][] TESTS = {
            { ROOT, "ids", new Long[] { 10l, 20l}},
    };
}
