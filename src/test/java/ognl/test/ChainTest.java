/*
 * Copyright 2020 OGNL Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ognl.test;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.SimpleNode;
import ognl.test.objects.Root;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link SimpleNode#isChain(OgnlContext)}.
 */
public class ChainTest {

    @Test
    public void test_isChain() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("#name.lastChar");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("#{name.lastChar, #boo}");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("boo = #{name.lastChar, #boo, foo()}");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("{name.lastChar, #boo, foo()}");
        assertTrue(node.isChain(context));

        node = (SimpleNode) Ognl.parseExpression("(name.lastChar, #boo, foo())");
        assertTrue(node.isChain(context));
    }

    @Test
    public void shouldShortCircuitAccessingNullChild() throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(null);
        Parent parent = new Parent(new Parent(null));
        context.put("parent", parent);

        assertNull(Ognl.getValue("#parent.child.child.name", context, parent));
    }

    @Test
    public void shouldEvaluateThisProperty() throws OgnlException {
        Root root = new Root();
        OgnlContext context = Ognl.createDefaultContext(root);

        assertEquals("empty", Ognl.getValue("map[$].(#this == null ? 'empty' : #this)", context, root));
    }

    public static class Child {
        private final String name;

        public Child(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Parent extends Child {
        private final Child child;

        public Parent(Child child) {
            super("parent of " + child);
            this.child = child;
        }

        public Child getChild() {
            return child;
        }
    }

}
