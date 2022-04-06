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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ASTStaticField extends SimpleNode implements NodeType {

    private static final long serialVersionUID = -6421261547066021884L;

    private String className;
    private String fieldName;
    private Class<?> getterClass;

    public ASTStaticField(int id) {
        super(id);
    }

    public ASTStaticField(OgnlParser p, int id) {
        super(p, id);
    }

    /**
     * Called from parser action.
     */
    void init(String className, String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        return OgnlRuntime.getStaticField(context, className, fieldName);
    }

    public boolean isNodeConstant(OgnlContext context)
            throws OgnlException {
        boolean result = false;
        Exception reason = null;

        try {
            Class<?> c = OgnlRuntime.classForName(context, className);

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static
             * fields because it is a reserved word. It is considered constant.
             */
            if (fieldName.equals("class")) {
                result = true;
            } else if (c.isEnum()) {
                result = true;
            } else {
                Field f = OgnlRuntime.getField(c, fieldName);
                if (f == null) {
                    throw new NoSuchFieldException(fieldName);
                }

                if (!Modifier.isStatic(f.getModifiers()))
                    throw new OgnlException("Field " + fieldName + " of class " + className + " is not static");

                result = Modifier.isFinal(f.getModifiers());
            }
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
            reason = e;
        }

        if (reason != null)
            throw new OgnlException("Could not get static field " + fieldName
                    + " from class " + className, reason);

        return result;
    }

    private Class<?> getFieldClass(OgnlContext context) throws OgnlException {
        Exception reason;
        try {
            Class<?> c = OgnlRuntime.classForName(context, className);

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static
             * fields because it is a reserved word. It is considered constant.
             */
            if (fieldName.equals("class")) {
                return c;
            } else if (c.isEnum()) {
                return c;
            } else {
                Field f = c.getField(fieldName);

                return f.getType();
            }
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
            reason = e;
        }

        throw new OgnlException("Could not get static field " + fieldName + " from class " + className, reason);
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return getterClass;
    }

    public String toString() {
        return "@" + className + "@" + fieldName;
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        try {
            Object obj = OgnlRuntime.getStaticField(context, className, fieldName);
            context.setCurrentObject(obj);
            getterClass = getFieldClass(context);
            context.setCurrentType(getterClass);
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
        return className + "." + fieldName;
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        try {
            Object obj = OgnlRuntime.getStaticField(context, className, fieldName);
            context.setCurrentObject(obj);
            getterClass = getFieldClass(context);
            context.setCurrentType(getterClass);
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return className + "." + fieldName;
    }
}
