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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OgnlRuntimeStricterInvocationTest {

    static Stream<Method> blockedMethods() throws Exception {
        return Stream.of(
                AccessibleObject.class.getMethod("setAccessible", boolean.class),
                AccessibleObject.class.getMethod("setAccessible", AccessibleObject[].class, boolean.class),
                System.class.getMethod("exit", int.class),
                System.class.getMethod("console"),
                AccessibleObjectHandler.class.getMethod("setAccessible", AccessibleObject.class, boolean.class),
                ClassResolver.class.getMethod("classForName", String.class, OgnlContext.class),
                MethodAccessor.class.getMethod("callMethod", OgnlContext.class, Object.class, String.class, Object[].class),
                MemberAccess.class.getMethod("isAccessible", OgnlContext.class, Object.class, Member.class, String.class),
                OgnlContext.class.getMethod("getRoot"),
                Runtime.class.getMethod("getRuntime"),
                ClassLoader.class.getMethod("getParent"),
                ProcessBuilder.class.getMethod("start"),
                Method.class.getMethod("invoke", Object.class, Object[].class),
                Constructor.class.getMethod("newInstance", Object[].class),
                Field.class.getMethod("get", Object.class),
                anyPublicMethodOf(Class.forName("sun.misc.Unsafe"))
        );
    }

    private static Method anyPublicMethodOf(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .min(Comparator.comparing(Method::getName))
                .orElseThrow(() -> new AssertionError("no public method on " + clazz));
    }

    static Stream<Method> allowedMethods() throws Exception {
        return Stream.of(
                Math.class.getMethod("max", int.class, int.class),
                String.class.getMethod("length"),
                System.class.getMethod("getProperty", String.class)
        );
    }

    @ParameterizedTest
    @MethodSource("blockedMethods")
    void blocksMethodsWithNoLegitimateUseInExpressions(Method method) {
        assertTrue(OgnlRuntime.isBlockedByStricterInvocation(method), method.toString());
    }

    @ParameterizedTest
    @MethodSource("allowedMethods")
    void allowsOrdinaryMethods(Method method) {
        assertFalse(OgnlRuntime.isBlockedByStricterInvocation(method), method.toString());
    }
}
