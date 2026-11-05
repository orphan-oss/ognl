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

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OGNL's built-in property resolution. A custom resolver can extend or delegate to it and filter its output.
 * A lenient lookup that throws is left out of the list.
 *
 * @since 3.4.18
 */
public class DefaultPropertyResolver implements PropertyResolver {

    public static final DefaultPropertyResolver INSTANCE = new DefaultPropertyResolver();

    @Override
    public List<PropertyCandidate> candidates(Class<?> targetClass, String propertyName, PropertyAccessKind kind) {
        switch (kind) {
            case READ:
                return read(targetClass, propertyName);
            case WRITE:
                return write(targetClass, propertyName);
            case INDEXED_READ:
                return indexed(targetClass, propertyName, false);
            default:
                return indexed(targetClass, propertyName, true);
        }
    }

    private static List<PropertyCandidate> read(Class<?> targetClass, String propertyName) {
        List<PropertyCandidate> candidates = new ArrayList<>(2);
        Method getter = OgnlRuntime.getGetMethod(targetClass, propertyName);
        if (getter != null) {
            candidates.add(PropertyCandidate.accessor(getter));
        } else {
            Method lenient = lenientRead(targetClass, propertyName);
            if (lenient != null) {
                candidates.add(PropertyCandidate.lenient(lenient));
            }
        }
        addField(candidates, targetClass, propertyName);
        return candidates;
    }

    private static List<PropertyCandidate> write(Class<?> targetClass, String propertyName) {
        List<PropertyCandidate> candidates = new ArrayList<>(3);
        Method setter = OgnlRuntime.getSetMethod(null, targetClass, propertyName);
        if (setter != null) {
            candidates.add(PropertyCandidate.accessor(setter));
        }
        addField(candidates, targetClass, propertyName);
        Method lenient = lenientWrite(targetClass, propertyName);
        if (lenient != null) {
            candidates.add(PropertyCandidate.lenient(lenient));
        }
        return candidates;
    }

    private static Method lenientRead(Class<?> targetClass, String propertyName) {
        try {
            return OgnlRuntime.getReadMethod(targetClass, propertyName);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Method lenientWrite(Class<?> targetClass, String propertyName) {
        try {
            return OgnlRuntime.getWriteMethod(targetClass, propertyName);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void addField(List<PropertyCandidate> candidates, Class<?> targetClass, String propertyName) {
        Field field = OgnlRuntime.getField(targetClass, propertyName);
        if (field != null) {
            candidates.add(PropertyCandidate.field(field));
        }
    }

    private static List<PropertyCandidate> indexed(Class<?> targetClass, String propertyName, boolean write) {
        Method method = indexedMethod(descriptor(targetClass, propertyName), write);
        if (method == null) {
            return Collections.emptyList();
        }
        List<Method> overloads = OgnlRuntime.getMethods(targetClass, method.getName(), false);
        if (overloads == null) {
            return Collections.emptyList();
        }
        List<PropertyCandidate> candidates = new ArrayList<>(overloads.size());
        for (Method overload : overloads) {
            candidates.add(PropertyCandidate.indexed(overload));
        }
        return candidates;
    }

    private static PropertyDescriptor descriptor(Class<?> targetClass, String propertyName) {
        try {
            return OgnlRuntime.getPropertyDescriptor(targetClass, propertyName);
        } catch (OgnlException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method indexedMethod(PropertyDescriptor pd, boolean write) {
        if (pd instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
            return write ? ipd.getIndexedWriteMethod() : ipd.getIndexedReadMethod();
        }
        if (pd instanceof ObjectIndexedPropertyDescriptor) {
            ObjectIndexedPropertyDescriptor oipd = (ObjectIndexedPropertyDescriptor) pd;
            return write ? oipd.getIndexedWriteMethod() : oipd.getIndexedReadMethod();
        }
        return null;
    }
}
