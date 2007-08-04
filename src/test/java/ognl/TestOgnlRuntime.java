package ognl;

import junit.framework.TestCase;
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

    class TestGetters {
        public boolean isEditorDisabled()
        {
            return false;
        }

        public boolean isDisabled()
        {
            return true;
        }

        public boolean isNotAvailable() {
            return false;
        }

        public boolean isAvailable() {
            return true;
        }
    }

    public void test_Get_Read_Method_Multiple()
            throws Exception
    {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "disabled");
        assertNotNull(m);

        assertEquals("isDisabled", m.getName());
    }

    public void test_Get_Read_Method_Multiple_Boolean_Getters()
            throws Exception
    {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "available");
        assertNotNull(m);

        assertEquals("isAvailable", m.getName());

        m = OgnlRuntime.getReadMethod(TestGetters.class, "notAvailable");
        assertNotNull(m);

        assertEquals("isNotAvailable", m.getName());
    }

    public void test_Find_Method_Mixed_Boolean_Getters()
    throws Exception
    {
        Method m = OgnlRuntime.getReadMethod(GetterMethods.class, "allowDisplay");
        assertNotNull(m);

        assertEquals("getAllowDisplay", m.getName());
    }

    public void test_Get_Appropriate_Method()
            throws Exception
    {
        ListSource list = new ListSourceImpl();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);

        Object ret = OgnlRuntime.callMethod(context, list, "addValue", null, new String[] {null});
        
        assert ret != null;
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

    public void test_Setter_Returns()
            throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        SetterReturns root = new SetterReturns();

        Method m = OgnlRuntime.getWriteMethod(root.getClass(), "value");
        assertTrue(m != null);

        Ognl.setValue("value", context, root, "12__");
        assertEquals(Ognl.getValue("value", context, root), "12__");
    }

    public void test_Class_Cache_Inspector()
            throws Exception
    {
        OgnlRuntime.clearCache();
        assertEquals(0, OgnlRuntime._propertyDescriptorCache.getSize());

        Root root = new Root();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null);
        Node expr = Ognl.compileExpression(context, root, "property.bean3.value != null");

        assertTrue((Boolean)expr.getAccessor().get(context, root));

        int size = OgnlRuntime._propertyDescriptorCache.getSize();
        assertTrue(size > 0);

        OgnlRuntime.clearCache();
        assertEquals(0, OgnlRuntime._propertyDescriptorCache.getSize());

        // now register class cache prevention

        OgnlRuntime.setClassCacheInspector(new TestCacheInspector());

        expr = Ognl.compileExpression(context, root, "property.bean3.value != null");
        assertTrue((Boolean)expr.getAccessor().get(context, root));

        assertEquals((size - 1), OgnlRuntime._propertyDescriptorCache.getSize());
    }

    class TestCacheInspector implements ClassCacheInspector {

        public boolean shouldCache(Class type)
        {
            if (type == null || type == Root.class)
                return false;

            return true;
        }
    }
}
