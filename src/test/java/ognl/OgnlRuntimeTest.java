package ognl;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class GenericClass<T> {
    public void bar(final T parameter) {
    }
}

class ExampleStringClass extends GenericClass<String> {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
}

class ExampleStringSubclass extends ExampleStringClass {
}

class ExampleTwoMethodClass {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass2 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass3 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass4 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass5 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass6 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass7 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass8 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass9 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass10 {
    public void foo(final Integer parameter1, final Date parameter2) {
    }
    public void bar(final String parameter2) {
    }
}


public class OgnlRuntimeTest {

    private static long cumulativelRunTestElapsedNanoTime;
    private static long totalNumberOfRunTestRuns;

    class Worker implements Callable<Class<?>[]> {

        private final Class<?> clazz;
        private final Method method;
        private final int invocationCount;

        public Worker(final Class<?> clazz, final Method method, final int invocationCount) {
            this.clazz = clazz;
            this.method = method;
            this.invocationCount = invocationCount;
        }

        public Class<?>[] call() throws Exception {
            Class<?>[] result = null;
            for (int i = this.invocationCount; i > 0; i--) {
                result = OgnlRuntime.findParameterTypes(this.clazz, this.method);
            }
            return result;
        }
    }

    private void runTest(final Class<?> clazz, final Method method, final int invocationCount, final int threadCount,
            final Class<?>[] expected) throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final List<Future<Class<?>[]>> futures = new ArrayList<Future<Class<?>[]>>(threadCount);
        totalNumberOfRunTestRuns++;
        final long testStartNanoTime = System.nanoTime();
        for (int i = threadCount; i > 0; i--) {
            futures.add(executor.submit(new Worker(clazz, method, invocationCount)));
        }
        for (final Future<Class<?>[]> future : futures) {
            Assert.assertArrayEquals(future.get(), expected);
        }
        final long testEndNanoTime = System.nanoTime();
        final long elapsedTestNanoTime = testEndNanoTime - testStartNanoTime;
        cumulativelRunTestElapsedNanoTime = cumulativelRunTestElapsedNanoTime + elapsedTestNanoTime;
        System.out.println("    OGNL runTest() elapsed time: " + elapsedTestNanoTime + " ns (" +
                (elapsedTestNanoTime / (1000 * 1000)) + " ms or " +
                (elapsedTestNanoTime / (1000 * 1000 * 1000)) + " s)");
        System.out.println("    OGNL runTest() runs cumulative elapsed time: " + cumulativelRunTestElapsedNanoTime + " ns (" +
                (cumulativelRunTestElapsedNanoTime / (1000 * 1000)) + " ms or " +
                (cumulativelRunTestElapsedNanoTime / (1000 * 1000 * 1000)) + " s)");
    }

    @Test
    public void testPerformanceRealGenericSingleThread() throws Exception {
        final Method barMethod = ExampleStringClass.class.getMethod("bar", Object.class);
        runTest(ExampleStringClass.class, barMethod, 10000000, 1, new Class[] { String.class });
    }

