package ognl.test;

import junit.framework.TestCase;
import ognl.ASTSequence;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.SimpleNode;

/**
 * Tests for {@link ASTSequence}.
 */
public class ASTSequenceTest extends TestCase {

    public void test_isSequence() throws Exception {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isSequence(context));

        node = (SimpleNode) Ognl.parseExpression("#name = 'boo', System.out.println(#name)");
        assertTrue(node.isSequence(context));

        node = (SimpleNode) Ognl.parseExpression("#name['foo'] = 'bar'");
        assertFalse(node.isSequence(context));
    }

}
