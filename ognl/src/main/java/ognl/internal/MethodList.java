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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The methods of a class that share a name, as kept in the method cache, together with what has already
 * been resolved for them.
 * <p>
 * Choosing which of the methods to call for a given set of argument types only depends on the methods in
 * the list, so the outcome is remembered here, next to the methods it was computed from. It goes away
 * with the list when the method cache is cleared, and it is dropped if the list is modified. At most
 * {@link #DEFAULT_MAX_RESOLUTIONS} outcomes are kept, the oldest is dropped first.
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
     * @param key identifies what was resolved, for example the argument types of a call.
     * @return the resolution stored for the key, or {@code null} if there is none.
     */
    public Object getResolution(Object key) {
        Resolutions current = currentResolutions();
        return current == null ? null : current.byKey.get(key);
    }

    /**
     * @param key        identifies what was resolved, for example the argument types of a call.
     * @param resolution the outcome to remember, never {@code null}.
     */
    public void putResolution(Object key, Object resolution) {
        Resolutions current = currentResolutions();
        if (current == null) {
            current = new Resolutions(modCount);
            resolutions = current;
        }
        if (current.byKey.putIfAbsent(key, resolution) == null) {
            current.insertionOrder.offer(key);
            while (current.byKey.size() > maxResolutions) {
                Object eldest = current.insertionOrder.poll();
                if (eldest == null) {
                    break;
                }
                current.byKey.remove(eldest);
            }
        }
    }

    /**
     * @return how many resolutions are currently stored.
     */
    public int resolutionCount() {
        Resolutions current = currentResolutions();
        return current == null ? 0 : current.byKey.size();
    }

    private Resolutions currentResolutions() {
        Resolutions current = resolutions;
        return current == null || current.modCount != modCount ? null : current;
    }

    private static final class Resolutions {

        private final int modCount;
        private final ConcurrentHashMap<Object, Object> byKey = new ConcurrentHashMap<>();
        private final ConcurrentLinkedQueue<Object> insertionOrder = new ConcurrentLinkedQueue<>();

        Resolutions(int modCount) {
            this.modCount = modCount;
        }
    }

    // The stored resolutions are derived from the methods, so they are not part of the list's identity

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
