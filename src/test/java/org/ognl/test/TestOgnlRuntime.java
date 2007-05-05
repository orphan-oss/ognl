package org.ognl.test;

import junit.framework.TestCase;
import ognl.MethodFailedException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import org.ognl.test.objects.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Tests various methods / functionality of {@link ognl.OgnlRuntime}.
 */
public class TestOgnlRuntime extends TestCase {

    public void test_Get_Super_Or_Interface_Class() throws Exception
    {
        ListSource list = new ListSourceImpl();

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "total");
        assertNotNull(m);
        
        assertEquals(ListSource.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    public void test_Get_Private_Class() throws Exception
    {
        List list = Arrays.asList(new String[]{"hello", "world"});
        
        Method m = OgnlRuntime.getReadMethod(list.getClass(), "iterator");
        assertNotNull(m);
        
        assertEquals(Iterable.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    public void test_Complicated_Inheritance() throws Exception
    {
        IForm form = new FormImpl();

        Method m = OgnlRuntime.getWriteMethod(form.getClass(), "clientId");
        assertNotNull(m);

        assertEquals(IComponent.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, form.getClass()));
    }

    public void test_Get_Read_Method()
     throws Exception
    {
        Method m = OgnlRuntime.getReadMethod(Bean2.class, "pageBreakAfter");
        assertNotNull(m);

        assertEquals("isPageBreakAfter", m.getName());
    }

    public void test_Call_Static_Method_Invalid_Class()
    {

        try {

            OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
            OgnlRuntime.callStaticMethod(context, "made.up.Name", "foo", null);

            fail("ClassNotFoundException should have been thrown by previous reference to <made.up.Name> class.");
        } catch (Exception et) {
            
            assertTrue(MethodFailedException.class.isInstance(et));
            assertTrue(et.getMessage().indexOf("made.up.Name") > -1);
        }
    }
}
