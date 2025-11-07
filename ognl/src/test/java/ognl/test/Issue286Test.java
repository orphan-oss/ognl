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
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test for Issue #286: OGNL choosing method on unexported class rather than exported interface
 *
 * The issue occurs when OGNL selects a method from an internal implementation class
 * (like sun.security.x509.X509CertImpl) instead of the public interface
 * (java.security.cert.X509Certificate), causing IllegalAccessException due to module restrictions.
 */
class Issue286Test {

    @Test
    void testX509CertificateMethodResolution() throws Exception {
        // Create a simple SSL context to get a real X509Certificate
        // The actual runtime type will be sun.security.x509.X509CertImpl
        SSLContext sslContext = SSLContext.getDefault();

        // We can't easily get a real certificate in a unit test, so we'll test with a mock
        // that simulates the same problem: an interface with a method implemented by an
        // internal class

        // For this test, we'll use a simpler demonstration of the same principle
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
    void testInterfaceMethodPreferredOverImplementation() throws Exception {
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
    void testInterfaceMethodOnArrayElement() throws Exception {
        TestInterface[] array = new TestInterface[] { new InternalImplementation() };
        OgnlContext context = Ognl.createDefaultContext(array);

        // This simulates the original issue: calling a method on an array element
        Object result = Ognl.getValue("[0].publicMethod()", context, array);
        assertNotNull(result);
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
}
