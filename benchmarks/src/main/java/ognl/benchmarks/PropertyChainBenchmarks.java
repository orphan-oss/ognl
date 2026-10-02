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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Interpreted evaluation of property chains of different lengths (issue #650). Every link of a chain
 * except the last one asks for the indexed property type of its source, so the cost per link is what
 * these benchmarks compare: {@code id} has no such link, {@code customer.name} has one and
 * {@code customer.address.city} has two.
 * <p>
 * The chains are evaluated on plain classes and on classes shaped like typical model objects, with a
 * base class and a few interfaces, because finding the interface class walks the type hierarchy.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class PropertyChainBenchmarks {

    public static class Address {
        public String getCity() {
            return "Springfield";
        }
    }

    public static class Customer {
        private final Address address = new Address();

        public String getName() {
            return "Alice";
        }

        public Address getAddress() {
            return address;
        }
    }

    public static class Order {
        private final Customer customer = new Customer();

        public long getId() {
            return 42L;
        }

        public Customer getCustomer() {
            return customer;
        }
    }

    public abstract static class BaseEntity implements Serializable, Cloneable {
        public long getId() {
            return 42L;
        }
    }

    public static class AddressEntity extends BaseEntity implements Comparable<AddressEntity> {
        public String getCity() {
            return "Springfield";
        }

        @Override
        public int compareTo(AddressEntity other) {
            return 0;
        }
    }

    public static class CustomerEntity extends BaseEntity implements Comparable<CustomerEntity> {
        private final AddressEntity address = new AddressEntity();

        public String getName() {
            return "Alice";
        }

        public AddressEntity getAddress() {
            return address;
        }

        @Override
        public int compareTo(CustomerEntity other) {
            return 0;
        }
    }

    public static class OrderEntity extends BaseEntity implements Comparable<OrderEntity> {
        private final CustomerEntity customer = new CustomerEntity();

        public CustomerEntity getCustomer() {
            return customer;
        }

        @Override
        public int compareTo(OrderEntity other) {
            return 0;
        }
    }

    private Order root;
    private OrderEntity entityRoot;
    private OgnlContext context;
    private OgnlContext entityContext;
    private Object singleProperty;
    private Object twoLinkChain;
    private Object threeLinkChain;

    @Setup
    public void setup() throws OgnlException {
        root = new Order();
        entityRoot = new OrderEntity();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        entityContext = Ognl.createDefaultContext(entityRoot, new DefaultMemberAccess(false));
        singleProperty = Ognl.parseExpression("id");
        twoLinkChain = Ognl.parseExpression("customer.name");
        threeLinkChain = Ognl.parseExpression("customer.address.city");
    }

    @Benchmark
    public Object singleProperty() throws OgnlException {
        return Ognl.getValue(singleProperty, context, root);
    }

    @Benchmark
    public Object twoLinkChain() throws OgnlException {
        return Ognl.getValue(twoLinkChain, context, root);
    }

    @Benchmark
    public Object threeLinkChain() throws OgnlException {
        return Ognl.getValue(threeLinkChain, context, root);
    }

    @Benchmark
    public Object twoLinkChainWithSupertypes() throws OgnlException {
        return Ognl.getValue(twoLinkChain, entityContext, entityRoot);
    }

    @Benchmark
    public Object threeLinkChainWithSupertypes() throws OgnlException {
        return Ognl.getValue(threeLinkChain, entityContext, entityRoot);
    }

}
