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
package ognl.test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import ognl.ClassResolver;
import ognl.ObjectPropertyAccessor;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.enhance.ContextClassLoader;
import ognl.enhance.EnhancedClassLoader;
import ognl.test.util.NameFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that uses Javassist to compile a property accessor
 * specifically tailored to the property.
 */
public class CompilingPropertyAccessor extends ObjectPropertyAccessor {

    private static final NameFactory NAME_FACTORY = new NameFactory("ognl.PropertyAccessor", "v");
    private static final Getter NotFoundGetter = (context, target, propertyName) -> null;
    private static final Getter DefaultGetter = (context, target, propertyName) -> {
        try {
            return OgnlRuntime.getMethodValue(context, target, propertyName, true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    };
    private static final Map<ClassResolver, ClassPool> pools = new HashMap<>();
    private static final Map<ClassResolver, EnhancedClassLoader> loaders = new HashMap<>();

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_CLASSES = new IdentityHashMap<>();
    private final Map<Class<?>, Map<String, Getter>> seenGetMethods = new IdentityHashMap<>();

    static {
        PRIMITIVE_WRAPPER_CLASSES.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Boolean.class, Boolean.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Byte.TYPE, Byte.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Byte.class, Byte.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Character.TYPE, Character.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Character.class, Character.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Short.TYPE, Short.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Short.class, Short.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Integer.TYPE, Integer.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Integer.class, Integer.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Long.TYPE, Long.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Long.class, Long.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Float.TYPE, Float.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Float.class, Float.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Double.TYPE, Double.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Double.class, Double.TYPE);
    }

    public static Class<?> getPrimitiveWrapperClass(Class<?> primitiveClass) {
        return PRIMITIVE_WRAPPER_CLASSES.get(primitiveClass);
    }

    public interface Getter {
        Object get(OgnlContext context, Object target, String propertyName);
    }

    public static Getter generateGetter(OgnlContext context, String code) throws OgnlException {
        String className = NAME_FACTORY.getNewClassName();

        try {
            ClassPool pool = pools.get(context.getClassResolver());
            EnhancedClassLoader loader = loaders.get(context.getClassResolver());
            CtClass newClass;
            CtClass ognlContextClass;
            CtClass objectClass;
            CtClass stringClass;
            CtMethod method;
            byte[] byteCode;
            Class<?> compiledClass;

            if ((pool == null) || (loader == null)) {
                ClassLoader classLoader = new ContextClassLoader(OgnlContext.class.getClassLoader(), context);

                pool = ClassPool.getDefault();
                pool.insertClassPath(new LoaderClassPath(classLoader));
                pools.put(context.getClassResolver(), pool);

                loader = new EnhancedClassLoader(classLoader);
                loaders.put(context.getClassResolver(), loader);
            }

            newClass = pool.makeClass(className);
            ognlContextClass = pool.get(OgnlContext.class.getName());
            objectClass = pool.get(Object.class.getName());
            stringClass = pool.get(String.class.getName());

            newClass.addInterface(pool.get(Getter.class.getName()));
            method = new CtMethod(objectClass, "get", new CtClass[]{ognlContextClass, objectClass, stringClass},
                    newClass);
            method.setBody("{" + code + "}");
            newClass.addMethod(method);
            byteCode = newClass.toBytecode();
            compiledClass = loader.defineClass(className, byteCode);
            return (Getter) compiledClass.newInstance();
        } catch (Throwable ex) {
            throw new OgnlException("Cannot create class", ex);
        }
    }

    private Getter getGetter(OgnlContext context, Object target, String propertyName) throws OgnlException {
        Getter result;
        Class<?> targetClass = target.getClass();
        Map<String, Getter> propertyMap;

        if ((propertyMap = seenGetMethods.get(targetClass)) == null) {
            propertyMap = new HashMap<>(101);
            seenGetMethods.put(targetClass, propertyMap);
        }
        if ((result = propertyMap.get(propertyName)) == null) {
            try {
                Method method = OgnlRuntime.getGetMethod(targetClass, propertyName);

                if (method != null) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        if (method.getReturnType().isPrimitive()) {
                            propertyMap.put(propertyName, result = generateGetter(context,
                                    "java.lang.Object\t\tresult;\n" + targetClass.getName() + "\t" + "t0 = ("
                                            + targetClass.getName() + ")$2;\n" + "\n" + "try {\n" + "   result = new "
                                            + getPrimitiveWrapperClass(method.getReturnType()).getName() + "(t0."
                                            + method.getName() + "());\n" + "} catch (java.lang.Exception ex) {\n"
                                            + "    throw new java.lang.RuntimeException(ex);\n" + "}\n"
                                            + "return result;"));
                        } else {
                            propertyMap.put(propertyName, result = generateGetter(context,
                                    "java.lang.Object\t\tresult;\n" + targetClass.getName() + "\t" + "t0 = ("
                                            + targetClass.getName() + ")$2;\n" + "\n" + "try {\n" + "   result = t0."
                                            + method.getName() + "();\n" + "} catch (java.lang.Exception ex) {\n"
                                            + "    throw new java.lang.RuntimeException(ex);\n" + "}\n"
                                            + "return result;"));
                        }
                    } else {
                        propertyMap.put(propertyName, result = DefaultGetter);
                    }
                } else {
                    propertyMap.put(propertyName, result = NotFoundGetter);
                }
            } catch (Exception ex) {
                throw new OgnlException("getting getter", ex);
            }
        }
        return result;
    }

    /**
     * Returns OgnlRuntime.NotFound if the property does not exist.
     */
    public Object getPossibleProperty(OgnlContext context, Object target, String name) throws OgnlException {
        Object result;

        if (context.get("_compile") != null) {
            Getter getter = getGetter(context, target, name);

            if (getter != NotFoundGetter) {
                result = getter.get(context, target, name);
            } else {
                try {
                    result = OgnlRuntime.getFieldValue(context, target, name, true);
                } catch (Exception ex) {
                    throw new OgnlException(name, ex);
                }
            }
        } else {
            result = super.getPossibleProperty(context, target, name);
        }
        return result;
    }
}
