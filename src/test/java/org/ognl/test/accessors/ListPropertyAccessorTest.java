package org.ognl.test.accessors;

import junit.framework.TestCase;
import ognl.ListPropertyAccessor;
import ognl.Ognl;
import ognl.OgnlContext;
import org.ognl.test.objects.Root;

import java.util.List;

/**
 * Tests functionality of various built in object accessors.
 */
public class ListPropertyAccessorTest extends TestCase {

    public void test_Get_Source_String_Number_Index()
    {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        Root root = new Root();

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentType(Integer.TYPE);

        assertEquals(".get(0)", pa.getSourceAccessor(context, root.getList(), "0"));

        assertEquals(List.class, context.getCurrentAccessor());
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Integer.TYPE, context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());
    }

    public void test_Get_Source_Object_Number_Index()
    {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        Root root = new Root();

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentType(Integer.class);

        assertEquals(".get(indexValue.intValue())", pa.getSourceAccessor(context, root.getList(), "indexValue"));

        assertEquals(List.class, context.getCurrentAccessor());
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Integer.class, context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());
    }

    
}