    @Test
    public void testPerformanceFakeGenericSingleThread() throws Exception {
        final Method fooMethod = ExampleStringClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringClass.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });
    }

    @Test
    public void testPerformanceNonGenericSingleThread() throws Exception {
        final Method fooMethod = ExampleStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringSubclass.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });
    }

    @Test
    public void testPerformanceRealGenericMultipleThreads() throws Exception {
        final Method barMethod = ExampleStringClass.class.getMethod("bar", Object.class);
        runTest(ExampleStringClass.class, barMethod, 100000, 100, new Class[] { String.class });
    }

    @Test
    public void testPerformanceFakeGenericMultipleThreads() throws Exception {
        final Method fooMethod = ExampleStringClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringClass.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });
    }

    @Test
    public void testPerformanceNotGenericMultipleThreads() throws Exception {
        final Method fooMethod = ExampleStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringSubclass.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });
    }

    @Test
    public void testPerformanceMultipleClassesMultipleMethodsSingleThread() throws Exception {
        final long testStartNanoTime = System.nanoTime();

        Method barMethod = ExampleTwoMethodClass.class.getMethod("bar", String.class);
        Method fooMethod = ExampleTwoMethodClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass2.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass2.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass2.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass2.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass3.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass3.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass3.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass3.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass4.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass4.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass4.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass4.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass5.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass5.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass5.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass5.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass6.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass6.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass6.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass6.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass7.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass7.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass7.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass7.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass8.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass8.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass8.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass8.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass9.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass9.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass9.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass9.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass10.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass10.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass10.class, barMethod, 10000000, 1, new Class[] { String.class });
        runTest(ExampleTwoMethodClass10.class, fooMethod, 10000000, 1, new Class[] { Integer.class, Date.class });

        final long testEndNanoTime = System.nanoTime();
        final long elapsedTestNanoTime = testEndNanoTime - testStartNanoTime;
        cumulativelRunTestElapsedNanoTime = cumulativelRunTestElapsedNanoTime + elapsedTestNanoTime;
        System.out.println("    OGNL testPerformanceMultipleClassesMultipleMethodsSingleThread() elapsed time: " + elapsedTestNanoTime + " ns (" +
                (elapsedTestNanoTime / (1000 * 1000)) + " ms or " +
                (elapsedTestNanoTime / (1000 * 1000 * 1000)) + " s)");
        if (totalNumberOfRunTestRuns > 0) {
            System.out.println("  OGNL testPerformanceMultipleClassesMultipleMethodsSingleThread() - runTest() average run time (so far over " + totalNumberOfRunTestRuns +
                    " runs): " + cumulativelRunTestElapsedNanoTime / totalNumberOfRunTestRuns + " ns (" +
                    (cumulativelRunTestElapsedNanoTime / (1000 * 1000)) / totalNumberOfRunTestRuns + " ms or " +
                    (cumulativelRunTestElapsedNanoTime / (1000 * 1000 * 1000)) / totalNumberOfRunTestRuns + " s)");
        }
    }

    @Test
    public void testPerformanceMultipleClassesMultipleMethodsMultipleThreads() throws Exception {
        final long testStartNanoTime = System.nanoTime();

        Method barMethod = ExampleTwoMethodClass.class.getMethod("bar", String.class);
        Method fooMethod = ExampleTwoMethodClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass2.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass2.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass2.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass2.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass3.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass3.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass3.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass3.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass4.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass4.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass4.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass4.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass5.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass5.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass5.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass5.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass6.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass6.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass6.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass6.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass7.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass7.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass7.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass7.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass8.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass8.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass8.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass8.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass9.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass9.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass9.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass9.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        barMethod = ExampleTwoMethodClass10.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass10.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass10.class, barMethod, 100000, 100, new Class[] { String.class });
        runTest(ExampleTwoMethodClass10.class, fooMethod, 100000, 100, new Class[] { Integer.class, Date.class });

        final long testEndNanoTime = System.nanoTime();
        final long elapsedTestNanoTime = testEndNanoTime - testStartNanoTime;
        cumulativelRunTestElapsedNanoTime = cumulativelRunTestElapsedNanoTime + elapsedTestNanoTime;
        System.out.println("    OGNL testPerformanceMultipleClassesMultipleMethodsMultipleThreads() elapsed time: " + elapsedTestNanoTime + " ns (" +
                (elapsedTestNanoTime / (1000 * 1000)) + " ms or " +
                (elapsedTestNanoTime / (1000 * 1000 * 1000)) + " s)");
        if (totalNumberOfRunTestRuns > 0) {
            System.out.println("  OGNL testPerformanceMultipleClassesMultipleMethodsMultipleThreads() - runTest() average run time (so far over " + totalNumberOfRunTestRuns +
                    " runs): " + cumulativelRunTestElapsedNanoTime / totalNumberOfRunTestRuns + " ns (" +
                    (cumulativelRunTestElapsedNanoTime / (1000 * 1000)) / totalNumberOfRunTestRuns + " ms or " +
                    (cumulativelRunTestElapsedNanoTime / (1000 * 1000 * 1000)) / totalNumberOfRunTestRuns + " s)");
        }
    }

    /**
     * Test OgnlRuntime version parsing mechanism.
     */
    @Test
    public void testMajorJavaVersionParse() {
        // Pre-JDK 9 version strings.
        Assert.assertEquals("JDK 5 version check failed ?", 5, OgnlRuntime.parseMajorJavaVersion("1.5"));
        Assert.assertEquals("JDK 5 version check failed ?", 5, OgnlRuntime.parseMajorJavaVersion("1.5.0"));
        Assert.assertEquals("JDK 5 version check failed ?", 5, OgnlRuntime.parseMajorJavaVersion("1.5.0_21-b11"));
        Assert.assertEquals("JDK 6 version check failed ?", 6, OgnlRuntime.parseMajorJavaVersion("1.6"));
        Assert.assertEquals("JDK 6 version check failed ?", 6, OgnlRuntime.parseMajorJavaVersion("1.6.0"));
        Assert.assertEquals("JDK 6 version check failed ?", 6, OgnlRuntime.parseMajorJavaVersion("1.6.0_43-b19"));
        Assert.assertEquals("JDK 7 version check failed ?", 7, OgnlRuntime.parseMajorJavaVersion("1.7"));
        Assert.assertEquals("JDK 7 version check failed ?", 7, OgnlRuntime.parseMajorJavaVersion("1.7.0"));
        Assert.assertEquals("JDK 7 version check failed ?", 7, OgnlRuntime.parseMajorJavaVersion("1.7.0_79-b15"));
        Assert.assertEquals("JDK 8 version check failed ?", 8, OgnlRuntime.parseMajorJavaVersion("1.8"));
        Assert.assertEquals("JDK 8 version check failed ?", 8, OgnlRuntime.parseMajorJavaVersion("1.8.0"));
        Assert.assertEquals("JDK 8 version check failed ?", 8, OgnlRuntime.parseMajorJavaVersion("1.8.0_201-b20"));
        Assert.assertEquals("JDK 8 version check failed ?", 8, OgnlRuntime.parseMajorJavaVersion("1.8.0-someopenjdkstyle"));
        Assert.assertEquals("JDK 8 version check failed ?", 8, OgnlRuntime.parseMajorJavaVersion("1.8.0_201-someopenjdkstyle"));
        // JDK 9 and later version strings.
        Assert.assertEquals("JDK 9 version check failed ?", 9, OgnlRuntime.parseMajorJavaVersion("9"));
        Assert.assertEquals("JDK 9 version check failed ?", 9, OgnlRuntime.parseMajorJavaVersion("9-ea+19"));
        Assert.assertEquals("JDK 9 version check failed ?", 9, OgnlRuntime.parseMajorJavaVersion("9+100"));
        Assert.assertEquals("JDK 9 version check failed ?", 9, OgnlRuntime.parseMajorJavaVersion("9-ea+19"));
        Assert.assertEquals("JDK 9 version check failed ?", 9, OgnlRuntime.parseMajorJavaVersion("9.1.3+15"));
        Assert.assertEquals("JDK 9 version check failed ?", 9, OgnlRuntime.parseMajorJavaVersion("9-someopenjdkstyle"));
        Assert.assertEquals("JDK 10 version check failed ?", 10, OgnlRuntime.parseMajorJavaVersion("10"));
        Assert.assertEquals("JDK 10 version check failed ?", 10, OgnlRuntime.parseMajorJavaVersion("10-ea+11"));
        Assert.assertEquals("JDK 10 version check failed ?", 10, OgnlRuntime.parseMajorJavaVersion("10+10"));
        Assert.assertEquals("JDK 10 version check failed ?", 10, OgnlRuntime.parseMajorJavaVersion("10-ea+11"));
        Assert.assertEquals("JDK 10 version check failed ?", 10, OgnlRuntime.parseMajorJavaVersion("10.1.3+15"));
        Assert.assertEquals("JDK 10 version check failed ?", 10, OgnlRuntime.parseMajorJavaVersion("10-someopenjdkstyle"));
        Assert.assertEquals("JDK 11 version check failed ?", 11, OgnlRuntime.parseMajorJavaVersion("11"));
        Assert.assertEquals("JDK 11 version check failed ?", 11, OgnlRuntime.parseMajorJavaVersion("11-ea+22"));
        Assert.assertEquals("JDK 11 version check failed ?", 11, OgnlRuntime.parseMajorJavaVersion("11+33"));
        Assert.assertEquals("JDK 11 version check failed ?", 11, OgnlRuntime.parseMajorJavaVersion("11-ea+19"));
        Assert.assertEquals("JDK 11 version check failed ?", 11, OgnlRuntime.parseMajorJavaVersion("11.1.3+15"));
        Assert.assertEquals("JDK 11 version check failed ?", 11, OgnlRuntime.parseMajorJavaVersion("11-someopenjdkstyle"));
    }

    /**
     * Test OgnlRuntime Major Version Check mechanism.
     */
    @Test
    public void testMajorJavaVersionCheck() {
        // Ensure no exceptions, basic ouput for test report and sanity check on minimum version.
        final int majorJavaVersion = OgnlRuntime.detectMajorJavaVersion();
        System.out.println("Major Java Version detected: " + majorJavaVersion);
        Assert.assertTrue("Major Java Version Check returned value (" + majorJavaVersion + ") less than minimum (5) ?", majorJavaVersion >= 5);
    }

    /**
     * Test OgnlRuntime value for _useJDK9PlusAccessHandler based on the System property
     *   represented by {@link OgnlRuntime#USE_JDK9PLUS_ACESS_HANDLER}.
     */
    @Test
    public void testAccessHanderStateFlag() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        final boolean defaultValue = false;          // Expected non-configured default
        boolean optionDefinedInEnvironment = false;  // Track if option defined in environment
        boolean flagValueFromEnvironment = false;    // Value result from environment retrieval
        try {
            final String propertyString = System.getProperty(OgnlRuntime.USE_JDK9PLUS_ACESS_HANDLER);
            if (propertyString != null && propertyString.length() > 0) {
                optionDefinedInEnvironment = true;
                flagValueFromEnvironment = Boolean.parseBoolean(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        if (optionDefinedInEnvironment) {
            System.out.println("System property " + OgnlRuntime.USE_JDK9PLUS_ACESS_HANDLER + " value: " + flagValueFromEnvironment);
        } else {
            System.out.println("System property " + OgnlRuntime.USE_JDK9PLUS_ACESS_HANDLER + " not present.  Default value should be: " + defaultValue);
        }
        System.out.println("Current OGNL value for use JDK9+ Access Handler: " + OgnlRuntime.getUseJDK9PlusAccessHandlerValue());
        Assert.assertEquals("Mismatch between system property (or default) and OgnlRuntime _usJDK9PlusAccessHandler flag state ?",
                optionDefinedInEnvironment ? flagValueFromEnvironment : defaultValue, OgnlRuntime.getUseJDK9PlusAccessHandlerValue());
    }

    /**
     * Test OgnlRuntime value for _useStricterInvocation based on the System properties
     *   represented by {@link OgnlRuntime#USE_STRICTER_INVOCATION}.
     */
    @Test
    public void testUseStricterInvocationStateFlag() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        final boolean defaultValue = true;           // Expected non-configured default
        boolean optionDefinedInEnvironment = false;  // Track if option defined in environment
        boolean flagValueFromEnvironment = true;     // Expected non-configured default
        try {
            final String propertyString = System.getProperty(OgnlRuntime.USE_STRICTER_INVOCATION);
            if (propertyString != null && propertyString.length() > 0) {
                optionDefinedInEnvironment = true;
                flagValueFromEnvironment = Boolean.parseBoolean(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        if (optionDefinedInEnvironment) {
            System.out.println("System property " + OgnlRuntime.USE_STRICTER_INVOCATION + " value: " + flagValueFromEnvironment);
        } else {
            System.out.println("System property " + OgnlRuntime.USE_STRICTER_INVOCATION + " not present.  Default value should be: " + defaultValue);
        }
        System.out.println("Current OGNL value for use stricter invocation: " + OgnlRuntime.getUseStricterInvocationValue());
        Assert.assertEquals("Mismatch between system property (or default) and OgnlRuntime _useStricterInvocation flag state ?",
                optionDefinedInEnvironment ? flagValueFromEnvironment : defaultValue, OgnlRuntime.getUseStricterInvocationValue());
    }

    /**
     * Test OgnlRuntime stricter invocation mode.
     */
    @Test
    public void testStricterInvocationMode() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        // Note: If stricter invocation mode is disabled (due to a system property being set for
        //   the JVM running the test) this test will not fail, but just skip the test.
        if ( OgnlRuntime.getUseStricterInvocationValue()) {
            try {
                final Class<?>[] singleClassArgument = new Class<?>[1];
                singleClassArgument[0] = int.class;
                final Method exitMethod = System.class.getMethod("exit", singleClassArgument);
                try {
                    OgnlRuntime.invokeMethod(System.class, exitMethod, new Object[] { -1 });
                    Assert.fail("Somehow got past invocation of a restricted exit call (nonsensical result) ?");
                } catch (IllegalAccessException iae) {
                    // Expected failure (failed during invocation)
                    System.out.println("Stricter invocation mode blocked restricted call (as expected).  Exception: " + iae);
                } catch (SecurityException se) {
                    // Possible exception if test is run with an active security manager)
                    System.out.println("Stricter invocation mode blocked by security manager (may be valid).  Exception: " + se);
                }

                singleClassArgument[0] = String.class;
                final Method execMethod = Runtime.class.getMethod("exec", singleClassArgument);
                try {
                    OgnlRuntime.invokeMethod(Runtime.getRuntime(), execMethod, new Object[] { "fakeCommand" });
                    Assert.fail("Somehow got past invocation of a restricted exec call ?");
                } catch (IllegalAccessException iae) {
                    // Expected failure (failed during invocation)
                    System.out.println("Stricter invocation mode blocked restricted call (as expected).  Exception: " + iae);
                } catch (SecurityException se) {
                    // Possible exception if test is run with an active security manager)
                    System.out.println("Stricter invocation mode blocked by security manager (may be valid).  Exception: " + se);
                }
            } catch (Exception ex) {
                Assert.fail("Unable to fully test stricter invocation mode.  Exception: " + ex);
            }
        } else {
            System.out.println("Not testing stricter invocation mode (disabled via system property).");
        }
    }

    /**
     * Test OgnlRuntime value for _useFirstMatchGetSetLookup based on the System property
     *   represented by {@link OgnlRuntime#USE_FIRSTMATCH_GETSET_LOOKUP}.
     */
    @Test
    public void testUseFirstMatchGetSetStateFlag() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        final boolean defaultValue = false;          // Expected non-configured default
        boolean optionDefinedInEnvironment = false;  // Track if option defined in environment
        boolean flagValueFromEnvironment = false;    // Value result from environment retrieval
        try {
            final String propertyString = System.getProperty(OgnlRuntime.USE_FIRSTMATCH_GETSET_LOOKUP);
            if (propertyString != null && propertyString.length() > 0) {
                optionDefinedInEnvironment = true;
                flagValueFromEnvironment = Boolean.parseBoolean(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        if (optionDefinedInEnvironment) {
            System.out.println("System property " + OgnlRuntime.USE_FIRSTMATCH_GETSET_LOOKUP + " value: " + flagValueFromEnvironment);
        } else {
            System.out.println("System property " + OgnlRuntime.USE_FIRSTMATCH_GETSET_LOOKUP + " not present.  Default value should be: " + defaultValue);
        }
        System.out.println("Current OGNL value for Use First Match Get/Set State Flag: " + OgnlRuntime.getUseFirstMatchGetSetLookupValue());
        Assert.assertEquals("Mismatch between system property (or default) and OgnlRuntime _useFirstMatchGetSetLookup flag state ?",
                optionDefinedInEnvironment ? flagValueFromEnvironment : defaultValue, OgnlRuntime.getUseFirstMatchGetSetLookupValue());
    }

    private Map defaultContext = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

    @Test // Success
    public void testForArray() throws Exception {
        Bean bean = new Bean();
        Ognl.setValue("chars", defaultContext, bean, new Character[]{'%', '_'});
        Assert.assertThat(bean.chars.length, IsEqual.equalTo(2));
        Assert.assertThat(bean.chars[0], IsEqual.equalTo('%'));
        Assert.assertThat(bean.chars[1], IsEqual.equalTo('_'));
    }

    @Test // Fail
    public void testForVarArgs() throws Exception {
        Bean bean = new Bean();
        Ognl.setValue("strings", defaultContext, bean, new String[]{"%", "_"});
        Assert.assertThat(bean.strings.length, IsEqual.equalTo(2));
        Assert.assertThat(bean.strings[0], IsEqual.equalTo("%"));
        Assert.assertThat(bean.strings[1], IsEqual.equalTo("_"));
    }

    static class Bean {
        private Character[] chars;
        private Integer index;
        private String[] strings;

        public void setChars(Character[] chars) {
            this.chars = chars;
        }
        public Character[] getChars() {
            return chars;
        }
        public void setStrings(String... strings) {
            this.strings = strings;
        }
        public String[] getStrings() {
            return strings;
        }
        public void setMix(Integer index, String... strings) {
            this.index = index;
            this.strings = strings;
        }
        public Integer getIndex() {
            return index;
        }
    }

    @Test
    public void shouldInvokeSyntheticBridgeMethod() throws Exception {
        StringBuilder root = new StringBuilder("abc");
        Assert.assertEquals((int) 'b',
                Ognl.getValue("codePointAt(1)", defaultContext, root));
    }

    @Test
    public void shouldInvokeSuperclassMethod() throws Exception {
        Map<Long, Long> root = Collections.singletonMap(3L, 33L);
        Assert.assertTrue((Boolean) Ognl.getValue("containsKey(3L)",
                defaultContext, root));
    }

    @Test
    public void shouldInvokeInterfaceMethod() throws Exception {
        Assert.assertTrue((Boolean) Ognl.getValue("isEmpty()", defaultContext,
                Collections.checkedCollection(new ArrayList<>(), String.class)));
    }

    public interface I1 {
        Integer getId();
    }

    public interface I2 {
        Integer getId();
    }

    @Test
    public void shouldMultipleInterfaceWithTheSameMethodBeFine()
            throws Exception {
        class C1 implements I1, I2 {
            public Integer getId() {
                return 100;
            }
        }
        Assert.assertEquals(100,
                Ognl.getValue("getId()", defaultContext, new C1()));
    }

    public interface I3<T> {
        T get();
    }

    @Test
    public void shouldTwoMethodsWithDifferentReturnTypeBeFine()
            throws Exception {
        class C1 implements I3<Long> {
            @Override
            public Long get() {
                return 3L;
            }
        }
        Assert.assertEquals(3L,
                Ognl.getValue("get()", defaultContext, new C1()));
    }
}
