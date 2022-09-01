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

import junit.framework.TestCase;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.SimpleNode;

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
