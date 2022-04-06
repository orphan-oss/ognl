package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.DefaultMemberAccess;
import org.ognl.Node;
import org.ognl.Ognl;
import org.ognl.OgnlContext;

import java.util.Map;

/**
 * Tests being able to set property on object with interface that doesn't define
 * setter.   See OGNL-115.
 */
public class PropertySetterTest extends TestCase {

    private Map map;
    private TestObject testObject = new TestObject("propertyValue");
    private String propertyKey = "property";

    public interface TestInterface {
        public String getProperty();
    }

    public class TestObject implements TestInterface {

        private String property;
        private Integer integerProperty = 1;

        public TestObject(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public Integer getIntegerProperty() {
            return integerProperty;
        }
    }


    public Map getMap() {
        return map;
    }
    public String getKey() {
        return "key";
    }

    public TestObject getObject() {
        return testObject;
    }

    public TestInterface getInterfaceObject() {
        return testObject;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void testEnhancedOgnl() throws Exception {
        OgnlContext context = (OgnlContext)Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        Node expression = Ognl.compileExpression(context, this, "interfaceObject.property");
        Ognl.setValue(expression, context, this, "hello");
        assertEquals("hello", getObject().getProperty() );

        // Fails if an interface is defined, but succeeds if not
        context.clear();

        expression = Ognl.compileExpression(context, this.getObject(), "property");
        Ognl.setValue(expression, context, this.getObject(), "hello");
        assertEquals("hello", getObject().getProperty() );
    }
}
