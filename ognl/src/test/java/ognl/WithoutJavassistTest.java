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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs in the {@code without-javassist} surefire execution, which drops javassist from the test classpath.
 */
class WithoutJavassistTest {

    private static final String JAVASSIST_MISSING = "Compiled mode needs org.javassist:javassist on the classpath";

    public static class Bean {
        private String name = "x";
        private final List<Integer> nums = List.of(1, 2, 3);

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Integer> getNums() {
            return nums;
        }
    }

    private Bean bean;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        bean = new Bean();
        context = Ognl.createDefaultContext(bean, new DefaultMemberAccess(false));
    }

    @Test
    void javassistIsAbsent() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("javassist.ClassPool"));
    }

    @Test
    void readsAndWritesWithoutJavassist() throws OgnlException {
        assertEquals(3, Ognl.getValue("name.length() + nums.{? #this > 1}.size()", context, bean));

        Ognl.setValue("name", context, bean, "y");
        assertEquals("y", Ognl.getValue("name", context, bean));

        assertEquals(Map.of('k', 1), Ognl.getValue("#{'k': nums[0]}", context, bean));
        assertEquals(List.of(2, 4, 6), Ognl.getValue("nums.{#this * 2}", context, bean));
        assertEquals(2, Ognl.getValue("@java.lang.Math@max(1, 2)", context, bean));
        assertEquals(0, Ognl.getValue("new java.util.ArrayList().size()", context, bean));
    }

    @Test
    void compileFailsWithOgnlException() {
        OgnlException e = assertThrows(OgnlException.class, () -> Ognl.compileExpression(context, bean, "name"));

        assertEquals(JAVASSIST_MISSING, e.getMessage());
    }

    @Test
    void getCompilerFailsWithIllegalState() {
        IllegalStateException e = assertThrows(IllegalStateException.class, OgnlRuntime::getCompiler);

        assertEquals(JAVASSIST_MISSING, e.getMessage());
    }
}
