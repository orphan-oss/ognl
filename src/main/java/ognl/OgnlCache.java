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

import ognl.internal.*;
import ognl.internal.entry.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.security.Permission;
import java.util.*;

/**
 * This class takes care of all the internal caching for OGNL.
 */
public class OgnlCache {

    private final CacheFactory cacheFactory = new HashMapCacheFactory();

    private final ClassCache<MethodAccessor> methodAccessors = cacheFactory.createClassCache();

    {
        MethodAccessor methodAccessor = new ObjectMethodAccessor();
        setMethodAccessor(Object.class, methodAccessor);
        setMethodAccessor(byte[].class, methodAccessor);
        setMethodAccessor(short[].class, methodAccessor);
        setMethodAccessor(char[].class, methodAccessor);
        setMethodAccessor(int[].class, methodAccessor);
        setMethodAccessor(long[].class, methodAccessor);
        setMethodAccessor(float[].class, methodAccessor);
        setMethodAccessor(double[].class, methodAccessor);
        setMethodAccessor(Object[].class, methodAccessor);
    }

    private final ClassCache<PropertyAccessor> propertyAccessors = cacheFactory.createClassCache();

    {
        PropertyAccessor propertyAccessor = new ArrayPropertyAccessor();
        setPropertyAccessor(Object.class, new ObjectPropertyAccessor());
        setPropertyAccessor(byte[].class, propertyAccessor);
        setPropertyAccessor(short[].class, propertyAccessor);
        setPropertyAccessor(char[].class, propertyAccessor);
        setPropertyAccessor(int[].class, propertyAccessor);
        setPropertyAccessor(long[].class, propertyAccessor);
        setPropertyAccessor(float[].class, propertyAccessor);
        setPropertyAccessor(double[].class, propertyAccessor);
        setPropertyAccessor(Object[].class, propertyAccessor);
        setPropertyAccessor(List.class, new ListPropertyAccessor());
        setPropertyAccessor(Map.class, new MapPropertyAccessor());
        setPropertyAccessor(Set.class, new SetPropertyAccessor());
        setPropertyAccessor(Iterator.class, new IteratorPropertyAccessor());
        setPropertyAccessor(Enumeration.class, new EnumerationPropertyAccessor());
    }

    private final ClassCache<ElementsAccessor> elementsAccessors = cacheFactory.createClassCache();

    {
        ElementsAccessor elementsAccessor = new ArrayElementsAccessor();
        setElementsAccessor(Object.class, new ObjectElementsAccessor());
        setElementsAccessor(byte[].class, elementsAccessor);
        setElementsAccessor(short[].class, elementsAccessor);
        setElementsAccessor(char[].class, elementsAccessor);
        setElementsAccessor(int[].class, elementsAccessor);
        setElementsAccessor(long[].class, elementsAccessor);
        setElementsAccessor(float[].class, elementsAccessor);
        setElementsAccessor(double[].class, elementsAccessor);
        setElementsAccessor(Object[].class, elementsAccessor);
        setElementsAccessor(Collection.class, new CollectionElementsAccessor());
        setElementsAccessor(Map.class, new MapElementsAccessor());
        setElementsAccessor(Iterator.class, new IteratorElementsAccessor());
        setElementsAccessor(Enumeration.class, new EnumerationElementsAccessor());
        setElementsAccessor(Number.class, new NumberElementsAccessor());
    }

    private final ClassCache<NullHandler> nullHandlers = cacheFactory.createClassCache();

    {
        NullHandler nullHandler = new ObjectNullHandler();
        setNullHandler(Object.class, nullHandler);
        setNullHandler(byte[].class, nullHandler);
        setNullHandler(short[].class, nullHandler);
        setNullHandler(char[].class, nullHandler);
        setNullHandler(int[].class, nullHandler);
        setNullHandler(long[].class, nullHandler);
        setNullHandler(float[].class, nullHandler);
        setNullHandler(double[].class, nullHandler);
        setNullHandler(Object[].class, nullHandler);
    }

    final ClassCache<Map<String, PropertyDescriptor>> propertyDescriptorCache =
            cacheFactory.createClassCache(new PropertyDescriptorCacheEntryFactory());

    private final ClassCache<List<Constructor<?>>> constructorCache =
            cacheFactory.createClassCache(key -> Arrays.asList(key.getConstructors()));

    private final Cache<DeclaredMethodCacheEntry, Map<String, List<Method>>> methodCache =
            cacheFactory.createCache(new DeclaredMethodCacheEntryFactory());

    private final Cache<PermissionCacheEntry, Permission> invokePermissionCache =
            cacheFactory.createCache(new PermissionCacheEntryFactory());

    private final ClassCache<Map<String, Field>> fieldCache =
            cacheFactory.createClassCache(new FieldCacheEntryFactory());

    private final Cache<Method, Class<?>[]> methodParameterTypesCache =
            cacheFactory.createCache(Method::getParameterTypes);

    final Cache<GenericMethodParameterTypeCacheEntry, Class<?>[]> genericMethodParameterTypesCache =
            cacheFactory.createCache(new GenericMethodParameterTypeFactory());

