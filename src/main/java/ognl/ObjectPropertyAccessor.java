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

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.UnsupportedCompilationException;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Implementation of PropertyAccessor that uses reflection on the target object's class to find a
 * field or a pair of set/get methods with the given property name.
 */
public class ObjectPropertyAccessor implements PropertyAccessor {

    /**
     * Ignore detecting and invoking read method when get property value.
     */
    private final boolean ignoreReadMethod;


    public ObjectPropertyAccessor() {
        this(false);
    }

    public ObjectPropertyAccessor(boolean ignoreReadMethod) {
        this.ignoreReadMethod = ignoreReadMethod;
    }

    /**
     * Returns OgnlRuntime.NotFound if the property does not exist.
     *
     * @param context the current execution context.
     * @param target  the object to get the property from.
     * @param name    the name of the property to get.
     * @return the current value of the given property in the given object.
     * @throws OgnlException if there is an error locating the property in the given object.
     */
    public Object getPossibleProperty(OgnlContext context, Object target, String name) throws OgnlException {
        Object result;

        try {
            if ((result = OgnlRuntime.getMethodValue(context, target, name, true, ignoreReadMethod)) == OgnlRuntime.NotFound) {
                result = OgnlRuntime.getFieldValue(context, target, name, true);
            }
        } catch (OgnlException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OgnlException(name, ex);
        }

        return result;
    }

    /**
     * Returns OgnlRuntime.NotFound if the property does not exist.
     *
     * @param context the current execution context.
     * @param target  the object to set the property in.
     * @param name    the name of the property to set.
     * @param value   the new value for the property.
     * @return the Object result of the property set operation.
     * @throws OgnlException if there is an error setting the property in the given object.
     */
    public Object setPossibleProperty(OgnlContext context, Object target, String name, Object value)
            throws OgnlException {
        Object result = null;
        try {
            if (!OgnlRuntime.setMethodValue(context, target, name, value, true)) {
                result = OgnlRuntime.setFieldValue(context, target, name, value, true) ? null : OgnlRuntime.NotFound;
            }

            if (result == OgnlRuntime.NotFound) {
                Method m = OgnlRuntime.getWriteMethod(target.getClass(), name);
                if (m != null && context.getMemberAccess().isAccessible(context, target, m, name)) {
                    result = m.invoke(target, value);
                }
            }
        } catch (OgnlException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OgnlException(name, ex);
        }

        return result;
    }

    public boolean hasGetProperty(OgnlContext context, Object target, Object oname) throws OgnlException {
        try {
            return OgnlRuntime.hasGetProperty(context, target, oname);
        } catch (IntrospectionException ex) {
            throw new OgnlException("checking if " + target + " has gettable property " + oname, ex);
        }
    }

    public boolean hasSetProperty(OgnlContext context, Object target, Object oname) throws OgnlException {
        try {
            return OgnlRuntime.hasSetProperty(context, target, oname);
        } catch (IntrospectionException ex) {
            throw new OgnlException("checking if " + target + " has settable property " + oname, ex);
        }
    }

    public Object getProperty(OgnlContext context, Object target, Object oname) throws OgnlException {
        String name = oname.toString();
        Object result = getPossibleProperty(context, target, name);

        if (result == OgnlRuntime.NotFound) {
            throw new NoSuchPropertyException(target, name);
        }

        return result;
    }

    public void setProperty(OgnlContext context, Object target, Object oname, Object value) throws OgnlException {
        String name = oname.toString();
        Object result = setPossibleProperty(context, target, name, value);
        if (result == OgnlRuntime.NotFound) {
            throw new NoSuchPropertyException(target, name);
        }
    }

    public Class<?> getPropertyClass(OgnlContext context, Object target, Object index) {
        try {
            Method m = OgnlRuntime.getReadMethod(target.getClass(), index.toString());
            if (m == null) {
                if (String.class.isAssignableFrom(index.getClass()) && !target.getClass().isArray()) {
                    String indexStr = (String) index;
                    String key = (indexStr.indexOf('"') >= 0) ? indexStr.replaceAll("\"", "") : indexStr;
                    try {
                        Field f = target.getClass().getField(key);
                        return f.getType();
                    } catch (NoSuchFieldException e) {
                        return null;
                    }
                }
                return null;
            }
            return m.getReturnType();
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object index) {
        try {

            String indexStr = index.toString();
            String methodName = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);
            Method m = OgnlRuntime.getReadMethod(target.getClass(), methodName);

            // try last ditch effort of checking if they were trying to do reflection via a return method value
            if (m == null && context.getCurrentObject() != null) {
                String currentObjectStr = context.getCurrentObject().toString();
                m = OgnlRuntime.getReadMethod(target.getClass(), (currentObjectStr.indexOf('"') >= 0 ? currentObjectStr.replaceAll("\"", "") : currentObjectStr));
            }

            if (m == null) {
                try {
                    if (String.class.isAssignableFrom(index.getClass()) && !target.getClass().isArray()) {
                        Field f = target.getClass().getField(methodName);

                        context.setCurrentType(f.getType());
                        context.setCurrentAccessor(f.getDeclaringClass());

                        return "." + f.getName();
                    }
                } catch (NoSuchFieldException e) {
                    // ignore
                }

                return "";
            }

            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

            return "." + m.getName() + "()";

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }

    public String getSourceSetter(OgnlContext context, Object target, Object index) {
        try {

            String indexStr = index.toString();
            String methodName = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);
            Method m = OgnlRuntime.getWriteMethod(target.getClass(), methodName);

            if (m == null && context.getCurrentObject() != null
                    && context.getCurrentObject().toString() != null) {
                String currentObjectStr = context.getCurrentObject().toString();
                m = OgnlRuntime.getWriteMethod(target.getClass(), (currentObjectStr.indexOf('"') >= 0 ? currentObjectStr.replaceAll("\"", "") : currentObjectStr));
            }

            if (m == null || m.getParameterTypes().length <= 0) {
                throw new UnsupportedCompilationException("Unable to determine setting expression on " + context.getCurrentObject()
                        + " with index of " + index);
            }

            Class<?> param = m.getParameterTypes()[0];
            String conversion;

            if (m.getParameterTypes().length > 1)
                throw new UnsupportedCompilationException("Object property accessors can only support single parameter setters.");


            if (param.isPrimitive()) {
                Class<?> wrapClass = OgnlRuntime.getPrimitiveWrapperClass(param);
                conversion = OgnlRuntime.getCompiler().createLocalReference(context,
                        "((" + wrapClass.getName() + ")ognl.OgnlOps#convertValue($3," + wrapClass.getName()
                                + ".class, true))." + OgnlRuntime.getNumericValueGetter(wrapClass),
                        param);

            } else if (param.isArray()) {
                conversion = OgnlRuntime.getCompiler().createLocalReference(context,
                        "(" + ExpressionCompiler.getCastString(param) + ")ognl.OgnlOps#toArray($3,"
                                + param.getComponentType().getName() + ".class)",
                        param);

            } else {
                conversion = OgnlRuntime.getCompiler().createLocalReference(context,
                        "(" + param.getName() + ")ognl.OgnlOps#convertValue($3,"
                                + param.getName()
                                + ".class)",
                        param);
            }

            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

            return "." + m.getName() + "(" + conversion + ")";

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }
}
