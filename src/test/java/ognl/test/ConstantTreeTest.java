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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstantTreeTest {

    /**
     * Field used in test
     */
    public static int nonFinalStaticVariable = 15;

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        Root root = new Root();
        context = Ognl.createDefaultContext(root);
    }

    @Test
    void testTrue() throws Exception {
        assertTrue(Ognl.isConstant("true", context));
    }

    @Test
    void test55() throws Exception {
        assertTrue(Ognl.isConstant("55", context));
    }

    @Test
    void testJavaAwtColorBlack() throws Exception {
        assertTrue(Ognl.isConstant("@java.awt.Color@black", context));
    }

    @Test
    void testNonFinalStaticVariable() throws Exception {
        assertFalse(Ognl.isConstant("@ognl.test.ConstantTreeTest@nonFinalStaticVariable", context));
    }

    @Test
    void testNonFinalStaticVariablePlus10() throws Exception {
        assertFalse(Ognl.isConstant("@ognl.test.ConstantTreeTest@nonFinalStaticVariable + 10", context));
    }

    @Test
    void test55Plus24PlusJavaAwtEventAltMask() throws Exception {
        assertTrue(Ognl.isConstant("55 + 24 + @java.awt.Event@ALT_MASK", context));
    }

    @Test
    void testName() throws Exception {
        assertFalse(Ognl.isConstant("name", context));
    }

    @Test
    void testNameI() throws Exception {
        assertFalse(Ognl.isConstant("name[i]", context));
    }

    @Test
    void testNameIProperty() throws Exception {
        assertFalse(Ognl.isConstant("name[i].property", context));
    }

    @Test
    void testNameFoo() throws Exception {
        assertFalse(Ognl.isConstant("name.{? foo }", context));
    }

    @Test
    void testNameFoo2() throws Exception {
        assertFalse(Ognl.isConstant("name.{ foo }", context));
    }

    @Test
    void testName25() throws Exception {
        assertFalse(Ognl.isConstant("name.{ 25 }", context));
    }
}