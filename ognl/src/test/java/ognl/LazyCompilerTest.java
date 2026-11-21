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

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.OgnlExpressionCompiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class LazyCompilerTest {

    private static class CustomCompiler<C extends OgnlContext<C>> extends ExpressionCompiler<C> {
    }

    private OgnlExpressionCompiler originalCompiler;

    @BeforeEach
    void setUp() {
        originalCompiler = OgnlRuntime.getCompiler();
    }

    @AfterEach
    void tearDown() {
        OgnlRuntime.setCompiler(originalCompiler);
    }

    @Test
    void defaultCompilerIsExpressionCompiler() {
        assertInstanceOf(ExpressionCompiler.class, OgnlRuntime.getCompiler());
    }

    @Test
    void concurrentFirstCallsShareOneCompiler() throws Exception {
        OgnlRuntime.setCompiler(null);
        int threads = 16;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<OgnlExpressionCompiler>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return OgnlRuntime.getCompiler();
                }));
            }
            start.countDown();

            OgnlExpressionCompiler first = results.get(0).get();
            assertInstanceOf(ExpressionCompiler.class, first);
            for (Future<OgnlExpressionCompiler> result : results) {
                assertSame(first, result.get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void customCompilerSetDuringFirstCallIsNotOverwritten() throws Exception {
        int workers = 4;
        CyclicBarrier go = new CyclicBarrier(workers + 1);
        CyclicBarrier done = new CyclicBarrier(workers + 1);
        AtomicBoolean running = new AtomicBoolean(true);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        try {
            for (int w = 0; w < workers; w++) {
                pool.submit(() -> {
                    while (running.get()) {
                        go.await();
                        if (running.get()) {
                            OgnlRuntime.getCompiler();
                        }
                        done.await();
                    }
                    return null;
                });
            }
            for (int i = 0; i < 20_000; i++) {
                OgnlRuntime.setCompiler(null);
                CustomCompiler<?> custom = new CustomCompiler<>();
                go.await();
                spin(i % 64);
                OgnlRuntime.setCompiler(custom);
                done.await();

                assertSame(custom, OgnlRuntime.getCompiler(), "iteration " + i);
            }
        } finally {
            running.set(false);
            go.reset();
            done.reset();
            pool.shutdownNow();
        }
    }

    private static void spin(int rounds) {
        for (int i = 0; i < rounds; i++) {
            Thread.onSpinWait();
        }
    }

    @Test
    void settingNullFallsBackToDefault() {
        OgnlRuntime.setCompiler(null);

        assertInstanceOf(ExpressionCompiler.class, OgnlRuntime.getCompiler());
    }

    @Test
    void customCompilerWins() {
        CustomCompiler<?> custom = new CustomCompiler<>();
        OgnlRuntime.setCompiler(custom);

        assertSame(custom, OgnlRuntime.getCompiler());
    }
}
