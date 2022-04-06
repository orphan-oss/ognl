package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.*;

/**
 * Test for OGNL-118.
 */
public class InExpressionTest extends TestCase {

    public void test_String_In()
            throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        Object node = Ognl.parseExpression("#name in {\"Greenland\", \"Austin\", \"Africa\", \"Rome\"}");
        Object root = null;

        context.put("name", "Austin");
        assertEquals(Boolean.TRUE, Ognl.getValue(node, context, root));
    }
}
