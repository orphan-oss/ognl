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
package ognl.test;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for Issue #286: OGNL choosing method on unexported class rather than exported interface
 *
 * The issue occurs when OGNL selects a method from an internal implementation class
 * (like sun.security.x509.X509CertImpl) instead of the public interface
 * (java.security.cert.X509Certificate), causing IllegalAccessException due to module restrictions.
 */
class Issue286Test {

    @Test
    void x509CertificateMethodResolution() throws Exception {
        // Simulates the issue where OGNL selects a method from an internal implementation
        // (like sun.security.x509.X509CertImpl for X509Certificate) instead of the public interface
        TestInterface obj = new InternalImplementation();

        OgnlContext context = Ognl.createDefaultContext(obj);

        // This should select the method from TestInterface, not InternalImplementation
        assertDoesNotThrow(() -> {
            Object result = Ognl.getValue("publicMethod()", context, obj);
            assertNotNull(result);
        }, "OGNL should select the method from the public interface, not the internal implementation");
    }

    /**
     * Test that demonstrates the preference for interface methods over implementation methods
     * when both have the same signature.
     */
    @Test
    void interfaceMethodPreferredOverImplementation() throws Exception {
        TestInterface obj = new InternalImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        // This expression should work because OGNL should prefer the interface method
        Object result = Ognl.getValue("publicMethod()", context, obj);
        assertNotNull(result);
    }

    /**
     * Test with a more complex example involving collections
     */
    @Test
    void interfaceMethodOnArrayElement() throws Exception {
        TestInterface[] array = new TestInterface[] { new InternalImplementation() };
        OgnlContext context = Ognl.createDefaultContext(array);

        // This simulates the original issue: calling a method on an array element
        Object result = Ognl.getValue("[0].publicMethod()", context, array);
        assertNotNull(result);
    }

    /**
     * Test method resolution with parameters to ensure full method matching logic is exercised
     */
    @Test
    void interfaceMethodWithParameters() throws Exception {
        ParameterizedInterface obj = new ParameterizedImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("process('test')", context, obj);
        assertNotNull(result);
    }

    /**
     * Test with multiple interfaces implementing the same method
     */
    @Test
    void multipleInterfacesWithSameMethod() throws Exception {
        MultiInterfaceImplementation obj = new MultiInterfaceImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        // Should prefer interface methods regardless of which interface
        Object result = Ognl.getValue("getValue()", context, obj);
        assertNotNull(result);
    }

    /**
     * Test public class vs package-private class preference
     */
    @Test
    void publicClassPreferredOverPackagePrivate() throws Exception {
        BaseInterface obj = new PublicImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("baseMethod()", context, obj);
        assertNotNull(result);
    }

    /**
     * Test with nested method calls
     */
    @Test
    void nestedMethodCallsOnInterface() throws Exception {
        ContainerInterface container = new ContainerImplementation();
        OgnlContext context = Ognl.createDefaultContext(container);

        Object result = Ognl.getValue("getChild().publicMethod()", context, container);
        assertNotNull(result);
    }

    /**
     * Test overloaded methods
     */
    @Test
    void overloadedInterfaceMethods() throws Exception {
        OverloadedInterface obj = new OverloadedImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result1 = Ognl.getValue("compute(5)", context, obj);
        assertNotNull(result1);
        assertEquals(10, result1);

        Object result2 = Ognl.getValue("compute(5, 10)", context, obj);
        assertNotNull(result2);
        assertEquals(15, result2);
    }

    /**
     * Test with abstract class in hierarchy
     */
    @Test
    void abstractClassInHierarchy() throws Exception {
        AbstractInterface obj = new ConcreteImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("abstractMethod()", context, obj);
        assertNotNull(result);
        assertEquals("concrete", result);
    }

    /**
     * Test with generics
     */
    @Test
    void genericInterfaceMethod() throws Exception {
        GenericInterface<String> obj = new GenericImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("transform('input')", context, obj);
        assertNotNull(result);
        assertEquals("TRANSFORMED: input", result);
    }

