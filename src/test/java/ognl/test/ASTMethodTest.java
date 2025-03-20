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

import ognl.ASTConst;
import ognl.ASTMethod;
import ognl.ASTProperty;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.SimpleNode;
import ognl.enhance.ExpressionCompiler;
import ognl.test.objects.Bean2;
import ognl.test.objects.Bean3;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ASTMethodTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @Test
    void contextTypes() {
        ASTMethod p = new ASTMethod(0);
        p.setMethodName("get");

        ASTConst pRef = new ASTConst(0);
        pRef.setValue("value");
        p.jjtAddChild(pRef, 0);

        Root root = new Root();

        context.setRoot(root.getMap());
        context.setCurrentObject(root.getMap());
        context.setCurrentType(root.getMap().getClass());

        assertEquals(".get(\"value\")", p.toGetSourceString(context, root.getMap()));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(context.getCurrentObject(), root.getMap().get("value"));
        assertTrue(Map.class.isAssignableFrom(context.getCurrentAccessor()));
        assertTrue(Map.class.isAssignableFrom(context.getPreviousType()));
        assertNull(context.getPreviousAccessor());

        assertEquals(".get(\"value\")", OgnlRuntime.getCompiler().castExpression(context, p, ".get(\"value\")"));
        assertNull(context.get(ExpressionCompiler.PRE_CAST));

        // now test one context level further to see casting work properly on base object types

        ASTProperty prop = new ASTProperty(0);
        ASTConst propRef = new ASTConst(0);
        propRef.setValue("bean3");
        prop.jjtAddChild(propRef, 0);

        Bean2 val = (Bean2) root.getMap().get("value");

        assertEquals(".getBean3()", prop.toGetSourceString(context, root.getMap().get("value")));

        assertEquals(context.getCurrentObject(), val.getBean3());
        assertEquals(Bean3.class, context.getCurrentType());
        assertEquals(Bean2.class, context.getCurrentAccessor());
        assertEquals(Object.class, context.getPreviousType());
        assertTrue(Map.class.isAssignableFrom(context.getPreviousAccessor()));

        assertEquals(").getBean3()", OgnlRuntime.getCompiler().castExpression(context, prop, ".getBean3()"));
    }

    @Test
    void isSimpleMethod() throws Exception {
        SimpleNode node = (SimpleNode) Ognl.parseExpression("#name");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("#name.lastChar");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("execute()");
        assertTrue(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("bean.execute()");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("bean.execute()");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("{name.lastChar, #boo, foo()}");
        assertFalse(node.isSimpleMethod(context));

        node = (SimpleNode) Ognl.parseExpression("(name.lastChar, #boo, foo())");
        assertFalse(node.isSimpleMethod(context));
    }
}
