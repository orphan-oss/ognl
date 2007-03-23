package org.ognl.test;

import junit.framework.TestCase;
import ognl.*;
import org.ognl.test.objects.Root;

import java.util.List;
import java.util.Map;

/**
 * Tests functionality of {@link ognl.ASTProperty}.
 */
public class ASTPropertyTest extends TestCase {

    public void test_Get_Source()
            throws Throwable
    {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();
        
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);
        
        assertEquals(".get(\"nested\")", p.toGetSourceString(context, root));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(root.get("nested"), context.getCurrentObject());

        assert Map.class.isAssignableFrom(context.getCurrentAccessor());

        assertEquals(null, context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());
    }

    public void test_Set_Source()
            throws Throwable
    {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);
        
        Map root = new Root().getMap();

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);
        
        assertEquals(".put(\"nested\", $3)", p.toSetSourceString(context, root));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(root.get("nested"), context.getCurrentObject());

        assert Map.class.isAssignableFrom(context.getCurrentAccessor());

        assertEquals(null, context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());
    }
    
    public void test_Complicated_List() throws Exception
    {
        Root root = new Root();
        OgnlContext context = (OgnlContext)Ognl.createDefaultContext(null);
        
        SimpleNode node = (SimpleNode) Ognl.compileExpression(context, root,
                "{ new org.ognl.test.objects.MenuItem('Home', 'Main', "
                    + "{ new org.ognl.test.objects.MenuItem('Help', 'Help'), "
                    + "new org.ognl.test.objects.MenuItem('Contact', 'Contact') }), " // end first item
                    + "new org.ognl.test.objects.MenuItem('UserList', getMessages().getMessage('menu.members')), " +
                    "new org.ognl.test.objects.MenuItem('account/BetSlipList', getMessages().getMessage('menu.account'), " +
                    "{ new org.ognl.test.objects.MenuItem('account/BetSlipList', 'My Bets'), " +
                    "new org.ognl.test.objects.MenuItem('account/TransactionList', 'My Transactions') }), " +
                    "new org.ognl.test.objects.MenuItem('About', 'About'), " +
                    "new org.ognl.test.objects.MenuItem('admin/Admin', getMessages().getMessage('menu.admin'), " +
                    "{ new org.ognl.test.objects.MenuItem('admin/AddEvent', 'Add event'), " +
                    "new org.ognl.test.objects.MenuItem('admin/AddResult', 'Add result') })}");
        
        assertTrue(List.class.isAssignableFrom(node.getAccessor().get(context, root).getClass()));
    }
}
