package org.ognl.test;

import junit.framework.TestCase;
import ognl.OgnlRuntime;
import org.ognl.test.objects.ListSource;
import org.ognl.test.objects.ListSourceImpl;

import java.lang.reflect.Method;

/**
 * Tests various methods / functionality of {@link ognl.OgnlRuntime}.
 */
public class TestOgnlRuntime extends TestCase {

    public void test_Get_Super_Or_Interface_Class() throws Exception
    {
        ListSource list = new ListSourceImpl();

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "total");
        assertNotNull(m);
        
        assertEquals(ListSourceImpl.class, OgnlRuntime.getSuperOrInterfaceClass(m, list.getClass()));
    }
}
