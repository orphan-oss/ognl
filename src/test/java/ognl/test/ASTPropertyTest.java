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

import ognl.ASTChain;
import ognl.ASTConst;
import ognl.ASTProperty;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.SimpleNode;
import ognl.test.objects.BaseGeneric;
import ognl.test.objects.GameGeneric;
import ognl.test.objects.GameGenericObject;
import ognl.test.objects.GenericRoot;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ASTPropertyTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @Test
    void test_Get_Indexed_Property_Type() throws Exception {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(root.getClass(), context.getCurrentType());
        assertNull(context.getPreviousType());
        assertEquals(root, context.getCurrentObject());
        assertNull(context.getCurrentAccessor());
        assertNull(context.getPreviousAccessor());

        int type = p.getIndexedPropertyType(context, root);

        assertEquals(OgnlRuntime.INDEXED_PROPERTY_NONE, type);
        assertEquals(root.getClass(), context.getCurrentType());
        assertNull(context.getPreviousType());
        assertNull(context.getCurrentAccessor());
        assertNull(context.getPreviousAccessor());
    }

    @Test
    void test_Get_Value_Body() throws Exception {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(root.getClass(), context.getCurrentType());
        assertNull(context.getPreviousType());
        assertEquals(root, context.getCurrentObject());
        assertNull(context.getCurrentAccessor());
        assertNull(context.getPreviousAccessor());

        Object value = p.getValue(context, root);

        assertEquals(root.get("nested"), value);
        assertEquals(root.getClass(), context.getCurrentType());
        assertNull(context.getPreviousType());
        assertNull(context.getCurrentAccessor());
        assertNull(context.getPreviousAccessor());
    }

    @Test
    void test_Get_Source() {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(".get(\"nested\")", p.toGetSourceString(context, root));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(Map.class, context.getCurrentAccessor());
        assertEquals(root.getClass(), context.getPreviousType());
        assertNull(context.getPreviousAccessor());

        assertEquals(root.get("nested"), context.getCurrentObject());

        assertTrue(Map.class.isAssignableFrom(context.getCurrentAccessor()));

        assertEquals(root.getClass(), context.getPreviousType());
        assertNull(context.getPreviousAccessor());
    }

    @Test
    void test_Set_Source() {
        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(false);
        ASTConst pRef = new ASTConst(0);
        pRef.setValue("nested");
        pRef.jjtSetParent(p);
        p.jjtAddChild(pRef, 0);

        Map root = new Root().getMap();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(pRef);

        assertEquals(".put(\"nested\", $3)", p.toSetSourceString(context, root));
        assertEquals(Object.class, context.getCurrentType());
        assertEquals(root.get("nested"), context.getCurrentObject());

        assertTrue(Map.class.isAssignableFrom(context.getCurrentAccessor()));

        assertEquals(root.getClass(), context.getPreviousType());
        assertNull(context.getPreviousAccessor());
    }

    @Test
    void test_Indexed_Object_Type() {
        ASTProperty listp = new ASTProperty(0);
        listp.setIndexedAccess(false);

        ASTConst listc = new ASTConst(0);
        listc.setValue("list");
        listc.jjtSetParent(listp);
        listp.jjtAddChild(listc, 0);

        ASTProperty p = new ASTProperty(0);
        p.setIndexedAccess(true);

        ASTProperty pindex = new ASTProperty(0);

        ASTConst pRef = new ASTConst(0);
        pRef.setValue("genericIndex");
        pRef.jjtSetParent(pindex);
        pindex.jjtAddChild(pRef, 0);

        p.jjtAddChild(pindex, 0);

        Root root = new Root();

        context.setRoot(root);
        context.setCurrentObject(root);
        context.setCurrentNode(listp);

        assertEquals(".getList()", listp.toGetSourceString(context, root));
        assertEquals(List.class, context.getCurrentType());
        assertEquals(Root.class, context.getCurrentAccessor());
        assertNull(context.getPreviousAccessor());
        assertEquals(root.getClass(), context.getPreviousType());
        assertEquals(root.getList(), context.getCurrentObject());

        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.setRoot(root);
        context.setCurrentObject(root);

        ASTChain chain = new ASTChain(0);
        listp.jjtSetParent(chain);
        chain.jjtAddChild(listp, 0);

        context.setCurrentNode(chain);

        assertEquals(".getList()", chain.toGetSourceString(context, root));
        assertEquals(List.class, context.getCurrentType());
        assertEquals(Root.class, context.getCurrentAccessor());
        assertNull(context.getPreviousAccessor());
        assertEquals(Root.class, context.getPreviousType());
        assertEquals(root.getList(), context.getCurrentObject());

        assertEquals(".get(ognl.OgnlOps#getIntValue(((ognl.test.objects.Root)$2)..getGenericIndex().toString()))", p.toGetSourceString(context, root.getList()));
        assertEquals(root.getArray(), context.getCurrentObject());
        assertEquals(Object.class, context.getCurrentType());
    }

    @Test
    void test_Complicated_List() throws Exception {
        Root root = new Root();

        SimpleNode node = (SimpleNode) Ognl.compileExpression(context, root,
                "{ new ognl.test.objects.MenuItem('Home', 'Main', "
                        + "{ new ognl.test.objects.MenuItem('Help', 'Help'), "
                        + "new ognl.test.objects.MenuItem('Contact', 'Contact') }), " // end first item
                        + "new ognl.test.objects.MenuItem('UserList', getMessages().getMessage('menu.members')), " +
                        "new ognl.test.objects.MenuItem('account/BetSlipList', getMessages().getMessage('menu.account'), " +
                        "{ new ognl.test.objects.MenuItem('account/BetSlipList', 'My Bets'), " +
                        "new ognl.test.objects.MenuItem('account/TransactionList', 'My Transactions') }), " +
                        "new ognl.test.objects.MenuItem('About', 'About'), " +
                        "new ognl.test.objects.MenuItem('admin/Admin', getMessages().getMessage('menu.admin'), " +
                        "{ new ognl.test.objects.MenuItem('admin/AddEvent', 'Add event'), " +
                        "new ognl.test.objects.MenuItem('admin/AddResult', 'Add result') })}");

        assertTrue(List.class.isAssignableFrom(node.getAccessor().get(context, root).getClass()));
    }

    @Test
    void test_Set_Chain_Indexed_Property() throws Exception {
        Root root = new Root();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("tab.searchCriteriaSelections[index1][index2]");
        node.setValue(context, root, Boolean.FALSE);

        assertEquals(Boolean.FALSE, root.getTab().getSearchCriteriaSelections().get(1).get(1));
    }

    @Test
    void test_Set_Generic_Property() throws Exception {
        GenericRoot root = new GenericRoot();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("cracker.param");
        node.setValue(context, root, "0");

        assertEquals(0, root.getCracker().getParam());

        node.setValue(context, root, "10");

        assertEquals(10, root.getCracker().getParam());
    }

    @Test
    void test_Get_Generic_Property() throws Exception {
        GenericRoot root = new GenericRoot();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("cracker.param");
        node.setValue(context, root, "0");

        assertEquals(0, node.getValue(context, root));

        node.setValue(context, root, "10");

        assertEquals(10, node.getValue(context, root));
    }

    @Test
    void test_Set_Get_Multiple_Generic_Types_Property() throws Exception {
        BaseGeneric<GameGenericObject, Long> root = new GameGeneric();

        context.setRoot(root);
        context.setCurrentObject(root);

        SimpleNode node = (SimpleNode) Ognl.parseExpression("ids");
        node.setValue(context, root, new String[]{"0", "20", "43"});

        assertArrayEquals(new Long[]{0L, 20L, 43L}, root.getIds());
        Object actual = node.getValue(context, root);
        assertArrayEquals((Object[]) actual, root.getIds());
    }
}
