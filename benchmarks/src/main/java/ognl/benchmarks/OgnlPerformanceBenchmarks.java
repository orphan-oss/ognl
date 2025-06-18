package ognl.benchmarks;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.SimpleNode;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
public class OgnlPerformanceBenchmarks {

    private OgnlContext context;
    private BenchmarkRootBean root;
    private SimpleNode constantExpression;
    private SimpleNode compiledConstantExpression;
    private SimpleNode singlePropertyExpression;
    private SimpleNode compiledSinglePropertyExpression;
    private SimpleNode propertyNavigationExpression;
    private SimpleNode compiledPropertyNavigationExpression;
    private SimpleNode propertyNavigationAndComparisonExpression;
    private SimpleNode compiledPropertyNavigationAndComparisonExpression;
    private SimpleNode propertyNavigationWithMapExpression;
    private SimpleNode compiledPropertyNavigationWithMapExpression;

    @Setup
    public void setup() {
        try {
            context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
            root = new BenchmarkRootBean();
            context.put("contextValue", "cvalue");

            // Parse and compile expressions
            constantExpression = (SimpleNode) Ognl.parseExpression("100 + 20 * 5");
            compiledConstantExpression = (SimpleNode) Ognl.compileExpression(context, root, "100 + 20 * 5");

            singlePropertyExpression = (SimpleNode) Ognl.parseExpression("bean2");
            compiledSinglePropertyExpression = (SimpleNode) Ognl.compileExpression(context, root, "bean2");

            propertyNavigationExpression = (SimpleNode) Ognl.parseExpression("bean2.bean3.value");
            compiledPropertyNavigationExpression = (SimpleNode) Ognl.compileExpression(context, root, "bean2.bean3.value");

            propertyNavigationAndComparisonExpression = (SimpleNode) Ognl.parseExpression("bean2.bean3.value <= 24");
            compiledPropertyNavigationAndComparisonExpression = (SimpleNode) Ognl.compileExpression(context, root, "bean2.bean3.value <= 24");

            propertyNavigationWithMapExpression = (SimpleNode) Ognl.parseExpression("bean2.bean3.map['foo']");
            compiledPropertyNavigationWithMapExpression = (SimpleNode) Ognl.compileExpression(context, root, "bean2.bean3.map['foo']");
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup benchmark", e);
        }
    }

    @Benchmark
    public void constantExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(constantExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void constantExpressionCompiled(Blackhole blackhole) {
        Object result = Ognl.getValue(compiledConstantExpression.getAccessor(), context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void singlePropertyExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(singlePropertyExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void singlePropertyExpressionCompiled(Blackhole blackhole) {
        Object result = Ognl.getValue(compiledSinglePropertyExpression.getAccessor(), context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void propertyNavigationExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(propertyNavigationExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void propertyNavigationExpressionCompiled(Blackhole blackhole) {
        Object result = Ognl.getValue(compiledPropertyNavigationExpression.getAccessor(), context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void propertyNavigationAndComparisonExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(propertyNavigationAndComparisonExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void propertyNavigationAndComparisonExpressionCompiled(Blackhole blackhole) {
        Object result = Ognl.getValue(compiledPropertyNavigationAndComparisonExpression.getAccessor(), context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void propertyNavigationWithMapExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(propertyNavigationWithMapExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void propertyNavigationWithMapExpressionCompiled(Blackhole blackhole) {
        Object result = Ognl.getValue(compiledPropertyNavigationWithMapExpression.getAccessor(), context, root);
        blackhole.consume(result);
    }

    // Bean classes for testing
    public static class BenchmarkRootBean {
        private BenchmarkNestedBean bean2 = new BenchmarkNestedBean();

        public BenchmarkNestedBean getBean2() {
            return bean2;
        }
    }

    public static class BenchmarkNestedBean {
        private BenchmarkLeafBean bean3 = new BenchmarkLeafBean();

        public BenchmarkLeafBean getBean3() {
            return bean3;
        }
    }

    public static class BenchmarkLeafBean {
        private int value = 20;
        private String nullValue;
        private int[] indexedValue = new int[100];
        private Map<String, String> map = new HashMap<>();

        public BenchmarkLeafBean() {
            map.put("foo", "bar");
        }

        public int getValue() {
            return value;
        }

        public void setNullValue(String value) {
            this.nullValue = value;
        }

        public int getIndexedValue(int index) {
            return indexedValue[index];
        }

        public Map<String, String> getMap() {
            return map;
        }
    }
}