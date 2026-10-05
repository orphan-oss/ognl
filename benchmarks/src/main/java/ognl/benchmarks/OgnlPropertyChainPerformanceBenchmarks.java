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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Interpreted property chains on classes shaped like typical model objects, with a base class and a few
 * interfaces (issue #650). Every link of a chain except the last one looks up the interface class of its
 * source, which walks the type hierarchy, so these complement the plain beans used by
 * {@link OgnlPerformanceBenchmarks}.
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
public class OgnlPropertyChainPerformanceBenchmarks {

    private OgnlContext context;
    private BenchmarkOrderBean root;
    private SimpleNode twoLinkChainExpression;
    private SimpleNode threeLinkChainExpression;

    @Setup
    public void setup() {
        try {
            context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
            root = new BenchmarkOrderBean();

            twoLinkChainExpression = (SimpleNode) Ognl.parseExpression("customer.name");
            threeLinkChainExpression = (SimpleNode) Ognl.parseExpression("customer.address.city");
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup benchmark", e);
        }
    }

    @Benchmark
    public void twoLinkChainExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(twoLinkChainExpression, context, root);
        blackhole.consume(result);
    }

    @Benchmark
    public void threeLinkChainExpressionInterpreted(Blackhole blackhole) throws OgnlException {
        Object result = Ognl.getValue(threeLinkChainExpression, context, root);
        blackhole.consume(result);
    }

    // Bean classes for testing
    public abstract static class BenchmarkBaseBean implements Serializable, Cloneable {
        public long getId() {
            return 42L;
        }
    }

    public static class BenchmarkOrderBean extends BenchmarkBaseBean implements Comparable<BenchmarkOrderBean> {
        private BenchmarkCustomerBean customer = new BenchmarkCustomerBean();

        public BenchmarkCustomerBean getCustomer() {
            return customer;
        }

        @Override
        public int compareTo(BenchmarkOrderBean other) {
            return 0;
        }
    }

    public static class BenchmarkCustomerBean extends BenchmarkBaseBean implements Comparable<BenchmarkCustomerBean> {
        private BenchmarkAddressBean address = new BenchmarkAddressBean();

        public String getName() {
            return "Alice";
        }

        public BenchmarkAddressBean getAddress() {
            return address;
        }

        @Override
        public int compareTo(BenchmarkCustomerBean other) {
            return 0;
        }
    }

    public static class BenchmarkAddressBean extends BenchmarkBaseBean implements Comparable<BenchmarkAddressBean> {
        public String getCity() {
            return "Springfield";
        }

        @Override
        public int compareTo(BenchmarkAddressBean other) {
            return 0;
        }
    }
}
