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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapCreationTest extends OgnlTestCase {

    private static Root ROOT = new Root();
    private static Map fooBarMap1;
    private static Map fooBarMap2;
    private static Map fooBarMap3;
    private static Map fooBarMap4;
    private static Map fooBarMap5;

    static {
        fooBarMap1 = new HashMap();
        fooBarMap1.put("foo", "bar");
        fooBarMap2 = new HashMap();
        fooBarMap2.put("foo", "bar");
        fooBarMap2.put("bar", "baz");
        fooBarMap3 = new HashMap();
        fooBarMap3.put("foo", null);
        fooBarMap3.put("bar", "baz");
        fooBarMap4 = new LinkedHashMap();
        fooBarMap4.put("foo", "bar");
        fooBarMap4.put("bar", "baz");
        fooBarMap5 = new TreeMap();
        fooBarMap5.put("foo", "bar");
        fooBarMap5.put("bar", "baz");
    }

    private static Object[][] TESTS = {
            // Map creation
            {ROOT, "#{ \"foo\" : \"bar\" }", fooBarMap1},
            {ROOT, "#{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", fooBarMap2},
            {ROOT, "#{ \"foo\", \"bar\" : \"baz\"  }", fooBarMap3},
            {ROOT, "#@java.util.LinkedHashMap@{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", fooBarMap4},
            {ROOT, "#@java.util.TreeMap@{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", fooBarMap5},

    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result
                        .addTest(new MapCreationTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                                TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new MapCreationTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                            TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new MapCreationTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
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
    public MapCreationTest()
    {
        super();
    }

    public MapCreationTest(String name)
    {
        super(name);
    }

    public MapCreationTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                           Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public MapCreationTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public MapCreationTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
