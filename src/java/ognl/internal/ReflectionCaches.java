package ognl.internal;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import ognl.OgnlInvokePermission;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ReflectionCaches {

    public static LazyCache<Class, String> canonicalName() {
        return new LazyCache<Class, String>(new Function<Class, String>() {
            public String apply(Class clazz) {
                return clazz.getCanonicalName();
            }
        });
    }

    public static LazyCache<Class, Set<Class>> interfaces() {
        return new LazyCache<Class, Set<Class>>(new Function<Class, Set<Class>>() {
            public Set<Class> apply(Class clazz) {
                return ImmutableSet.<Class>builder().addAll(Arrays.asList(clazz.getInterfaces())).build();
            }
        });
    }

    public static LazyCache<Class, Set<Constructor>> constructors() {
        return new LazyCache<Class, Set<Constructor>>(new Function<Class, Set<Constructor>>() {
            public Set<Constructor> apply(Class clazz) {
                return ImmutableSet.<Constructor>builder().addAll(Arrays.asList(clazz.getConstructors())).build();
            }
        });
    }

    public static LazyCache<Class, Set<Method>> methods() {
        return new LazyCache<Class, Set<Method>>(new Function<Class, Set<Method>>() {
            public Set<Method> apply(Class clazz) {
                return ImmutableSet.<Method>builder().addAll(Arrays.asList(clazz.getMethods())).build();
            }
        });
    }

    public static LazyCache<Class, Class> superClass() {
        return new LazyCache<Class, Class>(new Function<Class, Class>() {
            public Class apply(Class clazz) {
                return clazz.getSuperclass();
            }
        });
    }

    public static LazyCache<Class, Boolean> isPrimitive() {
        return new LazyCache<Class, Boolean>(new Function<Class, Boolean>() {
            public Boolean apply(Class clazz) {
                return clazz.isPrimitive();
            }
        });
    }

    public static LazyCache<Class, Boolean> isPublicInterface() {
        return new LazyCache<Class, Boolean>(new Function<Class, Boolean>() {
            public Boolean apply(Class clazz) {
                return Modifier.isPublic(clazz.getModifiers()) && clazz.isInterface() || clazz.isPrimitive();
            }
        });
    }

    public static LazyCache<Method, List<Class<?>>> methodParameterTypes() {
        return new LazyCache<Method, List<Class<?>>>(new Function<Method, List<Class<?>>>() {
            public List<Class<?>> apply(Method method) {
                return Collections.unmodifiableList(Arrays.asList(method.getParameterTypes()));
            }
        });
    }

    public static LazyCache<Method, Permission> permission() {
        return new LazyCache<Method, Permission>(new Function<Method, Permission>() {
            public Permission apply(Method method) {
                return new OgnlInvokePermission("invoke." + method.getDeclaringClass() + "." + method.getName());
            }
        });
    }

    public static LazyCache<Method, Set<Class<?>>> exceptionTypes() {
        return new LazyCache<Method, Set<Class<?>>>(new Function<Method, Set<Class<?>>>() {
            public Set<Class<?>> apply(Method method) {
                return ImmutableSet.<Class<?>>builder().addAll(Arrays.asList(method.getExceptionTypes())).build();
            }
        });
    }
}
