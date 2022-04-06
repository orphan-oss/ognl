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
package org.ognl.internal.entry;

import org.ognl.OgnlRuntime;
import org.ognl.internal.CacheException;

import java.lang.reflect.Method;
import java.util.*;

public abstract class MethodCacheEntryFactory<T extends MethodCacheEntry> implements CacheEntryFactory<T, Map<String, List<Method>>> {

    public Map<String, List<Method>> create(T key) throws CacheException {
        Map<String, List<Method>> result = new HashMap<>(23);

        collectMethods(key, key.targetClass, result);

        return result;
    }

    protected abstract boolean shouldCache(T key, Method method);

    private void collectMethods(T key, Class<?> c, Map<String, List<Method>> result) {
        Method[] ma;
        try {
            ma = c.getDeclaredMethods();
        } catch (SecurityException ignored) {
            ma = c.getMethods();
        }
        for (Method method : ma) {
            if (!OgnlRuntime.isMethodCallable(method))
                continue;

            if (shouldCache(key, method)) {
                List<Method> ml = result.computeIfAbsent(method.getName(), k -> new ArrayList<>());
                ml.add(method);
            }
        }
        final Class<?> superclass = c.getSuperclass();
        if (superclass != null) {
            collectMethods(key, superclass, result);
        }

        for (final Class<?> iface : c.getInterfaces()) {
            collectMethods(key, iface, result);
        }
    }

}
