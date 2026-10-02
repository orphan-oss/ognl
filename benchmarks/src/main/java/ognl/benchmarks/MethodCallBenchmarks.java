package ognl.benchmarks;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
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

import java.util.concurrent.TimeUnit;

/**
 * Interpreted method calls that are evaluated repeatedly with the same argument types (issue #651).
 * Each call has to pick the method to invoke among the ones with that name, so the benchmarks cover
 * a method without arguments, one with arguments, a name with several overloads, a varargs method and
 * a static method.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class MethodCallBenchmarks {

    public static class Formatter {

        public String name() {
            return "formatter";
        }

        public String describe(int width, String prefix) {
            return prefix;
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

    private Formatter root;
    private OgnlContext context;
    private Object noArguments;
    private Object twoArguments;
    private Object overloaded;
    private Object varArgs;
    private Object staticMethod;

    @Setup
    public void setup() throws OgnlException {
        root = new Formatter();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        noArguments = Ognl.parseExpression("name()");
        twoArguments = Ognl.parseExpression("describe(10, \"prefix\")");
        overloaded = Ognl.parseExpression("format(\"value\")");
        varArgs = Ognl.parseExpression("join(\", \", \"one\", \"two\", \"three\")");
        staticMethod = Ognl.parseExpression("@ognl.benchmarks.MethodCallBenchmarks$Formatter@valueOf(10)");
    }

    @Benchmark
    public Object noArguments() throws OgnlException {
        return Ognl.getValue(noArguments, context, root);
    }

    @Benchmark
    public Object twoArguments() throws OgnlException {
        return Ognl.getValue(twoArguments, context, root);
    }

    @Benchmark
    public Object overloaded() throws OgnlException {
        return Ognl.getValue(overloaded, context, root);
    }

    @Benchmark
    public Object varArgs() throws OgnlException {
        return Ognl.getValue(varArgs, context, root);
    }

    @Benchmark
    public Object staticMethod() throws OgnlException {
        return Ognl.getValue(staticMethod, context, root);
    }

}
