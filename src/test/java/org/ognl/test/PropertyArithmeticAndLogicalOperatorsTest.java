package org.ognl.test;

import junit.framework.TestSuite;
import org.ognl.test.objects.Root;
import org.ognl.test.objects.SimpleNumeric;
import org.ognl.test.objects.TestModel;

import java.util.Arrays;

/**
 *
 */
public class PropertyArithmeticAndLogicalOperatorsTest extends OgnlTestCase {

    private static Root ROOT = new Root();
    private static TestModel MODEL = new TestModel();
    private static SimpleNumeric NUMERIC = new SimpleNumeric();

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
            { ROOT, "property.bean3.value % 2 == 0", Boolean.TRUE},
            { ROOT, "genericIndex % 3 == 0", Boolean.FALSE},
            { ROOT, "genericIndex % theInt == property.bean3.value", Boolean.FALSE},
            { ROOT, "theInt / 100.0", ROOT.getTheInt() / 100.0},
            { ROOT, "@java.lang.Long@valueOf('100') == @java.lang.Long@valueOf('100')", Boolean.TRUE},
            { NUMERIC, "budget - timeBilled", new Double(NUMERIC.getBudget() - NUMERIC.getTimeBilled())},
            { NUMERIC, "(budget % tableSize) == 0", Boolean.TRUE}
    };

    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++)
        {
            if (TESTS[i].length == 5)
            {
                result.addTest(new PropertyArithmeticAndLogicalOperatorsTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
            } else
                result.addTest(new PropertyArithmeticAndLogicalOperatorsTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2]));
        }

        return result;
    }

    /*===================================================================
         Constructors
       ===================================================================*/
    public PropertyArithmeticAndLogicalOperatorsTest()
    {
        super();
    }

    public PropertyArithmeticAndLogicalOperatorsTest(String name)
    {
        super(name);
    }

    public PropertyArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString,
                                                     Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public PropertyArithmeticAndLogicalOperatorsTest(String name, Object root, String expressionString,
                                                     Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public PropertyArithmeticAndLogicalOperatorsTest(String name, Object root,
                                                     String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
