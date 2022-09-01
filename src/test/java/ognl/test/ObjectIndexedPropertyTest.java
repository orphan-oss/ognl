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
import ognl.test.objects.Bean1;
import ognl.test.objects.ObjectIndexed;

public class ObjectIndexedPropertyTest extends OgnlTestCase {

    private static ObjectIndexed OBJECT_INDEXED = new ObjectIndexed();
    private static Bean1 root = new Bean1();
    private static Object[][] TESTS = {
            // Arbitrary indexed properties
            {OBJECT_INDEXED, "attributes[\"bar\"]", "baz"}, // get non-indexed property through
            // attributes Map
            {OBJECT_INDEXED, "attribute[\"foo\"]", "bar"}, // get indexed property
            {OBJECT_INDEXED, "attribute[\"bar\"]", "baz", "newValue", "newValue"}, // set
            // indexed
            // property
            {OBJECT_INDEXED, "attribute[\"bar\"]", "newValue"},// get indexed property back to
            // confirm
            {OBJECT_INDEXED, "attributes[\"bar\"]", "newValue"}, // get property back through Map
            // to confirm
            {OBJECT_INDEXED, "attribute[\"other\"].attribute[\"bar\"]", "baz"}, // get indexed
            // property from
            // indexed, then
            // through other
            {OBJECT_INDEXED, "attribute[\"other\"].attributes[\"bar\"]", "baz"}, // get property
            // back through
            // Map to
            // confirm
            {OBJECT_INDEXED, "attribute[$]", OgnlException.class}, // illegal DynamicSubscript
            // access to object indexed
            // property
            {root, "bean2.bean3.indexedValue[25]", null}
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
                result.addTest(new ObjectIndexedPropertyTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                        TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new ObjectIndexedPropertyTest((String) TESTS[i][1], TESTS[i][0],
                            (String) TESTS[i][1], TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new ObjectIndexedPropertyTest((String) TESTS[i][1], TESTS[i][0],
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
    public ObjectIndexedPropertyTest()
    {
        super();
    }

    public ObjectIndexedPropertyTest(String name)
    {
        super(name);
    }

    public ObjectIndexedPropertyTest(String name, Object root, String expressionString, Object expectedResult,
                                     Object setValue, Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ObjectIndexedPropertyTest(String name, Object root, String expressionString, Object expectedResult,
                                     Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ObjectIndexedPropertyTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
