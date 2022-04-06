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

import org.ognl.enhance.ExpressionCompiler;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.List;

public class ASTCtor extends SimpleNode {

    private static final long serialVersionUID = -218799350410195779L;

    private String className;
    private boolean isArray;

    public ASTCtor(int id) {
        super(id);
    }

    public ASTCtor(OgnlParser p, int id) {
        super(p, id);
    }

    /**
     * Called from parser action.
     */
    void setClassName(String className) {
        this.className = className;
    }

    Class<?> getCreatedClass(OgnlContext context) throws ClassNotFoundException {
        return OgnlRuntime.classForName(context, className);
    }

    void setArray(boolean value) {
        isArray = value;
    }

    public boolean isArray() {
        return isArray;
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException {
        Object result, root = context.getRoot();
        int count = jjtGetNumChildren();
        Object[] args = new Object[count];

        for (int i = 0; i < count; ++i) {
            args[i] = children[i].getValue(context, root);
        }
        if (isArray) {
            if (args.length == 1) {
                try {
                    Class<?> componentClass = OgnlRuntime.classForName(context, className);
                    List<?> sourceList = null;
                    int size;

                    if (args[0] instanceof List) {
                        sourceList = (List<?>) args[0];
                        size = sourceList.size();
                    } else {
                        size = (int) OgnlOps.longValue(args[0]);
                    }
                    result = Array.newInstance(componentClass, size);
                    if (sourceList != null) {
                        TypeConverter converter = context.getTypeConverter();

                        for (int i = 0, icount = sourceList.size(); i < icount; i++) {
                            Object o = sourceList.get(i);

                            if ((o == null) || componentClass.isInstance(o)) {
                                Array.set(result, i, o);
                            } else {
                                Array.set(result, i, converter.convertValue(context, null, null, null, o,
                                        componentClass));
                            }
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    throw new OgnlException("array component class '" + className + "' not found", ex);
                }
            } else {
                throw new OgnlException("only expect array size or fixed initializer list");
            }
        } else {
            result = OgnlRuntime.callConstructor(context, className, args);
        }

        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("new " + className);

        if (isArray) {
            if (children[0] instanceof ASTConst) {
                result.append("[").append(children[0]).append("]");
            } else {
                result.append("[] ").append(children[0]);
            }
        } else {
            result.append("(");
            if ((children != null) && (children.length > 0)) {
                for (int i = 0; i < children.length; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }
                    result.append(children[i]);
                }
            }
            result.append(")");
        }
        return result.toString();
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        StringBuilder result = new StringBuilder("new " + className);

        Class<?> clazz;
        Object ctorValue;
        try {

            clazz = OgnlRuntime.classForName(context, className);

            ctorValue = this.getValueBody(context, target);
            context.setCurrentObject(ctorValue);

            if (ctorValue != null) {

                context.setCurrentType(ctorValue.getClass());
                context.setCurrentAccessor(ctorValue.getClass());
            }

            if (isArray)
                context.put("_ctorClass", clazz);

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        try {

            if (isArray) {
                if (children[0] instanceof ASTConst) {

                    result.append("[").append(children[0].toGetSourceString(context, target)).append("]");
                } else if (children[0] instanceof ASTProperty) {

                    result.append("[").append(ExpressionCompiler.getRootExpression(children[0], target, context)).append(children[0].toGetSourceString(context, target)).append("]");
                } else if (children[0] instanceof ASTChain) {

                    result.append("[").append(children[0].toGetSourceString(context, target)).append("]");
                } else {

                    result.append("[] ").append(children[0].toGetSourceString(context, target));
                }

            } else {
                result.append("(");

                if ((children != null) && (children.length > 0)) {

                    Object[] values = new Object[children.length];
                    String[] expressions = new String[children.length];
                    Class<?>[] types = new Class[children.length];

                    // first populate arrays with child values

                    for (int i = 0; i < children.length; i++) {

                        Object objValue = children[i].getValue(context, context.getRoot());
                        String value = children[i].toGetSourceString(context, target);

                        if (!(children[i] instanceof ASTRootVarRef)) {
                            value = ExpressionCompiler.getRootExpression(children[i], target, context) + value;
                        }

                        String cast = "";
                        if (ExpressionCompiler.shouldCast(children[i])) {

                            cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                        }
                        if (cast == null)
                            cast = "";

                        if (!(children[i] instanceof ASTConst))
                            value = cast + value;

                        values[i] = objValue;
                        expressions[i] = value;
                        types[i] = context.getCurrentType();
                    }

                    // now try and find a matching constructor

                    Constructor<?>[] cons = clazz.getConstructors();
                    Constructor<?> ctor = null;
                    Class<?>[] ctorParamTypes = null;

                    for (Constructor<?> con : cons) {
                        Class<?>[] ctorTypes = con.getParameterTypes();

                        if (OgnlRuntime.areArgsCompatible(values, ctorTypes)
                                && (ctor == null || OgnlRuntime.isMoreSpecific(ctorTypes, ctorParamTypes))) {
                            ctor = con;
                            ctorParamTypes = ctorTypes;
                        }
                    }

                    if (ctor == null)
                        ctor = OgnlRuntime.getConvertedConstructorAndArgs(context, clazz, OgnlRuntime.getConstructors(clazz), values, new Object[values.length]);

                    if (ctor == null)
                        throw new NoSuchMethodException("Unable to find constructor appropriate for arguments in class: " + clazz);

                    ctorParamTypes = ctor.getParameterTypes();

                    // now loop over child values again and build up the actual source string

                    for (int i = 0; i < children.length; i++) {
                        if (i > 0) {
                            result.append(", ");
                        }

                        String value = expressions[i];

                        if (types[i].isPrimitive()) {

                            String literal = OgnlRuntime.getNumericLiteral(types[i]);
                            if (literal != null)
                                value += literal;
                        }

                        if (ctorParamTypes[i] != types[i]) {

                            if (values[i] != null && !types[i].isPrimitive()
                                    && !values[i].getClass().isArray() && !(children[i] instanceof ASTConst)) {

                                value = "(" + OgnlRuntime.getCompiler().getInterfaceClass(values[i].getClass()).getName() + ")" + value;
                            } else if (!(children[i] instanceof ASTConst)
                                    || (children[i] instanceof ASTConst && !types[i].isPrimitive())) {

                                if (!types[i].isArray()
                                        && types[i].isPrimitive() && !ctorParamTypes[i].isPrimitive())
                                    value = "new " + ExpressionCompiler.getCastString(OgnlRuntime.getPrimitiveWrapperClass(types[i])) + "(" + value + ")";
                                else
                                    value = " ($w) " + value;
                            }
                        }

                        result.append(value);
                    }

                }
                result.append(")");
            }

            context.setCurrentType(ctorValue != null ? ctorValue.getClass() : clazz);
            context.setCurrentAccessor(clazz);
            context.setCurrentObject(ctorValue);

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        context.remove("_ctorClass");

        return result.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        return "";
    }
}
