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

import ognl.PropertyResolutionFixtures.BothBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyResolverInstallTest {

    @AfterEach
    void reset() {
        OgnlRuntime.setPropertyResolver(null);
    }

    @Test
    void defaultIsInstalled() {
        assertSame(DefaultPropertyResolver.INSTANCE, OgnlRuntime.getPropertyResolver());
    }

    @Test
    void installAndReset() {
        PropertyResolver custom = (c, n, k) -> List.of();
        OgnlRuntime.setPropertyResolver(custom);
        assertSame(custom, OgnlRuntime.getPropertyResolver());
        OgnlRuntime.setPropertyResolver(null);
        assertSame(DefaultPropertyResolver.INSTANCE, OgnlRuntime.getPropertyResolver());
    }

    @Test
    void installClearsCandidateCache() {
        assertFalse(PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ).isEmpty());
        OgnlRuntime.setPropertyResolver((c, n, k) -> List.of());
        assertTrue(PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ).isEmpty());
    }

    @Test
    void clearCacheClearsCandidates() {
        AtomicInteger calls = new AtomicInteger();
        OgnlRuntime.setPropertyResolver((c, n, k) -> {
            calls.incrementAndGet();
            return List.of();
        });
        PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ);
        PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ);
        assertEquals(1, calls.get());
        OgnlRuntime.clearCache();
        PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ);
        assertEquals(2, calls.get());
        OgnlRuntime.clearAdditionalCache();
        PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ);
        assertEquals(3, calls.get());
    }

    @Test
    void clearDropsPerClassEntries() {
        OgnlRuntime.clearCache();
        PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ);
        PropertyResolution.candidates(String.class, "bytes", PropertyAccessKind.READ);
        assertEquals(2, PropertyResolution.cachedClassCount());
        OgnlRuntime.clearCache();
        assertEquals(0, PropertyResolution.cachedClassCount());
    }

    @Test
    void nullClassIsCached() {
        AtomicInteger calls = new AtomicInteger();
        OgnlRuntime.setPropertyResolver((c, n, k) -> {
            calls.incrementAndGet();
            return List.of();
        });
        PropertyResolution.candidates(null, "value", PropertyAccessKind.READ);
        PropertyResolution.candidates(null, "value", PropertyAccessKind.READ);
        assertEquals(1, calls.get());
    }

    @Test
    void cachedListIsUnmodifiable() {
        List<PropertyCandidate> candidates = PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ);
        assertThrows(UnsupportedOperationException.class, candidates::clear);
    }

    @Test
    void nullResultIsRejected() {
        OgnlRuntime.setPropertyResolver((c, n, k) -> null);
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.READ));
        assertTrue(e.getMessage().contains("value"), e.getMessage());
        assertTrue(e.getMessage().contains(BothBean.class.getName()), e.getMessage());
        assertTrue(e.getMessage().contains("READ"), e.getMessage());
    }

    @Test
    void nullElementIsRejected() {
        OgnlRuntime.setPropertyResolver((c, n, k) -> Arrays.asList((PropertyCandidate) null));
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> PropertyResolution.candidates(BothBean.class, "value", PropertyAccessKind.WRITE));
        assertTrue(e.getMessage().contains("value"), e.getMessage());
    }
}
