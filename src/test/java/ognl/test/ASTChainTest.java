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
import ognl.test.objects.IndexedSetObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ASTChainTest {

    @Test
    void getIndexedValue() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        IndexedSetObject root = new IndexedSetObject();

        String expr = "thing[\"x\"].val";

        assertEquals(1, Ognl.getValue(expr, context, root));

        Ognl.setValue(expr, context, root, 2);

        assertEquals(2, Ognl.getValue(expr, context, root));
    }
}
