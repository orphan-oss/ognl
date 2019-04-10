package ognl;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

class DenyListStringSubclass extends ExampleStringClass {
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
     * Ensure adding the standard methods to the deny list doesn't generate exceptions.
     * Also tests a basic deny scenario, after applying elements to the deny list.
     * 
     * @throws Exception 
     */
    @Test
    public void testDenyListProcessing() throws Exception {
        final Method fooMethod = DenyListStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        final Method gcMethod = System.class.getMethod("gc", new Class<?>[0]);
        final DenyListStringSubclass denyListStringSubclass = new DenyListStringSubclass();

        // Initial invocation should not fail
        OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { new Integer(0), new Date() });

        // Verify invalid specialized processing parameter set is rejected
        try {
            OgnlRuntime.setSpecializedOgnlRuntime(null);
            throw new IllegalStateException("OgnlRuntime set specialized runtime accepted null parameter ?");
        } catch (IllegalArgumentException iae) {
            // Expected failure
        }

        // Enable deny list processing
        OgnlRuntime.setSpecializedOgnlRuntime(OgnlRuntimeMethodBlocking.getInstance());

        // Verify specialized processing parameter set is rejected once it is already set
        try {
            OgnlRuntime.setSpecializedOgnlRuntime(OgnlRuntimeMethodBlocking.getInstance());
            throw new Exception("OgnlRuntime set specialized runtime allowed multiple set operations ?");
        } catch (IllegalStateException ise) {
            // Expected failure
        }

        // Verify specialized processing parameter set (with null parameter) is rejected once it is already set
        try {
            OgnlRuntime.setSpecializedOgnlRuntime(null);
            throw new Exception("OgnlRuntime set specialized runtime allowed multiple set operations ?");
        } catch (IllegalStateException ise) {
            // Expected failure
        }

        // Add method to deny list, subsequent invocations should fail
        OgnlRuntimeMethodBlocking.addMethodToDenyList(fooMethod);
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { new Integer(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't block foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
        // Second attempt should still fail
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { new Integer(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't block foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call to gc method before standard deny list
        OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);

        // Attempt call to gc method after standard deny list
        OgnlRuntimeMethodBlocking.prepareStandardMethodDenyList();
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't block gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call both prepare deny list methods directly (should not cause errors to call them again)
        OgnlRuntimeMethodBlocking.prepareMinimalMethodDenyList();
        OgnlRuntimeMethodBlocking.prepareStandardMethodDenyList();
        // Attempt to call now-deny-listed method again
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't block gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
    }

}