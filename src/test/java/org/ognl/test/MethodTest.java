// --------------------------------------------------------------------------
// Copyright (c) 2004, Drew Davidson and Luke Blanshard
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// Neither the name of the Drew Davidson nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
// OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
// AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
// THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
// DAMAGE.
// --------------------------------------------------------------------------
package org.ognl.test;

import junit.framework.TestSuite;
import org.ognl.test.objects.*;

public class MethodTest extends OgnlTestCase
{

    private static Simple ROOT = new Simple();
    private static ListSource LIST = new ListSourceImpl();
    private static BaseGeneric<GameGenericObject, Long> GENERIC = new GameGeneric();

    private static Object[][] TESTS = {
            { "hashCode()", new Integer(ROOT.hashCode()) } ,
            { "getBooleanValue() ? \"here\" : \"\"", ""},
            { "getValueIsTrue(!false) ? \"\" : \"here\" ", ""},
            { "messages.format('ShowAllCount', one)", "foo"},
            { "getTestValue(@org.ognl.test.objects.SimpleEnum@ONE.value)", new Integer(2)},
            { "@org.ognl.test.MethodTest@getA().isProperty()", Boolean.FALSE},
            { "isDisabled()", Boolean.TRUE},
            { "isEditorDisabled()", Boolean.FALSE},
            { LIST, "addValue(name)", Boolean.TRUE},
            { "getDisplayValue(methodsTest.allowDisplay)", "test"},
            { "isThisVarArgsWorking(three, rootValue)", Boolean.TRUE},
            { GENERIC, "service.getFullMessageFor(value, null)", "Halo 3"}
    };

    public static class A
    {
        public boolean isProperty()
        {
            return false;
        }
    }

    public static A getA()
    {
        return new A();
    }

    /*
    * =================================================================== Public static methods
    * ===================================================================
    */
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for(int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3)
            {
                result.addTest(new MethodTest((String) TESTS[i][1] + " (" + TESTS[i][2] + ")", TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            } else
            {
                result.addTest(new MethodTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", ROOT, (String) TESTS[i][0], TESTS[i][1]));
            }
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public MethodTest()
    {
        super();
    }

    public MethodTest(String name)
    {
        super(name);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                      Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
