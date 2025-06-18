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
import ognl.test.objects.BaseBean;
import ognl.test.objects.FirstBean;
import ognl.test.objects.Root;
import ognl.test.objects.SecondBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests functionality of casting inherited method expressions.
 */
class InheritedMethodsTest {

    private static final Root ROOT = new Root();

    @Test
    void test_Base_Inheritance() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        String expression = "map.bean.name";
        BaseBean first = new FirstBean();
        BaseBean second = new SecondBean();

        ROOT.getMap().put("bean", first);

        Node node = Ognl.compileExpression(context, ROOT, expression);

        assertEquals(first.getName(), node.getAccessor().get(context, ROOT));

        ROOT.getMap().put("bean", second);

        assertEquals(second.getName(), node.getAccessor().get(context, ROOT));
    }
}
