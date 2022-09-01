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
import ognl.DefaultMemberAccess;
import ognl.OgnlContext;
import ognl.test.objects.Root;

public class PrivateAccessorTest extends OgnlTestCase {

    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            // Using private get/set methods
            {ROOT, "getPrivateAccessorIntValue()", new Integer(67)},
            {ROOT, "privateAccessorIntValue", new Integer(67)},
            {ROOT, "privateAccessorIntValue", new Integer(67), new Integer(100)},
            {ROOT, "privateAccessorIntValue2", new Integer(67)},
            {ROOT, "privateAccessorIntValue2", new Integer(67), new Integer(100)},
            {ROOT, "privateAccessorIntValue3", new Integer(67)},
            {ROOT, "privateAccessorIntValue3", new Integer(67), new Integer(100)},
            {ROOT, "privateAccessorBooleanValue", Boolean.TRUE},
            {ROOT, "privateAccessorBooleanValue", Boolean.TRUE, Boolean.FALSE},
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new PrivateAccessorTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                        TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new PrivateAccessorTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                            TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new PrivateAccessorTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                                TESTS[i][2], TESTS[i][3], TESTS[i][4]));
                    } else {
                        throw new RuntimeException("don't understand TEST format");
                    }
                }
            }
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public PrivateAccessorTest() {
        super();
    }

    public PrivateAccessorTest(String name) {
        super(name);
    }

    public PrivateAccessorTest(String name, Object root, String expressionString, Object expectedResult,
                               Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public PrivateAccessorTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public PrivateAccessorTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    public void setUp() {
        super.setUp();
        _context = new OgnlContext(null, null, new DefaultMemberAccess(true));
        _compileExpressions = false;
    }
}
