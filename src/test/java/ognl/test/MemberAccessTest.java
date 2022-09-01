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
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Simple;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class MemberAccessTest extends OgnlTestCase {

    private static final Simple ROOT = new Simple();

    private static final Object[][] TESTS = {
            {"@Runtime@getRuntime()", OgnlException.class},
            {"@System@getProperty('java.specification.version')", System.getProperty("java.specification.version")},
            {"bigIntValue", OgnlException.class},
            {"bigIntValue", OgnlException.class, 25, OgnlException.class},
            {"getBigIntValue()", OgnlException.class}, {"stringValue", ROOT.getStringValue()},
    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (Object[] test : TESTS) {
            result.addTest(new MemberAccessTest(test[0] + " (" + test[1] + ")", ROOT, (String) test[0], test[1]));
        }

        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public MemberAccessTest() {
        super();
    }

    public MemberAccessTest(String name) {
        super(name);
    }

    public MemberAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                            Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public MemberAccessTest(String name, Object root, String expressionString, Object expectedResult, Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public MemberAccessTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    public void setUp() {
        super.setUp();

        /* Should allow access at all to the Simple class except for the bigIntValue property */
        DefaultMemberAccess ma = new DefaultMemberAccess(false) {

            public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
                if (target == Runtime.class) {
                    return false;
                }
                if (target instanceof Simple) {
                    if (propertyName != null) {
                        return !propertyName.equals("bigIntValue")
                                && super.isAccessible(context, target, member, propertyName);
                    } else {
                        if (member instanceof Method) {
                            return !member.getName().equals("getBigIntValue")
                                    && !member.getName().equals("setBigIntValue")
                                    && super.isAccessible(context, target, member, propertyName);
                        }
                    }
                }
                return super.isAccessible(context, target, member, propertyName);
            }
        };

        _context = Ognl.createDefaultContext(null, ma, null, null);
    }
}
