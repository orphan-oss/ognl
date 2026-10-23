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
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
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

        assertThrows(OgnlException.class, () -> getValueInterpreted("anotherStringValue", memberAccess));
        assertThrows(OgnlException.class, () -> getValueCompiled("anotherStringValue", memberAccess));
    }

    @Test
    void deniedPublicStaticFieldIsRefusedInBothModes() throws Exception {
        ExcludedObjectMemberAccess memberAccess = new ExcludedObjectMemberAccess(false);
        memberAccess.exclude(Root.class.getField("STATIC_INT"));

        assertThrows(OgnlException.class, () -> getValueInterpreted("STATIC_INT", memberAccess));
        assertThrows(OgnlException.class, () -> getValueCompiled("STATIC_INT", memberAccess));
    }

    @Test
    void allowedPublicFieldIsReadInBothModes() throws Exception {
        DefaultMemberAccess memberAccess = new DefaultMemberAccess(false);

        assertEquals("foo", getValueInterpreted("anotherStringValue", memberAccess));
        assertEquals("foo", getValueCompiled("anotherStringValue", memberAccess));
    }

    @Test
    void staticMethodBlockedByStricterInvocationIsRefusedInBothModes() {
        DefaultMemberAccess memberAccess = new DefaultMemberAccess(false);

        assertThrows(OgnlException.class, () -> getValueInterpreted("@java.lang.Runtime@getRuntime()", memberAccess));
        assertThrows(OgnlException.class, () -> getValueCompiled("@java.lang.Runtime@getRuntime()", memberAccess));
    }

    @Test
    void allowedStaticMethodIsCalledInBothModes() throws Exception {
        DefaultMemberAccess memberAccess = new DefaultMemberAccess(false);

        assertEquals(2, getValueInterpreted("@java.lang.Math@max(1, 2)", memberAccess));
        assertEquals(2, getValueCompiled("@java.lang.Math@max(1, 2)", memberAccess));
    }

    private Object getValueInterpreted(String expression, DefaultMemberAccess memberAccess) throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        return Ognl.getValue(expression, context, root);
    }

    @SuppressWarnings("unchecked")
    private Object getValueCompiled(String expression, DefaultMemberAccess memberAccess) throws Exception {
        OgnlContext context = Ognl.createDefaultContext(root, memberAccess);
        Node node = Ognl.compileExpression(context, root, expression);
        return node.getAccessor().get(context, root);
    }
}
