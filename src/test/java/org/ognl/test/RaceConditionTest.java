package org.ognl.test;

import org.ognl.OgnlRuntime;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Johno Crawford (johno@sulake.com)
 */
public class RaceConditionTest {

    @Test
    public void testRaceCondition() throws Exception {
        runTest(TestAction.class, 1000, 10, Boolean.TRUE);
    }

    private static void runTest(Class<?> clazz, int invocationCount, int threadCount, Boolean expected) throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(threadCount);
        for (int i = threadCount; i > 0; i--) {
            futures.add(executor.submit(new Worker(clazz, invocationCount)));
        }
        for (final Future<Boolean> future : futures) {
            Assert.assertEquals(expected, future.get());
        }
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    private static class TestAction {

        public String execute() throws Exception {
            return "success";
        }

    }

    private static class Worker implements Callable<Boolean> {

        private final Class<?> clazz;
        private final int invocationCount;

        public Worker(final Class<?> clazz, final int invocationCount) throws Exception {
            this.clazz = clazz;
            this.invocationCount = invocationCount;
        }

        public Boolean call() throws Exception {
            for (int i = this.invocationCount; i > 0; i--) {
                Method method = OgnlRuntime.getMethod(null, clazz, "execute", null, false);
                if (method == null) {
                    return false;
                }
            }
            return true;
        }
    }

}
