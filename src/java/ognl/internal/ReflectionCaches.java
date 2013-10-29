package ognl.internal;

import com.google.common.base.Function;
import ognl.OgnlInvokePermission;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReflectionCaches {

    public static LazyCache<Class, String> canonicalName() {
        return new LazyCache<Class, String>(new Function<Class, String>() {
            public String apply(Class clazz) {
                return clazz.getCanonicalName();
            }
        });
    }

    public static LazyCache<Class, List<Class<?>>> interfaces() {
        return new LazyCache<Class, List<Class<?>>>(new Function<Class, List<Class<?>>>() {
            public List<Class<?>> apply(Class clazz) {
                return Collections.unmodifiableList(Arrays.<Class<?>>asList(clazz.getInterfaces()));
            }
        });
    }

    public static LazyCache<Class, List<Constructor>> constructors() {
        return new LazyCache<Class, List<Constructor>>(new Function<Class, List<Constructor>>() {
            public List<Constructor> apply(Class clazz) {
                return Collections.unmodifiableList(Arrays.asList(clazz.getConstructors()));
            }
        });
    }

    public static LazyCache<Class, List<Method>> methods() {
        return new LazyCache<Class, List<Method>>(new Function<Class, List<Method>>() {
            public List<Method> apply(Class clazz) {
                return Collections.unmodifiableList(Arrays.asList(clazz.getMethods()));
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

    public static LazyCache<Method, List<Class<?>>> exceptionTypes() {
        return new LazyCache<Method, List<Class<?>>>(new Function<Method, List<Class<?>>>() {
            public List<Class<?>> apply(Method method) {
                return Collections.unmodifiableList(Arrays.asList(method.getExceptionTypes()));
            }
        });
    }

    public static LazyCache<Method,Boolean> accessibleAccessHackCache() {
        return new LazyCache<Method, Boolean>(new Function<Method, Boolean>() {
            public Boolean apply(Method method) {
                if(!method.isAccessible())
                    method.setAccessible(true);
                return true;
            }
        });
    }
}
