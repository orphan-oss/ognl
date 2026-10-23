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
import ognl.ExcludedObjectMemberAccess;
import ognl.MethodFailedException;
import ognl.Node;
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.enhance.ExpressionAccessor;
import ognl.test.objects.Root;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Compiled and interpreted evaluation must agree on which members the {@code MemberAccess}
 * and the stricter-invocation rules let an expression reach.
 */
class CompiledMemberAccessTest {

    private final Root root = new Root();

    @Test
    void deniedPublicInstanceFieldIsRefusedInBothModes() throws Exception {
        ExcludedObjectMemberAccess memberAccess = new ExcludedObjectMemberAccess(false);
        memberAccess.exclude(Root.class.getField("anotherStringValue"));

        assertThrows(NoSuchPropertyException.class, () -> getValueInterpreted("anotherStringValue", memberAccess));

        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        ExpressionAccessor accessor = compile("anotherStringValue", context);
        assertThrows(NoSuchPropertyException.class, () -> accessor.get(context, root));
    }

    @Test
    void deniedPublicStaticFieldIsRefusedInBothModes() throws Exception {
        ExcludedObjectMemberAccess memberAccess = new ExcludedObjectMemberAccess(false);
        memberAccess.exclude(Root.class.getField("STATIC_INT"));

        assertThrows(NoSuchPropertyException.class, () -> getValueInterpreted("STATIC_INT", memberAccess));

        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        ExpressionAccessor accessor = compile("STATIC_INT", context);
        assertThrows(NoSuchPropertyException.class, () -> accessor.get(context, root));
    }

    @Test
    void allowedPublicFieldIsReadInBothModes() throws Exception {
        DefaultMemberAccess memberAccess = new DefaultMemberAccess(false);

        assertEquals("foo", getValueInterpreted("anotherStringValue", memberAccess));

        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        assertEquals("foo", compile("anotherStringValue", context).get(context, root));
    }

    @Test
    void staticMethodBlockedByStricterInvocationIsRefusedInBothModes() throws Exception {
        DefaultMemberAccess memberAccess = new DefaultMemberAccess(false);

        assertThrows(MethodFailedException.class, () -> getValueInterpreted("@java.lang.Runtime@getRuntime()", memberAccess));

        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        ExpressionAccessor accessor = compile("@java.lang.Runtime@getRuntime()", context);
        assertThrows(MethodFailedException.class, () -> accessor.get(context, root));
    }

    @Test
    void allowedStaticMethodIsCalledInBothModes() throws Exception {
        DefaultMemberAccess memberAccess = new DefaultMemberAccess(false);

        assertEquals(2, getValueInterpreted("@java.lang.Math@max(1, 2)", memberAccess));

        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        assertEquals(2, compile("@java.lang.Math@max(1, 2)", context).get(context, root));
    }

    private Object getValueInterpreted(String expression, DefaultMemberAccess memberAccess) throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        return Ognl.getValue(expression, context, root);
    }

    @SuppressWarnings("unchecked")
    private ExpressionAccessor compile(String expression, OgnlContext context) throws Exception {
        Node node = Ognl.compileExpression(context, root, expression);
        return node.getAccessor();
    }
}
