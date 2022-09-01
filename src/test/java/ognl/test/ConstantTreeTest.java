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
import ognl.Ognl;

public class ConstantTreeTest extends OgnlTestCase {

    public static int nonFinalStaticVariable = 15;

    private static Object[][] TESTS = {
            {"true", Boolean.TRUE},
            {"55", Boolean.TRUE},
            {"@java.awt.Color@black", Boolean.TRUE},
            {"@ognl.test.ConstantTreeTest@nonFinalStaticVariable", Boolean.FALSE},
            {"@ognl.test.ConstantTreeTest@nonFinalStaticVariable + 10", Boolean.FALSE},
            {"55 + 24 + @java.awt.Event@ALT_MASK", Boolean.TRUE},
            {"name", Boolean.FALSE},
            {"name[i]", Boolean.FALSE},
            {"name[i].property", Boolean.FALSE},
            {"name.{? foo }", Boolean.FALSE},
            {"name.{ foo }", Boolean.FALSE},
            {"name.{ 25 }", Boolean.FALSE}

    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            result.addTest(new ConstantTreeTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", null,
                    (String) TESTS[i][0], TESTS[i][1]));
        }
        return result;
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    protected void runTest()
            throws Exception {
        assertTrue(Ognl.isConstant(getExpression(), _context) == ((Boolean) getExpectedResult()).booleanValue());
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ConstantTreeTest() {
        super();
    }

    public ConstantTreeTest(String name) {
        super(name);
    }

    public ConstantTreeTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                            Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ConstantTreeTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ConstantTreeTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
