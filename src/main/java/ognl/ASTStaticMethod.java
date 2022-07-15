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

import ognl.OgnlParser;
import ognl.enhance.ExpressionCompiler;
import ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Method;
import java.util.Objects;

public class ASTStaticMethod extends SimpleNode implements NodeType {

    private static final long serialVersionUID = -116222026971367049L;

    private String className;
    private String methodName;
    private Class<?> getterClass;

    public ASTStaticMethod(int id) {
        super(id);
    }

    public ASTStaticMethod(OgnlParser p, int id) {
        super(p, id);
    }

    /**
     * Called from parser action.
     */
    void init(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException {
        Object[] args = new Object[jjtGetNumChildren()];
        Object root = context.getRoot();

        for (int i = 0, icount = args.length; i < icount; ++i) {
            args[i] = children[i].getValue(context, root);
        }

        return OgnlRuntime.callStaticMethod(context, className, methodName, args);
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return getterClass;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("@" + className + "@" + methodName);

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
        return result.toString();
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        StringBuilder result = new StringBuilder(className + "#" + methodName + "(");

        try {
            Class<?> clazz = OgnlRuntime.classForName(context, className);
            Method m = OgnlRuntime.getMethod(context, clazz, methodName, children, true);

            if (m == null) {
                throw new UnsupportedCompilationException("Unable to find class/method combo " + className + " / " + methodName);
            }

            if (!context.getMemberAccess().isAccessible(context, clazz, m, methodName)) {
                throw new UnsupportedCompilationException("Method is not accessible, check your jvm runtime security settings. " +
                        "For static class method " + className + " / " + methodName);
            }

            if ((children != null) && (children.length > 0)) {
                Class<?>[] parms = m.getParameterTypes();

                for (int i = 0; i < children.length; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }

                    Class<?> prevType = context.getCurrentType();

                    Object value = children[i].getValue(context, context.getRoot());
                    String parmString = children[i].toGetSourceString(context, context.getRoot());

                    if (parmString == null || parmString.trim().length() < 1)
                        parmString = "null";

                    // to undo type setting of constants when used as method parameters
                    if (children[i] instanceof ASTConst) {
                        context.setCurrentType(prevType);
                    }

                    parmString = ExpressionCompiler.getRootExpression(children[i], context.getRoot(), context) + parmString;

                    String cast = "";
                    if (ExpressionCompiler.shouldCast(children[i])) {
                        cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                    }

                    if (cast == null)
                        cast = "";

                    if (!(children[i] instanceof ASTConst))
                        parmString = cast + parmString;

                    Class<?> valueClass = value != null ? value.getClass() : null;
                    if (NodeType.class.isAssignableFrom(children[i].getClass()))
                        valueClass = ((NodeType) children[i]).getGetterClass();

                    if (valueClass != parms[i]) {
                        if (parms[i].isArray()) {
                            parmString = OgnlRuntime.getCompiler()
                                    .createLocalReference(context,
                                            "(" + ExpressionCompiler.getCastString(parms[i])
                                                    + ")ognl.OgnlOps.toArray(" + parmString + ", " + parms[i].getComponentType().getName()
                                                    + ".class, true)",
                                            parms[i]
                                    );

                        } else if (parms[i].isPrimitive()) {
                            Class<?> wrapClass = OgnlRuntime.getPrimitiveWrapperClass(parms[i]);

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "((" + wrapClass.getName()
                                            + ")ognl.OgnlOps.convertValue(" + parmString + ","
                                            + wrapClass.getName() + ".class, true))."
                                            + OgnlRuntime.getNumericValueGetter(wrapClass),
                                    parms[i]
                            );

                        } else if (parms[i] != Object.class) {
                            parmString = OgnlRuntime.getCompiler()
                                    .createLocalReference(context,
                                            "(" + parms[i].getName() + ")ognl.OgnlOps.convertValue(" + parmString + "," + parms[i].getName() + ".class)",
                                            parms[i]
                                    );
                        } else if ((children[i] instanceof NodeType
                                && ((NodeType) children[i]).getGetterClass() != null
                                && Number.class.isAssignableFrom(((NodeType) children[i]).getGetterClass()))
                                || Objects.requireNonNull(valueClass).isPrimitive()) {
                            parmString = " ($w) " + parmString;
                        }
                    }

                    result.append(parmString);
                }
            }

            result.append(")");

            try {
                Object contextObj = getValueBody(context, target);
                context.setCurrentObject(contextObj);
            } catch (Throwable t) {
                // ignore
            }

            getterClass = m.getReturnType();

            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        return toGetSourceString(context, target);
    }
}
