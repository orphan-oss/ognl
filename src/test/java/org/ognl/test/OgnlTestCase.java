//--------------------------------------------------------------------------
//  Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//  Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.DefaultMemberAccess;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.SimpleNode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;

public abstract class OgnlTestCase extends TestCase {

    protected OgnlContext _context;
    private String _expressionString;
    private SimpleNode _expression;
    private Object _expectedResult;
    private Object _root;
    protected boolean _compileExpressions = true;
    private boolean hasSetValue;
    private Object setValue;
    private boolean hasExpectedAfterSetResult;
    private Object expectedAfterSetResult;

    /*===================================================================
         Public static methods
       ===================================================================*/
    /**
     * Returns true if object1 is equal to object2 in either the
     * sense that they are the same object or, if both are non-null
     * if they are equal in the <CODE>equals()</CODE> sense.
     */
    public static boolean isEqual(Object object1, Object object2)
    {
        boolean result = false;

        if (object1 == object2) {
            result = true;
        } else {
            if ((object1 != null) && object1.getClass().isArray()) {
                if ((object2 != null) && object2.getClass().isArray() && (object2.getClass() == object1.getClass())) {
                    result = (Array.getLength(object1) == Array.getLength(object2));
                    if (result) {
                        for (int i = 0, icount = Array.getLength(object1); result && (i < icount); i++) {
                            result = isEqual(Array.get(object1, i), Array.get(object2, i));
                        }
                    }
                }
            } else {
                result = (object1 != null) && (object2 != null) && object1.equals(object2);
            }
        }
        return result;
    }

    /*===================================================================
         Constructors
       ===================================================================*/
    public OgnlTestCase()
    {
        super();
    }

    public OgnlTestCase(String name)
    {
        super(name);
    }

    public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult, Object setValue, Object expectedAfterSetResult)
    {
        this(name, root, expressionString, expectedResult, setValue);
        this.hasExpectedAfterSetResult = true;
        this.expectedAfterSetResult = expectedAfterSetResult;
    }

    public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        this(name, root, expressionString, expectedResult);
        this.hasSetValue = true;
        this.setValue = setValue;
    }

    public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult)
    {
        this(name);
        this._root = root;
        this._expressionString = expressionString;
        this._expectedResult = expectedResult;
    }

    /*===================================================================
         Public methods
       ===================================================================*/
    public String getExpressionDump(SimpleNode node)
    {
        StringWriter writer = new StringWriter();

        node.dump(new PrintWriter(writer), "   ");
        return writer.toString();
    }

    public String getExpressionString()
    {
        return _expressionString;
    }

    public SimpleNode getExpression()
            throws Exception
    {
        if (_expression == null)
        {
            _expression = (SimpleNode) Ognl.parseExpression(_expressionString);
        }

        if (_compileExpressions)
        {
            _expression = (SimpleNode) Ognl.compileExpression(_context, _root, _expressionString);
        }

        return _expression;
    }

    public Object getExpectedResult()
    {
        return _expectedResult;
    }

    public static void assertEquals(Object expected, Object actual)
    {
        if (expected != null && expected.getClass().isArray()
            && actual != null && actual.getClass().isArray()) {

            TestCase.assertEquals(Array.getLength(expected), Array.getLength(actual));

            int length = Array.getLength(expected);

            for (int i = 0; i < length; i++) {
                Object aexpected = Array.get(expected, i);
                Object aactual = Array.get(actual, i);

                if (aexpected != null && aactual != null && Boolean.class.isAssignableFrom(aexpected.getClass())) {
                    TestCase.assertEquals(aexpected.toString(), aactual.toString());
                } else
                    OgnlTestCase.assertEquals(aexpected, aactual);
            }
        } else if (expected != null && actual != null
                   && Character.class.isInstance(expected)
                   && Character.class.isInstance(actual)) {

            TestCase.assertEquals(((Character) expected).charValue(), ((Character) actual).charValue());
        } else {

            TestCase.assertEquals(expected, actual);
        }
    }

    /*===================================================================
         Overridden methods
       ===================================================================*/
    protected void runTest() throws Exception
    {
        Object testedResult = null;

        try {
            SimpleNode expr;

            testedResult = _expectedResult;
            expr = getExpression();

            assertEquals(_expectedResult, Ognl.getValue(expr, _context, _root));

            if (hasSetValue)
            {
                testedResult = hasExpectedAfterSetResult ? expectedAfterSetResult : setValue;
                Ognl.setValue(expr, _context, _root, setValue);

                assertEquals(testedResult, Ognl.getValue(expr, _context, _root));
            }

        } catch (Exception ex) {
            System.out.println("Caught exception " + ex);
            if (NullPointerException.class.isInstance(ex))
                ex.printStackTrace();

            if (RuntimeException.class.isInstance(ex) && ((RuntimeException) ex).getCause() != null
                && Exception.class.isAssignableFrom(((RuntimeException) ex).getCause().getClass()))
                ex = (Exception) ((RuntimeException) ex).getCause();

            if (testedResult instanceof Class)
            {
                assertTrue(Exception.class.isAssignableFrom((Class) testedResult));
            } else
                throw ex;
        }
    }

    protected void setUp()
    {
        _context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false), null, null);
    }
}
