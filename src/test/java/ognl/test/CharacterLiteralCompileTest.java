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

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * A character constant is carried into generated source as a numeric cast, so javassist does not have
 * to read it as a quoted literal. Its lexer decodes only {@code \n \t \r \f}, and it keeps char
 * arithmetic at char width where javac promotes to int.
 *
 * <p>The quoted form did not only fail to compile. For 30 characters in the ISO-control ranges it
 * compiled and returned the <em>wrong answer</em>, because javassist does not decode the unicode
 * escape that {@code getEscapedChar} produces: a vertical tab compared against {@code 'a'} was
 * {@code true} compiled and {@code false} interpreted.
 * {@link #isoControlCharactersAgreeWithInterpretedMode} covers the whole byte range.</p>
 *
 * <p>Backport of the character-literal part of <a
 * href="https://github.com/orphan-oss/ognl/issues/689">issue #689</a>. The {@code +} cases that issue
 * reports are not fixed here: on this branch {@code ASTAdd} rebuilds a character constant as a quoted
 * string from its value, so they need the compiled-mode rework that is on main.</p>
 */
public class CharacterLiteralCompileTest {

    private Root root;
    private OgnlContext context;

    @Before
    public void setUp() {
        root = new Root();
        context = (OgnlContext) Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
    }

    private void assertBothModes(String expression, Object expected) throws Exception {
        assertEquals("Interpreted failed for: " + expression,
                expected, Ognl.getValue(Ognl.parseExpression(expression), context, root));

        OgnlContext compiledContext = (OgnlContext) Ognl.createDefaultContext(root, context.getMemberAccess());
        Node compiled = Ognl.compileExpression(compiledContext, root, expression);
        assertEquals("Compiled failed for: " + expression,
                expected, compiled.getAccessor().get(compiledContext, root));
    }

    // Would not compile at all before: javassist cannot read a raw backspace in a quoted char literal

    @Test
    public void backspaceComparison() throws Exception {
        assertBothModes("'\\b' > 'a'", Boolean.FALSE);
    }

    @Test
    public void backspaceEquality() throws Exception {
        assertBothModes("'\\b' == '\\b'", Boolean.TRUE);
    }

    @Test
    public void backspaceInListLiteral() throws Exception {
        assertBothModes("{'\\b'}", Collections.singletonList('\b'));
    }

    // Compiled to a char-width result before, where OgnlOps promotes a character to int

    @Test
    public void subtraction() throws Exception {
        assertBothModes("'b' - 'a'", 1);
    }

    @Test
    public void multiplication() throws Exception {
        assertBothModes("'b' * 'a'", 9506);
    }

    @Test
    public void division() throws Exception {
        assertBothModes("'b' / 'a'", 1);
    }

    @Test
    public void modulus() throws Exception {
        assertBothModes("'b' % 'a'", 1);
    }

    @Test
    public void bitwiseOr() throws Exception {
        assertBothModes("'b' | 'a'", 99);
    }

    @Test
    public void bitwiseXor() throws Exception {
        assertBothModes("'b' ^ 'a'", 3);
    }

    @Test
    public void shiftLeft() throws Exception {
        assertBothModes("'b' << 1", 196);
    }

    @Test
    public void shiftRight() throws Exception {
        assertBothModes("'b' >> 1", 49);
    }

    @Test
    public void unsignedShiftRight() throws Exception {
        assertBothModes("'b' >>> 1", 49);
    }

    @Test
    public void negation() throws Exception {
        assertBothModes("-'b'", -98);
    }

    @Test
    public void bitwiseNot() throws Exception {
        assertBothModes("~'b'", -99);
    }

    @Test
    public void negationInEnclosingSubtraction() throws Exception {
        assertBothModes("1 - (-'b')", 99);
    }

    @Test
    public void subtractionInEnclosingAddition() throws Exception {
        assertBothModes("'b' - 'a' + 1", 2);
    }

    // Already matched, kept as regression guards for the new literal form

    @Test
    public void nul() throws Exception {
        assertBothModes("'\\u0000' < 'a'", Boolean.TRUE);
    }

    @Test
    public void comparison() throws Exception {
        assertBothModes("'a' > 'b'", Boolean.FALSE);
    }

    @Test
    public void listLiteral() throws Exception {
        assertBothModes("{'a','b'}", java.util.Arrays.asList('a', 'b'));
    }

    @Test
    public void singleQuoteWithString() throws Exception {
        assertBothModes("'\\'' + \"b\"", "'b");
    }

    @Test
    public void backslashWithString() throws Exception {
        assertBothModes("'\\\\' + \"b\"", "\\b");
    }

    @Test
    public void newlineWithString() throws Exception {
        assertBothModes("'\\n' + \"b\"", "\nb");
    }

    /**
     * The quoted form silently disagreed with interpreted mode for 30 characters and failed to compile
     * for one. Sweeping the range guards the whole class rather than the few values that were noticed.
     */
    @Test
    public void isoControlCharactersAgreeWithInterpretedMode() throws Exception {
        for (int c = 0; c <= 0xFF; c++) {
            String expression = "'\\u" + String.format("%04x", c) + "' > 'a'";

            Object interpreted;
            try {
                interpreted = Ognl.getValue(Ognl.parseExpression(expression), context, root);
            } catch (Exception parseRejected) {
                continue; // a quote or a backslash does not survive the expression text
            }

            OgnlContext compiledContext = (OgnlContext) Ognl.createDefaultContext(root, context.getMemberAccess());
            Node compiled = Ognl.compileExpression(compiledContext, root, expression);
            assertEquals("Modes diverged for: " + expression,
                    interpreted, compiled.getAccessor().get(compiledContext, root));
        }
    }
}
