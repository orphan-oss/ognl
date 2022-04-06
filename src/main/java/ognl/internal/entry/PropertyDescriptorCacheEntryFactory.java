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
package ognl.internal.entry;

import ognl.*;
import ognl.internal.CacheException;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;

public class PropertyDescriptorCacheEntryFactory implements ClassCacheEntryFactory<Map<String, PropertyDescriptor>> {

    public Map<String, PropertyDescriptor> create(Class<?> targetClass) throws CacheException {
        Map<String, PropertyDescriptor> result = new HashMap<>(101);
        PropertyDescriptor[] pda;
        try {
            pda = Introspector.getBeanInfo(targetClass).getPropertyDescriptors();

            for (PropertyDescriptor aPda : pda) {
                // workaround for Introspector bug 6528714 (bugs.sun.com)
                if (aPda.getReadMethod() != null && !OgnlRuntime.isMethodCallable(aPda.getReadMethod())) {
                    aPda.setReadMethod(
                            findClosestMatchingMethod(targetClass, aPda.getReadMethod(), aPda.getName(),
                                    aPda.getPropertyType(), true));
                }
                if (aPda.getWriteMethod() != null && !OgnlRuntime.isMethodCallable(aPda.getWriteMethod())) {
                    aPda.setWriteMethod(
                            findClosestMatchingMethod(targetClass, aPda.getWriteMethod(), aPda.getName(),
                                    aPda.getPropertyType(), false));
                }

                result.put(aPda.getName(), aPda);
            }

            findObjectIndexedPropertyDescriptors(targetClass, result);
        } catch (IntrospectionException | OgnlException e) {
            throw new CacheException(e);
        }
        return result;
    }

    static Method findClosestMatchingMethod(
            Class<?> targetClass,
            Method method,
            String propertyName,
            Class<?> propertyType,
            boolean isReadMethod
    ) throws OgnlException {
        List<Method> methods = OgnlRuntime.getDeclaredMethods(targetClass, propertyName, !isReadMethod);

        for (Method closestMethod : methods) {
            if (closestMethod.getName().equals(method.getName())
                    && method.getReturnType().isAssignableFrom(method.getReturnType())
                    && closestMethod.getReturnType() == propertyType
                    && closestMethod.getParameterTypes().length == method.getParameterTypes().length) {
                return closestMethod;
            }
        }

        return method;
    }

    private static void findObjectIndexedPropertyDescriptors(
            Class<?> targetClass, Map<String,
            PropertyDescriptor> intoMap
    ) throws OgnlException {
        Map<String, List<Method>> allMethods = OgnlRuntime.getMethods(targetClass, false);
        Map<String, List<Method>> pairs = new HashMap<>(101);

        for (Map.Entry<String, List<Method>> entry : allMethods.entrySet()) {
            String methodName = entry.getKey();
            List<Method> methods = entry.getValue();

            /*
             * Only process set/get where there is exactly one implementation of the method per class and those
             * implementations are all the same
             */
            if (indexMethodCheck(methods)) {
                boolean isGet = false, isSet;
                Method method = methods.get(0);

                if (((isSet = methodName.startsWith(OgnlRuntime.SET_PREFIX)) || (isGet = methodName.startsWith(OgnlRuntime.GET_PREFIX)))
                        && (methodName.length() > 3)) {
                    String propertyName = Introspector.decapitalize(methodName.substring(3));
                    Class<?>[] parameterTypes = OgnlRuntime.getParameterTypes(method);
                    int parameterCount = parameterTypes.length;

                    if (isGet && (parameterCount == 1) && (method.getReturnType() != Void.TYPE)) {
                        List<Method> pair = pairs.computeIfAbsent(propertyName, k -> new ArrayList<>());

                        pair.add(method);
                    }
                    if (isSet && (parameterCount == 2) && (method.getReturnType() == Void.TYPE)) {
                        List<Method> pair = pairs.computeIfAbsent(propertyName, k -> new ArrayList<>());

                        pair.add(method);
                    }
                }
            }
        }

        for (Map.Entry<String, List<Method>> entry : pairs.entrySet()) {
            String propertyName = entry.getKey();
            List<Method> methods = entry.getValue();

            if (methods.size() == 2) {
                Method method1 = methods.get(0), method2 = methods.get(1), setMethod =
                        (method1.getParameterTypes().length == 2) ? method1 : method2, getMethod =
                        (setMethod == method1) ? method2 : method1;
                Class<?> keyType = getMethod.getParameterTypes()[0], propertyType = getMethod.getReturnType();

                if (keyType == setMethod.getParameterTypes()[0] && propertyType == setMethod.getParameterTypes()[1]) {
                    ObjectIndexedPropertyDescriptor propertyDescriptor;

                    try {
                        propertyDescriptor = new ObjectIndexedPropertyDescriptor(propertyName, propertyType, getMethod, setMethod);
                    } catch (Exception ex) {
                        throw new OgnlException(
                                "creating object indexed property descriptor for '" + propertyName + "' in "
                                        + targetClass, ex);
                    }
                    intoMap.put(propertyName, propertyDescriptor);
                }
            }
        }
    }

    private static boolean indexMethodCheck(List<Method> methods) {
        boolean result = false;

        if (!methods.isEmpty()) {
            Method method = methods.get(0);
            Class<?>[] parameterTypes = OgnlRuntime.getParameterTypes(method);
            int numParameterTypes = parameterTypes.length;
            Class<?> lastMethodClass = method.getDeclaringClass();

            result = true;
            for (int i = 1; result && (i < methods.size()); i++) {
                Class<?> clazz = methods.get(i).getDeclaringClass();

                // Check to see if more than one method implemented per class
                if (lastMethodClass == clazz) {
                    result = false;
                } else {
                    Class<?>[] mpt = OgnlRuntime.getParameterTypes(method);
                    for (int j = 0; j < numParameterTypes; j++) {
                        if (parameterTypes[j] != mpt[j]) {
                            result = false;
                            break;
                        }
                    }
                }
                lastMethodClass = clazz;
            }
        }
        return result;
    }

}
