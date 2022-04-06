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
package org.ognl.internal;

import org.ognl.internal.entry.CacheEntryFactory;

import java.util.HashMap;
import java.util.Map;

public class HashMapCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache = new HashMap<>(512);

    private final CacheEntryFactory<K, V> cacheEntryFactory;

    public HashMapCache(CacheEntryFactory<K, V> cacheEntryFactory) {
        this.cacheEntryFactory = cacheEntryFactory;
    }

    public void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    public int getSize() {
        synchronized (cache) {
            return cache.size();
        }
    }

    public V get(K key) throws CacheException {
        V v = cache.get(key);
        if (shouldCreate(cacheEntryFactory, v)) {
            synchronized (cache) {
                v = cache.get(key);
                if (v != null) {
                    return v;
                }
                return put(key, cacheEntryFactory.create(key));
            }
        }
        return v;
    }

    protected boolean shouldCreate(CacheEntryFactory<K, V> cacheEntryFactory, V v) throws CacheException {
        return cacheEntryFactory != null && v == null;
    }

    public V put(K key, V value) {
        synchronized (cache) {
            cache.put(key, value);
            return value;
        }
    }


    public boolean contains(K key) {
        return this.cache.containsKey(key);
    }

}
