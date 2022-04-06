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

import org.ognl.DefaultMemberAccess;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.OgnlException;
import org.ognl.SimpleNode;
import org.ognl.test.objects.Bean1;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Performance extends Object
{

    private static int MAX_ITERATIONS = -1;
    private static boolean ITERATIONS_MODE;
    private static long MAX_TIME = -1L;
    private static boolean TIME_MODE;
    private static NumberFormat FACTOR_FORMAT = new DecimalFormat("0.000");

    private String _name;
    private OgnlContext _context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    private Bean1 _root = new Bean1();
    private SimpleNode _expression;
    private SimpleNode _compiledExpression;
    private Method _method;
    private int _iterations;
    private Serializable _mvelCompiled;
    private String _expressionString;
    private boolean _isMvel = false;
    private long t0;
    private long t1;

    /*
    * =================================================================== Private static classes
    * ===================================================================
    */
    private static class Results
    {

        int iterations;
        long time;
        boolean mvel;

        public Results(int iterations, long time, boolean mvel)
        {
            super();
            this.iterations = iterations;
            this.time = time;
            this.mvel = mvel;
        }

        public String getFactor(Results otherResults)
        {
            String ret = null;

            if (TIME_MODE) {
                float factor = 0;

                if (iterations < otherResults.iterations) {
                    factor = Math.max((float) otherResults.iterations, (float) iterations)
                             / Math.min((float) otherResults.iterations, (float) iterations);
                } else {
                    factor = Math.min((float) otherResults.iterations, (float) iterations)
                             / Math.max((float) otherResults.iterations, (float) iterations);
                }

                ret = FACTOR_FORMAT.format(factor);
                if (iterations > otherResults.iterations)
                    ret += " times faster than ";
                else
                    ret += " times slower than ";

            } else {
                float factor = Math.max((float) otherResults.time, (float) time)
                               / Math.min((float) otherResults.time, (float) time);

                ret = FACTOR_FORMAT.format(factor);
                if (time < otherResults.time)
                    ret += " times faster than ";
                else
                    ret += " times slower than ";
            }

            return ret;
        }
    }

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static void main(String[] args)
    {
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-time")) {
                TIME_MODE = true;
                MAX_TIME = Long.parseLong(args[++i]);
            } else if (args[i].equals("-iterations")) {
                ITERATIONS_MODE = true;
                MAX_ITERATIONS = Integer.parseInt(args[++i]);
            }
        }
        if (!TIME_MODE && !ITERATIONS_MODE) {
            TIME_MODE = true;
            MAX_TIME = 1500;
        }

        try {
            Performance[] tests = new Performance[] {
                    new Performance("Constant", "100 + 20 * 5", "testConstantExpression"),
                    //new Performance("Constant", "100 + 20 * 5", "testConstantExpression", false),
                    new Performance("Single Property", "bean2", "testSinglePropertyExpression"),
                    new Performance("Property Navigation", "bean2.bean3.value", "testPropertyNavigationExpression"),
                    /*new Performance("Property Setting with context key", "bean2.bean3.nullValue", "testPropertyNavigationSetting"),
                    new Performance("Property Setting with context key", "bean2.bean3.nullValue", "testPropertyNavigationSetting", true), */
                    new Performance("Property Navigation and Comparison", "bean2.bean3.value <= 24",
                                    "testPropertyNavigationAndComparisonExpression"),
                    /* new Performance("Property Navigation with Indexed Access", "bean2.bean3.indexedValue[25]",
                                    "testIndexedPropertyNavigationExpression"),
                    new Performance("Property Navigation with Indexed Access", "bean2.bean3.indexedValue[25]",
                                    "testIndexedPropertyNavigationExpression", true), */
                    new Performance("Property Navigation with Map Access", "bean2.bean3.map['foo']",
                                    "testPropertyNavigationWithMapExpression"),
                    /* new Performance("Property Navigation with Map value set", "bean2.bean3.map['foo']",
                                    "testPropertyNavigationWithMapSetting"),
                    new Performance("Property Navigation with Map value set", "bean2.bean3.map['foo']",
                                    "testPropertyNavigationWithMapSetting", true) */
            };

            boolean timeMode = TIME_MODE;
            boolean iterMode = ITERATIONS_MODE;
            long maxTime = MAX_TIME;
            int maxIterations = MAX_ITERATIONS;

            //TIME_MODE = false;
            //ITERATIONS_MODE = true;
            //maxIterations = 1000;

            runTests(tests, false);

            TIME_MODE = timeMode;
            ITERATIONS_MODE = iterMode;
            MAX_TIME = maxTime;
            MAX_ITERATIONS = maxIterations;

            System.out.println("\n\n============================================================================\n");

            Thread.sleep(2500);
            runTests(tests, true);

            //Thread.sleep(2000);

            System.out.println("\n\n============================================================================\n");
            // runTests(tests);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void runTests(Performance[] tests, boolean output)
            throws Exception
    {
        for(int i = 0; i < tests.length; i++) {
            Performance perf = tests[i];

            try {

                Results javaResults = perf.testJava(),
                        interpretedResults = perf.testExpression(false),
                        compiledResults = perf.testExpression(true);

                if (!output)
                    return;

                System.out.println((compiledResults.mvel ? "MVEL" : "OGNL") + " " + perf.getName() + ": " + perf.getExpression().toString());
                System.out.println("       java: " + javaResults.iterations + " iterations in " + javaResults.time + " ms");

                System.out.println("   compiled: " + compiledResults.iterations + " iterations in "
                                   + compiledResults.time + " ms ("
                                   + compiledResults.getFactor(javaResults) + "java)");

                System.out.println("interpreted: " + interpretedResults.iterations + " iterations in "
                                   + interpretedResults.time + " ms ("
                                   + interpretedResults.getFactor(javaResults) + "java)");

                System.out.println();

            } catch (OgnlException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
    * =================================================================== Constructors
    * ===================================================================
    */
    public Performance(String name, String expressionString, String javaMethodName)
            throws Exception
    {
        this(name, expressionString, javaMethodName, false);
    }

    public Performance(String name, String expressionString, String javaMethodName, boolean mvel)
            throws Exception
    {
        _name = name;
        _isMvel = mvel;
        _expressionString = expressionString;

        try {
            _method = getClass().getMethod(javaMethodName, new Class[] {});
        } catch (Exception ex) {
            throw new OgnlException("java method not found", ex);
        }

        if (!_isMvel)
        {
            _expression = (SimpleNode) Ognl.parseExpression(expressionString);
            _compiledExpression = (SimpleNode) Ognl.compileExpression(_context, _root, expressionString);
            Ognl.getValue(_expression, _context, _root);
            _context.put("contextValue", "cvalue");
        } else
        {
            //_mvelCompiled = MVEL.compileExpression(expressionString);
        }
    }

    /*
     * =================================================================== Protected methods
     * ===================================================================
     */
    protected void startTest()
    {
        _iterations = 0;
        t0 = t1 = System.currentTimeMillis();
    }

    protected Results endTest()
    {
        return new Results(_iterations, t1 - t0, _isMvel);
    }

    protected boolean done()
    {
        _iterations++;
        t1 = System.currentTimeMillis();

        if (TIME_MODE) {
            return (t1 - t0) >= MAX_TIME;
        } else {
            if (ITERATIONS_MODE) {
                return _iterations >= MAX_ITERATIONS;
            } else {
                throw new RuntimeException("no maximums specified");
            }
        }
    }

    /*
     * =================================================================== Public methods
     * ===================================================================
     */
    public String getName()
    {
        return _name;
    }

    public String getExpression()
    {
        return _expressionString;
    }

    public Results testExpression(boolean compiled)
            throws Exception
    {
        startTest();
        do {
            if (!_isMvel)
            {
                if (compiled)
                    Ognl.getValue(_compiledExpression.getAccessor(), _context, _root);
                else
                    Ognl.getValue(_expression, _context, _root);
            } else
            {
                /*
                if (compiled)
                    MVEL.executeExpression(_mvelCompiled, _root);
                else
                    MVEL.eval(_expressionString, _root);*/
            }
        } while(!done());
        return endTest();
    }

    public Results testJava()
            throws OgnlException
    {
        try {
            return (Results) _method.invoke(this, new Object[] {});
        } catch (Exception ex) {
            throw new OgnlException("invoking java method '" + _method.getName() + "'", ex);
        }
    }

    public Results testConstantExpression()
            throws OgnlException
    {
        startTest();
        do {
            int result = 100 + 20 * 5;
        } while(!done());
        return endTest();
    }

    public Results testSinglePropertyExpression()
            throws OgnlException
    {
        startTest();
        do {
            _root.getBean2();
        } while(!done());
        return endTest();
    }

    public Results testPropertyNavigationExpression()
            throws OgnlException
    {
        startTest();
        do {
            _root.getBean2().getBean3().getValue();
        } while(!done());
        return endTest();
    }

    public Results testPropertyNavigationSetting()
            throws OgnlException
    {
        startTest();
        do {
            _root.getBean2().getBean3().setNullValue("a value");
        } while(!done());
        return endTest();
    }

    public Results testPropertyNavigationAndComparisonExpression()
            throws OgnlException
    {
        startTest();
        do {
            boolean result = _root.getBean2().getBean3().getValue() < 24;
        } while(!done());
        return endTest();
    }

    public Results testIndexedPropertyNavigationExpression()
            throws OgnlException
    {
        startTest();
        do {
            _root.getBean2().getBean3().getIndexedValue(25);
        } while(!done());
        return endTest();
    }

    public Results testPropertyNavigationWithMapSetting()
            throws OgnlException
    {
        startTest();
        do {
            _root.getBean2().getBean3().getMap().put("bam", "bam");
        } while(!done());
        return endTest();
    }

    public Results testPropertyNavigationWithMapExpression()
            throws OgnlException
    {
        startTest();
        do {
            _root.getBean2().getBean3().getMap().get("foo");
        } while(!done());
        return endTest();
    }
}