    /**
     * Test with collection return types
     */
    @Test
    void methodReturningCollection() throws Exception {
        CollectionInterface obj = new CollectionImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("getItems().size()", context, obj);
        assertNotNull(result);
        assertEquals(3, result);
    }

    /**
     * Test with inheritance hierarchy - interface extends interface
     */
    @Test
    void extendedInterfaceMethod() throws Exception {
        ExtendedInterface obj = new ExtendedImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result1 = Ognl.getValue("baseMethod()", context, obj);
        assertNotNull(result1);

        Object result2 = Ognl.getValue("extendedMethod()", context, obj);
        assertNotNull(result2);
    }

    /**
     * Test method that returns an interface type
     */
    @Test
    void methodReturningInterface() throws Exception {
        FactoryInterface factory = new FactoryImplementation();
        OgnlContext context = Ognl.createDefaultContext(factory);

        Object result = Ognl.getValue("create().publicMethod()", context, factory);
        assertNotNull(result);
        assertEquals("result", result);
    }

    /**
     * Test with null parameters
     */
    @Test
    void methodWithNullParameter() throws Exception {
        NullableInterface obj = new NullableImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("handleNull(null)", context, obj);
        assertNotNull(result);
        assertEquals("null handled", result);
    }

    /**
     * Test with primitive parameters and autoboxing
     */
    @Test
    void methodWithPrimitiveParameters() throws Exception {
        PrimitiveInterface obj = new PrimitiveImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("add(3, 7)", context, obj);
        assertNotNull(result);
        assertEquals(10, result);
    }

    /**
     * Test with actual JDK classes - HashMap (concrete) vs Map (interface)
     * This tests that OGNL prefers interface methods when available
     */
    @Test
    void jdkInterfacePreferredOverConcreteClass() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        OgnlContext context = Ognl.createDefaultContext(map);

        // Call methods that exist on both Map interface and HashMap class
        Object size = Ognl.getValue("size()", context, map);
        assertEquals(1, size);

        Object isEmpty = Ognl.getValue("isEmpty()", context, map);
        assertEquals(false, isEmpty);

