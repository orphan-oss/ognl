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
import ognl.test.objects.Root;

public class PrimitiveArrayTest extends OgnlTestCase {
    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            // Primitive array creation
            {ROOT, "new boolean[5]", new boolean[5]},
            {ROOT, "new boolean[] { true, false }", new boolean[]{true, false}},
            {ROOT, "new boolean[] { 0, 1, 5.5 }", new boolean[]{false, true, true}},
            {ROOT, "new char[] { 'a', 'b' }", new char[]{'a', 'b'}},
            {ROOT, "new char[] { 10, 11 }", new char[]{(char) 10, (char) 11}},
            {ROOT, "new byte[] { 1, 2 }", new byte[]{1, 2}},
            {ROOT, "new short[] { 1, 2 }", new short[]{1, 2}},
            {ROOT, "new int[six]", new int[ROOT.six]},
            {ROOT, "new int[#root.six]", new int[ROOT.six]},
            {ROOT, "new int[6]", new int[6]},
            {ROOT, "new int[] { 1, 2 }", new int[]{1, 2}},
            {ROOT, "new long[] { 1, 2 }", new long[]{1, 2}},
            {ROOT, "new float[] { 1, 2 }", new float[]{1, 2}},
            {ROOT, "new double[] { 1, 2 }", new double[]{1, 2}},

    };

    /*===================================================================
         Public static methods
       ===================================================================*/
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new PrimitiveArrayTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new PrimitiveArrayTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new PrimitiveArrayTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
    public PrimitiveArrayTest() {
        super();
    }

    public PrimitiveArrayTest(String name) {
        super(name);
    }

    public PrimitiveArrayTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public PrimitiveArrayTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public PrimitiveArrayTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
