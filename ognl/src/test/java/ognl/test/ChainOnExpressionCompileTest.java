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
import ognl.test.objects.ObjectParameterTarget;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A method or property chained on a parenthesised expression must compile to a call on the boxed result, as
 * interpreted mode evaluates it (#735).
 */
class ChainOnExpressionCompileTest {

    private OgnlContext context;
    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
        context.put("i", 2);
        context.put("t", new ObjectParameterTarget());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "(1 + 2).toString()",
            "(-3).toString()",
            "(-(1 + 2)).toString()",
            "(1 + 2).intValue()",
            "(1L + 2).toString()",
            "(1.5 + 2).toString()",
            "(1 + 2).toString().length()",
            "(1 + 2).class",
            "(#i + 2).toString()",
            "(1 & 2).toString()",
            "(1 + 2).hashCode()",
            "(2 * 3).equals(6)",
            "(1 + 2).compareTo(#i)",
            "index.toString()",
            "index.hashCode()",
            "index.doubleValue()",
            "(1 == 1).booleanValue()",
            "(1 < 2).toString()",
            "(index + 1).toString()",
            "(index * 2).toString()",
            "(1 + 2).toString() + \"x\"",
            "(1 + 2).toString() == \"3\"",
            "(1 + 2).toString().getClass().name",
            "('a' + 1).toString()",
            "(1 == 1).toString()",
            "(!true).toString()",
            "(1 > 0 ? 1 : 2).toString()",
            "(true ? 1 : 2).longValue()",
            "(true ? 1 : 2).doubleValue()",
            "(true ? \"a\" : \"b\").length()",
            "(true || false).toString()",
            "(true && false).toString()",
            "(true || false).booleanValue()",
            "(map.size() + 1).toString()",
            "(map.size() + 1 + index).toString()",
            "(map.size() * 2).toString()",
            "(array[1] + 1).toString()",
            "(list.size() + 1).toString()",
            "(getMap().size() + 1).toString()",
            "#t.take((1 + 2).toString())",
            "#t.take((1 + 2).toString().length())",
    })
    void chainedCallMatchesInterpretedMode(String expression) throws Exception {
        Object expected = Ognl.getValue(expression, context, root);

        Node node = (Node) Ognl.compileExpression(context, root, expression);
        Object actual = node.getAccessor().get(context, root);

        assertEquals(expected, actual, expression);
        assertEquals(expected.getClass(), actual.getClass(), expression);
    }

    @Test
    void setterPassReachesTheChainedArgument() throws Exception {
        ObjectParameterTarget target = new ObjectParameterTarget();
        Node node = (Node) Ognl.compileExpression(context, target, "wrap((1 + 2).toString()).extra");
        node.getAccessor().set(context, target, "x");
        assertEquals("String=3", target.getWrapped());
        assertEquals("x", target.getExtra());
    }
}
