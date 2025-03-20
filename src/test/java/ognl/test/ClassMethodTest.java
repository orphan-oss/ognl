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
import ognl.test.objects.CorrectedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassMethodTest {

    private CorrectedObject corrected;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        corrected = new CorrectedObject();
        context = Ognl.createDefaultContext(corrected, new DefaultMemberAccess(true));
    }

    @Test
    void testGetClassName() throws Exception {
        Object actual = Ognl.getValue("getClass().getName()", context, corrected);
        assertEquals(corrected.getClass().getName(), actual);
    }

    @Test
    void testGetClassInterfaces() throws Exception {
        Class<?>[] actual = (Class<?>[]) Ognl.getValue("getClass().getInterfaces()", context, corrected);
        assertArrayEquals(corrected.getClass().getInterfaces(), actual);
    }

    @Test
    void testGetClassInterfacesLength() throws Exception {
        Object actual = Ognl.getValue("getClass().getInterfaces().length", context, corrected);
        assertEquals(corrected.getClass().getInterfaces().length, actual);
    }

    @Test
    void testSystemClassGetInterfaces() throws Exception {
        Class<?>[] actual = (Class<?>[]) Ognl.getValue("@System@class.getInterfaces()", context, corrected);
        assertArrayEquals(System.class.getInterfaces(), actual);
    }

    @Test
    void testClassGetName() throws Exception {
        Object actual = Ognl.getValue("@Class@class.getName()", context, corrected);
        assertEquals(Class.class.getName(), actual);
    }

    @Test
    void testImageObserverClassGetName() throws Exception {
        Object actual = Ognl.getValue("@java.awt.image.ImageObserver@class.getName()", context, corrected);
        assertEquals(java.awt.image.ImageObserver.class.getName(), actual);
    }
}
