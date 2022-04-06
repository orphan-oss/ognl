package org.ognl.test.accessors;

import junit.framework.TestCase;
import org.ognl.DefaultMemberAccess;
import org.ognl.ListPropertyAccessor;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.enhance.ExpressionCompiler;
import org.ognl.test.objects.ListSource;
import org.ognl.test.objects.ListSourceImpl;
import org.ognl.test.objects.Root;

import java.util.List;
import java.util.Map;

/**
 * Tests functionality of various built in object accessors.
 */
public class ListPropertyAccessorTest extends TestCase {

    private Map context;

    public void setUp() throws Exception {
        super.setUp();
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    public void test_Get_Source_String_Number_Index()
    {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        Root root = new Root();

        OgnlContext context = (OgnlContext) this.context;
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

        OgnlContext context = (OgnlContext) this.context;
        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentType(Integer.class);

        assertEquals(".get(indexValue.intValue())", pa.getSourceAccessor(context, root.getList(), "indexValue"));

        assertEquals(List.class, context.getCurrentAccessor());
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Integer.class, context.getPreviousType());
        assertEquals(null, context.getPreviousAccessor());
    }

    public void test_List_To_Object_Property_Accessor_Read() throws Exception
    {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        ListSource list = new ListSourceImpl();

        OgnlContext context = (OgnlContext) this.context;
        context.setRoot(list);
        context.setCurrentObject(list);

        assertEquals(".getTotal()", pa.getSourceAccessor(context, list, "total"));

        assertNull(context.get(ExpressionCompiler.PRE_CAST));
        assertEquals(int.class, context.getCurrentType());
        assertEquals(ListSource.class, context.getCurrentAccessor());
   }
}
