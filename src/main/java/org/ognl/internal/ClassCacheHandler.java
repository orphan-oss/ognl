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

public class ClassCacheHandler {

    private ClassCacheHandler() {
    }

    public static <T> T getHandler(Class<?> forClass, ClassCache<T> handlers) throws CacheException {
        T answer;

        synchronized (handlers) {
            answer = handlers.get(forClass);
            if (answer == null) {
                Class<?> keyFound;

                if (forClass.isArray()) {
                    answer = handlers.get(Object[].class);
                    keyFound = null;
                } else {
                    keyFound = forClass;
                    outer:
                    for (Class<?> clazz = forClass; clazz != null; clazz = clazz.getSuperclass()) {
                        answer = handlers.get(clazz);
                        if (answer != null) {
                            keyFound = clazz;
                            break;
                        }
                        Class<?>[] interfaces = clazz.getInterfaces();
                        for (Class<?> iface : interfaces) {
                            answer = handlers.get(iface);
                            if (answer == null) {
                                /* Try super-interfaces */
                                answer = getHandler(iface, handlers);
                            }
                            if (answer != null) {
                                keyFound = iface;
                                break outer;
                            }
                        }
                    }
                }
                if (answer != null && keyFound != forClass) {
                    handlers.put(forClass, answer);
                }
            }
        }
        return answer;
    }

}
