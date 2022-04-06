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
package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.DefaultMemberAccess;
import org.ognl.Node;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.test.objects.BaseBean;
import org.ognl.test.objects.FirstBean;
import org.ognl.test.objects.Root;
import org.ognl.test.objects.SecondBean;

/**
 * Tests functionality of casting inherited method expressions.
 */
public class InheritedMethodsTest extends TestCase {

    private static final Root ROOT = new Root();

    public void test_Base_Inheritance() throws Exception {
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
