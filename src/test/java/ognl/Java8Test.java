package ognl;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

public class Java8Test extends TestCase {

    public void testDefaultMethodOnClass() {
        /* defaultMethod(); */
        List defaultMethod = OgnlRuntime.getMethods(ClassWithDefaults.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
        Method method = OgnlRuntime.getReadMethod(ClassWithDefaults.class, "defaultMethod");
        assertNotNull(method);
    }

    public void testDefaultMethodOnSubClass() {
        /* defaultMethod(); */
        List defaultMethod = OgnlRuntime.getMethods(SubClassWithDefaults.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
        Method method = OgnlRuntime.getReadMethod(SubClassWithDefaults.class, "defaultMethod");
        assertNotNull(method);
    }

}

class SubClassWithDefaults extends ClassWithDefaults {
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
