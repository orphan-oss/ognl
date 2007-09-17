package org.ognl.test;

import junit.framework.TestCase;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for bug OGNL-117.
 */
public class SelfReferenceReflectionTest extends TestCase {

    private Map map;
    private TestObject testObject = new TestObject("propertyValue");
    private String propertyKey = "property";

    public class TestObject {
        private String property;
        private Integer integerProperty = 1;

        public TestObject(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
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

    public String getPropertyKey() {
        return propertyKey;
    }

    public void testEnhancedOgnl() throws Exception {
        map = new HashMap();
        map.put("key", "value");
        OgnlContext context = (OgnlContext)Ognl.createDefaultContext(this);

        Node expression = Ognl.compileExpression(context, this, "object[#this.propertyKey]");
        assertEquals("propertyValue", Ognl.getValue(expression.getAccessor(), context, this)) ;

        // Succeeds with 2.7.1-20070723.185910-9
        context.clear();
        Node expression2 = Ognl.compileExpression(context, this, "object[propertyKey]");
        assertEquals("propertyValue", Ognl.getValue(expression2.getAccessor(), context, this)) ;

        propertyKey = "integerProperty";

        // Succeeds
        assertEquals(1, Ognl.getValue(expression, context, this)) ;

        context.clear();

        // Fails with 2.7.1-20070723.185910-9
        assertEquals(1, Ognl.getValue(expression, context, this)) ;
    }
}
