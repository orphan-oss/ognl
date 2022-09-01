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

import java.math.BigInteger;
import java.util.Arrays;

public class ProjectionSelectionTest extends OgnlTestCase
{
    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            // Projection, selection
             { ROOT, "array.{class}",
                    Arrays.asList(new Class[] { Integer.class, Integer.class, Integer.class, Integer.class }) },
            { ROOT, "map.array.{? #this > 2 }", Arrays.asList(new Integer[] { new Integer(3), new Integer(4) }) },
            { ROOT, "map.array.{^ #this > 2 }", Arrays.asList(new Integer[] { new Integer(3) }) },
            { ROOT, "map.array.{$ #this > 2 }", Arrays.asList(new Integer[] { new Integer(4) }) },
            { ROOT, "map.array[*].{?true} instanceof java.util.Collection", Boolean.TRUE },
            { null, "#fact=1, 30H.{? #fact = #fact * (#this+1), false }, #fact",
                    new BigInteger("265252859812191058636308480000000") },
            };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for(int i = 0; i < TESTS.length; i++) {
            result.addTest(new ProjectionSelectionTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                    TESTS[i][2]));
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ProjectionSelectionTest()
    {
        super();
    }

    public ProjectionSelectionTest(String name)
    {
        super(name);
    }

    public ProjectionSelectionTest(String name, Object root, String expressionString, Object expectedResult,
            Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ProjectionSelectionTest(String name, Object root, String expressionString, Object expectedResult,
            Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ProjectionSelectionTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
