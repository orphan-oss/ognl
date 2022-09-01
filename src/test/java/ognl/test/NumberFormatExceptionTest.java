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
import ognl.OgnlException;
import ognl.test.objects.Simple;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberFormatExceptionTest extends OgnlTestCase
{
    private static Simple           SIMPLE = new Simple();

    private static Object[][]       TESTS = {
                                        // NumberFormatException handling (default is to throw NumberFormatException on bad string conversions)
                                        { SIMPLE, "floatValue", new Float(0f), new Float(10f), new Float(10f) },
                                        { SIMPLE, "floatValue", new Float(10f), "x10x", OgnlException.class },

                                        { SIMPLE, "intValue", new Integer(0), new Integer(34), new Integer(34) },
                                        { SIMPLE, "intValue", new Integer(34), "foobar", OgnlException.class },
                                        { SIMPLE, "intValue", new Integer(34), "", OgnlException.class },
                                        { SIMPLE, "intValue", new Integer(34), "       \t", OgnlException.class },
                                        { SIMPLE, "intValue", new Integer(34), "       \t1234\t\t", new Integer(1234) },

                                        { SIMPLE, "bigIntValue", BigInteger.valueOf(0), BigInteger.valueOf(34), BigInteger.valueOf(34) },
                                        { SIMPLE, "bigIntValue", BigInteger.valueOf(34), null, null },
                                        { SIMPLE, "bigIntValue", null, "", OgnlException.class },
                                        { SIMPLE, "bigIntValue", null, "foobar", OgnlException.class },

                                        { SIMPLE, "bigDecValue", new BigDecimal(0.0), new BigDecimal(34.55), new BigDecimal(34.55) },
                                        { SIMPLE, "bigDecValue", new BigDecimal(34.55), null, null },
                                        { SIMPLE, "bigDecValue", null, "", OgnlException.class },
                                        { SIMPLE, "bigDecValue", null, "foobar", OgnlException.class }

                                    };

	/*===================================================================
		Public static methods
	  ===================================================================*/
    public static TestSuite suite()
    {
        TestSuite       result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new NumberFormatExceptionTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new NumberFormatExceptionTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new NumberFormatExceptionTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
	public NumberFormatExceptionTest()
	{
	    super();
	}

	public NumberFormatExceptionTest(String name)
	{
	    super(name);
	}

    public NumberFormatExceptionTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public NumberFormatExceptionTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public NumberFormatExceptionTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
