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
import ognl.test.objects.Simple;

public class ContextVariableTest extends OgnlTestCase {

    private static Object ROOT = new Simple();
    private static Object[][] TESTS = {
            // Naming and referring to names
            {"#root", ROOT}, // Special root reference
            {"#this", ROOT}, // Special this reference
            {"#f=5, #s=6, #f + #s", new Integer(11)},
            {"#six=(#five=5, 6), #five + #six", new Integer(11)},
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            result.addTest(new ContextVariableTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", ROOT,
                    (String) TESTS[i][0], TESTS[i][1]));
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ContextVariableTest() {
        super();
    }

    public ContextVariableTest(String name) {
        super(name);
    }

    public ContextVariableTest(String name, Object root, String expressionString, Object expectedResult,
                               Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ContextVariableTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ContextVariableTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
