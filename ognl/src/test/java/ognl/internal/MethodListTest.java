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
package ognl.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MethodListTest {

    private static Method method(String name) throws NoSuchMethodException {
        return Object.class.getMethod(name);
    }

    @Test
    void storesResolutions() throws Exception {
        MethodList list = new MethodList();
        list.add(method("toString"));

        assertNull(list.getResolution("key"));
        assertEquals(0, list.resolutionCount());

        list.putResolution("key", "value");

        assertSame("value", list.getResolution("key"));
        assertNull(list.getResolution("other"));
        assertEquals(1, list.resolutionCount());
    }

    @Test
    void dropsResolutionsWhenModified() throws Exception {
        MethodList list = new MethodList();
        list.add(method("toString"));
        list.putResolution("key", "value");

        list.add(method("hashCode"));

        assertNull(list.getResolution("key"));
        assertEquals(0, list.resolutionCount());

        list.putResolution("other", "value");

        assertNull(list.getResolution("key"));
        assertSame("value", list.getResolution("other"));
        assertEquals(1, list.resolutionCount());
    }

    @Test
    void comparesAsAList() throws Exception {
        Method toString = method("toString");
        MethodList list = new MethodList();
        list.add(toString);
        list.putResolution("key", "value");

        List<Method> plain = new ArrayList<>();
        plain.add(toString);

        assertEquals(plain, list);
        assertEquals(plain.hashCode(), list.hashCode());
    }

}
