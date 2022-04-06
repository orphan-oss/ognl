package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.*;

public class DefaultClassResolverTest extends TestCase {

    public void testClassInDefaultPackageResolution() throws Exception {
        DefaultClassResolver resolver = new DefaultClassResolver();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null,
                new DefaultMemberAccess(false));
        assertNotNull(resolver.classForName("ClassInDefaultPackage", context));
    }

    public void testEnsureClassNotFoundException() throws Exception {
        DefaultClassResolver resolver = new DefaultClassResolver();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null,
                new DefaultMemberAccess(false));
        try {
            resolver.classForName("no.such.Class", context);
            fail("Expected ClassNotFoundException as the specified class does not exist.");
        } catch (Exception e) {
            assertEquals(ClassNotFoundException.class, e.getClass());
            assertEquals("no.such.Class", e.getMessage());
        }
    }

    public void testEnsureClassNotFoundExceptionReportsSpecifiedName()
            throws Exception {
        DefaultClassResolver resolver = new DefaultClassResolver();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null,
                new DefaultMemberAccess(false));
        try {
            resolver.classForName("BogusClass", context);
            fail("Expected ClassNotFoundException as the specified class does not exist.");
        } catch (Exception e) {
            assertEquals(ClassNotFoundException.class, e.getClass());
            assertEquals("BogusClass", e.getMessage());
        }
    }

}
