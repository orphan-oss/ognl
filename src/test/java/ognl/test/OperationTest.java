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
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.SimpleNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SimpleNode#isOperation(OgnlContext)}.
 */
public class OperationTest {

    @Test
    public void test_isOperation() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name = 'boo'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name['foo'] = 'bar'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#name.foo = 'bar' + 'foo'");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("{name.foo = 'bar' + 'foo', #name.foo()}");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("('bar' + 'foo', #name.foo())");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-bar");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-(#bar)");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-1");
        assertFalse(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("-(#bar+#foo)");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#bar=3,#foo=4(#bar-#foo)");
        assertTrue(node.isOperation(context));

        node = (SimpleNode) Ognl.parseExpression("#bar-3");
        assertTrue(node.isOperation(context));
    }

}
