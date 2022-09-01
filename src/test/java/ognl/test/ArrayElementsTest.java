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
import ognl.TypeConverter;
import ognl.test.objects.Root;

import java.util.Arrays;

public class ArrayElementsTest extends OgnlTestCase {

    private static String[] STRING_ARRAY = new String[]{"hello", "world"};
    private static int[] INT_ARRAY = new int[]{10, 20};
    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
            // Array elements test
            {STRING_ARRAY, "length", new Integer(2)},
            {STRING_ARRAY, "#root[1]", "world"},
            {INT_ARRAY, "#root[1]", new Integer(20)},
            {INT_ARRAY, "#root[1]", new Integer(20), "50", new Integer(50)},
            {INT_ARRAY, "#root[1]", new Integer(50), new String[]{"50", "100"}, new Integer(50)},
            {ROOT, "intValue", new Integer(0), new String[]{"50", "100"}, new Integer(50)},
            {ROOT, "array", ROOT.getArray(), new String[]{"50", "100"}, new int[]{50, 100}},
            {null, "\"{Hello}\".toCharArray()[6]", new Character('}')},
            {null, "\"Tapestry\".toCharArray()[2]", new Character('p')},
            {null, "{'1','2','3'}", Arrays.asList(new Object[]{new Character('1'), new Character('2'), new Character('3')})},
            {null, "{ true, !false }", Arrays.asList(new Boolean[] { Boolean.TRUE, Boolean.TRUE }) }
    };

    /*
    * =================================================================== Private static methods
    * ===================================================================
    */
    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for (int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3) {
                result.addTest(new ArrayElementsTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                                                     TESTS[i][2]));
            } else {
                if (TESTS[i].length == 4) {
                    result.addTest(new ArrayElementsTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
                                                         TESTS[i][2], TESTS[i][3]));
                } else {
                    if (TESTS[i].length == 5) {
                        result.addTest(new ArrayElementsTest((String) TESTS[i][1], TESTS[i][0], (String) TESTS[i][1],
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
    public ArrayElementsTest()
    {
        super();
    }

    public ArrayElementsTest(String name)
    {
        super(name);
    }

    public ArrayElementsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                             Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ArrayElementsTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ArrayElementsTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    protected void setUp()
    {
        TypeConverter arrayConverter;

        super.setUp();
        /**
         arrayConverter = new DefaultTypeConverter() {

         public Object convertValue(Map context, Object target, Member member, String propertyName, Object value,
         Class toType)
         {
         if (value.getClass().isArray()) {
         if (!toType.isArray()) {
         value = Array.get(value, 0);
         }
         }
         return super.convertValue(context, target, member, propertyName, value, toType);
         }
         };
         _context.setTypeConverter(arrayConverter); */
    }
}
