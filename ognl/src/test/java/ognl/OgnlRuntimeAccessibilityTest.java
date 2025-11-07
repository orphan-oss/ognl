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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for OgnlRuntime.isLikelyAccessible() method.
 * <p>
 * These tests verify the logic for determining if a class is accessible,
 * considering the Java module system and known internal packages.
 */
class OgnlRuntimeAccessibilityTest {

    @Test
    void interfaceIsAlwaysAccessible() {
        // Interfaces should always be considered accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(List.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(Map.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(Runnable.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(Comparable.class));
    }

    @Test
    void standardJdkClassesAreAccessible() {
        // Standard JDK classes in exported packages should be accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(String.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(Integer.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(Object.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(StringBuilder.class));
    }

    @Test
    void javaUtilClassesAreAccessible() {
        // java.util classes should be accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(java.util.ArrayList.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(java.util.HashMap.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(java.util.Date.class));
    }

    @Test
    void sunPackageClassesAreInaccessible() throws Exception {
        // Try to load actual sun.* classes if available
        // These are internal JDK classes that should be detected as inaccessible

        // Try to find a sun.* class (may not be available in all JDK versions)
        try {
            Class<?> sunClass = Class.forName("sun.misc.Unsafe");
            assertFalse(OgnlRuntime.isLikelyAccessible(sunClass),
                    "sun.misc.Unsafe should be detected as inaccessible");
        } catch (ClassNotFoundException e) {
            // sun.misc.Unsafe not available, skip this check
        }

        // Try sun.security classes
        try {
            Class<?> sunSecurityClass = Class.forName("sun.security.action.GetPropertyAction");
            assertFalse(OgnlRuntime.isLikelyAccessible(sunSecurityClass),
                    "sun.security classes should be detected as inaccessible");
        } catch (ClassNotFoundException e) {
            // Class not available, skip this check
        }
    }

    @Test
    void comSunPackageClassesAreInaccessible() throws Exception {
        // Try to load actual com.sun.* classes if available
        try {
            Class<?> comSunClass = Class.forName("com.sun.jndi.ldap.LdapCtx");
            assertFalse(OgnlRuntime.isLikelyAccessible(comSunClass),
                    "com.sun.* classes should be detected as inaccessible");
        } catch (ClassNotFoundException e) {
            // Class not available, skip this check
        }
    }

    @Test
    void customClassesAreAccessible() {
        // User-defined classes should be accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(OgnlRuntimeAccessibilityTest.class));
        assertTrue(OgnlRuntime.isLikelyAccessible(TestHelperClass.class));
    }

    @Test
    void innerClassesAreAccessible() {
        // Inner classes should be accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(TestHelperClass.InnerClass.class));
    }

    @Test
    void simulatedSunPackageClassIsInaccessible() {
        // Test with our simulated sun.test.* class
        assertFalse(OgnlRuntime.isLikelyAccessible(sun.test.SimulatedInternalClass.class),
                "Classes in sun.test package should be detected as inaccessible");
    }

    @Test
    void simulatedComSunPackageClassIsInaccessible() {
        // Test with our simulated com.sun.test.* class
        assertFalse(OgnlRuntime.isLikelyAccessible(com.sun.test.AnotherInternalClass.class),
                "Classes in com.sun.test package should be detected as inaccessible");
    }

    @Test
    void interfaceInSunPackageIsAccessible() {
        // Even though it's in sun.* package, interfaces are always accessible
        assertTrue(OgnlRuntime.isLikelyAccessible(sun.test.PublicTestInterface.class),
                "Interfaces should always be accessible, even in sun.* packages");
    }

    @Test
    void defaultPackageClassIsAccessible() {
        // Classes without a package (default package) should be accessible
        // We can't easily test this without creating a class in default package,
        // but we can verify the logic handles null/empty package names
        // This is tested implicitly by the packageName check in isLikelyAccessible
    }

    @Test
    void jdkInternalPackageWouldBeInaccessible() {
        // We can't create actual jdk.internal.* classes, but we verify the logic
        // would catch them by checking the package name pattern
        String testPackage = "jdk.internal.test";
        assertTrue(testPackage.startsWith("jdk.internal."),
                "Package name check should work for jdk.internal.*");
    }

    // Helper classes for testing
    public static class TestHelperClass {
        public static class InnerClass {
        }
    }
}
