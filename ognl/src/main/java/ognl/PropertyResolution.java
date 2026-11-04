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

import ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class PropertyResolution {

    private static final Class<?> NULL_TARGET = NullTarget.class;
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Resolved>> READ = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Resolved>> WRITE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Resolved>> INDEXED_READ = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Resolved>> INDEXED_WRITE = new ConcurrentHashMap<>();

    private PropertyResolution() {
    }

    static List<PropertyCandidate> candidates(Class<?> targetClass, String propertyName, PropertyAccessKind kind) {
        return resolved(targetClass, propertyName, kind).candidates;
    }

    static Resolved resolved(Class<?> targetClass, String propertyName, PropertyAccessKind kind) {
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Resolved>> byClass = byClass(kind);
        Class<?> key = targetClass == null ? NULL_TARGET : targetClass;
        ConcurrentHashMap<String, Resolved> byName = byClass.get(key);
        if (byName == null) {
            byName = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, Resolved> raced = byClass.putIfAbsent(key, byName);
            if (raced != null) {
                byName = raced;
            }
        }
        Resolved resolved = byName.get(propertyName);
        if (resolved == null) {
            resolved = new Resolved(resolve(targetClass, propertyName, kind));
            Resolved raced = byName.putIfAbsent(propertyName, resolved);
            if (raced != null) {
                resolved = raced;
            }
        }
        return resolved;
    }

    private static ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Resolved>> byClass(PropertyAccessKind kind) {
        if (kind == PropertyAccessKind.READ) {
            return READ;
        }
        if (kind == PropertyAccessKind.WRITE) {
            return WRITE;
        }
        return kind == PropertyAccessKind.INDEXED_READ ? INDEXED_READ : INDEXED_WRITE;
    }

    static <C extends OgnlContext<C>> PropertyCandidate firstUsable(C context, Object target, String propertyName,
                                                                    Resolved resolved, PropertyAccessKind kind, int from) {
        List<PropertyCandidate> candidates = resolved.candidates;
        for (int i = from, size = candidates.size(); i < size; i++) {
            PropertyCandidate candidate = candidates.get(i);
            if (isUsable(context, target, propertyName, candidate, kind)) {
                return candidate;
            }
        }
        return null;
    }

    static <C extends OgnlContext<C>> boolean isUsable(C context, Object target, String propertyName,
                                                       PropertyCandidate candidate, PropertyAccessKind kind) {
        if (candidate.step() == PropertyCandidate.Step.FIELD) {
            Field field = (Field) candidate.member();
            return (kind != PropertyAccessKind.WRITE || isAssignable(field))
                    && OgnlRuntime.isFieldAccessible(context, target, field, propertyName);
        }
        if (candidate.step() == PropertyCandidate.Step.LENIENT && kind == PropertyAccessKind.READ
                && context.isIgnoreReadMethods()) {
            return false;
        }
        return OgnlRuntime.isMethodAccessible(context, target, (Method) candidate.member(),
                candidate.step() == PropertyCandidate.Step.INDEXED ? null : propertyName);
    }

    static <M extends Member> M requireCompilable(OgnlContext<?> context, Class<?> targetClass, String propertyName,
                                                  PropertyAccessKind kind, M member) {
        if (member != null && !isCompilable(context, targetClass, propertyName, kind, member)) {
            throw new UnsupportedCompilationException("Property " + propertyName
                    + " is not offered by the PropertyResolver on " + targetClass.getName());
        }
        return member;
    }

    static boolean isCompilable(OgnlContext<?> context, Class<?> targetClass, String propertyName,
                                PropertyAccessKind kind, Member member) {
        if (OgnlRuntime.getPropertyResolver() == DefaultPropertyResolver.INSTANCE) {
            return true;
        }
        List<PropertyCandidate> candidates = candidates(targetClass, propertyName, kind);
        if (kind == PropertyAccessKind.INDEXED_READ || kind == PropertyAccessKind.INDEXED_WRITE) {
            return hasDefaultMethodAccessor(targetClass)
                    && allListed(candidates, OgnlRuntime.getMethods(targetClass, member.getName(), false))
                    && allListed(candidates, OgnlRuntime.getMethods(targetClass, member.getName(), true));
        }
        if (candidates.isEmpty()) {
            return false;
        }
        PropertyCandidate first = candidates.get(0);
        if (kind == PropertyAccessKind.READ && first.step() == PropertyCandidate.Step.LENIENT) {
            return false;
        }
        return sameMember(first, member);
    }

    private static boolean hasDefaultMethodAccessor(Class<?> targetClass) {
        try {
            return OgnlRuntime.getMethodAccessor(targetClass).getClass() == ObjectMethodAccessor.class;
        } catch (OgnlException e) {
            return false;
        }
    }

    private static boolean allListed(List<PropertyCandidate> candidates, List<Method> overloads) {
        return overloads == null || overloads.stream().allMatch(overload -> isListed(candidates, overload));
    }

    static boolean isListed(List<PropertyCandidate> candidates, Method method) {
        for (PropertyCandidate candidate : candidates) {
            if (sameMember(candidate, method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameMember(PropertyCandidate candidate, Member member) {
        Member offered = candidate.member();
        if (member instanceof Method method) {
            return offered instanceof Method offeredMethod
                    && (offeredMethod.equals(method) || sameVirtualMethod(offeredMethod, method));
        }
        return member instanceof Field && candidate.step() == PropertyCandidate.Step.FIELD && offered.equals(member);
    }

    private static boolean sameVirtualMethod(Method a, Method b) {
        return isVirtual(a) && isVirtual(b)
                && (a.getDeclaringClass().isAssignableFrom(b.getDeclaringClass())
                || b.getDeclaringClass().isAssignableFrom(a.getDeclaringClass()))
                && a.getName().equals(b.getName())
                && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
    }

    private static boolean isVirtual(Method method) {
        int modifiers = method.getModifiers();
        return !Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers);
    }

    static boolean isAssignable(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers);
    }

    static void clear() {
        READ.clear();
        WRITE.clear();
        INDEXED_READ.clear();
        INDEXED_WRITE.clear();
    }

    private static List<PropertyCandidate> resolve(Class<?> targetClass, String propertyName, PropertyAccessKind kind) {
        PropertyResolver resolver = OgnlRuntime.getPropertyResolver();
        List<PropertyCandidate> candidates = Objects.requireNonNull(
                resolver.candidates(targetClass, propertyName, kind),
                () -> describe(resolver, "null", targetClass, propertyName, kind));
        for (PropertyCandidate candidate : candidates) {
            Objects.requireNonNull(candidate, () -> describe(resolver, "a null candidate", targetClass, propertyName, kind));
            if (!fits(candidate.step(), kind)) {
                throw new IllegalStateException(describe(resolver, "a " + candidate.step() + " candidate", targetClass,
                        propertyName, kind));
            }
        }
        return List.copyOf(candidates);
    }

    private static boolean fits(PropertyCandidate.Step step, PropertyAccessKind kind) {
        boolean indexedKind = kind == PropertyAccessKind.INDEXED_READ || kind == PropertyAccessKind.INDEXED_WRITE;
        return indexedKind == (step == PropertyCandidate.Step.INDEXED);
    }

    private static String describe(PropertyResolver resolver, String what, Class<?> targetClass, String propertyName,
                                   PropertyAccessKind kind) {
        return resolver.getClass().getName() + " returned " + what + " for " + kind
                + " of property '" + propertyName + "' on " + (targetClass == null ? "null" : targetClass.getName());
    }

    static int cachedClassCount() {
        Set<Class<?>> classes = new HashSet<>();
        for (PropertyAccessKind kind : PropertyAccessKind.values()) {
            classes.addAll(byClass(kind).keySet());
        }
        classes.remove(NULL_TARGET);
        return classes.size();
    }

    static final class Resolved {
        final List<PropertyCandidate> candidates;
        final Method accessor;

        private Resolved(List<PropertyCandidate> candidates) {
            this.candidates = candidates;
            this.accessor = !candidates.isEmpty() && candidates.get(0).step() == PropertyCandidate.Step.ACCESSOR
                    ? (Method) candidates.get(0).member()
                    : null;
        }
    }

    private interface NullTarget {
    }
}
