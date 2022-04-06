package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.*;
import org.ognl.enhance.ExpressionCompiler;
import org.ognl.test.objects.Bean2;
import org.ognl.test.objects.Bean3;
import org.ognl.test.objects.Root;

import java.util.Map;

/**
 * Tests {@link ASTMethod}.
 */
public class ASTMethodTest extends TestCase {

    private Map context;

    public void setUp() throws Exception {
        super.setUp();
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    public void test_Context_Types()
            throws Throwable
    {
        ASTMethod p = new ASTMethod(0);
        p.setMethodName("get");

        ASTConst pRef = new ASTConst(0);
        pRef.setValue("value");
        p.jjtAddChild(pRef, 0);

        Root root = new Root();

        OgnlContext context = (OgnlContext) this.context;
        context.setRoot(root.getMap());
        context.setCurrentObject(root.getMap());
        context.setCurrentType(root.getMap().getClass());

        assertEquals(p.toGetSourceString(context, root.getMap()), ".get(\"value\")");
        assertEquals(context.getCurrentType(), Object.class);
        assertEquals(root.getMap().get("value"), context.getCurrentObject());
        assert Map.class.isAssignableFrom(context.getCurrentAccessor());
        assert Map.class.isAssignableFrom(context.getPreviousType());
        assert context.getPreviousAccessor() == null;

        assertEquals(OgnlRuntime.getCompiler().castExpression(context, p, ".get(\"value\")"),  ".get(\"value\")");
        assert context.get(ExpressionCompiler.PRE_CAST) == null;

        // now test one context level further to see casting work properly on base object types

        ASTProperty prop = new ASTProperty(0);
        ASTConst propRef = new ASTConst(0);
        propRef.setValue("bean3");
        prop.jjtAddChild(propRef, 0);

        Bean2 val = (Bean2)root.getMap().get("value");

        assertEquals(prop.toGetSourceString(context, root.getMap().get("value")), ".getBean3()");

        assertEquals(context.getCurrentObject(), val.getBean3());
        assertEquals(context.getCurrentType(), Bean3.class);
        assertEquals(context.getCurrentAccessor(), Bean2.class);
        assertEquals(Object.class, context.getPreviousType());
        assert Map.class.isAssignableFrom(context.getPreviousAccessor());

        assertEquals(OgnlRuntime.getCompiler().castExpression(context, prop, ".getBean3()"), ").getBean3()");

    }

    public void test_isSimpleMethod() throws Exception {
        OgnlContext context = (OgnlContext) this.context;

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("#name.lastChar");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("execute()");
        assertTrue(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("bean.execute()");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("bean.execute()");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("{name.lastChar, #boo, foo()}");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("(name.lastChar, #boo, foo())");
        assertFalse(node.isSimpleMethod(context));
    }
}
