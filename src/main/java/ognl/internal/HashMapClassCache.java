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

import ognl.ClassCacheInspector;
import ognl.internal.entry.CacheEntryFactory;

public class HashMapClassCache<T> extends HashMapCache<Class<?>, T> implements ClassCache<T> {

    private ClassCacheInspector inspector;

    public HashMapClassCache(CacheEntryFactory<Class<?>, T> entryFactory) {
        super(entryFactory);
    }

    public void setClassInspector(ClassCacheInspector inspector) {
        this.inspector = inspector;
    }

    public T put(Class<?> key, T value) {
        if (inspector != null && !inspector.shouldCache(key)) {
            return value;
        }
        return super.put(key, value);
    }

}
