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
import ognl.MethodFailedException;
import ognl.NoSuchPropertyException;
import ognl.test.objects.IndexedSetObject;
import ognl.test.objects.Root;

public class IndexAccessTest extends OgnlTestCase {

    private static Root ROOT = new Root();
    private static IndexedSetObject INDEXED_SET = new IndexedSetObject();

    private static Object[][] TESTS = {
            {ROOT, "list[index]", ROOT.getList().get(ROOT.getIndex())},
            {ROOT, "list[objectIndex]", ROOT.getList().get(ROOT.getObjectIndex().intValue())},
            {ROOT, "array[objectIndex]", ROOT.getArray()[ROOT.getObjectIndex().intValue()]},
            {ROOT, "array[getObjectIndex()]", ROOT.getArray()[ROOT.getObjectIndex().intValue()]},
            {ROOT, "array[genericIndex]", ROOT.getArray()[((Integer) ROOT.getGenericIndex()).intValue()]},
            {ROOT, "booleanArray[self.objectIndex]", Boolean.FALSE},
            {ROOT, "booleanArray[getObjectIndex()]", Boolean.FALSE},
            {ROOT, "booleanArray[nullIndex]", NoSuchPropertyException.class},
            {ROOT, "list[size() - 1]", MethodFailedException.class},
            {ROOT, "(index == (array.length - 3)) ? 'toggle toggleSelected' : 'toggle'", "toggle toggleSelected"},
            {ROOT, "\"return toggleDisplay('excdisplay\"+index+\"', this)\"", "return toggleDisplay('excdisplay1', this)"},
            {ROOT, "map[mapKey].split('=')[0]", "StringStuff"},
            {ROOT, "booleanValues[index1][index2]", Boolean.FALSE},
            {ROOT, "tab.searchCriteria[index1].displayName", "Woodland creatures"},
            {ROOT, "tab.searchCriteriaSelections[index1][index2]", Boolean.TRUE},
            {ROOT, "tab.searchCriteriaSelections[index1][index2]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE},
            {ROOT, "map['bar'].value", 100, 50, 50},
            {INDEXED_SET, "thing[\"x\"].val", 1, 2, 2}
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 5) {
                result.addTest(new IndexAccessTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2],
                        TESTS[i][3], TESTS[i][4]));
            } else {
                result.addTest(new IndexAccessTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            }
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public IndexAccessTest() {
        super();
    }

    public IndexAccessTest(String name) {
        super(name);
    }

    public IndexAccessTest(String name, Object root, String expressionString, Object expectedResult,
                           Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public IndexAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public IndexAccessTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }

    public void setUp() {
        super.setUp();
    }
}
