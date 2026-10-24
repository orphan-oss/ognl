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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A numeric expression passed to an {@code Object} parameter must arrive as the computed number in compiled mode,
 * exactly as it does in interpreted mode (#694).
 */
class ObjectParameterCompileTest {

    private OgnlContext context;
    private ObjectParameterTarget root;

    @BeforeEach
    void setUp() {
        root = new ObjectParameterTarget();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(false));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "take(1 + 2)       | Integer=3",
            "take(1 + 2 + 3)   | Integer=6",
            "take(1L + 2)      | Long=3",
            "take(1.5 + 1)     | Double=2.5",
            "take((1 + 2))     | Integer=3",
            "take(1 - 2)       | Integer=-1",
            "take(2 * 3)       | Integer=6",
            "take(7 / 2)       | Integer=3",
            "take(7 % 2)       | Integer=1",
    })
    void instanceMethodBoxesTheWholeArgument(String expression, String expected) throws Exception {
        assertBothModes(expression, expected);
    }

    @Test
    void setterPassBoxesTheWholeArgument() throws Exception {
        Ognl.setValue("wrap(1 + 2).extra", context, root, "x");
        assertEquals("Integer=3", root.getWrapped(), "interpreted");

        root = new ObjectParameterTarget();
        Node node = (Node) Ognl.compileExpression(context, root, "wrap(1 + 2).extra");
        node.getAccessor().set(context, root, "y");
        assertEquals("Integer=3", root.getWrapped(), "compiled");
        assertEquals("y", root.getExtra(), "compiled");
    }

    private void assertBothModes(String expression, String expected) throws Exception {
        assertEquals(expected, Ognl.getValue(expression, context, root), "interpreted: " + expression);

        Node node = (Node) Ognl.compileExpression(context, root, expression);
        assertEquals(expected, node.getAccessor().get(context, root), "compiled: " + expression);
    }
}