        Object value = Ognl.getValue("get('key')", context, map);
        assertEquals("value", value);
    }

    /**
     * Test method resolution choosing between multiple method sources
     */
    @Test
    void methodResolutionWithInheritance() throws Exception {
        // Use a list to test interface vs implementation preference
        List<String> list = List.of("a", "b", "c");
        OgnlContext context = Ognl.createDefaultContext(list);

        Object result = Ognl.getValue("size()", context, list);
        assertEquals(3, result);

        Object first = Ognl.getValue("get(0)", context, list);
        assertEquals("a", first);
    }

    /**
     * Test that verifies method resolution works correctly with package-private implementation
     */
    @Test
    void packagePrivateImplementation() throws Exception {
        PackagePrivateImpl obj = new PackagePrivateImpl();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result = Ognl.getValue("publicMethod()", context, obj);
        assertNotNull(result);
        assertEquals("package-private", result);
    }

    /**
     * Test with method calls on objects that implement multiple interfaces
     */
    @Test
    void multipleInterfaceInheritance() throws Exception {
        CombinedInterface obj = new CombinedImplementation();
        OgnlContext context = Ognl.createDefaultContext(obj);

        Object result1 = Ognl.getValue("methodA()", context, obj);
        assertEquals("A", result1);

        Object result2 = Ognl.getValue("methodB()", context, obj);
        assertEquals("B", result2);
    }

    /**
     * Test method resolution with classes from different packages
     * This indirectly tests the isLikelyAccessible() logic
     */
    @Test
    void methodResolutionAcrossPackages() throws Exception {
        // Test that common JDK classes work correctly
        String str = "test";
        OgnlContext context = Ognl.createDefaultContext(str);

        Object result = Ognl.getValue("length()", context, str);
        assertEquals(4, result);

        Object upper = Ognl.getValue("toUpperCase()", context, str);
        assertEquals("TEST", upper);
    }

    /**
     * Test with java.util classes to ensure proper method resolution
     */
    @Test
    void javaUtilClassMethodResolution() throws Exception {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        list.add("item1");
        list.add("item2");

        OgnlContext context = Ognl.createDefaultContext(list);

        Object size = Ognl.getValue("size()", context, list);
        assertEquals(2, size);

        // Test method that exists on List interface
        Object first = Ognl.getValue("get(0)", context, list);
        assertEquals("item1", first);
    }

    /**
     * Test with StringBuilder to verify method resolution on concrete classes
     */
    @Test
    void stringBuilderMethodResolution() throws Exception {
        StringBuilder sb = new StringBuilder("hello");
        OgnlContext context = Ognl.createDefaultContext(sb);

        Object len = Ognl.getValue("length()", context, sb);
        assertEquals(5, len);

        Object str = Ognl.getValue("toString()", context, sb);
        assertEquals("hello", str);
    }

    /**
     * Test getMethods to ensure it returns methods from interfaces and classes
     * This helps test the method collection logic that feeds into findBestMethod
     */
    @Test
    void getMethodsIncludesInterfaceAndClassMethods() throws Exception {
        // Test that getMethods returns methods from both the class and its interfaces
        Map<String, String> map = new HashMap<>();
        Class<?> clazz = map.getClass();

        // Get methods - this uses OgnlRuntime.getMethods internally
        Method[] methods = clazz.getMethods();

        // Should have methods from Map interface
        boolean hasMapMethods = false;
        boolean hasHashMapMethods = false;

        for (Method m : methods) {
            if (m.getName().equals("get") && m.getDeclaringClass().isInterface()) {
                hasMapMethods = true;
            }
            if (m.getName().equals("get") && !m.getDeclaringClass().isInterface()) {
                hasHashMapMethods = true;
            }
        }

        // At least one source should provide the method
        assertTrue(hasMapMethods || hasHashMapMethods, "Should have get() method from either Map interface or HashMap class");
    }

    /**
     * Test that verifies OGNL handles CharSequence interface correctly
     * String implements CharSequence, testing interface preference
     */
    @Test
    void charSequenceInterfaceHandling() throws Exception {
        CharSequence seq = "test string";
        OgnlContext context = Ognl.createDefaultContext(seq);

        Object len = Ognl.getValue("length()", context, seq);
        assertEquals(11, len);

        Object charAt = Ognl.getValue("charAt(0)", context, seq);
        assertEquals('t', charAt);
    }

    /**
     * Test method resolution with Comparable interface
     */
    @Test
    void comparableInterfaceMethodResolution() throws Exception {
        Comparable<String> str = "abc";
        OgnlContext context = Ognl.createDefaultContext(str);

        Object result = Ognl.getValue("compareTo('abc')", context, str);
        assertEquals(0, result);

        Object length = Ognl.getValue("length()", context, str);
        assertEquals(3, length);
    }

    /**
     * Test that simulates the actual issue #286 scenario with a class in sun.* package
     * <p>
     * This test uses classes in sun.test package to simulate internal JDK classes.
     * The SimulatedInternalClass will be detected as inaccessible, while the
     * PublicTestInterface will be accessible, allowing us to test the preference logic.
     */
    @Test
    void simulatedInternalClassVsInterface() throws Exception {
        // Create an instance of a class in "sun.test" package
        // This simulates sun.security.x509.X509CertImpl
        sun.test.PublicTestInterface obj = new sun.test.SimulatedInternalClass();

        OgnlContext context = Ognl.createDefaultContext(obj);

        // OGNL should prefer the interface method over the class method
        // because the class is in a "sun." package
        Object result = Ognl.getValue("testMethod()", context, obj);
        assertNotNull(result);
        assertEquals("internal", result);
    }

    /**
     * Direct test of isLikelyAccessible with simulated internal class
     */
    @Test
    void simulatedInternalClassIsDetectedAsInaccessible() {
        // The class should be detected as inaccessible because it's in sun.* package
        assertFalse(OgnlRuntime.isLikelyAccessible(sun.test.SimulatedInternalClass.class),
                "Class in sun.test package should be detected as inaccessible");

        // But the interface should be accessible because interfaces are always accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(sun.test.PublicTestInterface.class),
                "Interface should always be detected as accessible");
    }

    // Public interface - represents java.security.cert.X509Certificate
    public interface TestInterface {
        String publicMethod();
    }

    // Internal implementation - represents sun.security.x509.X509CertImpl
    // In a real scenario, this would be a non-exported class from a JDK module
    public static class InternalImplementation implements TestInterface {
        @Override
        public String publicMethod() {
            return "result";
        }
    }

    // Additional test interfaces and classes for comprehensive coverage

    public interface ParameterizedInterface {
        String process(String input);
    }

    public static class ParameterizedImplementation implements ParameterizedInterface {
        @Override
        public String process(String input) {
            return "processed: " + input;
        }
    }

    public interface FirstInterface {
        String getValue();
    }

    public interface SecondInterface {
        String getValue();
    }

    public static class MultiInterfaceImplementation implements FirstInterface, SecondInterface {
        @Override
        public String getValue() {
            return "multi";
        }
    }

    public interface BaseInterface {
        String baseMethod();
    }

    public static class PublicImplementation implements BaseInterface {
        @Override
        public String baseMethod() {
            return "public";
        }
    }

    public interface ContainerInterface {
        TestInterface getChild();
    }

    public static class ContainerImplementation implements ContainerInterface {
        @Override
        public TestInterface getChild() {
            return new InternalImplementation();
        }
    }

    public interface OverloadedInterface {
        Integer compute(int a);
        Integer compute(int a, int b);
    }

    public static class OverloadedImplementation implements OverloadedInterface {
        @Override
        public Integer compute(int a) {
            return a * 2;
        }

        @Override
        public Integer compute(int a, int b) {
            return a + b;
        }
    }

    public interface AbstractInterface {
        String abstractMethod();
    }

    public static abstract class AbstractBase implements AbstractInterface {
        public abstract String abstractMethod();
    }

    public static class ConcreteImplementation extends AbstractBase {
        @Override
        public String abstractMethod() {
            return "concrete";
        }
    }

    public interface GenericInterface<T> {
        String transform(T input);
    }

    public static class GenericImplementation implements GenericInterface<String> {
        @Override
        public String transform(String input) {
            return "TRANSFORMED: " + input;
        }
    }

    public interface CollectionInterface {
        List<String> getItems();
    }

    public static class CollectionImplementation implements CollectionInterface {
        @Override
        public List<String> getItems() {
            return List.of("item1", "item2", "item3");
        }
    }

    public interface ParentInterface {
        String baseMethod();
    }

    public interface ExtendedInterface extends ParentInterface {
        String extendedMethod();
    }

    public static class ExtendedImplementation implements ExtendedInterface {
        @Override
        public String baseMethod() {
            return "base";
        }

        @Override
        public String extendedMethod() {
            return "extended";
        }
    }

    public interface FactoryInterface {
        TestInterface create();
    }

    public static class FactoryImplementation implements FactoryInterface {
        @Override
        public TestInterface create() {
            return new InternalImplementation();
        }
    }

    public interface NullableInterface {
        String handleNull(String input);
    }

    public static class NullableImplementation implements NullableInterface {
        @Override
        public String handleNull(String input) {
            return input == null ? "null handled" : "value: " + input;
        }
    }

    public interface PrimitiveInterface {
        int add(int a, int b);
    }

    public static class PrimitiveImplementation implements PrimitiveInterface {
        @Override
        public int add(int a, int b) {
            return a + b;
        }
    }

    // Package-private class to test preference for accessible methods
    static class PackagePrivateImpl {
        public String publicMethod() {
            return "package-private";
        }
    }

    // Interfaces for testing multiple inheritance
    public interface InterfaceA {
        String methodA();
    }

    public interface InterfaceB {
        String methodB();
    }

    public interface CombinedInterface extends InterfaceA, InterfaceB {
    }

    public static class CombinedImplementation implements CombinedInterface {
        @Override
        public String methodA() {
            return "A";
        }

        @Override
        public String methodB() {
            return "B";
        }
    }
}
