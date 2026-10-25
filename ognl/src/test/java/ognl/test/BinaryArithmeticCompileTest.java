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

import ognl.DefaultMemberAccess;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.CharHolder;
import ognl.test.objects.ObjectParameterTarget;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A binary arithmetic expression on variables must compile to the value and class interpreted mode answers,
 * on its own, nested under another operator, and as a method or constructor argument (#733, #736, #719).
 */
class BinaryArithmeticCompileTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        context.put("s", (short) 2);
        context.put("i", 2);
        context.put("l", 2L);
        context.put("d", 2.5);
        context.put("by", (byte) 2);
        context.put("h", new CharHolder());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "#i * #i",
            "#i - #i",
            "#i / #i",
            "#i % #i",
            "#i + #i",
            "#i - 1",
            "2 * #i",
            "#l * #l",
            "#d * #d",
            "#s + #s",
            "#s - #s",
            "#s * #s",
            "#s / #s",
            "#s % #s",
            "#s + #s + #s",
            "#s << 1",
            "#by + #by",
            "#by - #by",
            "#by * #by",
            "-#by + #by",
            "#s + #i",
            "#s + 1",
            "#s * 2 - 1",
            "(#s * 2) - 1",
            "(2 * #s) - 1",
            "(2 * #s) + 1",
            "(2 * #s) * 3",
            "(2 * #i) - 1",
            "(2 * #i) + 1",
            "(2 * #i) / 2",
            "(2 * #i) % 3",
            "(2 * #i) - #i",
            "1 - (2 * #i)",
            "(2 - #i) - 1",
            "(#i - 1) - 1",
            "(#i * #i) - 1",
            "(#i + #i) - 1",
            "(#s + #s) * 2",
            "(#s + #s) - #s",
            "-(#s + #s)",
            "-(#s * #s)",
            "(2 * #l) - 1",
            "(2 * #d) - 1",
            "(1 + 2) - 1",
            "(2 * 3) - 1",
            "intValue * 2",
            "(intValue * 2) - 1",
            "#h.takesInteger(#i * #i)",
            "#h.takesInteger(#s * 2)",
            "#h.takesInteger(#i + #i)",
            "#h.takesInteger(#s + #s)",
            "#h.plusOne(#i * #i)",
            "#h.overload(#i * #i)",
            "@ognl.test.objects.CharHolder@statInteger(#i * #i)",
            "@ognl.test.objects.CharHolder@statInteger(1 + 2)",
            "new ognl.test.objects.ObjectParameterTarget(1 + 2).description",
            "new ognl.test.objects.ObjectParameterTarget(1 - 2).description",
            "new ognl.test.objects.ObjectParameterTarget(#i + 2).description",
            "new ognl.test.objects.ObjectParameterTarget(#i * 2).description",
            "new ognl.test.objects.ObjectParameterTarget(#s + #s).description",
    })
    void compiledModeMatchesInterpretedMode(String expression) throws Exception {
        Object interpreted = Ognl.getValue(expression, context, root);

        Node node = (Node) Ognl.compileExpression(context, root, expression);
        Object compiled = node.getAccessor().get(context, root);

        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
        assertEquals(interpreted.getClass(), compiled.getClass(), "compiled type diverged from interpreted for: " + expression);
    }

    @Test
    void setterPassCoercesTheArgumentOperands() throws Exception {
        ObjectParameterTarget target = new ObjectParameterTarget();
        Node node = (Node) Ognl.compileExpression(context, target, "wrap(#i * #i).extra");
        node.getAccessor().set(context, target, "x");

        assertEquals("Integer=4", target.getWrapped());
        assertEquals("x", target.getExtra());
    }
}
