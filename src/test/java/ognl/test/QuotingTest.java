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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuotingTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
    }

    @Test
    void testCharacterQuoting() throws Exception {
        assertEquals('c', Ognl.getValue("'c'", context, (Object) null));
        assertEquals('s', Ognl.getValue("'s'", context, (Object) null));
    }

    @Test
    void testStringQuoting() throws Exception {
        assertEquals("string", Ognl.getValue("'string'", context, (Object) null));
        assertEquals("string", Ognl.getValue("\"string\"", context, (Object) null));
        assertEquals("bar", Ognl.getValue("'' + 'bar'", context, (Object) null));
        assertEquals("yyyy年MM月dd日", Ognl.getValue("'yyyy年MM月dd日'", context, (Object) null));
    }

}
