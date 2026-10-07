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
package ognl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Issues #684 and #685: accessors derived for a class are a cache of what is registered.
 */
class OgnlRuntimeHandlerRegistrationTest {

    public static class Base {
    }

    public static class LookedUpBefore extends Base {
    }

    public static class LookedUpAfter extends Base {
    }

    @AfterEach
    void tearDown() {
        OgnlRuntime.setPropertyAccessor(Base.class, null);
        OgnlRuntime.setMethodAccessor(Base.class, null);
    }

    @Test
    void propertyAccessorRegisteredLaterReachesSubclassLookedUpBefore() throws Exception {
        PropertyAccessor<?> initial = OgnlRuntime.getPropertyAccessor(LookedUpBefore.class);
        ObjectPropertyAccessor custom = new ObjectPropertyAccessor() {
        };

        OgnlRuntime.setPropertyAccessor(Base.class, custom);

        assertNotSame(custom, initial);
        assertSame(custom, OgnlRuntime.getPropertyAccessor(LookedUpBefore.class));
        assertSame(custom, OgnlRuntime.getPropertyAccessor(LookedUpAfter.class));
    }

    @Test
    void methodAccessorRegisteredLaterReachesSubclassLookedUpBefore() throws Exception {
        MethodAccessor<?> initial = OgnlRuntime.getMethodAccessor(LookedUpBefore.class);
        ObjectMethodAccessor custom = new ObjectMethodAccessor() {
        };

        OgnlRuntime.setMethodAccessor(Base.class, custom);

        assertNotSame(custom, initial);
        assertSame(custom, OgnlRuntime.getMethodAccessor(LookedUpBefore.class));
    }

    @Test
    void clearingTheCachesKeepsRegisteredAccessors() throws Exception {
        ObjectPropertyAccessor custom = new ObjectPropertyAccessor() {
        };
        OgnlRuntime.setPropertyAccessor(Base.class, custom);
        assertSame(custom, OgnlRuntime.getPropertyAccessor(LookedUpBefore.class));

        OgnlRuntime.clearCache();

        assertSame(custom, OgnlRuntime.getPropertyAccessor(LookedUpBefore.class));
        assertSame(custom, OgnlRuntime.getPropertyAccessor(Base.class));
    }

    @Test
    void registeringNullGoesBackToTheInheritedAccessor() throws Exception {
        PropertyAccessor<?> inherited = OgnlRuntime.getPropertyAccessor(LookedUpBefore.class);
        OgnlRuntime.setPropertyAccessor(Base.class, new ObjectPropertyAccessor() {
        });

        OgnlRuntime.setPropertyAccessor(Base.class, null);

        assertSame(inherited, OgnlRuntime.getPropertyAccessor(LookedUpBefore.class));
    }

}
