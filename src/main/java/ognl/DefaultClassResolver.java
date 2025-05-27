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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Default class resolution.  Uses Class.forName() to look up classes by name.
 * It also looks in the "java.lang" package if the class named does not give
 * a package specifier, allowing easier usage of these classes.
 */
public class DefaultClassResolver<C extends OgnlContext<C>> implements ClassResolver<C> {

    private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<>(101);

    public DefaultClassResolver() {
        super();
    }

    public <T> Class<T> classForName(String className, C context) throws ClassNotFoundException {
        Class<?> result = classes.get(className);
        if (result != null) {
            return (Class<T>) result;
        }
        try {
            result = toClassForName(className);
        } catch (ClassNotFoundException e) {
            if (className.indexOf('.') > -1) {
                throw e;
            }
            // The class was not in the default package.
            // Try prepending 'java.lang.'.
            try {
                result = toClassForName("java.lang." + className);
            } catch (ClassNotFoundException e2) {
                // Report the specified class name as-is.
                throw e;
            }
        }
        classes.putIfAbsent(className, result);
        return (Class<T>) result;
    }

    protected Class<?> toClassForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

}
