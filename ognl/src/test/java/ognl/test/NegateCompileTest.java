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
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A negated expression, variable or property must compile to the value interpreted mode answers (#722).
 */
class NegateCompileTest {

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
        context.put("b", java.math.BigInteger.valueOf(2));
        context.put("by", (byte) 2);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-(1 + 2)",
            "-(1 - 2)",
            "-(2 * 3)",
            "-(7 / 2)",
            "-(7 % 2)",
            "-(5 | 3)",
            "-(1 << 2)",
            "-(1L + 2)",
            "-(1.5 + 1)",
            "-(1.5f + 1)",
            "-(1 + 2) + 10",
            "10 + -(1 + 2)",
            "-(1 + 2) * 2",
            "-(1 + 2) - 1",
            "-(1 + 2) + #i",
            "--(1 + 2)",
            "-(-(1 + 2))",
            "-3",
            "--3",
            "-(#s + 1)",
            "-(#i + 1)",
            "-#s",
            "-#i",
            "-#l",
            "-#d",
            "-#b",
            "-#by",
            "-(#s)",
            "~#s",
            "~#by",
            "-'a'",
            "-intValue",
            "-(intValue)",
            "-(intValue + 1)",
            "~(1 + 2)",
            "~#i",
            "~intValue",
            "~(intValue + 1)",
    })
    void compiledModeMatchesInterpretedMode(String expression) throws Exception {
        Object interpreted = Ognl.getValue(expression, context, root);

        Node node = (Node) Ognl.compileExpression(context, root, expression);
        Object compiled = node.getAccessor().get(context, root);

        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
        assertEquals(interpreted.getClass(), compiled.getClass(), "compiled type diverged from interpreted for: " + expression);
    }
}
