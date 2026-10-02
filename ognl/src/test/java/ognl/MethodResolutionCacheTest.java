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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue #651: the method chosen for a call is remembered per type, name and argument types.
 */
class MethodResolutionCacheTest {

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
        Object staticCall = Ognl.parseExpression("@ognl.MethodResolutionCacheTest$Formatter@twice(\"a\")");

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
    void ambiguousChoiceIsReportedOnEveryCall() throws Exception {
        Object tree = Ognl.parseExpression("ambiguous(1, 2)");
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        Object first;
        Object second;
        try {
            System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
            first = Ognl.getValue(tree, context, formatter);
            second = Ognl.getValue(tree, context, formatter);
        } finally {
            System.setErr(originalErr);
        }

        assertEquals(first, second);
        String[] reports = captured.toString(StandardCharsets.UTF_8).split("please report!", -1);
        assertEquals(3, reports.length, "expected the ambiguity to be reported once per call");
        assertEquals(0, methods("ambiguous", false).resolutionCount());
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

    @Test
    void methodListDropsResolutionsWhenModified() throws Exception {
        MethodList list = new MethodList();
        list.add(Formatter.class.getMethod("echo", String.class));
        assertNull(list.getResolution("key"));
        assertEquals(0, list.resolutionCount());

        list.putResolution("key", "value");
        assertSame("value", list.getResolution("key"));
        assertEquals(1, list.resolutionCount());

        list.add(Formatter.class.getMethod("format", int.class));
        assertNull(list.getResolution("key"));
        assertEquals(0, list.resolutionCount());

        list.putResolution("other", "value");
        assertNull(list.getResolution("key"));
        assertEquals(1, list.resolutionCount());
    }

    @Test
    void methodListStillComparesAsAList() throws Exception {
        Method echo = Formatter.class.getMethod("echo", String.class);
        MethodList list = new MethodList();
        list.add(echo);
        list.putResolution("key", "value");

        List<Method> plain = new ArrayList<>();
        plain.add(echo);

        assertEquals(plain, list);
        assertEquals(plain.hashCode(), list.hashCode());
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

}
