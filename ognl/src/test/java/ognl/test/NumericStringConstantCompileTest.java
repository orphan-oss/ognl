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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A constant used as an operand or as an index key must behave the same in compiled and interpreted mode.
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
    void compiledModeMatchesInterpretedMode(String expression) throws Exception {
        Node node = (Node) Ognl.parseExpression(expression);
        Class<?> interpreted = assertThrows(Exception.class, () -> node.getValue(context, root)).getClass();
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
            "\"a\" + 1",      // character-typed value and numeric value concatenation
            "\"x\\by\" + 1",   // backspace inside a string literal
            "'\\b' + \"y\"",   // backspace character literal
            "\"a\" + 1L",       // the long literal's compiled source carries an L suffix (#687)
            "\"a\" + 1.5f",     // the float literal's compiled source carries an f suffix
            "1L + \"a\"",
            "1.5f + \"a\"",
            "\"a\" + 1L + 2.5f",
            "\"a\" + 1.0"       // a double literal has no suffix and must keep working
    })
    void concatenatedLiteralMatchesInterpretedMode(String expression) throws Exception {
        assertSameValue(expression);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"a\u0001b\" + 1",   // U+0001, the lowest control character javassist has to carry raw
            "\"a\u000bb\" + 1",   // vertical tab, between the named escapes
            "\"a\u001bb\" + 1",   // escape, the highest C0 control character
            "\"a\u007fb\" + 1",   // delete
            "\"a\u0085b\" + 1",   // next line, a C1 control character
            "\"a\u009fb\" + 1",   // the highest C1 control character
            "'\u0001' + \"y\""    // a control character as a character literal
    })
    void controlCharacterLiteralMatchesInterpretedMode(String expression) throws Exception {
        assertSameValue(expression);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"a\" + (1 == 1)",  // a comparison operand is neither a constant nor one of the excluded node types
            "\"a\" + (1 > 2)",
            "\"a\" + !false",
            "\"a\" + {1, 2}",    // a list operand
            "\"a\" + new java.lang.StringBuilder(\"x\")"
    })
    void nonConstantOperandMatchesInterpretedMode(String expression) throws Exception {
        assertSameValue(expression);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 * null", "1 - null", "1 / null", "null * 1"})
    void nullNumericOperandMatchesInterpretedMode(String expression) throws Exception {
        assertSameValue(expression);
    }

    static Stream<Arguments> indexKeys() {
        return Stream.of(
                Arguments.of("map[\"a\\\"b\"]", "a\"b"),  // a quote inside a string index key
                Arguments.of("map[\"a\u0001b\"]", "a\u0001b"),  // a control character inside a string index key
                Arguments.of("map['\\'']", '\'')           // a character index key
        );
    }

    @ParameterizedTest
    @MethodSource("indexKeys")
    void indexKeyGetterMatchesInterpretedMode(String expression, Object key) throws Exception {
        root.getMap().put(key, "found");

        Object interpreted = ((Node) Ognl.parseExpression(expression)).getValue(context, root);
        Object compiled = Ognl.compileExpression(context, root, expression).getAccessor().get(context, root);

        assertEquals("found", interpreted, "the index key " + key + " did not reach the map");
        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
    }

    @ParameterizedTest
    @MethodSource("indexKeys")
    void indexKeySetterMatchesInterpretedMode(String expression, Object key) throws Exception {
        Root interpretedRoot = new Root();
        OgnlContext interpretedContext = Ognl.createDefaultContext(interpretedRoot, new DefaultMemberAccess(false));
        ((Node) Ognl.parseExpression(expression)).setValue(interpretedContext, interpretedRoot, "stored");

        Root compiledRoot = new Root();
        OgnlContext compiledContext = Ognl.createDefaultContext(compiledRoot, new DefaultMemberAccess(false));
        Ognl.compileExpression(compiledContext, compiledRoot, expression)
                .getAccessor().set(compiledContext, compiledRoot, "stored");

        assertEquals("stored", interpretedRoot.getMap().get(key), "the interpreted setter did not store under " + key);
        assertEquals(interpretedRoot.getMap().get(key), compiledRoot.getMap().get(key),
                "compiled mode diverged from interpreted for: " + expression);
    }

    private void assertSameValue(String expression) throws Exception {
        Object interpreted = ((Node) Ognl.parseExpression(expression)).getValue(context, root);
        Object compiled = Ognl.compileExpression(context, root, expression).getAccessor().get(context, root);
        assertEquals(interpreted, compiled, "compiled mode diverged from interpreted for: " + expression);
    }
}
