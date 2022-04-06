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
package org.ognl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as properties to
 * index into Lists.
 */
public class ListPropertyAccessor extends ObjectPropertyAccessor implements PropertyAccessor {

    public Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException {
        List<?> list = (List<?>) target;

        if (name instanceof String) {
            Object result;

            if (name.equals("size")) {
                result = list.size();
            } else {
                if (name.equals("iterator")) {
                    result = list.iterator();
                } else {
                    if (name.equals("isEmpty") || name.equals("empty")) {
                        result = list.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        result = super.getProperty(context, target, name);
                    }
                }
            }

            return result;
        }

        if (name instanceof Number)
            return list.get(((Number) name).intValue());

        if (name instanceof DynamicSubscript) {
            int len = list.size();
            switch (((DynamicSubscript) name).getFlag()) {
                case DynamicSubscript.FIRST:
                    return len > 0 ? list.get(0) : null;
                case DynamicSubscript.MID:
                    return len > 0 ? list.get(len / 2) : null;
                case DynamicSubscript.LAST:
                    return len > 0 ? list.get(len - 1) : null;
                case DynamicSubscript.ALL:
                    return new ArrayList<>(list);
            }
        }

        throw new NoSuchPropertyException(target, name);
    }

    public void setProperty(OgnlContext context, Object target, Object name, Object value)
            throws OgnlException {
        if (name instanceof String && !((String) name).contains("$")) {
            super.setProperty(context, target, name, value);
            return;
        }

        List<Object> list = (List<Object>) target;

        if (name instanceof Number) {
            list.set(((Number) name).intValue(), value);
            return;
        }

        if (name instanceof DynamicSubscript) {
            int len = list.size();
            switch (((DynamicSubscript) name).getFlag()) {
                case DynamicSubscript.FIRST:
                    if (len > 0) list.set(0, value);
                    return;
                case DynamicSubscript.MID:
                    if (len > 0) list.set(len / 2, value);
                    return;
                case DynamicSubscript.LAST:
                    if (len > 0) list.set(len - 1, value);
                    return;
                case DynamicSubscript.ALL: {
                    if (!(value instanceof Collection)) throw new OgnlException("Value must be a collection");
                    list.clear();
                    list.addAll((Collection<?>) value);
                    return;
                }
            }
        }

        throw new NoSuchPropertyException(target, name);
    }

    public Class<?> getPropertyClass(OgnlContext context, Object target, Object index) {
        if (index instanceof String) {
            String indexStr = (String) index;
            String key = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);
            if (key.equals("size")) {
                return int.class;
            } else {
                if (key.equals("iterator")) {
                    return Iterator.class;
                } else {
                    if (key.equals("isEmpty") || key.equals("empty")) {
                        return boolean.class;
                    } else {
                        return super.getPropertyClass(context, target, index);
                    }
                }
            }
        }

        if (index instanceof Number)
            return Object.class;

        return null;
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object index) {
        String indexStr = index.toString();
        if (indexStr.indexOf('"') >= 0)
            indexStr = indexStr.replaceAll("\"", "");

        if (index instanceof String) {
            if (indexStr.equals("size")) {
                context.setCurrentAccessor(List.class);
                context.setCurrentType(int.class);
                return ".size()";
            } else {
                if (indexStr.equals("iterator")) {
                    context.setCurrentAccessor(List.class);
                    context.setCurrentType(Iterator.class);
                    return ".iterator()";
                } else {
                    if (indexStr.equals("isEmpty") || indexStr.equals("empty")) {
                        context.setCurrentAccessor(List.class);
                        context.setCurrentType(boolean.class);
                        return ".isEmpty()";
                    }
                }
            }
        }

        // TODO: This feels really inefficient, must be some better way
        // check if the index string represents a method on a custom class implementing java.util.List instead..
        if (context.getCurrentObject() != null && !(context.getCurrentObject() instanceof Number)) {
            try {
                Method m = OgnlRuntime.getReadMethod(target.getClass(), indexStr);
                if (m != null) {
                    return super.getSourceAccessor(context, target, index);
                }
            } catch (Throwable t) {
                throw OgnlOps.castToRuntime(t);
            }
        }

        context.setCurrentAccessor(List.class);

        // need to convert to primitive for list index access
        // System.out.println("Curent type: " + context.getCurrentType() + " current object type " + context.getCurrentObject().getClass());

        if (!context.getCurrentType().isPrimitive() && Number.class.isAssignableFrom(context.getCurrentType())) {
            indexStr += "." + OgnlRuntime.getNumericValueGetter(context.getCurrentType());
        } else if (context.getCurrentObject() != null && Number.class.isAssignableFrom(context.getCurrentObject().getClass())
                && !context.getCurrentType().isPrimitive()) {
            // means it needs to be cast first as well

            String toString = index instanceof String && context.getCurrentType() != Object.class ? "" : ".toString()";

            indexStr = "org.ognl.OgnlOps#getIntValue(" + indexStr + toString + ")";
        }

        context.setCurrentType(Object.class);

        return ".get(" + indexStr + ")";
    }

    public String getSourceSetter(OgnlContext context, Object target, Object index) {
        String indexStr = index.toString();
        if (indexStr.indexOf('"') >= 0)
            indexStr = indexStr.replaceAll("\"", "");

        // TODO: This feels really inefficient, must be some better way
        // check if the index string represents a method on a custom class implementing java.util.List instead..
       /* System.out.println("Listpropertyaccessor setter using index: " + index + " and current object: " + context.getCurrentObject()
        + " number is current object? " + Number.class.isInstance(context.getCurrentObject()));*/

        if (context.getCurrentObject() != null && !(context.getCurrentObject() instanceof Number)) {
            try {
                Method m = OgnlRuntime.getWriteMethod(target.getClass(), indexStr);

                if (m != null || !context.getCurrentType().isPrimitive()) {
                    // System.out.println("super source setter returned: " + super.getSourceSetter(context, target, index));
                    return super.getSourceSetter(context, target, index);
                }

            } catch (Throwable t) {
                throw OgnlOps.castToRuntime(t);
            }
        }

        context.setCurrentAccessor(List.class);

        // need to convert to primitive for list index access

        if (!context.getCurrentType().isPrimitive() && Number.class.isAssignableFrom(context.getCurrentType())) {
            indexStr += "." + OgnlRuntime.getNumericValueGetter(context.getCurrentType());
        } else if (context.getCurrentObject() != null && Number.class.isAssignableFrom(context.getCurrentObject().getClass()) && !context.getCurrentType().isPrimitive()) {
            // means it needs to be cast first as well
            String toString = index instanceof String && context.getCurrentType() != Object.class ? "" : ".toString()";
            indexStr = "org.ognl.OgnlOps#getIntValue(" + indexStr + toString + ")";
        }
        context.setCurrentType(Object.class);

        return ".set(" + indexStr + ", $3)";
    }
}
