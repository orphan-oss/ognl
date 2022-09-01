/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * and/or LICENSE file distributed with this work for additional
 * information regarding copyright ownership.  The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
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
package ognl.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.Objects;

/**
 * Builds and provides a JVM wide singleton shared thread-safe with all permissions granted security manager for ognl
 *
 * @since 3.1.24
 */
public class OgnlSecurityManagerFactory extends SecureClassLoader {

    private static volatile Object ognlSecurityManager;

    private final Class<?> ognlSecurityManagerClass;

    public static Object getOgnlSecurityManager() {
        if (ognlSecurityManager == null) {
            synchronized (SecurityManager.class) {
                if (ognlSecurityManager == null) {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm == null || !sm.getClass().getName().equals(OgnlSecurityManager.class.getName())) {
                        try {
                            ognlSecurityManager = new OgnlSecurityManagerFactory().build(sm);
                        } catch (Exception ignored) {
                            // not expected at all; anyway keep and return null as ognlSecurityManager
                        }
                    } else {
                        ognlSecurityManager = sm;
                    }
                }
            }
        }
        return ognlSecurityManager;
    }

    private OgnlSecurityManagerFactory() throws IOException {
        super(OgnlSecurityManagerFactory.class.getClassLoader());

        PermissionCollection pc = new AllPermission().newPermissionCollection();
        pc.add(new AllPermission()); // grant all permissions to simulate JDK itself SecurityManager
        ProtectionDomain pd = new ProtectionDomain(null, pc);

        byte[] byteArray = toByteArray(Objects.requireNonNull(getParent().getResourceAsStream(
                OgnlSecurityManager.class.getName().replace('.', '/') + ".class")));
        ognlSecurityManagerClass = defineClass(null, byteArray, 0, byteArray.length, pd);
    }

    private Object build(SecurityManager parentSecurityManager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return ognlSecurityManagerClass.getConstructor(SecurityManager.class).newInstance(parentSecurityManager);
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int n;
        byte[] buffer = new byte[4096];
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }

        return output.toByteArray();
    }
}
