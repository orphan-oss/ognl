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
package ognl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class GenericClass<T> {
    @SuppressWarnings("unused")
    public void bar(final T parameter) {
    }
}

class ExampleStringClass extends GenericClass<String> {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }
}

class ExampleStringSubclass extends ExampleStringClass {
}

class ExampleTwoMethodClass {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass2 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass3 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass4 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass5 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass6 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass7 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass8 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass9 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}

class ExampleTwoMethodClass10 {
    @SuppressWarnings("unused")
    public void foo(final Integer parameter1, final Date parameter2) {
    }

    @SuppressWarnings("unused")
    public void bar(final String parameter2) {
    }
}


public class OgnlRuntimeTest {

    private static long cumulativelRunTestElapsedNanoTime;
    private static long totalNumberOfRunTestRuns;

    static class Worker implements Callable<Class<?>[]> {

        private final Class<?> clazz;
        private final Method method;
        private final int invocationCount;

        public Worker(final Class<?> clazz, final Method method, final int invocationCount) {
            this.clazz = clazz;
            this.method = method;
            this.invocationCount = invocationCount;
        }

        public Class<?>[] call() {
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
        final List<Future<Class<?>[]>> futures = new ArrayList<>(threadCount);
        totalNumberOfRunTestRuns++;
        final long testStartNanoTime = System.nanoTime();
        for (int i = threadCount; i > 0; i--) {
            futures.add(executor.submit(new Worker(clazz, method, invocationCount)));
        }
        for (final Future<Class<?>[]> future : futures) {
            assertArrayEquals(expected, future.get());
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
        runTest(ExampleStringClass.class, barMethod, 10000000, 1, new Class[]{String.class});
    }

    @Test
    public void testPerformanceFakeGenericSingleThread() throws Exception {
        final Method fooMethod = ExampleStringClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringClass.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});
    }

