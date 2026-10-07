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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodListTest {

    private static final class Outcome implements MethodList.Resolution {
    }

    private static final MethodList.Resolution ONE = new Outcome();
    private static final MethodList.Resolution TWO = new Outcome();
    private static final MethodList.Resolution THREE = new Outcome();

    private static Method method(String name) throws NoSuchMethodException {
        return Object.class.getMethod(name);
    }

    private static MethodList listOf(String... methodNames) throws NoSuchMethodException {
        MethodList list = new MethodList();
        for (String name : methodNames) {
            list.add(method(name));
        }
        return list;
    }

    @Test
    void storesResolutions() throws Exception {
        MethodList list = listOf("toString");

        assertNull(list.resolutions().get("key"));
        assertEquals(0, list.resolutionCount());

        list.resolutions().put("key", ONE);

        assertSame(ONE, list.resolutions().get("key"));
        assertNull(list.resolutions().get("other"));
        assertEquals(1, list.resolutionCount());
    }

    @Test
    void resolutionsExposeTheMethodsTheyBelongTo() throws Exception {
        MethodList list = listOf("toString", "hashCode");

        MethodList.Resolutions resolutions = list.resolutions();
        list.add(method("notify"));

        assertEquals(Arrays.asList(method("toString"), method("hashCode")), resolutions.methods());
        assertThrows(UnsupportedOperationException.class, () -> resolutions.methods().clear());
    }

    @Test
    void sameContentKeepsTheSameResolutions() throws Exception {
        MethodList list = listOf("toString");

        assertSame(list.resolutions(), list.resolutions());
    }

    @Test
    void dropsResolutionsWhenAnElementIsAdded() throws Exception {
        MethodList list = listOf("toString");
        list.resolutions().put("key", ONE);

        list.add(method("hashCode"));

        assertNull(list.resolutions().get("key"));
        assertEquals(0, list.resolutionCount());

        list.resolutions().put("other", TWO);

        assertNull(list.resolutions().get("key"));
        assertSame(TWO, list.resolutions().get("other"));
        assertEquals(1, list.resolutionCount());
    }

    @Test
    void dropsResolutionsWhenAnElementIsReplaced() throws Exception {
        MethodList list = listOf("toString");
        list.resolutions().put("key", ONE);

        list.set(0, method("hashCode"));

        assertEquals(0, list.resolutionCount());
        assertNull(list.resolutions().get("key"));
    }

    @Test
    void dropsResolutionsWhenAnElementIsReplacedThroughAnIterator() throws Exception {
        MethodList list = listOf("toString");
        list.resolutions().put("key", ONE);

        ListIterator<Method> iterator = list.listIterator();
        iterator.next();
        iterator.set(method("hashCode"));

        assertEquals(0, list.resolutionCount());
        assertNull(list.resolutions().get("key"));
    }

    @Test
    void dropsResolutionsWhenAnElementIsReplacedThroughASubList() throws Exception {
        MethodList list = listOf("toString", "hashCode");
        list.resolutions().put("key", ONE);

        list.subList(1, 2).set(0, method("notify"));

        assertEquals(0, list.resolutionCount());
        assertNull(list.resolutions().get("key"));
    }

    @Test
    void dropsResolutionsWhenElementsAreSwapped() throws Exception {
        MethodList list = listOf("toString", "hashCode");
        list.resolutions().put("key", ONE);

        Collections.swap(list, 0, 1);

        assertEquals(0, list.resolutionCount());
        assertNull(list.resolutions().get("key"));
    }

    @Test
    void resolutionComputedBeforeAChangeIsNotServedAfterIt() throws Exception {
        MethodList list = listOf("toString");

        // a resolution is being computed from the current content...
        MethodList.Resolutions resolutions = list.resolutions();
        // ...the list is changed in the meantime...
        list.set(0, method("hashCode"));
        // ...and the outdated outcome is stored afterwards
        resolutions.put("key", ONE);

        assertEquals(0, list.resolutionCount());
        assertNull(list.resolutions().get("key"));
    }

    @Test
    void changedCloneDoesNotSeeTheResolutionsOfTheOriginal() throws Exception {
        MethodList list = listOf("toString");
        list.resolutions().put("key", ONE);

        MethodList clone = (MethodList) list.clone();
        clone.clear();

        assertEquals(0, clone.resolutionCount());
        assertNull(clone.resolutions().get("key"));
        assertNotSame(list.resolutions(), clone.resolutions());
        assertSame(ONE, list.resolutions().get("key"));
    }

    @Test
    void dropsOldestResolutionWhenLimitIsReached() throws Exception {
        MethodList list = new MethodList(2);
        list.add(method("toString"));

        list.resolutions().put("first", ONE);
        list.resolutions().put("second", TWO);
        list.resolutions().put("third", THREE);

        assertNull(list.resolutions().get("first"));
        assertSame(TWO, list.resolutions().get("second"));
        assertSame(THREE, list.resolutions().get("third"));
        assertEquals(2, list.resolutionCount());
    }

    @Test
    void storingAgainDoesNotRefreshPosition() throws Exception {
        MethodList list = new MethodList(2);
        list.add(method("toString"));

        list.resolutions().put("first", ONE);
        list.resolutions().put("second", TWO);
        list.resolutions().put("first", ONE);
        list.resolutions().put("third", THREE);

        assertNull(list.resolutions().get("first"));
        assertSame(TWO, list.resolutions().get("second"));
        assertSame(THREE, list.resolutions().get("third"));
    }

    @Test
    void staysWithinLimitUnderConcurrentPuts() throws Exception {
        int limit = 8;
        MethodList list = new MethodList(limit);
        list.add(method("toString"));

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                int offset = t * 1000;
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < 1000; i++) {
                        list.resolutions().put(offset + i, ONE);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertEquals(limit, list.resolutionCount());
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> new MethodList(0));
    }

}
