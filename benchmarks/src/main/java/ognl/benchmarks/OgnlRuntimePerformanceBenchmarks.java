package ognl.benchmarks;

import ognl.OgnlRuntime;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1, warmups = 1, jvmArgs = {
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class OgnlRuntimePerformanceBenchmarks {

    private static class Worker implements Callable<Class<?>[]> {
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
                        final Class<?>[] expected, Blackhole blackhole) throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final List<Future<Class<?>[]>> futures = new ArrayList<>(threadCount);

        for (int i = threadCount; i > 0; i--) {
            futures.add(executor.submit(new Worker(clazz, method, invocationCount)));
        }

        for (final Future<Class<?>[]> future : futures) {
            blackhole.consume(future.get());
        }

        executor.shutdown();
    }

    @Benchmark
    public void testPerformanceRealGenericSingleThread(Blackhole blackhole) throws Exception {
        final Method barMethod = ExampleStringClass.class.getMethod("bar", Object.class);
        runTest(ExampleStringClass.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceFakeGenericSingleThread(Blackhole blackhole) throws Exception {
        final Method fooMethod = ExampleStringClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringClass.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceNonGenericSingleThread(Blackhole blackhole) throws Exception {
        final Method fooMethod = ExampleStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringSubclass.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceRealGenericMultipleThreads(Blackhole blackhole) throws Exception {
        final Method barMethod = ExampleStringClass.class.getMethod("bar", Object.class);
        runTest(ExampleStringClass.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceFakeGenericMultipleThreads(Blackhole blackhole) throws Exception {
        final Method fooMethod = ExampleStringClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringClass.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceNotGenericMultipleThreads(Blackhole blackhole) throws Exception {
        final Method fooMethod = ExampleStringSubclass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleStringSubclass.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceMultipleClassesMultipleMethodsSingleThread(Blackhole blackhole) throws Exception {
        Method barMethod = ExampleTwoMethodClass.class.getMethod("bar", String.class);
        Method fooMethod = ExampleTwoMethodClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass2.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass2.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass2.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass2.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass3.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass3.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass3.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass3.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass4.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass4.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass4.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass4.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass5.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass5.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass5.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass5.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass6.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass6.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass6.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass6.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass7.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass7.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass7.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass7.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass8.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass8.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass8.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass8.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass9.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass9.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass9.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass9.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass10.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass10.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass10.class, barMethod, 10000000, 1, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass10.class, fooMethod, 10000000, 1, new Class[]{Integer.class, Date.class}, blackhole);
    }

    @Benchmark
    public void testPerformanceMultipleClassesMultipleMethodsMultipleThreads(Blackhole blackhole) throws Exception {
        Method barMethod = ExampleTwoMethodClass.class.getMethod("bar", String.class);
        Method fooMethod = ExampleTwoMethodClass.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass2.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass2.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass2.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass2.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass3.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass3.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass3.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass3.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass4.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass4.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass4.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass4.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass5.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass5.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass5.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass5.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass6.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass6.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass6.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass6.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass7.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass7.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass7.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass7.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass8.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass8.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass8.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass8.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass9.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass9.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass9.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass9.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);

        barMethod = ExampleTwoMethodClass10.class.getMethod("bar", String.class);
        fooMethod = ExampleTwoMethodClass10.class.getMethod("foo", Integer.class, Date.class);
        runTest(ExampleTwoMethodClass10.class, barMethod, 100000, 100, new Class[]{String.class}, blackhole);
        runTest(ExampleTwoMethodClass10.class, fooMethod, 100000, 100, new Class[]{Integer.class, Date.class}, blackhole);
    }

    // Test classes
    static class GenericClass<T> {
        @SuppressWarnings("unused")
        public void bar(final T parameter) {
        }
    }

    static class ExampleStringClass extends GenericClass<String> {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }
    }

    static class ExampleStringSubclass extends ExampleStringClass {
    }

    static class ExampleTwoMethodClass {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass2 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass3 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass4 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass5 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass6 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass7 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass8 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass9 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }

    static class ExampleTwoMethodClass10 {
        @SuppressWarnings("unused")
        public void foo(final Integer parameter1, final Date parameter2) {
        }

        @SuppressWarnings("unused")
        public void bar(final String parameter2) {
        }
    }
}