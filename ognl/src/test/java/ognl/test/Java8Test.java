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
import ognl.OgnlException;
import ognl.OgnlRuntime;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Java8Test<C extends OgnlContext<C>> {

    @Test
    void testDefaultMethodOnClass() {
        /* defaultMethod(); */
        List<?> defaultMethod = OgnlRuntime.getMethods(ClassWithDefaults.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
        Method method = OgnlRuntime.getReadMethod(ClassWithDefaults.class, "defaultMethod");
        assertNotNull(method);
    }

    @Test
    void testDefaultMethodOnSubClass() {
        /* defaultMethod(); */
        List<?> defaultMethod = OgnlRuntime.getMethods(SubClassWithDefaults.class, "defaultMethod", false);
        assertNotNull(defaultMethod);
        Method method = OgnlRuntime.getReadMethod(SubClassWithDefaults.class, "defaultMethod");
        assertNotNull(method);
    }

    @Test
    void testGetDeclaredMethods() {
        List<?> defaultMethod = OgnlRuntime.getDeclaredMethods(SubClassWithDefaults.class, "name", false);
        assertNotNull(defaultMethod);
        defaultMethod = OgnlRuntime.getDeclaredMethods(ClassWithDefaults.class, "name", false);
        assertNotNull(defaultMethod);
    }

    @Test
    void testAccessingDefaultMethod() throws OgnlException {
        ClassWithDefaults root = new ClassWithDefaults();
        Object value = Ognl.getValue("name", Ognl.<C>createDefaultContext(root), root);

        assertEquals("name", value);
    }

}

class SubClassWithDefaults extends ClassWithDefaults {

    public String getName() {
        return "name";
    }

}

class ClassWithDefaults implements SubInterfaceWithDefaults {

}

interface InterfaceWithDefaults {
    default void defaultMethod() {
    }

    default String getName() {
        return "name";
    }
}

interface SubInterfaceWithDefaults extends InterfaceWithDefaults {
}
