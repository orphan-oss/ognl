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

public class SimpleNavigationChainTreeTest extends OgnlTestCase {

    private static Object[][] TESTS = {
            {"name", Boolean.TRUE},
            {"name[i]", Boolean.FALSE},
            {"name + foo", Boolean.FALSE},
            {"name.foo", Boolean.TRUE}
    };

    /*===================================================================
         Public static methods
       ===================================================================*/
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            result.addTest(new SimpleNavigationChainTreeTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", null, (String) TESTS[i][0], TESTS[i][1]));
        }
        return result;
    }

    /*===================================================================
         Constructors
       ===================================================================*/
    public SimpleNavigationChainTreeTest()
    {
        super();
    }

    public SimpleNavigationChainTreeTest(String name)
    {
        super(name);
    }

    public SimpleNavigationChainTreeTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public SimpleNavigationChainTreeTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public SimpleNavigationChainTreeTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }

    /*===================================================================
         Overridden methods
       ===================================================================*/
    protected void runTest() throws Exception
    {
        assertTrue(Ognl.isSimpleNavigationChain(getExpression(), _context) == ((Boolean) getExpectedResult()).booleanValue());
    }
}
