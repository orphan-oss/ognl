/**
 * 
 */
package org.ognl.test;

import junit.framework.TestCase;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import org.ognl.test.objects.BaseBean;
import org.ognl.test.objects.FirstBean;
import org.ognl.test.objects.Root;
import org.ognl.test.objects.SecondBean;


/**
 * Tests functionality of casting inherited method expressions.
 * 
 */
public class InheritedMethodsTest extends TestCase
{
    
    private static Root ROOT = new Root();
    
    public void test_Base_Inheritance()
    throws Exception
    {
        OgnlContext context = (OgnlContext)Ognl.createDefaultContext(null);
        String expression = "map.bean.name";
        BaseBean first = new FirstBean();
        BaseBean second = new SecondBean();
        
        ROOT.getMap().put("bean", first);
        
        Node node = Ognl.compileExpression(context, ROOT, expression);
        
        assertEquals(first.getName(), node.getAccessor().get(context, ROOT));
        
        ROOT.getMap().put("bean", second);
        
        assertEquals(second.getName(), node.getAccessor().get(context, ROOT));
    }
}
