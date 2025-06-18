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

import java.lang.reflect.Array;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as properties to
 * index into Java arrays.
 */
public class ArrayPropertyAccessor extends ObjectPropertyAccessor implements PropertyAccessor {

    public Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException {
        Object result = null;

        if (name instanceof String) {
            if (name.equals("length")) {
                result = Array.getLength(target);
            } else {
                result = super.getProperty(context, target, name);
            }
        } else {
            Object index = name;

            if (index instanceof DynamicSubscript) {
                int len = Array.getLength(target);

                switch (((DynamicSubscript) index).getFlag()) {
                    case DynamicSubscript.ALL:
                        result = Array.newInstance(target.getClass().getComponentType(), len);
                        System.arraycopy(target, 0, result, 0, len);
                        break;
                    case DynamicSubscript.FIRST:
                        index = (len > 0) ? 0 : -1;
                        break;
                    case DynamicSubscript.MID:
                        index = (len > 0) ? (len / 2) : -1;
                        break;
                    case DynamicSubscript.LAST:
                        index = (len > 0) ? (len - 1) : -1;
                        break;
                }
            }
            if (result == null) {
                if (index instanceof Number) {
                    int i = ((Number) index).intValue();

                    result = (i >= 0) ? Array.get(target, i) : null;
                } else {
                    throw new NoSuchPropertyException(target, index);
                }
            }
        }
        return result;
    }

    public void setProperty(OgnlContext context, Object target, Object name, Object value) throws OgnlException {
        Object index = name;
        boolean isNumber = (index instanceof Number);

        if (isNumber || (index instanceof DynamicSubscript)) {
            TypeConverter converter = context.getTypeConverter();
            Object convertedValue = converter.convertValue(context, target, null, name.toString(), value, target.getClass().getComponentType());
            if (isNumber) {
                int i = ((Number) index).intValue();

                if (i >= 0) {
                    Array.set(target, i, convertedValue);
                }
            } else {
                int len = Array.getLength(target);

                switch (((DynamicSubscript) index).getFlag()) {
                    case DynamicSubscript.ALL:
                        System.arraycopy(target, 0, convertedValue, 0, len);
                        return;
                    case DynamicSubscript.FIRST:
                        index = (len > 0) ? 0 : -1;
                        break;
                    case DynamicSubscript.MID:
                        index = (len > 0) ? (len / 2) : -1;
                        break;
                    case DynamicSubscript.LAST:
                        index = (len > 0) ? (len - 1) : -1;
                        break;
                }
            }
        } else {
            if (name instanceof String) {
                super.setProperty(context, target, name, value);
            } else {
                throw new NoSuchPropertyException(target, index);
            }
        }
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object index) {
        String indexStr = index.toString();

        // need to convert to primitive for list index access

        // System.out.println("index class " + index.getClass() + " current type " + context.getCurrentType() + " current object class " + context.getCurrentObject().getClass());

        if (context.getCurrentType() != null && !context.getCurrentType().isPrimitive()
                && Number.class.isAssignableFrom(context.getCurrentType())) {
            indexStr += "." + OgnlRuntime.getNumericValueGetter(context.getCurrentType());
        } else if (context.getCurrentObject() != null && Number.class.isAssignableFrom(context.getCurrentObject().getClass())
                && !context.getCurrentType().isPrimitive()) {
            // means it needs to be cast first as well
            String toString = index instanceof String && context.getCurrentType() != Object.class ? "" : ".toString()";
            indexStr = "ognl.OgnlOps#getIntValue(" + indexStr + toString + ")";
        }

        context.setCurrentAccessor(target.getClass());
        context.setCurrentType(target.getClass().getComponentType());

        return "[" + indexStr + "]";
    }

    public String getSourceSetter(OgnlContext context, Object target, Object index) {
        String indexStr = index.toString();

        // need to convert to primitive for list index access

        if (context.getCurrentType() != null && !context.getCurrentType().isPrimitive()
                && Number.class.isAssignableFrom(context.getCurrentType())) {
            indexStr += "." + OgnlRuntime.getNumericValueGetter(context.getCurrentType());
        } else if (context.getCurrentObject() != null && Number.class.isAssignableFrom(context.getCurrentObject().getClass())
                && !context.getCurrentType().isPrimitive()) {
            // means it needs to be cast first as well
            String toString = index instanceof String && context.getCurrentType() != Object.class ? "" : ".toString()";
            indexStr = "ognl.OgnlOps#getIntValue(" + indexStr + toString + ")";
        }

        Class<?> type = target.getClass().isArray() ? target.getClass().getComponentType() : target.getClass();

        context.setCurrentAccessor(target.getClass());
        context.setCurrentType(target.getClass().getComponentType());

        if (type.isPrimitive()) {
            Class<?> wrapClass = OgnlRuntime.getPrimitiveWrapperClass(type);
            return "[" + indexStr + "]=((" + wrapClass.getName() + ")ognl.OgnlOps.convertValue($3," + wrapClass.getName()
                    + ".class, true))." + OgnlRuntime.getNumericValueGetter(wrapClass);
        } else {
            return "[" + indexStr + "]=ognl.OgnlOps.convertValue($3," + type.getName() + ".class)";
        }
    }
}
