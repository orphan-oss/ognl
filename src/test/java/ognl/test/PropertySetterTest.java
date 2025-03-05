/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ognl.test;

import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests being able to set property on object with interface that doesn't define setter.
 * See OGNL-115.
 */
class PropertySetterTest {

    private final TestObject testObject = new TestObject("propertyValue");

    public interface TestInterface {
        String getProperty();
    }

    public static class TestObject implements TestInterface {
        private String property;

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
            return 1;
        }
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
        return "property";
    }

    @Test
    public void testEnhancedOgnl() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Node expression = Ognl.compileExpression(context, null, "interfaceObject.property");
        Ognl.setValue(expression, context, this, "hello");

        assertEquals("hello", getObject().getProperty());

        // Fails if an interface is defined, but succeeds if not
        context.clear();

        expression = Ognl.compileExpression(context, this.getObject(), "property");
        Ognl.setValue(expression, context, this.getObject(), "hello");

        assertEquals("hello", getObject().getProperty());
    }
}
