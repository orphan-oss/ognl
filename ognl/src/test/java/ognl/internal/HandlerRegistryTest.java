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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HandlerRegistryTest {

    interface Named {
    }

    static class Base {
    }

    static class Sub extends Base {
    }

    static class OtherSub extends Base implements Named {
    }

    private ClassCache<String> derived;
    private HandlerRegistry<String> registry;

    @BeforeEach
    void setUp() {
        derived = new HashMapCacheFactory().createClassCache();
        registry = new HandlerRegistry<>(derived);
        registry.register(Object.class, "object");
        registry.register(Object[].class, "array");
    }

    @Test
    void findsTheHandlerOfTheNearestRegisteredSuperclass() {
        registry.register(Base.class, "base");

        assertEquals("base", registry.get(Base.class));
        assertEquals("base", registry.get(Sub.class));
        assertEquals("object", registry.get(String.class));
    }

    @Test
    void anInterfaceOfTheClassWinsOverItsSuperclass() {
        registry.register(Base.class, "base");
        registry.register(Named.class, "named");

        assertEquals("named", registry.get(OtherSub.class));
        assertEquals("base", registry.get(Sub.class));
    }

    @Test
    void arraysUseTheHandlerOfObjectArrayUnlessRegistered() {
        registry.register(int[].class, "ints");

        assertEquals("ints", registry.get(int[].class));
        assertEquals("array", registry.get(String[].class));
    }

    @Test
    void laterRegistrationReachesSubclassesLookedUpBefore() {
        assertEquals("object", registry.get(Sub.class));

        registry.register(Base.class, "base");

        assertEquals("base", registry.get(Sub.class));
    }

    @Test
    void clearingWhatWasDerivedKeepsTheRegistrations() {
        registry.register(Base.class, "base");
        assertEquals("base", registry.get(Sub.class));
        assertEquals(1, derived.getSize());

        derived.clear();

        assertEquals(0, derived.getSize());
        assertEquals("base", registry.get(Sub.class));
        assertEquals("base", registry.get(Base.class));
    }

    @Test
    void registeringNullRemovesTheRegistration() {
        registry.register(Base.class, "base");
        assertEquals("base", registry.get(Sub.class));

        registry.register(Base.class, null);

        assertEquals("object", registry.get(Sub.class));
    }

    @Test
    void returnsNullWhenNothingApplies() {
        HandlerRegistry<String> empty = new HandlerRegistry<>(new HashMapCacheFactory().createClassCache());

        assertNull(empty.get(Sub.class));
    }

}
