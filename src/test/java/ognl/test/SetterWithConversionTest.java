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

public class SetterWithConversionTest extends OgnlTestCase
{
    private static Root             ROOT = new Root();

    private static Object[][]       TESTS = {
                                        // Property set with conversion
                                        { ROOT, "intValue", new Integer(0), new Double(6.5), new Integer(6) },
                                        { ROOT, "intValue", new Integer(6), new Double(1025.87645), new Integer(1025) },
                                        { ROOT, "intValue", new Integer(1025), "654", new Integer(654) },
                                        { ROOT, "stringValue", null, new Integer(25), "25" },
                                        { ROOT, "stringValue", "25", new Float(100.25), "100.25" },
                                        { ROOT, "anotherStringValue", "foo", new Integer(0), "0" },
                                        { ROOT, "anotherStringValue", "0", new Double(0.5), "0.5" },
                                        { ROOT, "anotherIntValue", new Integer(123), "5", new Integer(5) },
                                        { ROOT, "anotherIntValue", new Integer(5), new Double(100.25), new Integer(100) },
                                //          { ROOT, "anotherIntValue", new Integer(100), new String[] { "55" }, new Integer(55)},
                                //          { ROOT, "yetAnotherIntValue", new Integer(46), new String[] { "55" }, new Integer(55)},

                                    };

	/*===================================================================
		Public static methods
	  ===================================================================*/
    public static TestSuite suite()
    {
        TestSuite       result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new SetterWithConversionTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new SetterWithConversionTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new SetterWithConversionTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
	public SetterWithConversionTest()
	{
	    super();
	}

	public SetterWithConversionTest(String name)
	{
	    super(name);
	}

    public SetterWithConversionTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public SetterWithConversionTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public SetterWithConversionTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
