/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ognl;

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.OgnlExpressionCompiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Issue #650: the interface class of a chain link's source is looked up once per class.
 */
class InterfaceClassCacheTest {

    public static class Customer {
        public String getName() {
            return "Alice";
        }
    }

    public static class Order {
        private final Customer customer = new Customer();

        public Customer getCustomer() {
            return customer;
        }
    }

    private static class CountingCompiler<C extends OgnlContext<C>> extends ExpressionCompiler<C> {

        private final ConcurrentHashMap<Class<?>, AtomicInteger> calls = new ConcurrentHashMap<>();

        @Override
        public Class<?> getInterfaceClass(Class<?> clazz) {
            calls.computeIfAbsent(clazz, c -> new AtomicInteger()).incrementAndGet();
            return super.getInterfaceClass(clazz);
        }

        int calls(Class<?> clazz) {
            AtomicInteger count = calls.get(clazz);
            return count == null ? 0 : count.get();
        }
    }

    private static class ConstantCompiler<C extends OgnlContext<C>> extends ExpressionCompiler<C> {

        @Override
        public Class<?> getInterfaceClass(Class<?> clazz) {
            return Object.class;
        }
    }

    private OgnlExpressionCompiler<?> originalCompiler;
    private CountingCompiler<?> compiler;

    @BeforeEach
    void setUp() {
        originalCompiler = OgnlRuntime.getCompiler();
        compiler = new CountingCompiler<>();
        OgnlRuntime.setCompiler(compiler);
    }

    @AfterEach
    void tearDown() {
        OgnlRuntime.setClassCacheInspector(null);
        OgnlRuntime.setCompiler(originalCompiler);
    }

    @Test
    void chainLooksUpInterfaceClassOncePerClass() throws Exception {
        Order order = new Order();
        OgnlContext context = Ognl.createDefaultContext(order, new DefaultMemberAccess(false));
        Object tree = Ognl.parseExpression("customer.name");

        for (int i = 0; i < 5; i++) {
            assertEquals("Alice", Ognl.getValue(tree, context, order));
        }

        assertEquals(1, compiler.calls(Order.class));
    }

    @Test
    void returnsWhatTheCompilerReturns() {
        assertSame(List.class, OgnlRuntime.getInterfaceClass(ArrayList.class));
        assertSame(List.class, OgnlRuntime.getInterfaceClass(ArrayList.class));
        assertSame(Order.class, OgnlRuntime.getInterfaceClass(Order.class));
        assertSame(Order.class, OgnlRuntime.getInterfaceClass(Order.class));

        assertEquals(1, compiler.calls(ArrayList.class));
        assertEquals(1, compiler.calls(Order.class));
    }

    @Test
    void newCompilerIsAskedAgain() {
        assertSame(Order.class, OgnlRuntime.getInterfaceClass(Order.class));

        OgnlExpressionCompiler<?> other = new ConstantCompiler<>();
        OgnlRuntime.setCompiler(other);

        assertSame(Object.class, OgnlRuntime.getInterfaceClass(Order.class));

        OgnlRuntime.setCompiler(compiler);

        assertSame(Order.class, OgnlRuntime.getInterfaceClass(Order.class));
        assertEquals(2, compiler.calls(Order.class));
    }

    @Test
    void classCacheInspectorCanKeepAClassOutOfTheCache() {
        OgnlRuntime.setClassCacheInspector(type -> type != Order.class);

        assertSame(Order.class, OgnlRuntime.getInterfaceClass(Order.class));
        assertSame(Order.class, OgnlRuntime.getInterfaceClass(Order.class));
        assertSame(Customer.class, OgnlRuntime.getInterfaceClass(Customer.class));
        assertSame(Customer.class, OgnlRuntime.getInterfaceClass(Customer.class));

        assertEquals(2, compiler.calls(Order.class));
        assertEquals(1, compiler.calls(Customer.class));
    }

    @Test
    void clearCacheDropsInterfaceClasses() {
        OgnlRuntime.getInterfaceClass(Order.class);

        OgnlRuntime.clearCache();
        OgnlRuntime.getInterfaceClass(Order.class);

        assertEquals(2, compiler.calls(Order.class));
    }

    @Test
    void clearAdditionalCacheDropsInterfaceClasses() {
        OgnlRuntime.getInterfaceClass(Order.class);

        OgnlRuntime.clearAdditionalCache();
        OgnlRuntime.getInterfaceClass(Order.class);

        assertEquals(2, compiler.calls(Order.class));
    }

}