    @Test
    public void testPerformanceNonGenericSingleThread() throws Exception {
        final Method fooMethod = ExampleStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringSubclass.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});
    }

    @Test
    public void testPerformanceRealGenericMultipleThreads() throws Exception {
        final Method barMethod = ExampleStringClass.class.getMethod("bar", Object.class);
        runTest(ExampleStringClass.class, barMethod, 100000, 100, new Class[]{String.class});
    }

    @Test
    public void testPerformanceFakeGenericMultipleThreads() throws Exception {
        final Method fooMethod = ExampleStringClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringClass.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});
    }

    @Test
    public void testPerformanceNotGenericMultipleThreads() throws Exception {
        final Method fooMethod = ExampleStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringSubclass.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});
    }

    @Test
    public void testPerformanceMultipleClassesMultipleMethodsSingleThread() throws Exception {
        final long testStartNanoTime = System.nanoTime();

        Method barMethod = ExampleTwoMethodClass.class.getMethod("bar", String.class);
        Method fooMethod = ExampleTwoMethodClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass2.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass2.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass2.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass2.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass3.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass3.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass3.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass3.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass4.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass4.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass4.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass4.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass5.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass5.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass5.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass5.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass6.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass6.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass6.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass6.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass7.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass7.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass7.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass7.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass8.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass8.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass8.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass8.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass9.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass9.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass9.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass9.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass10.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass10.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass10.class, barMethod, 10000000, 1, new Class[]{String.class});
        runTest(ExampleTwoMethodClass10.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class});

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
        runTest(ExampleTwoMethodClass.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass2.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass2.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass2.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass2.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass3.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass3.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass3.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass3.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass4.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass4.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass4.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass4.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass5.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass5.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass5.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass5.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass6.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass6.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass6.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass6.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass7.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass7.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass7.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass7.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass8.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass8.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass8.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass8.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass9.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass9.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass9.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass9.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

        barMethod = ExampleTwoMethodClass10.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass10.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass10.class, barMethod, 100000, 100, new Class[]{String.class});
        runTest(ExampleTwoMethodClass10.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class});

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
        assertEquals(5, OgnlRuntime.parseMajorJavaVersion("1.5"), "JDK 5 version check failed ?");
        assertEquals(5, OgnlRuntime.parseMajorJavaVersion("1.5.0"), "JDK 5 version check failed ?");
        assertEquals(5, OgnlRuntime.parseMajorJavaVersion("1.5.0_21-b11"), "JDK 5 version check failed ?");
        assertEquals(6, OgnlRuntime.parseMajorJavaVersion("1.6"), "JDK 6 version check failed ?");
        assertEquals(6, OgnlRuntime.parseMajorJavaVersion("1.6.0"), "JDK 6 version check failed ?");
        assertEquals(6, OgnlRuntime.parseMajorJavaVersion("1.6.0_43-b19"), "JDK 6 version check failed ?");
        assertEquals(7, OgnlRuntime.parseMajorJavaVersion("1.7"), "JDK 7 version check failed ?");
        assertEquals(7, OgnlRuntime.parseMajorJavaVersion("1.7.0"), "JDK 7 version check failed ?");
        assertEquals(7, OgnlRuntime.parseMajorJavaVersion("1.7.0_79-b15"), "JDK 7 version check failed ?");
        assertEquals(8, OgnlRuntime.parseMajorJavaVersion("1.8"), "JDK 8 version check failed ?");
        assertEquals(8, OgnlRuntime.parseMajorJavaVersion("1.8.0"), "JDK 8 version check failed ?");
        assertEquals(8, OgnlRuntime.parseMajorJavaVersion("1.8.0_201-b20"), "JDK 8 version check failed ?");
        assertEquals(8, OgnlRuntime.parseMajorJavaVersion("1.8.0-someopenjdkstyle"), "JDK 8 version check failed ?");
        assertEquals(8, OgnlRuntime.parseMajorJavaVersion("1.8.0_201-someopenjdkstyle"), "JDK 8 version check failed ?");
        // JDK 9 and later version strings.
        assertEquals(9, OgnlRuntime.parseMajorJavaVersion("9"), "JDK 9 version check failed ?");
        assertEquals(9, OgnlRuntime.parseMajorJavaVersion("9-ea+19"), "JDK 9 version check failed ?");
        assertEquals(9, OgnlRuntime.parseMajorJavaVersion("9+100"), "JDK 9 version check failed ?");
        assertEquals(9, OgnlRuntime.parseMajorJavaVersion("9-ea+19"), "JDK 9 version check failed ?");
        assertEquals(9, OgnlRuntime.parseMajorJavaVersion("9.1.3+15"), "JDK 9 version check failed ?");
        assertEquals(9, OgnlRuntime.parseMajorJavaVersion("9-someopenjdkstyle"), "JDK 9 version check failed ?");
        assertEquals(10, OgnlRuntime.parseMajorJavaVersion("10"), "JDK 10 version check failed ?");
        assertEquals(10, OgnlRuntime.parseMajorJavaVersion("10-ea+11"), "JDK 10 version check failed ?");
        assertEquals(10, OgnlRuntime.parseMajorJavaVersion("10+10"), "JDK 10 version check failed ?");
        assertEquals(10, OgnlRuntime.parseMajorJavaVersion("10-ea+11"), "JDK 10 version check failed ?");
        assertEquals(10, OgnlRuntime.parseMajorJavaVersion("10.1.3+15"), "JDK 10 version check failed ?");
        assertEquals(10, OgnlRuntime.parseMajorJavaVersion("10-someopenjdkstyle"), "JDK 10 version check failed ?");
        assertEquals(11, OgnlRuntime.parseMajorJavaVersion("11"), "JDK 11 version check failed ?");
        assertEquals(11, OgnlRuntime.parseMajorJavaVersion("11-ea+22"), "JDK 11 version check failed ?");
        assertEquals(11, OgnlRuntime.parseMajorJavaVersion("11+33"), "JDK 11 version check failed ?");
        assertEquals(11, OgnlRuntime.parseMajorJavaVersion("11-ea+19"), "JDK 11 version check failed ?");
        assertEquals(11, OgnlRuntime.parseMajorJavaVersion("11.1.3+15"), "JDK 11 version check failed ?");
        assertEquals(11, OgnlRuntime.parseMajorJavaVersion("11-someopenjdkstyle"), "JDK 11 version check failed ?");
    }

    /**
     * Test OgnlRuntime Major Version Check mechanism.
     */
    @Test
    public void testMajorJavaVersionCheck() {
        // Ensure no exceptions, basic ouput for test report and sanity check on minimum version.
        final int majorJavaVersion = OgnlRuntime.detectMajorJavaVersion();
        System.out.println("Major Java Version detected: " + majorJavaVersion);
        assertTrue(majorJavaVersion >= 5, "Major Java Version Check returned value (" + majorJavaVersion + ") less than minimum (5) ?");
    }

    /**
     * Test OgnlRuntime value for _useStricterInvocation based on the System properties
     * represented by {@link OgnlRuntime#USE_STRICTER_INVOCATION}.
     */
    @Test
    public void testUseStricterInvocationStateFlag() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        final boolean defaultValue = true;           // Expected non-configured default
        boolean optionDefinedInEnvironment = false;  // Track if option defined in environment
        boolean flagValueFromEnvironment = true;     // Expected non-configured default
        try {
            final String propertyString = System.getProperty(OgnlRuntime.USE_STRICTER_INVOCATION);
            if (propertyString != null && !propertyString.isEmpty()) {
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
        assertEquals(optionDefinedInEnvironment ? flagValueFromEnvironment : defaultValue, OgnlRuntime.getUseStricterInvocationValue(),
                "Mismatch between system property (or default) and OgnlRuntime _useStricterInvocation flag state ?");
    }

    /**
     * Test OgnlRuntime stricter invocation mode.
     */
    @Test
    public void testStricterInvocationMode() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        // Note: If stricter invocation mode is disabled (due to a system property being set for
        //   the JVM running the test) this test will not fail, but just skip the test.
        if (OgnlRuntime.getUseStricterInvocationValue()) {
            try {
                final Class<?>[] singleClassArgument = new Class<?>[1];
                singleClassArgument[0] = int.class;
                final Method exitMethod = System.class.getMethod("exit", singleClassArgument);
                try {
                    OgnlRuntime.invokeMethod(System.class, exitMethod, new Object[]{-1});
                    fail("Somehow got past invocation of a restricted exit call (nonsensical result) ?");
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
                    OgnlRuntime.invokeMethod(Runtime.getRuntime(), execMethod, new Object[]{"fakeCommand"});
                    fail("Somehow got past invocation of a restricted exec call ?");
                } catch (IllegalAccessException iae) {
                    // Expected failure (failed during invocation)
                    System.out.println("Stricter invocation mode blocked restricted call (as expected).  Exception: " + iae);
                } catch (SecurityException se) {
                    // Possible exception if test is run with an active security manager)
                    System.out.println("Stricter invocation mode blocked by security manager (may be valid).  Exception: " + se);
                }
            } catch (Exception ex) {
                fail("Unable to fully test stricter invocation mode.  Exception: " + ex);
            }
        } else {
            System.out.println("Not testing stricter invocation mode (disabled via system property).");
        }
    }

    /**
     * Test OgnlRuntime value for _useFirstMatchGetSetLookup based on the System property
     * represented by {@link OgnlRuntime#USE_FIRSTMATCH_GETSET_LOOKUP}.
     */
    @Test
    public void testUseFirstMatchGetSetStateFlag() {
        // Ensure no exceptions, basic ouput for test report and sanity check on flag state.
        final boolean defaultValue = false;          // Expected non-configured default
        boolean optionDefinedInEnvironment = false;  // Track if option defined in environment
        boolean flagValueFromEnvironment = false;    // Value result from environment retrieval
        try {
            final String propertyString = System.getProperty(OgnlRuntime.USE_FIRSTMATCH_GETSET_LOOKUP);
            if (propertyString != null && !propertyString.isEmpty()) {
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
        assertEquals(optionDefinedInEnvironment ? flagValueFromEnvironment : defaultValue, OgnlRuntime.getUseFirstMatchGetSetLookupValue(),
                "Mismatch between system property (or default) and OgnlRuntime _useFirstMatchGetSetLookup flag state ?");
    }

    private final OgnlContext defaultContext = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

    @Test // Success
    public void testForArray() throws Exception {
        Bean bean = new Bean();
        Ognl.setValue("chars", defaultContext, bean, new Character[]{'%', '_'});
        assertEquals(2, bean.chars.length);
        assertEquals('%', bean.chars[0]);
        assertEquals('_', bean.chars[1]);
    }

    @Test // Fail
    public void testForVarArgs() throws Exception {
        Bean bean = new Bean();
        Ognl.setValue("strings", defaultContext, bean, new String[]{"%", "_"});
        assertEquals(2, bean.strings.length);
        assertEquals("%", bean.strings[0]);
        assertEquals("_", bean.strings[1]);
    }

    static class Bean {
        private Character[] chars;
        private Integer index;
        private String[] strings;

        @SuppressWarnings("unused")
        public void setChars(Character[] chars) {
            this.chars = chars;
        }

        @SuppressWarnings("unused")
        public Character[] getChars() {
            return chars;
        }

        @SuppressWarnings("unused")
        public void setStrings(String... strings) {
            this.strings = strings;
        }

        @SuppressWarnings("unused")
        public String[] getStrings() {
            return strings;
        }

        @SuppressWarnings("unused")
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
        assertEquals((int) 'b', Ognl.getValue("codePointAt(1)", defaultContext, root));
    }

    @Test
    public void shouldInvokeSuperclassMethod() throws Exception {
        Map<Long, Long> root = Collections.singletonMap(3L, 33L);
        assertTrue((Boolean) Ognl.getValue("containsKey(3L)", defaultContext, root));
    }

    @Test
    public void shouldInvokeInterfaceMethod() throws Exception {
        assertTrue((Boolean) Ognl.getValue("isEmpty()", defaultContext, Collections.checkedCollection(new ArrayList<>(), String.class)));
    }

    public interface I1 {
        @SuppressWarnings("unused")
        Integer getId();
    }

    public interface I2 {
        @SuppressWarnings("unused")
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
        assertEquals(100, Ognl.getValue("getId()", defaultContext, new C1()));
    }

    public interface I3<T> {
        T get();
    }

    public interface I4 {
        Long get();
    }

    @Test
    public void shouldTwoMethodsWithDifferentReturnTypeBeFine()
            throws Exception {
        class C1 implements I3<Long>, I4 {
            @Override
            public Long get() {
                return 3L;
            }
        }
        assertEquals(3L, Ognl.getValue("get()", defaultContext, new C1()));
    }

    @Test
    public void shouldSameMethodOfDifferentParentsBeCallable() throws Exception {
        Map<String, Object> root = new HashMap<>();
        root.put("d1", java.sql.Date.valueOf("2022-01-01"));
        root.put("d2", java.sql.Date.valueOf("2022-01-02"));
        defaultContext.setRoot(root);
        assertEquals(-1, Ognl.getValue("d1.compareTo(d2)", defaultContext, root));
    }

}
