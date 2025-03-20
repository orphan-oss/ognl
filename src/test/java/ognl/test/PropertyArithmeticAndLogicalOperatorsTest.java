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

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.Root;
import ognl.test.objects.SimpleNumeric;
import ognl.test.objects.TestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyArithmeticAndLogicalOperatorsTest {

    private Root root;
    private TestModel model;
    private SimpleNumeric numeric;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        model = new TestModel();
        numeric = new SimpleNumeric();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testBooleanExpressions() throws Exception {
        assertEquals(Boolean.TRUE, Ognl.getValue("objectIndex > 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("false", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("!false || true", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("property.bean3.value >= 24", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("(unassignedCopyModel.optionCount > 0 && canApproveCopy) || entry.copy.size() > 0", context, model));
        assertEquals(Boolean.FALSE, Ognl.getValue(" !(printDelivery || @Boolean@FALSE)", context, root));
    }

    @Test
    void testIntegerExpressions() throws Exception {
        assertEquals(1, Ognl.getValue("genericIndex-1", context, root));
        assertEquals(((root.getRenderNavigation() ? 0 : 1) + root.getMap().size()) * root.getTheInt(), Ognl.getValue("((renderNavigation ? 0 : 1) + map.size) * theInt", context, root));
        assertEquals(Arrays.asList(root.getTheInt() + 1), Ognl.getValue("{theInt + 1}", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("(getIndexedProperty('nested').size - 1) > genericIndex", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("(getIndexedProperty('nested').size + 1) >= genericIndex", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("(getIndexedProperty('nested').size + 1) == genericIndex", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("(getIndexedProperty('nested').size + 1) < genericIndex", context, root));
        assertEquals(root.getMap().size() * ((Integer) root.getGenericIndex()).intValue(), Ognl.getValue("map.size * genericIndex", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("property == property", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("property.bean3.value % 2 == 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("genericIndex % 3 == 0", context, root));
        assertEquals(Boolean.FALSE, Ognl.getValue("genericIndex % theInt == property.bean3.value", context, root));
        assertEquals(root.getTheInt() / 100.0, Ognl.getValue("theInt / 100.0", context, root));
        assertEquals(Boolean.TRUE, Ognl.getValue("@java.lang.Long@valueOf('100') == @java.lang.Long@valueOf('100')", context, root));
    }

    @Test
    void testDoubleExpressions() throws Exception {
        assertEquals(numeric.getBudget() - numeric.getTimeBilled(), Ognl.getValue("budget - timeBilled", context, numeric));
        assertEquals(Boolean.TRUE, Ognl.getValue("(budget % tableSize) == 0", context, numeric));
    }
}