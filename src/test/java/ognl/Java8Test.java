package ognl;

import java.beans.IntrospectionException;
import java.util.List;

import junit.framework.TestCase;

public class Java8Test extends TestCase {

    public void testDefaultMethodOnClass() {
        /* defaultMethod(); */
        List defaultMethod = OgnlRuntime.getMethods(ClassWithDefaults.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
    }

    public void testDefaultMethodOnSubClass() {
        /* defaultMethod(); */
        List defaultMethod = OgnlRuntime.getMethods(SubClassWithDefaults.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
    }

    public void testGetDeclaredMethods() throws IntrospectionException, OgnlException{
        List defaultMethod = OgnlRuntime.getDeclaredMethods(SubClassWithDefaults.class, "name", false);
        assertNotNull(defaultMethod);
        defaultMethod = OgnlRuntime.getDeclaredMethods(ClassWithDefaults.class, "name", false);
        assertNotNull(defaultMethod);
    }

}

class SubClassWithDefaults extends ClassWithDefaults {

	public String getName() { return "name"; }

}

class ClassWithDefaults /* implements SubInterfaceWithDefaults */ {

}

/**
 * This won't work till switching to Java 8
 *
interface InterfaceWithDefaults {
    default public void defaultMethod() { }
    default public String getName() { return "name"; }
}
interface SubInterfaceWithDefaults extends InterfaceWithDefaults {
}
 */
