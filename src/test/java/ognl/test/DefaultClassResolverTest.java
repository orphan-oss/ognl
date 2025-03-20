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

import ognl.DefaultClassResolver;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class DefaultClassResolverTest {

    @Test
    void testClassInDefaultPackageResolution() throws Exception {
        DefaultClassResolver resolver = new DefaultClassResolver();
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        assertNotNull(resolver.classForName("ClassInDefaultPackage", context));
    }

    @Test
    void testEnsureClassNotFoundException() {
        DefaultClassResolver resolver = new DefaultClassResolver();
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        try {
            resolver.classForName("no.such.Class", context);
            fail("Expected ClassNotFoundException as the specified class does not exist.");
        } catch (Exception e) {
            assertEquals(ClassNotFoundException.class, e.getClass());
            assertEquals("no.such.Class", e.getMessage());
        }
    }

    @Test
    void testEnsureClassNotFoundExceptionReportsSpecifiedName() {
        DefaultClassResolver resolver = new DefaultClassResolver();
        OgnlContext context = Ognl.createDefaultContext(null,
                new DefaultMemberAccess(false));
        try {
            resolver.classForName("BogusClass", context);
            fail("Expected ClassNotFoundException as the specified class does not exist.");
        } catch (Exception e) {
            assertEquals(ClassNotFoundException.class, e.getClass());
            assertEquals("BogusClass", e.getMessage());
        }
    }

}
