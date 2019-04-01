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

public class OgnlRuntimeTest {

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
        for (int i = threadCount; i > 0; i--) {
            futures.add(executor.submit(new Worker(clazz, method, invocationCount)));
        }
        for (final Future<Class<?>[]> future : futures) {
            Assert.assertArrayEquals(future.get(), expected);
        }
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

    /**
     * Ensure adding the standard methods to the deny list doesn't generate exceptions.
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

        // Add method to deny list, subsequent invocations should fail
        OgnlRuntime.addMethodToDenyList(fooMethod);
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { new Integer(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't blook foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
        // Second attempt should still fail
        try {
            OgnlRuntime.invokeMethod(denyListStringSubclass, fooMethod, new Object[] { new Integer(0), new Date() });
            throw new IllegalStateException("OgnlRuntime deny list didn't blook foo method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call to gc method before standard deny list
        OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);

        // Attempt call to gc method after standard deny list
        OgnlRuntime.prepareStandardMethodDenyList();
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't blook gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }

        // Attempt call both prepare deny list methods directly (should not cause errors to call them again)
        OgnlRuntime.prepareMinimalMethodDenyList();
        OgnlRuntime.prepareStandardMethodDenyList();
        // Attempt to call now-deny listed method again
        try {
            OgnlRuntime.invokeMethod(System.class, gcMethod, new Object[0]);
            throw new IllegalStateException("OgnlRuntime deny list didn't blook gc method ?");
        } catch (IllegalAccessException iae) {
            // Expected invocation failure
        }
    }

}