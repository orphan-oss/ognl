package ognl;

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

}

class SubClassWithDefaults extends ClassWithDefaults {

}

class ClassWithDefaults /* implements InterfaceWithDefaults */ {

}

/**
 * This won't work till switching to Java 8
 *
interface InterfaceWithDefaults {
    default public void defaultMethod() { }
}
 */
