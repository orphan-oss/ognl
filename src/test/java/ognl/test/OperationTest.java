package ognl.test;

import junit.framework.TestCase;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.SimpleNode;

/**
 * Tests for {@link SimpleNode#isOperation(OgnlContext)}.
 */
public class OperationTest extends TestCase {

    public void test_isOperation() throws Exception {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name = 'boo'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name['foo'] = 'bar'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name.foo = 'bar' + 'foo'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("{name.foo = 'bar' + 'foo', #name.foo()}");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("('bar' + 'foo', #name.foo())");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-bar");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-(#bar)");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-1");
        assertFalse(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-(#bar+#foo)");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#bar=3,#foo=4(#bar-#foo)");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#bar-3");
        assertTrue(node.isOperation(context));
    }

}
