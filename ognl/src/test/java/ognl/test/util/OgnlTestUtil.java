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
package ognl.test.util;

import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class OgnlTestUtil {

    public enum EvaluationMode {
        INTERPRETED,
        COMPILED
    }

    private OgnlTestUtil() {
    }

    public static Object getValueInterpreted(String expression, OgnlContext context, Object root) throws OgnlException {
        Object tree = Ognl.parseExpression(expression);
        return ((Node) tree).getValue(context.withRoot(root), root);
    }

    @SuppressWarnings("unchecked")
    public static Object getValueCompiled(String expression, OgnlContext context, Object root) throws Exception {
        Node node = Ognl.compileExpression(context, root, expression);
        return node.getAccessor().get(context, root);
    }

    public static void assertBothModes(String expression, OgnlContext context, Object root, Object expected) throws Exception {
        Object interpreted = getValueInterpreted(expression, context, root);
        assertEquals(expected, interpreted, "Interpreted mode failed for: " + expression);

        // Reset context for compiled mode
        OgnlContext compiledContext = Ognl.createDefaultContext(root, context.getMemberAccess());
        compiledContext.setValues(context.getValues());

        Object compiled = getValueCompiled(expression, compiledContext, root);
        assertEquals(expected, compiled, "Compiled mode failed for: " + expression);
    }

    public static void assertBothModesMatch(String expression, OgnlContext context, Object root) throws Exception {
        Object interpreted = getValueInterpreted(expression, context, root);

        OgnlContext compiledContext = Ognl.createDefaultContext(root, context.getMemberAccess());
        compiledContext.setValues(context.getValues());

        Object compiled = getValueCompiled(expression, compiledContext, root);
        assertEquals(interpreted, compiled,
                "Interpreted and compiled modes produced different results for: " + expression
                        + "\n  interpreted: " + interpreted + " (" + (interpreted != null ? interpreted.getClass().getName() : "null") + ")"
                        + "\n  compiled:    " + compiled + " (" + (compiled != null ? compiled.getClass().getName() : "null") + ")");
    }
}
