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

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.SimpleNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

class ShortCircuitingExpressionTest {

    private OgnlContext context;

    @ParameterizedTest
    @MethodSource("testValues")
    void shouldShortCircuitExpressionEvaluations(String expression, Object expected) throws OgnlException {
        assertEquals(expected, Ognl.getValue(expression, null));
    }

    @Test
    void shouldEvaluateNumber() throws Exception {
        SimpleNode expression = (SimpleNode) Ognl.compileExpression(context, null, "(#x=99) && #x.doubleValue()");
        assertEquals(99.0, Ognl.getValue(expression, context, (Object) null));
    }

    @Test
    void shouldThrowException() {
        try {
            Ognl.getValue("#root ? 99 : someProperty", null);
            fail();
        } catch (Throwable e) {
            assertInstanceOf(OgnlException.class, e);
        }
    }

    public static class B {
        private String b;

        public B(String b) {
            this.b = b;
        }

        public String getB() {
            return b;
        }
    }

    public static class Params {
        public B[] params;

        public Params(B[] params) {
            this.params = params;
        }

        public B[] getParams() {
            return params;
        }
    }

    @Test
    void shouldCompare() throws OgnlException {
        Object root = new Params(new B[]{new B("a")});

        OgnlContext ctx = Ognl.createDefaultContext(null);

        Object val1 = Ognl.getValue("\"a\".equals(params[0].b)", ctx, root);
        Object val2 = Ognl.getValue("\"a\".equals(params[0].b)", root);

        assertEquals(Boolean.TRUE, val1);
        assertEquals(Boolean.TRUE, val2);
    }

    private static Stream<Arguments> testValues() {
        return Stream.of(
                Arguments.of("#root ? someProperty : 99", 99),
                Arguments.of("(#x=99)? #x.someProperty : #x", null),
                Arguments.of("#xyzzy.doubleValue()", null),
                Arguments.of("#xyzzy && #xyzzy.doubleValue()", null),
                Arguments.of("#xyzzy || 101", 101),
                Arguments.of("99 || 101", 99)
        );
    }

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
    }
}
