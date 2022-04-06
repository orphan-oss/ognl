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
package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.DefaultMemberAccess;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.SimpleNode;

/**
 * Tests for {@link SimpleNode#isChain(OgnlContext)}.
 */
public class ChainTest extends TestCase {

    public void test_isChain() throws Exception {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

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

}
