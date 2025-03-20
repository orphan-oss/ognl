package ognl.test.accessors;

import ognl.DefaultMemberAccess;
import ognl.ListPropertyAccessor;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.enhance.ExpressionCompiler;
import ognl.test.objects.ListSource;
import ognl.test.objects.ListSourceImpl;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests functionality of various built-in object accessors.
 */
class ListPropertyAccessorTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @Test
    void test_Get_Source_String_Number_Index() {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        Root root = new Root();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentType(Integer.TYPE);

        assertEquals(".get(0)", pa.getSourceAccessor(context, root.getList(), "0"));

        assertEquals(List.class, context.getCurrentAccessor());
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Integer.TYPE, context.getPreviousType());
        assertNull(context.getPreviousAccessor());
    }

    @Test
    void test_Get_Source_Object_Number_Index() {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        Root root = new Root();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentType(Integer.class);

        assertEquals(".get(indexValue.intValue())", pa.getSourceAccessor(context, root.getList(), "indexValue"));

        assertEquals(List.class, context.getCurrentAccessor());
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Integer.class, context.getPreviousType());
        assertNull(context.getPreviousAccessor());
    }

    @Test
    void test_List_To_Object_Property_Accessor_Read() {
        ListPropertyAccessor pa = new ListPropertyAccessor();

        ListSource list = new ListSourceImpl();

        context.setRoot(list);
        context.setCurrentObject(list);

        assertEquals(".getTotal()", pa.getSourceAccessor(context, list, "total"));

        assertNull(context.get(ExpressionCompiler.PRE_CAST));
        assertEquals(int.class, context.getCurrentType());
        assertEquals(ListSource.class, context.getCurrentAccessor());
    }
}
