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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A string constant used as an operand of a numeric operator must behave the same in compiled and interpreted mode.
 */
class NumericStringConstantCompileTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        context.put("c", 'a');
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-\"a b\"",   // a space would break the generated `-<text>` into two tokens
            "~\"a b\"",
            "-\"x)y\"",   // a paren would unbalance the generated source
            "1 + \"a\" * 2",
            "\"a\" - 1",
    })
    void compiledModeMatchesInterpretedMode(String expression) {
        Class<?> interpreted = assertThrows(Exception.class,
                () -> ((Node) Ognl.parseExpression(expression)).getValue(context, root)).getClass();
        Class<?> compiled = assertThrows(Exception.class,
                () -> Ognl.compileExpression(context, root, expression).getAccessor().get(context, root)).getClass();
        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"a\" + stringValue",
            "\"a\\\\\" + 1",   // backslash at the end of the literal
            "\"x\\ny\" + 1",   // newline inside the literal
            "\"q\\\"q\" + 1",  // quote inside the literal
            "#c + \"b\"",      // string literal after a character-typed value
            "#c + \"b\\\\\"",  // escaping still applies after a character-typed value
            "\"b\" + #c",      // character-typed value after a string literal
            "\"a\" + 1"       // character-typed value and numeric value concatenation
    })
    void concatenatedLiteralMatchesInterpretedMode(String expression) throws Exception {
        Object interpreted = ((Node) Ognl.parseExpression(expression)).getValue(context, root);
        Object compiled = Ognl.compileExpression(context, root, expression).getAccessor().get(context, root);
        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
    }
}
