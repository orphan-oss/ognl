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

import ognl.internal.MethodList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue #651: the method chosen for a call is remembered per type, name and argument types.
 */
class OgnlRuntimeMethodResolutionCacheTest {

    public static class Formatter {

        public String format(int value) {
            return "int";
        }

        public String format(double value) {
            return "double";
        }

        public String format(String value) {
            return "string";
        }

        public String echo(String value) {
            return String.valueOf(value);
        }

        public String join(String separator, String... parts) {
            return String.join(separator, parts);
        }

        public String twice(int value) {
            return "instance";
        }

        public static String twice(String value) {
            return "static";
        }

        public String ambiguous(Integer first, Object second) {
            return "integer-object";
        }

        public String ambiguous(Object first, Integer second) {
            return "object-integer";
        }

        public String original(String value) {
            return "original";
        }

        public String replacement(String value) {
            return "replacement";
        }
    }

    private Formatter formatter;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        OgnlRuntime.clearCache();
        formatter = new Formatter();
        context = Ognl.createDefaultContext(formatter, new DefaultMemberAccess(false));
    }

    @AfterEach
    void tearDown() {
        OgnlRuntime.clearCache();
    }

    private static MethodList methods(String name, boolean staticMethods) {
        return assertInstanceOf(MethodList.class, OgnlRuntime.getMethods(Formatter.class, name, staticMethods));
    }

    @Test
    void sameCallSiteResolvesEachArgumentTypeToItsOverload() throws Exception {
        Object tree = Ognl.parseExpression("format(#arg)");

        for (int round = 0; round < 3; round++) {
            context.put("arg", 1);
            assertEquals("int", Ognl.getValue(tree, context, formatter));
            context.put("arg", "text");
            assertEquals("string", Ognl.getValue(tree, context, formatter));
            context.put("arg", 2.5d);
            assertEquals("double", Ognl.getValue(tree, context, formatter));
        }

        assertEquals(3, methods("format", false).resolutionCount());
    }

    @Test
    void nullArgumentIsResolvedSeparatelyFromNonNull() throws Exception {
        Object tree = Ognl.parseExpression("echo(#arg)");

        for (int round = 0; round < 3; round++) {
            context.put("arg", null);
            assertEquals("null", Ognl.getValue(tree, context, formatter));
            context.put("arg", "text");
            assertEquals("text", Ognl.getValue(tree, context, formatter));
        }

        assertEquals(2, methods("echo", false).resolutionCount());
    }

    @Test
    void varArgsCallKeepsWorkingWhenRepeated() throws Exception {
        Object tree = Ognl.parseExpression("join(\"-\", \"a\", \"b\", \"c\")");
        Object shorter = Ognl.parseExpression("join(\"-\", \"a\")");

        for (int round = 0; round < 3; round++) {
            assertEquals("a-b-c", Ognl.getValue(tree, context, formatter));
            assertEquals("a", Ognl.getValue(shorter, context, formatter));
        }
    }

    @Test
    void staticAndInstanceMethodsOfTheSameNameAreResolvedSeparately() throws Exception {
        Object instanceCall = Ognl.parseExpression("twice(2)");
        Object staticCall = Ognl.parseExpression("@ognl.OgnlRuntimeMethodResolutionCacheTest$Formatter@twice(\"a\")");

        for (int round = 0; round < 3; round++) {
            assertEquals("instance", Ognl.getValue(instanceCall, context, formatter));
            assertEquals("static", Ognl.getValue(staticCall, context, formatter));
        }

        assertNotSame(methods("twice", false), methods("twice", true));
        assertEquals(1, methods("twice", false).resolutionCount());
        assertEquals(1, methods("twice", true).resolutionCount());
    }

    @Test
    void rememberedMethodStillHonoursMemberAccess() throws Exception {
        Object tree = Ognl.parseExpression("format(1)");
        assertEquals("int", Ognl.getValue(tree, context, formatter));
        assertEquals("int", Ognl.getValue(tree, context, formatter));

        OgnlContext denying = Ognl.createDefaultContext(formatter, new DenyingAccess("format"));
        assertThrows(OgnlException.class, () -> Ognl.getValue(tree, denying, formatter));
        assertThrows(OgnlException.class, () -> Ognl.getValue(tree, denying, formatter));

        assertEquals("int", Ognl.getValue(tree, context, formatter));
    }

    @Test
    void ambiguousChoiceIsNotRemembered() throws Exception {
        // Both overloads match (1, 2) equally well, which OgnlRuntime reports on System.err on every call
        Object tree = Ognl.parseExpression("ambiguous(1, 2)");

        Object first = Ognl.getValue(tree, context, formatter);
        Object second = Ognl.getValue(tree, context, formatter);

        assertEquals(first, second);
        assertEquals(0, methods("ambiguous", false).resolutionCount());
    }

    @Test
    void methodReplacedInTheListIsNotCalledFromARememberedChoice() throws Exception {
        Object tree = Ognl.parseExpression("original(\"x\")");
        assertEquals("original", Ognl.getValue(tree, context, formatter));
        assertEquals("original", Ognl.getValue(tree, context, formatter));
        MethodList methods = methods("original", false);
        assertEquals(1, methods.resolutionCount());

        // OgnlRuntime.getMethods() hands out the cached list itself, and set() is not a structural change
        methods.set(0, Formatter.class.getMethod("replacement", String.class));

        assertEquals(0, methods.resolutionCount());
        assertEquals("replacement", Ognl.getValue(tree, context, formatter));
        assertEquals("replacement", Ognl.getValue(tree, context, formatter));
        assertEquals(1, methods.resolutionCount());
    }

    @Test
    void clearCacheDropsRememberedMethods() throws Exception {
        Object tree = Ognl.parseExpression("format(1)");
        assertEquals("int", Ognl.getValue(tree, context, formatter));
        MethodList before = methods("format", false);
        assertEquals(1, before.resolutionCount());

        OgnlRuntime.clearCache();

        MethodList after = methods("format", false);
        assertNotSame(before, after);
        assertEquals(0, after.resolutionCount());
        assertEquals("int", Ognl.getValue(tree, context, formatter));
    }

    @Test
    void listsSuppliedByCallersAreResolvedAsBefore() {
        List<Method> copy = new ArrayList<>(methods("format", false));
        Object[] args = {"text"};

        for (int round = 0; round < 2; round++) {
            Method method = OgnlRuntime.getAppropriateMethod(context, formatter, formatter, null, "format", copy,
                    args, new Object[1]);
            assertEquals(String.class, method.getParameterTypes()[0]);
        }

        assertEquals(0, methods("format", false).resolutionCount());
    }

    private static class DenyingAccess extends AbstractMemberAccess {

        private final String deniedMember;

        DenyingAccess(String deniedMember) {
            this.deniedMember = deniedMember;
        }

        @Override
        public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
            return Modifier.isPublic(member.getModifiers()) && !member.getName().equals(deniedMember);
        }
    }

    @Test
    void argumentClassFromChildLoaderOfTargetIsNotRemembered() throws Exception {
        List<Object> list = new ArrayList<>();
        context.put("list", list);
        Object tree = Ognl.parseExpression("#list.add(#arg)");

        context.put("arg", "text");
        Ognl.getValue(tree, context, formatter);
        context.put("arg", new Formatter());
        Ognl.getValue(tree, context, formatter);
        Ognl.getValue(tree, context, formatter);

        assertEquals(3, list.size());
        MethodList add = assertInstanceOf(MethodList.class, OgnlRuntime.getMethods(ArrayList.class, "add", false));
        assertEquals(1, add.resolutionCount());
    }


    private static final class ChildLoader extends ClassLoader {

        ChildLoader() {
            super(OgnlRuntimeMethodResolutionCacheTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.equals(Formatter.class.getName())) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (java.io.InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = in.readAllBytes();
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (java.io.IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return loaded;
            }
        }
    }

    @Test
    void typeClassFromChildLoaderOfListOwnerIsNotRemembered() throws Exception {
        Object target = new ChildLoader().loadClass(Formatter.class.getName()).getDeclaredConstructor().newInstance();
        List<Method> methods = OgnlRuntime.getMethods(Object.class, "toString", false);

        OgnlRuntime.callAppropriateMethod(context, target, target, "toString", null, methods, new Object[0]);

        assertEquals(0, assertInstanceOf(MethodList.class, methods).resolutionCount());
    }

    @Test
    void typeClassMatchingListOwnerIsRemembered() throws Exception {
        List<Method> methods = OgnlRuntime.getMethods(Formatter.class, "toString", false);

        OgnlRuntime.callAppropriateMethod(context, formatter, formatter, "toString", null, methods, new Object[0]);

        assertEquals(1, assertInstanceOf(MethodList.class, methods).resolutionCount());
    }

}
