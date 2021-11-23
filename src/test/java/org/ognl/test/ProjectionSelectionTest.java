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
import org.ognl.test.objects.Root;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class ProjectionSelectionTest extends OgnlTestCase {

    private static final Root ROOT = new Root();

    private static final Object[][] TESTS = {
            // Projection, selection
            {ROOT, "array.{class}", Arrays.asList(Integer.class, Integer.class, Integer.class, Integer.class)},
            {ROOT, "map.array.{? #this > 2 }", Arrays.asList(3, 4)},
            {ROOT, "map.array.{^ #this > 2 }", Collections.singletonList(3)},
            {ROOT, "map.array.{$ #this > 2 }", Collections.singletonList(4)},
            {ROOT, "map.array[*].{?true} instanceof java.util.Collection", Boolean.TRUE},
            {ROOT, "#fact=1, 30H.{? #fact = #fact * (#this+1), false }, #fact", new BigInteger("265252859812191058636308480000000")},
    };

    public static TestSuite suite() {
        TestSuite result = new TestSuite();

        for (Object[] test : TESTS) {
            result.addTest(
                    new ProjectionSelectionTest(
                            (String) test[1],
                            test[0],
                            (String) test[1],
                            test[2]
                    )
            );
        }
        return result;
    }

    public ProjectionSelectionTest() {
        super();
    }

    public ProjectionSelectionTest(String name) {
        super(name);
    }

    public ProjectionSelectionTest(String name, Object root, String expressionString, Object expectedResult,
                                   Object setValue, Object expectedAfterSetResult) {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public ProjectionSelectionTest(String name, Object root, String expressionString, Object expectedResult,
                                   Object setValue) {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public ProjectionSelectionTest(String name, Object root, String expressionString, Object expectedResult) {
        super(name, root, expressionString, expectedResult);
    }
}
