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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The methods of a class that share a name, as kept in the method cache, together with what has already
 * been resolved for them.
 * <p>
 * Choosing which of the methods to call for a given set of argument types only depends on the methods in
 * the list, so the outcome is remembered here, next to the methods it was computed from. It goes away
 * with the list when the method cache is cleared.
 * <p>
 * The list is still an ordinary, modifiable list. What was resolved is tied to the exact methods the list
 * held at that moment (see {@link #resolutions()}), so nothing resolved for an earlier content is used
 * after the list changes in any way, including replacing an element.
 */
public final class MethodList extends ArrayList<Method> {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_MAX_RESOLUTIONS = 64;

    private final int maxResolutions;
    private transient volatile Resolutions resolutions;

    public MethodList() {
        this(DEFAULT_MAX_RESOLUTIONS);
    }

    /**
     * @param maxResolutions how many resolutions to keep, the oldest one is dropped first once it is reached.
     */
    public MethodList(int maxResolutions) {
        if (maxResolutions < 1) {
            throw new IllegalArgumentException("maxResolutions must be positive, was " + maxResolutions);
        }
        this.maxResolutions = maxResolutions;
    }

    /**
     * Returns what has been resolved for the methods this list holds right now. If the list changed since
     * the last call, an empty set of resolutions for the new content is returned and the previous one is
     * no longer handed out.
     * <p>
     * Resolve against {@link Resolutions#methods()}, not against this list: that is the content the
     * resolutions belong to, and it cannot change while it is being walked.
     *
     * @return the resolutions for the current content of this list.
     */
    public Resolutions resolutions() {
        Resolutions current = resolutions;
        if (current == null || !current.isFor(this)) {
            current = new Resolutions(toArray(new Method[0]), maxResolutions);
            resolutions = current;
        }
        return current;
    }

    /**
     * @return how many resolutions are stored for the current content of this list.
     */
    public int resolutionCount() {
        Resolutions current = resolutions;
        return current == null || !current.isFor(this) ? 0 : current.size();
    }

    /**
     * The outcome of resolving something against the methods of a {@link MethodList}.
     */
    public interface Resolution {
    }

    /**
     * What has been resolved for one exact content of a {@link MethodList}.
     */
    public static final class Resolutions {

        private final Method[] snapshot;
        private final List<Method> methods;
        private final int maxResolutions;
        private final ConcurrentHashMap<Object, Resolution> byKey = new ConcurrentHashMap<>();
        private final ConcurrentLinkedQueue<Object> insertionOrder = new ConcurrentLinkedQueue<>();

        private Resolutions(Method[] snapshot, int maxResolutions) {
            this.snapshot = snapshot;
            this.methods = Collections.unmodifiableList(Arrays.asList(snapshot));
            this.maxResolutions = maxResolutions;
        }

        /**
         * @return the methods these resolutions were computed from, unmodifiable.
         */
        public List<Method> methods() {
            return methods;
        }

        /**
         * @param key identifies what was resolved, for example the argument types of a call.
         * @return the resolution stored for the key, or {@code null} if there is none.
         */
        public Resolution get(Object key) {
            return byKey.get(key);
        }

        /**
         * @param key        identifies what was resolved, for example the argument types of a call.
         * @param resolution the outcome to remember, computed from {@link #methods()}, never {@code null}.
         */
        public void put(Object key, Resolution resolution) {
            if (byKey.putIfAbsent(key, resolution) == null) {
                // Only one writer trims at a time. Otherwise two of them can each see one entry too many
                // and each drop one, leaving the map below the limit.
                synchronized (insertionOrder) {
                    insertionOrder.offer(key);
                    while (byKey.size() > maxResolutions) {
                        Object eldest = insertionOrder.poll();
                        if (eldest == null) {
                            break;
                        }
                        byKey.remove(eldest);
                    }
                }
            }
        }

        /**
         * @return how many resolutions are stored.
         */
        public int size() {
            return byKey.size();
        }

        private boolean isFor(MethodList list) {
            int size = list.size();
            if (size != snapshot.length) {
                return false;
            }
            try {
                for (int i = 0; i < size; i++) {
                    if (list.get(i) != snapshot[i]) {
                        return false;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                // the list shrank while it was being compared
                return false;
            }
            return true;
        }
    }

}
