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

public class OperatorTest extends OgnlTestCase {
    private static Object[][] TESTS = {
            {null, "\"one\" > \"two\"", Boolean.FALSE},
            {null, "\"one\" >= \"two\"", Boolean.FALSE},
            {null, "\"one\" < \"two\"", Boolean.TRUE},
            {null, "\"one\" <= \"two\"", Boolean.TRUE},
            {null, "\"one\" == \"two\"", Boolean.FALSE},
            {null, "\"o\" > \"o\"", Boolean.FALSE},
            {null, "\"o\" gt \"o\"", Boolean.FALSE},
            {null, "\"o\" >= \"o\"", Boolean.TRUE},
            {null, "\"o\" gte \"o\"", Boolean.TRUE},
            {null, "\"o\" < \"o\"", Boolean.FALSE},
            {null, "\"o\" lt \"o\"", Boolean.FALSE},
            {null, "\"o\" <= \"o\"", Boolean.TRUE},
            {null, "\"o\" lte \"o\"", Boolean.TRUE},
            {null, "\"o\" == \"o\"", Boolean.TRUE},
            {null, "\"o\" eq \"o\"", Boolean.TRUE},
    };

    /*===================================================================
        Public static methods
      ===================================================================*/
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new OperatorTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new OperatorTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new OperatorTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
    public OperatorTest() {
        super();
    }

    public OperatorTest(String name) {
        super(name);
    }

    public OperatorTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public OperatorTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public OperatorTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
