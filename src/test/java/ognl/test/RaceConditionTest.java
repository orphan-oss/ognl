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

import ognl.OgnlRuntime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class RaceConditionTest {

    @Test
    void testRaceCondition() throws Exception {
        runTest(TestAction.class, 1000, 10, Boolean.TRUE);
    }

    private static void runTest(Class<?> clazz, int invocationCount, int threadCount, Boolean expected) throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final List<Future<Boolean>> futures = new ArrayList<>(threadCount);
        for (int i = threadCount; i > 0; i--) {
            futures.add(executor.submit(new Worker(clazz, invocationCount)));
        }
        for (final Future<Boolean> future : futures) {
            Assertions.assertEquals(expected, future.get());
        }
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    private static class TestAction {

        public String execute() throws Exception {
            return "success";
        }

    }

    private static class Worker implements Callable<Boolean> {

        private final Class<?> clazz;
        private final int invocationCount;

        public Worker(final Class<?> clazz, final int invocationCount) {
            this.clazz = clazz;
            this.invocationCount = invocationCount;
        }

        public Boolean call() throws Exception {
            for (int i = this.invocationCount; i > 0; i--) {
                Method method = OgnlRuntime.getMethod(null, clazz, "execute", null, false);
                if (method == null) {
                    return false;
                }
            }
            return true;
        }
    }
}
