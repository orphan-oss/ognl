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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * A constant used as an operand or as an index key must behave the same in compiled and interpreted mode.
 */
public class NumericStringConstantCompileTest {

    private static final String[] NUMERIC_OPERANDS = {
            "-\"a b\"",   // a space would break the generated `-<text>` into two tokens
            "~\"a b\"",
            "-\"x)y\"",   // a paren would unbalance the generated source
            "1 + \"a\" * 2",
            "\"a\" - 1",
    };

    private static final String[] CONCATENATIONS = {
            "\"a\" + stringValue",
            "\"a\\\\\" + 1",   // backslash at the end of the literal
            "\"x\\ny\" + 1",   // newline inside the literal
            "\"q\\\"q\" + 1",  // quote inside the literal
            "#c + \"b\"",      // string literal after a character-typed value
            "#c + \"b\\\\\"",  // escaping still applies after a character-typed value
            "\"b\" + #c",      // character-typed value after a string literal
            "\"a\" + 1",
            "'\\'' + \"b\"",    // a quote character literal before a string literal
            "\"b\" + '\\''",    // a quote character literal after a string literal
            "'\\'' + 1",        // a quote character literal concatenated with a number
            "'\\\\' + \"b\"",   // a backslash character literal
            "'\\\"' + \"b\"",    // a double-quote character literal
            "\"x\\by\" + 1",     // backspace inside a string literal
            "'\\b' + \"y\"",     // backspace character literal
            "\"a\" + 1L",         // the long literal's compiled source carries an L suffix (#687)
            "\"a\" + 1.5f",       // the float literal's compiled source carries an f suffix
            "1L + \"a\"",
            "1.5f + \"a\"",
            "\"a\" + 1L + 2.5f",
            "\"a\" + 1.0"         // a double literal has no suffix and must keep working
    };

    private static final String[] CONTROL_CHARACTERS = {
            "\"a\u0001b\" + 1",   // U+0001, the lowest control character javassist has to carry raw
            "\"a\u000bb\" + 1",   // vertical tab, between the named escapes
            "\"a\u001bb\" + 1",   // escape, the highest C0 control character
            "\"a\u007fb\" + 1",   // delete
            "\"a\u0085b\" + 1",   // next line, a C1 control character
            "\"a\u009fb\" + 1",   // the highest C1 control character
            "'\u0001' + \"y\""    // a control character as a character literal
    };

    private static final String[] NON_CONSTANT_OPERANDS = {
            "\"a\" + (1 == 1)",  // a comparison operand is neither a constant nor one of the excluded node types
            "\"a\" + (1 > 2)",
            "\"a\" + !false",
            "\"a\" + {1, 2}",    // a list operand
            "\"a\" + new java.lang.StringBuilder(\"x\")"
    };

    private static final String[] NULL_NUMERIC_OPERANDS = {
            "1 * null",
            "1 - null",
            "1 / null",
            "null * 1"
    };

    private static final Object[][] INDEX_KEYS = {
            {"map[\"a\\\"b\"]", "a\"b"},        // a quote inside a string index key
            {"map[\"a\u0001b\"]", "a\u0001b"},  // a control character inside a string index key
            {"map['\\'']", '\''}                // a character index key
    };

    private OgnlContext context;
    private Root root;

    @Before
    public void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        context.put("c", 'a');
    }

    @Test
    public void compiledModeMatchesInterpretedMode() throws Exception {
        for (final String expression : NUMERIC_OPERANDS) {
            final Node node = (Node) Ognl.parseExpression(expression);
            Class<?> interpreted = assertThrows(Exception.class, () -> node.getValue(context, root)).getClass();
            Class<?> compiled = assertThrows(Exception.class,
                    () -> Ognl.compileExpression(context, root, expression).getAccessor().get(context, root)).getClass();
            assertEquals("compiled mode diverged from interpreted for: " + expression, interpreted, compiled);
        }
    }

    @Test
    public void concatenatedLiteralMatchesInterpretedMode() throws Exception {
        assertSameValue(CONCATENATIONS);
    }

    @Test
    public void controlCharacterLiteralMatchesInterpretedMode() throws Exception {
        assertSameValue(CONTROL_CHARACTERS);
    }

    @Test
    public void nonConstantOperandMatchesInterpretedMode() throws Exception {
        assertSameValue(NON_CONSTANT_OPERANDS);
    }

    @Test
    public void nullNumericOperandMatchesInterpretedMode() throws Exception {
        assertSameValue(NULL_NUMERIC_OPERANDS);
    }

    @Test
    public void indexKeyGetterMatchesInterpretedMode() throws Exception {
        for (Object[] indexKey : INDEX_KEYS) {
            String expression = (String) indexKey[0];
            root.getMap().put(indexKey[1], "found");

            Object interpreted = ((Node) Ognl.parseExpression(expression)).getValue(context, root);
            Object compiled = Ognl.compileExpression(context, root, expression).getAccessor().get(context, root);

            assertEquals("the index key " + indexKey[1] + " did not reach the map", "found", interpreted);
            assertEquals("compiled mode diverged from interpreted for: " + expression, interpreted, compiled);
        }
    }

    @Test
    public void indexKeySetterMatchesInterpretedMode() throws Exception {
        for (Object[] indexKey : INDEX_KEYS) {
            String expression = (String) indexKey[0];

            Root interpretedRoot = new Root();
            OgnlContext interpretedContext = Ognl.createDefaultContext(interpretedRoot, new DefaultMemberAccess(false));
            ((Node) Ognl.parseExpression(expression)).setValue(interpretedContext, interpretedRoot, "stored");

            Root compiledRoot = new Root();
            OgnlContext compiledContext = Ognl.createDefaultContext(compiledRoot, new DefaultMemberAccess(false));
            Ognl.compileExpression(compiledContext, compiledRoot, expression)
                    .getAccessor().set(compiledContext, compiledRoot, "stored");

            assertEquals("the interpreted setter did not store under " + indexKey[1],
                    "stored", interpretedRoot.getMap().get(indexKey[1]));
            assertEquals("compiled mode diverged from interpreted for: " + expression,
                    interpretedRoot.getMap().get(indexKey[1]), compiledRoot.getMap().get(indexKey[1]));
        }
    }

    private void assertSameValue(String[] expressions) throws Exception {
        for (String expression : expressions) {
            Object interpreted = ((Node) Ognl.parseExpression(expression)).getValue(context, root);
            Object compiled = Ognl.compileExpression(context, root, expression).getAccessor().get(context, root);
            assertEquals("compiled mode diverged from interpreted for: " + expression, interpreted, compiled);
        }
    }
}
