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
 * A long or float constant carries its own literal suffix in compiled source, so no node that embeds it may add
 * another one (#721).
 */
class NumericLiteralSuffixCompileTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-5L>>>2",
            "-5L >> 1",
            "-5L << 1",
            "-5L * 2",
            "5L & 3",
            "~5L",
            "5L * 2",
            "7L / 2",
            "5L | 3",
            "1L << 40",
            "2147483647L * 2L",
            "(2147483647L * 2L) - 1",
            "1000000L * 1000000L",
            "--1f",
            "new ognl.test.objects.Simple(\"foo\", 1.0f, 2).floatValue",
    })
    void compiledModeMatchesInterpretedMode(String expression) throws Exception {
        Object interpreted = Ognl.getValue(expression, context, root);

        Node node = (Node) Ognl.compileExpression(context, root, expression);
        Object compiled = node.getAccessor().get(context, root);

        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
        assertEquals(interpreted.getClass(), compiled.getClass(), "compiled type diverged from interpreted for: " + expression);
    }
}
