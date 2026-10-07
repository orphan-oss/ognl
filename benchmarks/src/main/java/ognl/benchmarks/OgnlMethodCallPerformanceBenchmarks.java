package ognl.benchmarks;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.SimpleNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Interpreted method calls evaluated repeatedly with arguments of the same types (issue #651). Each call
 * has to pick the method to invoke among the ones with that name.
 */
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
public class OgnlMethodCallPerformanceBenchmarks {

    private OgnlContext context;
    private BenchmarkFormatterBean root;
    private SimpleNode noArgumentsMethodExpression;
    private SimpleNode overloadedMethodExpression;
    private SimpleNode varArgsMethodExpression;
    private SimpleNode staticMethodExpression;

    @Setup
    public void setup() {
        try {
            context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
            root = new BenchmarkFormatterBean();

            noArgumentsMethodExpression = (SimpleNode) Ognl.parseExpression("name()");
            overloadedMethodExpression = (SimpleNode) Ognl.parseExpression("format(\"value\")");
            varArgsMethodExpression = (SimpleNode) Ognl.parseExpression("join(\", \", \"one\", \"two\", \"three\")");
            staticMethodExpression = (SimpleNode) Ognl.parseExpression(
                    "@ognl.benchmarks.OgnlMethodCallPerformanceBenchmarks$BenchmarkFormatterBean@valueOf(10)");
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup benchmark", e);
        }
    }

    @Benchmark
    public void noArgumentsMethodExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(noArgumentsMethodExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void overloadedMethodExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(overloadedMethodExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void varArgsMethodExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(varArgsMethodExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void staticMethodExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(staticMethodExpression, context, root);
        blackhole.consume(result);
    }

    // Bean classes for testing
    public static class BenchmarkFormatterBean {

        public String name() {
            return "formatter";
        }

        public String format(int value) {
            return "int";
        }

        public String format(long value) {
            return "long";
        }

        public String format(double value) {
            return "double";
        }

        public String format(String value) {
            return "string";
        }

        public String format(Object value) {
            return "object";
        }

        public String join(String separator, String... parts) {
            return separator;
        }

        public static String valueOf(int value) {
            return "static";
        }
    }
}
