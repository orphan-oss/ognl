package ognl;

import junit.framework.TestCase;

import java.util.List;

public class Java8Test extends TestCase /* implements InterfaceWithDefaults */ {

    public void testDefaultMethod() {
        /* defaultMethod(); */
        List defaultMethod = OgnlRuntime.getMethods(Java8Test.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
    }

}

/**
 * This won't work till switching to Java 8
 *
interface InterfaceWithDefaults {
    default public void defaultMethod() { }
}
*/
