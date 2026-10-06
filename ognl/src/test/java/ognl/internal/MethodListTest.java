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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void dropsOldestResolutionWhenLimitIsReached() throws Exception {
        MethodList list = new MethodList(2);
        list.add(method("toString"));

        list.putResolution("first", "1");
        list.putResolution("second", "2");
        list.putResolution("third", "3");

        assertNull(list.getResolution("first"));
        assertSame("2", list.getResolution("second"));
        assertSame("3", list.getResolution("third"));
        assertEquals(2, list.resolutionCount());
    }

    @Test
    void storingAgainDoesNotRefreshPosition() throws Exception {
        MethodList list = new MethodList(2);
        list.add(method("toString"));

        list.putResolution("first", "1");
        list.putResolution("second", "2");
        list.putResolution("first", "1");
        list.putResolution("third", "3");

        assertNull(list.getResolution("first"));
        assertSame("2", list.getResolution("second"));
        assertSame("3", list.getResolution("third"));
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
                        list.putResolution(offset + i, "value");
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
