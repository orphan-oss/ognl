package ognl;

import junit.framework.TestCase;

/**
 * Test for OGNL-118.
 */
public class InExpressionTest extends TestCase {

    public void test_String_In()
            throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        Object node = Ognl.parseExpression("#name in {\"Greenland\", \"Austin\", \"Africa\", \"Rome\"}");
        Object root = null;

        context.put("name", "Austin");
        assertEquals(Boolean.TRUE, Ognl.getValue(node, context, root));
    }
}
