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
import ognl.test.objects.CorrectedObject;

public class ClassMethodTest extends OgnlTestCase
{

    private static CorrectedObject CORRECTED = new CorrectedObject();

    private static Object[][] TESTS = {
            // Methods on Class
            { CORRECTED, "getClass().getName()", CORRECTED.getClass().getName() },
            { CORRECTED, "getClass().getInterfaces()", CORRECTED.getClass().getInterfaces() },
            { CORRECTED, "getClass().getInterfaces().length", new Integer(CORRECTED.getClass().getInterfaces().length) },
            { null, "@System@class.getInterfaces()", System.class.getInterfaces() },
            { null, "@Class@class.getName()", Class.class.getName() },
            { null, "@java.awt.image.ImageObserver@class.getName()", java.awt.image.ImageObserver.class.getName() },
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for(int i = 0; i < TESTS.length; i++) {
            result.addTest(new ClassMethodTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ClassMethodTest()
    {
        super();
    }

    public ClassMethodTest(String name)
    {
        super(name);
    }

    public ClassMethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                           Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ClassMethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ClassMethodTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