    private final Cache<Constructor<?>, Class<?>[]> ctorParameterTypesCache =
            cacheFactory.createCache(Constructor::getParameterTypes);

    private final Cache<Method, MethodAccessEntryValue> methodAccessCache =
            cacheFactory.createCache(new MethodAccessCacheEntryFactory());

    private final MethodPermCacheEntryFactory methodPermCacheEntryFactory =
            new MethodPermCacheEntryFactory(System.getSecurityManager());

    private final Cache<Method, Boolean> methodPermCache = cacheFactory.createCache(methodPermCacheEntryFactory);

    public Class<?>[] getMethodParameterTypes(Method method) throws CacheException {
        return methodParameterTypesCache.get(method);
    }

    public Class<?>[] getParameterTypes(Constructor<?> constructor) throws CacheException {
        return ctorParameterTypesCache.get(constructor);
    }

    public List<Constructor<?>> getConstructor(Class<?> clazz) throws CacheException {
        return constructorCache.get(clazz);
    }

    public Map<String, Field> getField(Class<?> clazz) throws CacheException {
        return fieldCache.get(clazz);
    }

    public Map<String, List<Method>> getMethod(DeclaredMethodCacheEntry declaredMethodCacheEntry) throws CacheException {
        return methodCache.get(declaredMethodCacheEntry);
    }

    public Map<String, PropertyDescriptor> getPropertyDescriptor(Class<?> clazz) throws CacheException {
        return propertyDescriptorCache.get(clazz);
    }

    public Permission getInvokePermission(PermissionCacheEntry permissionCacheEntry) throws CacheException {
        return invokePermissionCache.get(permissionCacheEntry);
    }

    public MethodAccessor getMethodAccessor(Class<?> clazz) throws OgnlException {
        MethodAccessor methodAccessor = ClassCacheHandler.getHandler(clazz, methodAccessors);
        if (methodAccessor != null) {
            return methodAccessor;
        }
        throw new OgnlException("No method accessor for " + clazz);
    }

    public void setMethodAccessor(Class<?> clazz, MethodAccessor accessor) {
        methodAccessors.put(clazz, accessor);
    }

    public void setPropertyAccessor(Class<?> clazz, PropertyAccessor accessor) {
        propertyAccessors.put(clazz, accessor);
    }

    public PropertyAccessor getPropertyAccessor(Class<?> clazz) throws OgnlException {
        PropertyAccessor propertyAccessor = ClassCacheHandler.getHandler(clazz, propertyAccessors);
        if (propertyAccessor != null) {
            return propertyAccessor;
        }
        throw new OgnlException("No property accessor for class " + clazz);
    }

    /**
     * Registers the specified {@link ClassCacheInspector} with all class reflection based internal caches. This may
     * have a significant performance impact so be careful using this in production scenarios.
     *
     * @param inspector The inspector instance that will be registered with all internal cache instances.
     */
    public void setClassCacheInspector(ClassCacheInspector inspector) {
        propertyDescriptorCache.setClassInspector(inspector);
        constructorCache.setClassInspector(inspector);
        //TODO: methodCache and invokePC should allow to use classCacheInsecptor
//        _methodCache.setClassInspector( inspector );
//        _invokePermissionCache.setClassInspector( inspector );
        fieldCache.setClassInspector(inspector);
    }

    public Class<?>[] getGenericMethodParameterTypes(GenericMethodParameterTypeCacheEntry key) throws CacheException {
        return genericMethodParameterTypesCache.get(key);
    }

    public boolean getMethodPerm(Method method) throws CacheException {
        return methodPermCache.get(method);
    }

    public MethodAccessEntryValue getMethodAccess(Method method) throws CacheException {
        return methodAccessCache.get(method);
    }

    public void clear() {
        methodParameterTypesCache.clear();
        ctorParameterTypesCache.clear();
        propertyDescriptorCache.clear();
        genericMethodParameterTypesCache.clear();
        constructorCache.clear();
        methodCache.clear();
        invokePermissionCache.clear();
        fieldCache.clear();
        methodAccessCache.clear();
    }

    public ElementsAccessor getElementsAccessor(Class<?> clazz) throws OgnlException {
        ElementsAccessor answer = ClassCacheHandler.getHandler(clazz, elementsAccessors);
        if (answer != null) {
            return answer;
        }
        throw new OgnlException("No elements accessor for class " + clazz);
    }

    public void setElementsAccessor(Class<?> clazz, ElementsAccessor accessor) {
        elementsAccessors.put(clazz, accessor);
    }

    public NullHandler getNullHandler(Class<?> clazz) throws OgnlException {
        NullHandler answer = ClassCacheHandler.getHandler(clazz, nullHandlers);
        if (answer != null) {
            return answer;
        }
        throw new OgnlException("No null handler for class " + clazz);
    }

    public void setNullHandler(Class<?> clazz, NullHandler handler) {
        nullHandlers.put(clazz, handler);
    }

    public void setSecurityManager(SecurityManager securityManager) {
        methodPermCacheEntryFactory.setSecurityManager(securityManager);
    }

}
