package org.ognl.test;

import junit.framework.TestCase;
import ognl.ASTConst;
import ognl.ASTProperty;
import ognl.Ognl;
import ognl.OgnlContext;
import org.ognl.test.objects.Root;

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
        
        /*
        ASTProperty p2 = new ASTProperty(0);
        ASTConst pRef2 = new ASTConst(0);
        pRef2.setValue("deep");
        p.jjtAddChild(pRef2, 0);

        ASTChain chain = new ASTChain(0);
        chain.jjtAddChild(p, 0);
        chain.jjtAddChild(p2, 1);
        */

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

        /*
        //assertEquals(context.getCurrentType(), Map.class);
        assertEquals(context.getCurrentObject(), root.getMap());
        assertEquals(context.getCurrentAccessor(), Root.class);
        assertEquals(context.getPreviousType(), null);
        
        assert context.getPreviousAccessor() == null;
        
        assertEquals(OgnlRuntime.getCompiler().castExpression(context, p, ".getMap()"), ".getMap()");*/
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
}
