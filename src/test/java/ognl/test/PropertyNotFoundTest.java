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

import junit.framework.TestSuite;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

public class PropertyNotFoundTest extends OgnlTestCase {
    private static final Blah BLAH = new Blah();

    private static Object[][] TESTS = {
            {BLAH, "webwork.token.name", OgnlException.class, "W value", OgnlException.class},
    };

    /*===================================================================
        Public static classes
      ===================================================================*/
    public static class Blah {
        String x;
        String y;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }
    }

    public static class BlahPropertyAccessor implements PropertyAccessor {
        public void setProperty(OgnlContext context, Object target, Object name, Object value) throws OgnlException {
        }

        public Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException {
            if ("x".equals(name) || "y".equals(name)) {
                return OgnlRuntime.getProperty(context, target, name);
            }
            return null;
        }

        public String getSourceAccessor(OgnlContext context, Object target, Object index) {
            return index.toString();
        }

        public String getSourceSetter(OgnlContext context, Object target, Object index) {
            return index.toString();
        }
    }

    /*===================================================================
      Public static methods
    ===================================================================*/
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new PropertyNotFoundTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new PropertyNotFoundTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new PropertyNotFoundTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
                    } else {
                        throw new RuntimeException("don't understand TEST format");
                    }
                }
            }
        }
        return result;
    }

    /*===================================================================
        Constructors
      ===================================================================*/
    public PropertyNotFoundTest() {
        super();
    }

    public PropertyNotFoundTest(String name) {
        super(name);
    }

    public PropertyNotFoundTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public PropertyNotFoundTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public PropertyNotFoundTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }

    protected void setUp() {
        super.setUp();
        OgnlRuntime.setPropertyAccessor(Blah.class, new BlahPropertyAccessor());
    }
}
