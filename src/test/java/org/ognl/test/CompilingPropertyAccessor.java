//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.ognl.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import ognl.ObjectPropertyAccessor;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import org.ognl.test.util.ContextClassLoader;
import org.ognl.test.util.EnhancedClassLoader;
import org.ognl.test.util.NameFactory;

/**
 * Implementation of PropertyAccessor that uses Javassist to compile
 * a property accessor specifically tailored to the property.
 */
public class CompilingPropertyAccessor extends ObjectPropertyAccessor
{
    private static NameFactory                  NAME_FACTORY = new NameFactory("ognl.PropertyAccessor", "v");
    private static Getter                       NotFoundGetter = new Getter() { public Object get(OgnlContext context, Object target, String propertyName) { return null; } };
    private static Getter                       DefaultGetter = new Getter() {
                                                    public Object get(OgnlContext context, Object target, String propertyName)
                                                    {
                                                        try {
                                                            return OgnlRuntime.getMethodValue(context, target, propertyName, true);
                                                        } catch (Exception ex) {
                                                            throw new RuntimeException(ex);
                                                        }
                                                    }
                                                };
    private static Map                          pools = new HashMap();
    private static Map                          loaders = new HashMap();

    private static java.util.IdentityHashMap    PRIMITIVE_WRAPPER_CLASSES = new IdentityHashMap();
    private java.util.IdentityHashMap           seenGetMethods = new java.util.IdentityHashMap();

    static
    {
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

    public static Class getPrimitiveWrapperClass(Class primitiveClass)
    {
        return (Class)PRIMITIVE_WRAPPER_CLASSES.get(primitiveClass);
    }

    public interface Getter
    {
        public Object get(OgnlContext context, Object target, String propertyName);
    }

    public static Getter generateGetter(OgnlContext context, String code) throws OgnlException
    {
        String                  className = NAME_FACTORY.getNewClassName();

        try
        {
            ClassPool               pool = (ClassPool)pools.get(context.getClassResolver());
            EnhancedClassLoader     loader = (EnhancedClassLoader)loaders.get(context.getClassResolver());
            CtClass                 newClass;
            CtClass                 ognlContextClass;
            CtClass                 objectClass;
            CtClass                 stringClass;
            CtMethod                method;
            byte[]                  byteCode;
            Class                   compiledClass;

            if ((pool == null) || (loader == null)) {
                ClassLoader     classLoader = new ContextClassLoader(OgnlContext.class.getClassLoader(), context);

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
                method = new CtMethod(objectClass, "get", new CtClass[] { ognlContextClass, objectClass, stringClass }, newClass);
                method.setBody("{" + code + "}");
            newClass.addMethod(method);
            byteCode = newClass.toBytecode();
            compiledClass = loader.defineClass(className, byteCode);
            return (Getter)compiledClass.newInstance();
        } catch (Throwable ex) {
            throw new OgnlException("Cannot create class", ex);
        }
    }

    private Getter getGetter(OgnlContext context, Object target, String propertyName) throws OgnlException
    {
        Getter      result;
        Class       targetClass = target.getClass();
        Map         propertyMap;

        if ((propertyMap = (Map)seenGetMethods.get(targetClass)) == null) {
            propertyMap = new HashMap(101);
            seenGetMethods.put(targetClass, propertyMap);
        }
        if ((result = (Getter)propertyMap.get(propertyName)) == null) {
            try {
                Method      method = OgnlRuntime.getGetMethod(context, targetClass, propertyName);

                if (method != null) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        if (method.getReturnType().isPrimitive()) {
                            propertyMap.put(propertyName, result = generateGetter(context,
                                                            "java.lang.Object\t\tresult;\n" +
                                                            targetClass.getName() + "\t" + "t0 = (" + targetClass.getName() + ")$2;\n" +
                                                            "\n" +
                                                            "try {\n" +
                                                            "   result = new " + getPrimitiveWrapperClass(method.getReturnType()).getName() + "(t0." + method.getName() + "());\n" +
                                                            "} catch (java.lang.Exception ex) {\n" +
                                                            "    throw new java.lang.RuntimeException(ex);\n" +
                                                            "}\n" +
                                                            "return result;"
                                                        ));
                        } else {
                            propertyMap.put(propertyName, result = generateGetter(context,
                                                            "java.lang.Object\t\tresult;\n" +
                                                            targetClass.getName() + "\t" + "t0 = (" + targetClass.getName() + ")$2;\n" +
                                                            "\n" +
                                                            "try {\n" +
                                                            "   result = t0." + method.getName() + "();\n" +
                                                            "} catch (java.lang.Exception ex) {\n" +
                                                            "    throw new java.lang.RuntimeException(ex);\n" +
                                                            "}\n" +
                                                            "return result;"
                                                        ));
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
        Returns OgnlRuntime.NotFound if the property does not exist.
     */
    public Object getPossibleProperty( Map context, Object target, String name) throws OgnlException
    {
        Object          result;
        OgnlContext     ognlContext = (OgnlContext)context;

        if (context.get("_compile") != null) {
            Getter        getter = getGetter(ognlContext, target, name);

            if (getter != NotFoundGetter) {
                result = getter.get(ognlContext, target, name);
            } else {
                try {
                    result = OgnlRuntime.getFieldValue(ognlContext, target, name, true);
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
