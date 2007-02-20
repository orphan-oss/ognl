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
import org.ognl.test.objects.Bean1;


/**
 * Tests functionality of {@link ExpressionCompiler}.
 * 
 * @author jkuhnert
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
}
