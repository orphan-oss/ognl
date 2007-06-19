package org.ognl.test;

import junit.framework.TestSuite;
import org.ognl.test.objects.Root;
import org.ognl.test.objects.TestModel;

import java.util.Arrays;

/**
 *
 */
public class PropertyArithmeticTest extends OgnlTestCase {

    private static Root ROOT = new Root();
    private static TestModel MODEL = new TestModel();
    
    private static Object[][] TESTS = {
            { ROOT, "objectIndex > 0", Boolean.TRUE},
            { ROOT, "false", Boolean.FALSE},
            { ROOT, "!false || true", Boolean.TRUE},
            { ROOT, "property.bean3.value >= 24", Boolean.TRUE},
            { ROOT, "genericIndex-1", new Integer(1)},
            { ROOT, "((renderNavigation ? 0 : 1) + map.size) * theInt", new Integer(((ROOT.getRenderNavigation() ? 0 : 1 ) + ROOT.getMap().size()) * ROOT.getTheInt())},
            { ROOT, "{theInt + 1}", Arrays.asList(new Integer(ROOT.getTheInt() + 1)) },
            { MODEL, "(unassignedCopyModel.optionCount > 0 && canApproveCopy) || entry.copy.size() > 0", Boolean.TRUE },
            { ROOT, " !(printDelivery || @Boolean@FALSE)", Boolean.FALSE},
            { ROOT, "(getIndexedProperty('nested').size - 1) > genericIndex", Boolean.FALSE},
            { ROOT, "(getIndexedProperty('nested').size + 1) >= genericIndex", Boolean.TRUE},
            { ROOT, "(getIndexedProperty('nested').size + 1) == genericIndex", Boolean.TRUE},
            { ROOT, "(getIndexedProperty('nested').size + 1) < genericIndex", Boolean.FALSE},
            { ROOT, "map.size * genericIndex", new Integer(ROOT.getMap().size() * ((Integer)ROOT.getGenericIndex()).intValue())},
            { ROOT, "property == property", Boolean.TRUE},
    };

    public static TestSuite suite()
    {
        TestSuite       result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {

            if (TESTS[i].length == 5) {

                result.addTest(new PropertyArithmeticTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
            } else
                result.addTest(new PropertyArithmeticTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2]));
        }

        return result;
    }

    /*===================================================================
         Constructors
       ===================================================================*/
    public PropertyArithmeticTest()
    {
        super();
    }

    public PropertyArithmeticTest(String name)
    {
        super(name);
    }

    public PropertyArithmeticTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public PropertyArithmeticTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public PropertyArithmeticTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
