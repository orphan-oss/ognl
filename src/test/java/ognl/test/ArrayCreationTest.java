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
import ognl.ExpressionSyntaxException;
import ognl.test.objects.Entry;
import ognl.test.objects.Root;
import ognl.test.objects.Simple;

public class ArrayCreationTest extends OgnlTestCase {

    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            // Array creation
            {ROOT, "new String[] { \"one\", \"two\" }", new String[]{"one", "two"}},
            {ROOT, "new String[] { 1, 2 }", new String[]{"1", "2"}},
            {ROOT, "new Integer[] { \"1\", 2, \"3\" }",
                    new Integer[]{new Integer(1), new Integer(2), new Integer(3)}},
            {ROOT, "new String[10]", new String[10]},
            {ROOT, "new Object[4] { #root, #this }", ExpressionSyntaxException.class},
            {ROOT, "new Object[4]", new Object[4]},
            {ROOT, "new Object[] { #root, #this }", new Object[]{ROOT, ROOT}},
            {ROOT,
                    "new ognl.test.objects.Simple[] { new ognl.test.objects.Simple(), new ognl.test.objects.Simple(\"foo\", 1.0f, 2) }",
                    new Simple[]{new Simple(), new Simple("foo", 1.0f, 2)}},
            {ROOT, "new ognl.test.objects.Simple[5]", new Simple[5]},
            {ROOT, "new ognl.test.objects.Simple(new Object[5])", new Simple(new Object[5])},
            {ROOT, "new ognl.test.objects.Simple(new String[5])", new Simple(new String[5])},
            {ROOT, "objectIndex ? new ognl.test.objects.Entry[] { new ognl.test.objects.Entry(), new ognl.test.objects.Entry()} "
                    + ": new ognl.test.objects.Entry[] { new ognl.test.objects.Entry(), new ognl.test.objects.Entry()} ",
                    new Entry[]{new Entry(), new Entry()}}
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new ArrayCreationTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                        TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new ArrayCreationTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                            TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new ArrayCreationTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
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
    public ArrayCreationTest() {
        super();
    }

    public ArrayCreationTest(String name) {
        super(name);
    }

    public ArrayCreationTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                             Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ArrayCreationTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ArrayCreationTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
