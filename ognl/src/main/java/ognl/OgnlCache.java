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

import ognl.internal.Cache;
import ognl.internal.CacheException;
import ognl.internal.CacheFactory;
import ognl.internal.ClassCache;
import ognl.internal.HandlerRegistry;
import ognl.internal.HashMapCacheFactory;
import ognl.internal.entry.CacheEntryFactory;
import ognl.internal.entry.ClassCacheEntryFactory;
import ognl.internal.entry.DeclaredMethodCacheEntry;
import ognl.internal.entry.DeclaredMethodCacheEntryFactory;
import ognl.internal.entry.FieldCacheEntryFactory;
import ognl.internal.entry.GenericMethodParameterTypeCacheEntry;
import ognl.internal.entry.GenericMethodParameterTypeFactory;
import ognl.internal.entry.PropertyDescriptorCacheEntryFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class takes care of all the internal caching for OGNL.
 * <p>
 * Every cache is created through {@link #newCache(CacheEntryFactory)}, {@link #newClassCache(ClassCacheEntryFactory)}
 * or {@link #newMap()}, which also records it. {@link #clear()} and {@link #setClassCacheInspector(ClassCacheInspector)}
 * then go through what was recorded, so a cache cannot be left out of either.
 */
public class OgnlCache {

    private final CacheFactory cacheFactory = new HashMapCacheFactory();

    // Everything clear() has to empty, and every per-class cache the inspector applies to
    private final List<Runnable> clearActions = new ArrayList<>();
    private final List<ClassCache<?>> classCaches = new ArrayList<>();

    private <K, V> Cache<K, V> newCache(CacheEntryFactory<K, V> entryFactory) {
        Cache<K, V> cache = cacheFactory.createCache(entryFactory);
        clearActions.add(cache::clear);
        return cache;
    }

    private <V> ClassCache<V> newClassCache(ClassCacheEntryFactory<V> entryFactory) {
        ClassCache<V> cache = cacheFactory.createClassCache(entryFactory);
        clearActions.add(cache::clear);
        classCaches.add(cache);
        return cache;
    }

    private <K, V> Map<K, V> newMap() {
        Map<K, V> map = new ConcurrentHashMap<>();
        clearActions.add(map::clear);
        return map;
    }

    private final HandlerRegistry<MethodAccessor> methodAccessors = new HandlerRegistry<>(newClassCache(null));

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

    private final HandlerRegistry<PropertyAccessor> propertyAccessors = new HandlerRegistry<>(newClassCache(null));

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

    private final HandlerRegistry<ElementsAccessor> elementsAccessors = new HandlerRegistry<>(newClassCache(null));

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

    private final HandlerRegistry<NullHandler> nullHandlers = new HandlerRegistry<>(newClassCache(null));

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
            newClassCache(new PropertyDescriptorCacheEntryFactory());

    private final ClassCache<List<Constructor<?>>> constructorCache =
            newClassCache(key -> Arrays.asList(key.getConstructors()));

    private final Cache<DeclaredMethodCacheEntry, Map<String, List<Method>>> methodCache =
            newCache(new DeclaredMethodCacheEntryFactory());

    private final ClassCache<Map<String, Field>> fieldCache =
            newClassCache(new FieldCacheEntryFactory());

    private final Cache<Method, Class<?>[]> methodParameterTypesCache =
            newCache(Method::getParameterTypes);

    final Cache<GenericMethodParameterTypeCacheEntry, Class<?>[]> genericMethodParameterTypesCache =
            newCache(new GenericMethodParameterTypeFactory());

    private final Cache<Constructor<?>, Class<?>[]> ctorParameterTypesCache =
            newCache(Constructor::getParameterTypes);

    private final ClassCache<Class<?>> interfaceClassCache =
            newClassCache(key -> OgnlRuntime.getCompiler().getInterfaceClass(key));

    // Whether invoking a method needs it to be made accessible first (see OgnlRuntime#invokeMethod)
    private final Map<Method, Boolean> methodNeedsAccessCache = newMap();

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

    public Class<?> getInterfaceClass(Class<?> clazz) throws CacheException {
        return interfaceClassCache.get(clazz);
    }

    void clearInterfaceClassCache() {
        interfaceClassCache.clear();
    }

    public <C extends OgnlContext<C>> MethodAccessor<C> getMethodAccessor(Class<?> clazz) throws OgnlException {
        MethodAccessor methodAccessor = methodAccessors.get(clazz);
        if (methodAccessor != null) {
            return methodAccessor;
        }
        throw new OgnlException("No method accessor for " + clazz);
    }

    public void setMethodAccessor(Class<?> clazz, MethodAccessor accessor) {
        methodAccessors.register(clazz, accessor);
    }

    public void setPropertyAccessor(Class<?> clazz, PropertyAccessor accessor) {
        propertyAccessors.register(clazz, accessor);
    }

    public <C extends OgnlContext<C>> PropertyAccessor<C> getPropertyAccessor(Class<?> clazz) throws OgnlException {
        PropertyAccessor<C> propertyAccessor = propertyAccessors.get(clazz);
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
        for (ClassCache<?> classCache : classCaches) {
            classCache.setClassInspector(inspector);
        }
    }

    public Class<?>[] getGenericMethodParameterTypes(GenericMethodParameterTypeCacheEntry key) throws CacheException {
        return genericMethodParameterTypesCache.get(key);
    }

    @Deprecated(since = "3.4.6", forRemoval = true)
    public boolean getMethodPerm(Method method) throws CacheException {
        return true;
    }

    Boolean getMethodNeedsAccess(Method method) {
        return methodNeedsAccessCache.get(method);
    }

    void putMethodNeedsAccess(Method method, Boolean needsAccess) {
        methodNeedsAccessCache.put(method, needsAccess);
    }

    /**
     * Empties every cache. Registered accessors and null handlers are kept, what was derived from them is not.
     */
    public void clear() {
        for (Runnable clearAction : clearActions) {
            clearAction.run();
        }
    }

    /**
     * Records a cache kept outside this class, so that {@link #clear()} empties it too.
     */
    void register(Runnable clearAction) {
        clearActions.add(clearAction);
    }

    public ElementsAccessor getElementsAccessor(Class<?> clazz) throws OgnlException {
        ElementsAccessor answer = elementsAccessors.get(clazz);
        if (answer != null) {
            return answer;
        }
        throw new OgnlException("No elements accessor for class " + clazz);
    }

    public void setElementsAccessor(Class<?> clazz, ElementsAccessor accessor) {
        elementsAccessors.register(clazz, accessor);
    }

    public <C extends OgnlContext<C>> NullHandler<C> getNullHandler(Class<?> clazz) throws OgnlException {
        NullHandler<C> answer = nullHandlers.get(clazz);
        if (answer != null) {
            return answer;
        }
        throw new OgnlException("No null handler for class " + clazz);
    }

    public void setNullHandler(Class<?> clazz, NullHandler handler) {
        nullHandlers.register(clazz, handler);
    }

}
