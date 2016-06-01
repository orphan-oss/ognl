package org.ognl.test;

import junit.framework.TestCase;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.SimpleNode;

/**
 * Tests for {@link ognl.SimpleNode#isOperation(OgnlContext)}.
 */
public class OperationTest extends TestCase {

    public void test_isOperation() throws Exception {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name = 'boo'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name['foo'] = 'bar'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name.foo = 'bar' + 'foo'");
        assertTrue(node.isOperation(context));
    }

}
