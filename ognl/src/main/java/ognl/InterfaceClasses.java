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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InterfaceClasses {

    private InterfaceClasses() {
    }

    public static Class<?> of(Class<?> clazz) {
        if (Modifier.isPublic(clazz.getModifiers()) && clazz.isInterface() || clazz.isPrimitive()) {
            return clazz;
        }

        return of(clazz, clazz.getInterfaces());
    }

    private static Class<?> of(Class<?> clazz, Class<?>[] interfaces) {
        for (Class<?> anInterface : interfaces) {
            if (List.class.isAssignableFrom(anInterface))
                return List.class;
            else if (Iterator.class.isAssignableFrom(anInterface))
                return Iterator.class;
            else if (Map.class.isAssignableFrom(anInterface))
                return Map.class;
            else if (Set.class.isAssignableFrom(anInterface))
                return Set.class;
            else if (Collection.class.isAssignableFrom(anInterface))
                return Collection.class;
        }

        final Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            final Class<?>[] superClazzInterfaces = superClazz.getInterfaces();
            if (superClazzInterfaces.length > 0)
                return of(superClazz, superClazzInterfaces);
        }

        return clazz;
    }
}
