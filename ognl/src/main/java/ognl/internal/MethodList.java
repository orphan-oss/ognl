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

/**
 * The methods of a class that share a name, as kept in the method cache, together with what has already
 * been resolved for them.
 * <p>
 * Choosing which of the methods to call for a given set of argument types only depends on the methods in
 * the list, so the outcome is remembered here, next to the methods it was computed from. It goes away
 * with the list when the method cache is cleared, and it is dropped if the list is modified.
 */
public final class MethodList extends ArrayList<Method> {

    private static final long serialVersionUID = 1L;

    private transient volatile ConcurrentHashMap<Object, Object> resolutions;
    private transient int resolutionsModCount;

    /**
     * @param key identifies what was resolved, for example the argument types of a call.
     * @return the resolution stored for the key, or {@code null} if there is none.
     */
    public Object getResolution(Object key) {
        ConcurrentHashMap<Object, Object> current = resolutions;
        if (current == null || resolutionsModCount != modCount) {
            return null;
        }
        return current.get(key);
    }

    /**
     * @param key        identifies what was resolved, for example the argument types of a call.
     * @param resolution the outcome to remember, never {@code null}.
     */
    public void putResolution(Object key, Object resolution) {
        ConcurrentHashMap<Object, Object> current = resolutions;
        if (current == null || resolutionsModCount != modCount) {
            current = new ConcurrentHashMap<>();
            resolutionsModCount = modCount;
            resolutions = current;
        }
        current.put(key, resolution);
    }

    /**
     * @return how many resolutions are currently stored.
     */
    public int resolutionCount() {
        ConcurrentHashMap<Object, Object> current = resolutions;
        if (current == null || resolutionsModCount != modCount) {
            return 0;
        }
        return current.size();
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
