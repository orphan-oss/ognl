package org.ognl.test;

import junit.framework.TestCase;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.SimpleNode;

/**
 * Tests for {@link SimpleNode#isChain(OgnlContext)}.
 */
public class ChainTest extends TestCase {

    public void test_isChain() throws Exception {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("#name.lastChar");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("#{name.lastChar, #boo}");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("boo = #{name.lastChar, #boo, foo()}");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("{name.lastChar, #boo, foo()}");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("(name.lastChar, #boo, foo())");
        assertTrue(node.isChain(context));
    }

}
