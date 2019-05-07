package ognl;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

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


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
        final Method addMethodToDenyList = OgnlMethodBlocker.class.getMethod("addMethodToDenyList", new Class<?>[] {Method.class});
        final DenyListStringSubclass denyListStringSubclass = new DenyListStringSubclass();

        // Initial invocation should not fail
        OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { Integer.valueOf(0), new Date() });

        // Verify calling addMethodToDenyList within OGNL itself is rejected (for any of 3 reasons)
        try {
            OgnlRuntime.invokeMethod(OgnlMethodBlocker.class, addMethodToDenyList, new Object[] { addMethodToDenyList });
            throw new Exception("OgnlRuntimeMethodBlocking add to deny list callable within OgnlRuntime ?");
        } catch (InvocationTargetException ite) {
            // Expected failure (failed during invocation)
        } catch (IllegalStateException ise) {
            // Expected failure (failed during addMethodToDenyList call)
        } catch (IllegalArgumentException iae) {
            // Expected failure (failed during addMethodToDenyList call)
        }

        // Add method to deny list, subsequent invocations should fail
        OgnlMethodBlocker.addMethodToDenyList(fooMethod);
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { Integer.valueOf(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't block foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
        // Second attempt should still fail
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { Integer.valueOf(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't block foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call to gc method before standard deny list
        OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);

        // Attempt call to gc method after standard deny list
        OgnlMethodBlocker.prepareStandardMethodDenyList();
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't block gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call both prepare deny list methods directly (should not cause errors to call them again)
        OgnlMethodBlocker.prepareMinimalMethodDenyList();
        OgnlMethodBlocker.prepareStandardMethodDenyList();
        // Attempt to call now-deny-listed method again
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't block gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
    }

    /**
     * Ensure adding the standard methods to the deny list doesn't generate exceptions.
     * Also tests a basic deny scenario, after applying elements to the deny list.
     * All tests run with OgnlSecurityManager enabled.
     * 
     * Note: Depends on @FixMethodOrder(MethodSorters.NAME_ASCENDING) combined with
     *   prefix testZLastTest to ensure it is the last test called.  This should ensure
     *   the SecurityManager doesn't impact other tests and testDenyListProcessing() will
     *   already have been called, installing the deny list.
     * 
     * @throws Exception 
     */
    @Test
    public void testZLastTestSecurityManagerAndDenyListProcessing() throws Exception {
        final Method fooMethod = DenyListStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        final Method gcMethod = System.class.getMethod("gc", new Class<?>[0]);
        final Method addMethodToDenyList = OgnlMethodBlocker.class.getMethod("addMethodToDenyList", new Class<?>[] {Method.class});
        final Method installOgnlSecurityManager = OgnlSecurityManager.class.getMethod("installOgnlSecurityManager", new Class<?>[]{SecurityManager.class, boolean.class});
        final DenyListStringSubclass denyListStringSubclass = new DenyListStringSubclass();

        // Due to ascending name test ordering, the deny listhould already be installed.

        // Ensure OgnlSecurityManager installation cannot be called via invokeMethod(), even before
        //   the OgnlSecurityManager is installed
        try {
            OgnlRuntime.invokeMethod(OgnlSecurityManager.class, installOgnlSecurityManager, new Object[] { null, false });
            throw new Exception("OGNL Security Manager install callable within OgnlRuntime invokeMethod() ?");
        } catch (InvocationTargetException ite) {
            // Expected failure (failed during invocation)
        } catch (IllegalAccessException iae) {
            // Expected failure (failed during invokeMethod() call)
        }

        assertFalse("OGNL Security Manager already installed ?", OgnlSecurityManager.isOgnlSecurityManagerInstalled());
        // Note: Once installed (in whatever order the tests are called) the Security Manager will remain in place.
        assertFalse("OGNL Security Manager JVM option install succeeded (not disabled by default) ?", OgnlSecurityManager.installOgnlSecurityManagerViaJVMOption());
        assertTrue("Unable to install OGNL Security manager ?", OgnlSecurityManager.installOgnlSecurityManager(false));
        assertTrue("OGNL Security Manager not installed ?", OgnlSecurityManager.isOgnlSecurityManagerInstalled());

        try {
            assertFalse("Able to re-install OGNL Security manager ?", OgnlSecurityManager.installOgnlSecurityManager(false));
        } catch (SecurityException se) {
            // Expected failure
        }

        // Ensure OgnlSecurityManager installation cannot be called via invokeMethod(),
        //   after the OgnlSecurityManager is installed
        try {
            OgnlRuntime.invokeMethod(OgnlSecurityManager.class, installOgnlSecurityManager, new Object[] { null });
            throw new Exception("OgnlSecurityManager install callable within OgnlRuntime invokeMethod() ?");
        } catch (InvocationTargetException ite) {
            // Expected failure (failed during invocation)
        } catch (IllegalAccessException iae) {
            // Expected failure (failed during invokeMethod() call)
        }

        // Verify calling addMethodToDenyList within OGNL itself is rejected (for any of 3 reasons)
        try {
            OgnlRuntime.invokeMethod(OgnlMethodBlocker.class, addMethodToDenyList, new Object[] { addMethodToDenyList });
            throw new Exception("OgnlRuntimeMethodBlocking add to deny list callable within OgnlRuntime invokeMethod() ?");
        } catch (InvocationTargetException ite) {
            // Expected failure (failed during invocation)
        } catch (IllegalStateException ise) {
            // Expected failure (failed during addMethodToDenyList call)
        } catch (IllegalArgumentException iae) {
            // Expected failure (failed during addMethodToDenyList call)
        }

        // Add method to deny list, subsequent invocations should fail
        OgnlMethodBlocker.addMethodToDenyList(fooMethod);
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { Integer.valueOf(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't block foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
        // Second attempt should still fail
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { Integer.valueOf(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't block foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call to gc method after standard deny list
        OgnlMethodBlocker.prepareStandardMethodDenyList();
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't block gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call both prepare deny list methods directly (should not cause errors to call them again)
        OgnlMethodBlocker.prepareMinimalMethodDenyList();
        OgnlMethodBlocker.prepareStandardMethodDenyList();
        // Attempt to call now-deny-listed method again
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't block gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        assertFalse("OGNL Security Manager is enabled for current thread ?", OgnlSecurityManager.isEnabledForCurrentThread());
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            assertNotNull("Clipboard is null?", clipboard);
        } catch (HeadlessException he) {
            // Acceptable failure in any state with OGNL Security Manager disabled
        }
        assertTrue("Unable to enable OGNL Security Manager for current thread ?", OgnlSecurityManager.enableForCurrentThread());
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            throw new IllegalStateException("Should not be able to access clipboard while OGNL Security Manager active");
        } catch (SecurityException se) {
            // Epxected failure with OGNL Security Manager active
        } catch (HeadlessException he) {
            // Acceptable failure in any state with OGNL Security Manager enabled on a "headless" system
        }
        // Test some various calls while enabled
        testPerformanceMultipleClassesMultipleMethodsSingleThread();
        testPerformanceMultipleClassesMultipleMethodsMultipleThreads();
        // Test call depth processing
        assertTrue("Unable to increment call depth ?", OgnlSecurityManager.incrementInvocationDepthForCurrentThread() == 1);
        assertFalse("Able to disable while still in use (according to call depth) ?", OgnlSecurityManager.disableForCurrentThread());
        assertTrue("Unable to decrement call depth ?", OgnlSecurityManager.decrementInvocationDepthForCurrentThread() == 0);
        assertTrue("Unable to disable when not in use (according to call depth) ?", OgnlSecurityManager.disableForCurrentThread());
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            assertNotNull("Clipboard is null?", clipboard);
        } catch (HeadlessException he) {
            // Acceptable failure in any state with OGNL Security Manager disabled
        }

        // Confirm OGNL Security Manager still installed after all processing
        assertTrue("OGNL Security Manager not installed ?", OgnlSecurityManager.isOgnlSecurityManagerInstalled());
    }

}