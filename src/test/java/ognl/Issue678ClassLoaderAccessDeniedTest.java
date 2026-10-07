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

import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Issue #678: a SecurityManager that denies {@code getClassLoader} must not fail a method call; the choice of
 * method is then just not remembered.
 */
class Issue678ClassLoaderAccessDeniedTest {

    public static class Isolated {
        public String value() {
            return "isolated";
        }
    }

    static class DenyGetClassLoader extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            if (perm instanceof RuntimePermission && "getClassLoader".equals(perm.getName())) {
                throw new SecurityException("getClassLoader denied");
            }
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            checkPermission(perm);
        }
    }

    @Test
    void methodCallSucceedsWhenClassLoaderAccessIsDenied() throws Exception {
        assumeTrue(OgnlRuntime.detectMajorJavaVersion() < 18, "SecurityManager cannot be installed on JDK 18+");

        URL testClasses = Isolated.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader unrelatedLoader = new URLClassLoader(new URL[]{testClasses}, null)) {
            Class<?> isolatedClass = unrelatedLoader.loadClass(Isolated.class.getName());
            assertNotSame(Isolated.class, isolatedClass);
            Object target = isolatedClass.newInstance();
            OgnlContext context = Ognl.createDefaultContext(target, new DefaultMemberAccess(false));

            System.setSecurityManager(new DenyGetClassLoader());
            try {
                assertEquals("isolated", Ognl.getValue("value()", context, target));
            } finally {
                System.setSecurityManager(null);
            }
        }
    }
}
