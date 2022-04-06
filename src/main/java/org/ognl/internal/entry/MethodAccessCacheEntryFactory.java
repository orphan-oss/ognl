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

import org.ognl.internal.CacheException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodAccessCacheEntryFactory implements CacheEntryFactory<Method, MethodAccessEntryValue> {

    public static final MethodAccessEntryValue INACCESSIBLE_NON_PUBLIC_METHOD = new MethodAccessEntryValue(false, true);

    public static final MethodAccessEntryValue ACCESSIBLE_NON_PUBLIC_METHOD = new MethodAccessEntryValue(true, true);

    public static final MethodAccessEntryValue PUBLIC_METHOD = new MethodAccessEntryValue(true);

    public MethodAccessEntryValue create(Method method) throws CacheException {
        final boolean notPublic = !Modifier.isPublic(method.getModifiers())
                || !Modifier.isPublic(method.getDeclaringClass().getModifiers());
        if (!notPublic) {
            return PUBLIC_METHOD;
        }
        if (!method.isAccessible()) {
            return INACCESSIBLE_NON_PUBLIC_METHOD;
        }
        return ACCESSIBLE_NON_PUBLIC_METHOD;
    }

}
