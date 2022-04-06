package org.ognl.test;

import junit.framework.TestCase;
import static org.ognl.test.OgnlTestCase.isEqual;

import org.ognl.*;
import org.ognl.test.objects.*;

import java.util.List;
import java.util.Map;

/**
 * Tests functionality of {@link ASTProperty}.
 */
public class ASTPropertyTest extends TestCase {

    private OgnlContext context;

    public void setUp() throws Exception {
        super.setUp();
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    public void test_Get_Indexed_Property_Type() throws Exception
    {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(root.getClass(), context.getCurrentType());
        assertEquals(null, context.getPreviousType());
        assertEquals(root, context.getCurrentObject());
        assertEquals(null, context.getCurrentAccessor());
        assertEquals(null, context.getPreviousAccessor());

        int type = p.getIndexedPropertyType(context, root);

        assertEquals(OgnlRuntime.INDEXED_PROPERTY_NONE, type);
        assertEquals(root.getClass(), context.getCurrentType());
        assertEquals(null, context.getPreviousType());
        assertEquals(null, context.getCurrentAccessor());
        assertEquals(null, context.getPreviousAccessor());
    }

    public void test_Get_Value_Body() throws Exception
    {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(root.getClass(), context.getCurrentType());
        assertEquals(null, context.getPreviousType());
        assertEquals(root, context.getCurrentObject());
        assertEquals(null, context.getCurrentAccessor());
        assertEquals(null, context.getPreviousAccessor());

        Object value = p.getValue(context, root);

        assertEquals(root.get("nested"), value);
        assertEquals(root.getClass(), context.getCurrentType());
        assertEquals(null, context.getPreviousType());
        assertEquals(null, context.getCurrentAccessor());
        assertEquals(null, context.getPreviousAccessor());
    }

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

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(".get(\"nested\")", p.toGetSourceString(context, root));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Map.class, context.getCurrentAccessor());
        assertEquals(root.getClass(), context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());

        assertEquals(root.get("nested"), context.getCurrentObject());

        assert Map.class.isAssignableFrom(context.getCurrentAccessor());

        assertEquals(root.getClass(), context.getPreviousType());
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

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(".put(\"nested\", $3)", p.toSetSourceString(context, root));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(root.get("nested"), context.getCurrentObject());

        assert Map.class.isAssignableFrom(context.getCurrentAccessor());

        assertEquals(root.getClass(), context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());
    }

    public void test_Indexed_Object_Type()
            throws Throwable
    {
        //ASTChain chain = new ASTChain(0);

        ASTProperty listp = new ASTProperty(0);
        listp.setIndexedAccess(false);
        //listp.jjtSetParent(chain);

        ASTConst listc = new ASTConst(0);
        listc.setValue("list");
        listc.jjtSetParent(listp);
        listp.jjtAddChild(listc, 0);

        //chain.jjtAddChild(listp, 0);

        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(true);

        ASTProperty pindex = new ASTProperty(0);

        ASTConst pRef = new ASTConst(0);
        pRef.setValue("genericIndex");
        pRef.jjtSetParent(pindex);
        pindex.jjtAddChild(pRef, 0);

        p.jjtAddChild(pindex, 0);
        //chain.jjtAddChild(p, 1);

        Root root = new Root();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(listp);

        assertEquals(".getList()", listp.toGetSourceString(context, root));
        assertEquals(List.class, context.getCurrentType());
        assertEquals(Root.class, context.getCurrentAccessor());
        assertEquals(null, context.getPreviousAccessor());
        assertEquals(root.getClass(), context.getPreviousType());
        assertEquals(root.getList(), context.getCurrentObject());

        // re test with chain

        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.setRoot(root);
        context.setCurrentObject(root);

        ASTChain chain = new ASTChain(0);
        listp.jjtSetParent(chain);
        chain.jjtAddChild(listp, 0);

        context.setCurrentNode(chain);

        assertEquals(".getList()", chain.toGetSourceString(context, root));
        assertEquals(List.class, context.getCurrentType());
        assertEquals(Root.class, context.getCurrentAccessor());
        assertEquals(null, context.getPreviousAccessor());
        assertEquals(Root.class, context.getPreviousType());
        assertEquals(root.getList(), context.getCurrentObject());

        // test with only getIndex

        assertEquals(".get(org.ognl.OgnlOps#getIntValue(((org.ognl.test.objects.Root)$2)..getGenericIndex().toString()))", p.toGetSourceString(context, root.getList()));
        assertEquals(root.getArray(), context.getCurrentObject());
        assertEquals(Object.class, context.getCurrentType());
    }

    public void test_Complicated_List() throws Exception
    {
        Root root = new Root();

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

    public void test_Set_Chain_Indexed_Property() throws Exception
    {
        Root root = new Root();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("tab.searchCriteriaSelections[index1][index2]");
        node.setValue(context, root, Boolean.FALSE);
    }

    public void test_Set_Generic_Property() throws Exception
    {
        GenericRoot root = new GenericRoot();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("cracker.param");
        node.setValue(context, root, "0");

        assertEquals( new Integer(0), root.getCracker().getParam());

        node.setValue(context, root, "10");

        assertEquals(new Integer(10), root.getCracker().getParam());
    }

    public void test_Get_Generic_Property() throws Exception
    {
        GenericRoot root = new GenericRoot();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("cracker.param");
        node.setValue(context, root, "0");

        assertEquals(new Integer(0), node.getValue(context, root));

        node.setValue(context, root, "10");

        assertEquals(new Integer(10), node.getValue(context, root));
    }

    public void test_Set_Get_Multiple_Generic_Types_Property() throws Exception
    {
        BaseGeneric<GameGenericObject, Long> root = new GameGeneric();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("ids");
        node.setValue(context, root, new String[] {"0", "20", "43"});

        isEqual(new Long[] {new Long(0), new Long(20), new Long(43)}, root.getIds());
        isEqual(node.getValue(context, root), root.getIds());
    }
}
