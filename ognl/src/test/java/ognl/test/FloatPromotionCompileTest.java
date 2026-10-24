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
 * Interpreted mode promotes a float combined with an int or long to double; compiled mode must do the same
 * instead of computing in float (#727).
 */
class FloatPromotionCompileTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        context.put("f", 16777216f);
        context.put("i", 1);
        context.put("s", (short) 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "16777216f + 1",
            "16777216f + 1L",
            "1 + 16777216f",
            "1.5f + 1",
            "1 - 1.5f",
            "2 * 1.5f",
            "3f / 2",
            "7.5f % 2",
            "5f-2F+0",
            "16777216f + 1 + 1.0",
            "(16777216f + 1) + 2.5f",
            "16777216f + 1 + 2.5f",
            "#f + 1",
            "#f + #i",
            "#f * 2L",
            "16777216f + 1 + \"x\"",
            "16777216f + 1 + 'c'",
            "\"x\" + 16777216f + 1",
            "(16777216f * #s) + 1",
            "16777216f * #s + 1",
            "(16777216f * #s) - 1",
            "16777216f + #s",
            "1.5f + 2.5f",
            "5f-2F",
            "--1f",
            "1.5f + 1.0",
            "1.5f + 1.5",
    })
    void compiledModeMatchesInterpretedMode(String expression) throws Exception {
        Object interpreted = Ognl.getValue(expression, context, root);

        Node node = (Node) Ognl.compileExpression(context, root, expression);
        Object compiled = node.getAccessor().get(context, root);

        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
        assertEquals(interpreted.getClass(), compiled.getClass(), "compiled type diverged from interpreted for: " + expression);
    }
}
