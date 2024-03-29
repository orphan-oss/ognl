package ognl.test;

import junit.framework.TestCase;
import ognl.ASTChain;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.IndexedSetObject;

/**
 * Tests for {@link ASTChain}.
 */
public class ASTChainTest extends TestCase {

    public void test_Get_Indexed_Value() throws Exception {

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        IndexedSetObject root = new IndexedSetObject();

        String expr = "thing[\"x\"].val";

        assertEquals(1, Ognl.getValue(expr, context, root));

        Ognl.setValue(expr, context, root, new Integer(2));

        assertEquals(2, Ognl.getValue(expr, context, root));
    }
}
