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
import ognl.InappropriateExpressionException;
import ognl.NoSuchPropertyException;
import ognl.test.objects.Root;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SetterTest extends OgnlTestCase
{
    private static Root             ROOT = new Root();

    static Set _list = new HashSet();
    static {
        _list.add("Test1");
    }

    private static Object[][]       TESTS = {
            // Setting values
            { ROOT.getMap(), "newValue", null, new Integer(101) },
            { ROOT, "settableList[0]", "foo", "quux" }, // absolute indexes
            { ROOT, "settableList[0]", "quux" },
            { ROOT, "settableList[2]", "baz", "quux" },
            { ROOT, "settableList[2]", "quux" },
            { ROOT, "settableList[$]", "quux", "oompa" }, // special indexes
            { ROOT, "settableList[$]", "oompa" },
            { ROOT, "settableList[^]", "quux", "oompa" },
            { ROOT, "settableList[^]", "oompa" },
            { ROOT, "settableList[|]", "bar", "oompa" },
            { ROOT, "settableList[|]", "oompa" },
            { ROOT, "map.newValue", new Integer(101), new Integer(555) },
            { ROOT, "map", ROOT.getMap(), new HashMap(), NoSuchPropertyException.class },
            { ROOT.getMap(), "newValue2 || put(\"newValue2\",987), newValue2", new Integer(987), new Integer(1002) },
            { ROOT, "map.(someMissingKey || newValue)", new Integer(555), new Integer(666) },
            { ROOT.getMap(), "newValue || someMissingKey", new Integer(666), new Integer(666) }, // no setting happens!
            { ROOT, "map.(newValue && aKey)", null, new Integer(54321)},
            { ROOT, "map.(someMissingKey && newValue)", null, null }, // again, no setting
            { null, "0", new Integer(0), null, InappropriateExpressionException.class }, // illegal for setting, no property
            { ROOT, "map[0]=\"map.newValue\", map[0](#this)", new Integer(666), new Integer(888) },
            { ROOT, "selectedList", null, _list, IllegalArgumentException.class},
            { ROOT, "openTransitionWin", Boolean.FALSE, Boolean.TRUE}
    };

    /*===================================================================
         Public static methods
       ===================================================================*/
    public static TestSuite suite()
    {
        TestSuite       result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new SetterTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new SetterTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new SetterTest((String)TESTS[i][1], TESTS[i][0], (String)TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
    public SetterTest()
    {
        super();
    }

    public SetterTest(String name)
    {
        super(name);
    }

    public SetterTest(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public SetterTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public SetterTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
