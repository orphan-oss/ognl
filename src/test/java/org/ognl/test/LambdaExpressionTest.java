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

import java.math.BigInteger;
import java.util.Arrays;

public class LambdaExpressionTest extends OgnlTestCase {

    private static final Object[][] TESTS = {
            // Lambda expressions
            {new Object[]{}, "#a=:[33](20).longValue().{0}.toArray().length", 33},
            {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30)", 1409286144},
            {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30L)", -8764578968847253504L},
            {null, "#fact=:[#this<=1? 1 : #fact(#this-1) * #this], #fact(30h)",
                    new BigInteger("265252859812191058636308480000000")},
            {null, "#bump = :[ #this.{ #this + 1 } ], (#bump)({ 1, 2, 3 })", Arrays.asList(2, 3, 4)},
            {null, "#call = :[ \"calling \" + [0] + \" on \" + [1] ], (#call)({ \"x\", \"y\" })", "calling x on y"},

    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (Object[] test : TESTS) {
            result.addTest(
                    new LambdaExpressionTest(
                            (String) test[1],
                            test[0],
                            (String) test[1],
                            test[2]
                    )
            );
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public LambdaExpressionTest() {
        super();
    }

    public LambdaExpressionTest(String name) {
        super(name);
    }

    public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult,
                                Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult,
                                Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public LambdaExpressionTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
