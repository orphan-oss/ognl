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

import java.util.Arrays;

public class CollectionDirectPropertyTest extends OgnlTestCase {

    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            // Collection direct properties
            {Arrays.asList(new String[]{"hello", "world"}), "size", new Integer(2)},
            {Arrays.asList(new String[]{"hello", "world"}), "isEmpty", Boolean.FALSE},
            {Arrays.asList(new String[]{}), "isEmpty", Boolean.TRUE},
            {Arrays.asList(new String[]{"hello", "world"}), "iterator.next", "hello"},
            {Arrays.asList(new String[]{"hello", "world"}), "iterator.hasNext", Boolean.TRUE},
            {Arrays.asList(new String[]{"hello", "world"}), "#it = iterator, #it.next, #it.next, #it.hasNext",
                    Boolean.FALSE},
            {Arrays.asList(new String[]{"hello", "world"}), "#it = iterator, #it.next, #it.next", "world"},
            {Arrays.asList(new String[]{"hello", "world"}), "size", new Integer(2)},
            {ROOT, "map[\"test\"]", ROOT},
            {ROOT, "map.size", new Integer(ROOT.getMap().size())},
            {ROOT, "map.keySet", ROOT.getMap().keySet()},
            {ROOT, "map.values", ROOT.getMap().values()},
            {ROOT, "map.keys.size", new Integer(ROOT.getMap().keySet().size())},
            {ROOT, "map[\"size\"]", ROOT.getMap().get("size")},
            {ROOT, "map.isEmpty", ROOT.getMap().isEmpty() ? Boolean.TRUE : Boolean.FALSE},
            {ROOT, "map[\"isEmpty\"]", null},
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new CollectionDirectPropertyTest((String) TESTS[i][1], TESTS[i][0],
                        (String) TESTS[i][1], TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new CollectionDirectPropertyTest((String) TESTS[i][1], TESTS[i][0],
                            (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new CollectionDirectPropertyTest((String) TESTS[i][1], TESTS[i][0],
                                (String) TESTS[i][1], TESTS[i][2], TESTS[i][3], TESTS[i][4]));
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
    public CollectionDirectPropertyTest() {
        super();
    }

    public CollectionDirectPropertyTest(String name) {
        super(name);
    }

    public CollectionDirectPropertyTest(String name, Object root, String expressionString, Object expectedResult,
                                        Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public CollectionDirectPropertyTest(String name, Object root, String expressionString, Object expectedResult,
                                        Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public CollectionDirectPropertyTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
