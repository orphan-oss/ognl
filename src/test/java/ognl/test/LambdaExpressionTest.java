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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class LambdaExpressionTest extends OgnlTestCase {

    private static Object[][] TESTS = {
            // Lambda expressions
            {null, "#a=:[33](20).longValue().{0}.toArray().length", new Integer(33)},
            {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30)", new Integer(1409286144)},
            {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30L)", new Long(-8764578968847253504L)},
            {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30h)",
                    new BigInteger("265252859812191058636308480000000")},
            {null, "#bump = :[ #this.{ #this + 1 } ], (#bump)({ 1, 2, 3 })",
                    new ArrayList(Arrays.asList(new Integer[]{new Integer(2), new Integer(3), new Integer(4)}))},
            {null, "#call = :[ \"calling \" + [0] + \" on \" + [1] ], (#call)({ \"x\", \"y\" })", "calling x on y"},

    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            result.addTest(new LambdaExpressionTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                    TESTS[i][2]));
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public LambdaExpressionTest() {
        super();
    }

    public LambdaExpressionTest(String name) {
        super(name);
    }

    public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult,
                                Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult,
                                Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
