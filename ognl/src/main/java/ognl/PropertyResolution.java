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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class PropertyResolution {

    private static volatile Generation generation = new Generation();

    private PropertyResolution() {
    }

    static List<PropertyCandidate> candidates(Class<?> targetClass, String propertyName, PropertyAccessKind kind) {
        Map<String, List<PropertyCandidate>> byName = generation.forClass(targetClass).get(kind);
        List<PropertyCandidate> candidates = byName.get(propertyName);
        if (candidates == null) {
            candidates = resolve(targetClass, propertyName, kind);
            List<PropertyCandidate> raced = byName.putIfAbsent(propertyName, candidates);
            if (raced != null) {
                candidates = raced;
            }
        }
        return candidates;
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
        return OgnlRuntime.isMethodAccessible(context, target, (Method) candidate.member(), propertyName);
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
            return allListed(candidates, OgnlRuntime.getMethods(targetClass, member.getName(), false))
                    && allListed(candidates, OgnlRuntime.getMethods(targetClass, member.getName(), true));
        }
        if (candidates.isEmpty()) {
            return false;
        }
        PropertyCandidate first = candidates.get(0);
        if (kind == PropertyAccessKind.READ && first.step() == PropertyCandidate.Step.LENIENT
                && context.isIgnoreReadMethods()) {
            return false;
        }
        return sameMember(first, member);
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
            return offered instanceof Method offeredMethod && offeredMethod.getName().equals(method.getName())
                    && Arrays.equals(offeredMethod.getParameterTypes(), method.getParameterTypes());
        }
        return member instanceof Field && candidate.step() == PropertyCandidate.Step.FIELD
                && offered.getName().equals(member.getName());
    }

    static boolean isAssignable(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers);
    }

    static void clear() {
        generation = new Generation();
    }

    private static List<PropertyCandidate> resolve(Class<?> targetClass, String propertyName, PropertyAccessKind kind) {
        PropertyResolver resolver = OgnlRuntime.getPropertyResolver();
        List<PropertyCandidate> candidates = resolver.candidates(targetClass, propertyName, kind);
        if (candidates == null) {
            throw nullResult(resolver, "null", targetClass, propertyName, kind);
        }
        for (PropertyCandidate candidate : candidates) {
            if (candidate == null) {
                throw nullResult(resolver, "a null candidate", targetClass, propertyName, kind);
            }
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

    private static NullPointerException nullResult(PropertyResolver resolver, String what, Class<?> targetClass,
                                                   String propertyName, PropertyAccessKind kind) {
        return new NullPointerException(describe(resolver, what, targetClass, propertyName, kind));
    }

    private static String describe(PropertyResolver resolver, String what, Class<?> targetClass, String propertyName,
                                   PropertyAccessKind kind) {
        return resolver.getClass().getName() + " returned " + what + " for " + kind
                + " of property '" + propertyName + "' on " + (targetClass == null ? "null" : targetClass.getName());
    }

    static int cachedClassCount() {
        return generation.byClass.size();
    }

    private static final class Generation {
        private final Map<Class<?>, Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>>> byClass =
                new ConcurrentHashMap<>();
        private final Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>> nullClass = newKindMap();

        Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>> forClass(Class<?> targetClass) {
            if (targetClass == null) {
                return nullClass;
            }
            Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>> byKind = byClass.get(targetClass);
            if (byKind == null) {
                byKind = newKindMap();
                Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>> raced = byClass.putIfAbsent(targetClass, byKind);
                if (raced != null) {
                    byKind = raced;
                }
            }
            return byKind;
        }

        private static Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>> newKindMap() {
            Map<PropertyAccessKind, Map<String, List<PropertyCandidate>>> byKind = new EnumMap<>(PropertyAccessKind.class);
            for (PropertyAccessKind kind : PropertyAccessKind.values()) {
                byKind.put(kind, new ConcurrentHashMap<>());
            }
            return byKind;
        }
    }
}
