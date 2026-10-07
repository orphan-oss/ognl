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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handlers (accessors, null handlers) registered for classes, and the handler each other class ends up
 * with by inheriting the one of its nearest registered supertype.
 * <p>
 * What was registered and what was derived from it are kept apart. The registrations stay until they are
 * replaced. What was derived is only a cache: it is dropped whenever a handler is registered, so that a
 * new registration reaches the subclasses that were already looked up, and it can be cleared at any time
 * without losing a registration.
 *
 * @param <T> the type of the handlers.
 */
public final class HandlerRegistry<T> {

    private final Map<Class<?>, T> registered = new ConcurrentHashMap<>();
    private final ClassCache<T> derived;

    /**
     * @param derived where the handler found for each class that was looked up is kept. Clearing it is
     *                safe, the handlers are found again on demand.
     */
    public HandlerRegistry(ClassCache<T> derived) {
        this.derived = derived;
    }

    /**
     * @param clazz   the class to register the handler for.
     * @param handler the handler for the class and for its subtypes without a closer registration,
     *                {@code null} removes the registration.
     */
    public synchronized void register(Class<?> clazz, T handler) {
        if (handler == null) {
            registered.remove(clazz);
        } else {
            registered.put(clazz, handler);
        }
        derived.clear();
    }

    /**
     * @param forClass the class to find a handler for.
     * @return the handler registered for the class or for its nearest registered supertype, {@code null}
     * if there is none.
     */
    public T get(Class<?> forClass) {
        // One lookup in the common case: the answer for a class is kept here whether it was registered
        // for the class itself or inherited. register() drops all of it, so it is never out of date.
        T answer = derived.get(forClass);
        if (answer != null) {
            return answer;
        }
        synchronized (this) {
            answer = registered.get(forClass);
            if (answer == null) {
                answer = derive(forClass);
            }
            if (answer != null) {
                derived.put(forClass, answer);
            }
        }
        return answer;
    }

    private T derive(Class<?> forClass) {
        if (forClass.isArray()) {
            return registered.get(Object[].class);
        }
        for (Class<?> clazz = forClass; clazz != null; clazz = clazz.getSuperclass()) {
            T answer = registered.get(clazz);
            if (answer != null) {
                return answer;
            }
            for (Class<?> iface : clazz.getInterfaces()) {
                answer = get(iface);
                if (answer != null) {
                    return answer;
                }
            }
        }
        return null;
    }

}
