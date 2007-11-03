/**
 *
 */
package org.ognl.test.enhance;

import junit.framework.TestCase;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.enhance.ExpressionCompiler;
import ognl.enhance.OgnlExpressionCompiler;
import org.ognl.test.objects.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Tests functionality of {@link ExpressionCompiler}.
 */
public class TestExpressionCompiler extends TestCase
{
    OgnlExpressionCompiler _compiler;
    OgnlContext _context = (OgnlContext) Ognl.createDefaultContext(null);

    public void setUp()
    {
        _compiler = new ExpressionCompiler();
    }

    public void test_Get_Property_Access()
            throws Throwable
    {
        Node expr = (Node)Ognl.parseExpression("bean2");
        Bean1 root = new Bean1();

        _compiler.compileExpression(_context, expr, root);

        assertNotNull(expr.getAccessor().get(_context, root));
    }

    public void test_Get_Indexed_Property()
            throws Throwable
    {
        Node expr = (Node)Ognl.parseExpression("bean2.bean3.indexedValue[25]");
        Bean1 root = new Bean1();

        assertNull(Ognl.getValue(expr, _context, root));

        _compiler.compileExpression(_context, expr, root);

        assertNull(expr.getAccessor().get(_context, root));
    }

    public void test_Set_Indexed_Property()
            throws Throwable
    {
        Node expr = (Node)Ognl.parseExpression("bean2.bean3.indexedValue[25]");
        Bean1 root = new Bean1();

        assertNull(Ognl.getValue(expr, _context, root));

        _compiler.compileExpression(_context, expr, root);

        expr.getAccessor().set(_context, root, "test string");

        assertEquals("test string", expr.getAccessor().get(_context, root));
    }

    public void test_Expression()
            throws Throwable
    {
        Node expr = (Node)Ognl.parseExpression("bean2.bean3.value <= 24");
        Bean1 root = new Bean1();

        assertEquals(Boolean.FALSE, Ognl.getValue(expr, _context, root));

        _compiler.compileExpression(_context, expr, root);

        assertEquals(Boolean.FALSE, expr.getAccessor().get(_context, root));
    }

    public void test_Get_Context_Property()
            throws Throwable
    {
        _context.put("key", "foo");
        Node expr = (Node)Ognl.parseExpression("bean2.bean3.map[#key]");
        Bean1 root = new Bean1();

        assertEquals("bar", Ognl.getValue(expr, _context, root));

        _compiler.compileExpression(_context, expr, root);

        assertEquals("bar", expr.getAccessor().get(_context, root));

        _context.put("key", "bar");

        assertEquals("baz", Ognl.getValue(expr, _context, root));
        assertEquals("baz", expr.getAccessor().get(_context, root));
    }
    
    public void test_Set_Context_Property()
            throws Throwable
    {
        _context.put("key", "foo");
        Node expr = (Node)Ognl.parseExpression("bean2.bean3.map[#key]");
        Bean1 root = new Bean1();

        _compiler.compileExpression(_context, expr, root);

        assertEquals("bar", expr.getAccessor().get(_context, root));

        _context.put("key", "bar");
        assertEquals("baz", expr.getAccessor().get(_context, root));

        expr.getAccessor().set(_context, root, "bam");
        assertEquals("bam", expr.getAccessor().get(_context, root));
    }

    public void test_Property_Index()
            throws Throwable
    {
        Root root = new Root();
        Node expr = (Node) Ognl.compileExpression(_context, root, "{index + 1}");

        Object ret = expr.getAccessor().get(_context, root);

        assertTrue(Collection.class.isInstance(ret));
    }

    public void test_Root_Expression_Inheritance()
            throws Throwable
    {
        Inherited obj1 = new TestInherited1();
        Inherited obj2 = new TestInherited2();

        Node expr = (Node) Ognl.compileExpression(_context, obj1, "myString");

        assertEquals(expr.getAccessor().get(_context, obj1), "inherited1");
        assertEquals(expr.getAccessor().get(_context, obj2), "inherited2");
    }

    public void test_Create_Empty_Collection()
            throws Throwable
    {
        Node expr = (Node) Ognl.compileExpression(_context, null, "{}");

        Object ret = expr.getAccessor().get(_context, null);

        assertNotNull(ret);
        assertTrue(Collection.class.isAssignableFrom(ret.getClass()));
    }

    public String getKey()
    {
        return "key";
    }

    public void test_Indexed_Property()
            throws Throwable
    {
        Map map = new HashMap();
        map.put("key", "value");

        Node expression = Ognl.compileExpression(_context, this, "key");
        assertEquals("key", expression.getAccessor().get(_context, this));
    }

    IndexedMapObject mapObject = new IndexedMapObject("propertyValue");

    public IndexedMapObject getObject()
    {
        return mapObject;
    }

    public String getPropertyKey()
    {
        return "property";
    }

    public void test_Indexed_Map_Property()
            throws Throwable
    {
        assertEquals("propertyValue", Ognl.getValue("object[propertyKey]", this));

        _context.clear();
        Node expression = Ognl.compileExpression(_context, this, "object[#this.propertyKey]");
        assertEquals("propertyValue", expression.getAccessor().get(_context, this));

        _context.clear();
        expression = Ognl.compileExpression(_context, this, "object[propertyKey]");
        assertEquals("propertyValue", expression.getAccessor().get(_context, this));
    }

    public void test_Set_Generic_Property() throws Exception
    {
        _context.clear();
        
        GenericRoot root = new GenericRoot();

        Node node = Ognl.compileExpression(_context, root, "cracker.param");
        assertEquals(null, node.getAccessor().get(_context, root));

        node.getAccessor().set(_context, root, new Integer(0));
        assertEquals(new Integer(0), node.getAccessor().get(_context, root));

        node.getAccessor().set(_context, root, new Integer(12));
        assertEquals(new Integer(12), node.getAccessor().get(_context, root));
    }
}
