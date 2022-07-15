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
import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Method;
import java.util.List;

public class ASTMethod extends SimpleNode implements OrderedReturn, NodeType {

    private static final long serialVersionUID = -6108508556131109533L;

    private String methodName;
    private String lastExpression;
    private String coreExpression;
    private Class<?> getterClass;

    public ASTMethod(int id) {
        super(id);
    }

    public ASTMethod(OgnlParser p, int id) {
        super(p, id);
    }

    /**
     * Called from parser action.
     *
     * @param methodName the method name.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the method name that this node will call.
     *
     * @return the method name.
     */
    public String getMethodName() {
        return methodName;
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object[] args = new Object[jjtGetNumChildren()];

        Object result, root = context.getRoot();

        for (int i = 0, icount = args.length; i < icount; ++i) {
            args[i] = children[i].getValue(context, root);
        }

        result = OgnlRuntime.callMethod(context, source, methodName, args);

        if (result == null) {
            NullHandler nh = OgnlRuntime.getNullHandler(OgnlRuntime.getTargetClass(source));
            result = nh.nullMethodResult(context, source, methodName, args);
        }

        return result;

    }

    public String getLastExpression() {
        return lastExpression;
    }

    public String getCoreExpression() {
        return coreExpression;
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return getterClass;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(methodName);

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
        /*  System.out.println("methodName is " + _methodName + " for target " + target + " target class: " + (target != null ? target.getClass() : null)
  + " current type: " + context.getCurrentType());*/
        if (target == null)
            throw new UnsupportedCompilationException("Target object is null.");

        String post = "";
        StringBuilder result;
        Method m;

        try {
            m = OgnlRuntime.getMethod(context, context.getCurrentType() != null ? context.getCurrentType() : target.getClass(), methodName, children, false);
            Class<?>[] argumentClasses = getChildrenClasses(context, children);
            if (m == null)
                m = OgnlRuntime.getReadMethod(target.getClass(), methodName, argumentClasses);

            if (m == null) {
                m = OgnlRuntime.getWriteMethod(target.getClass(), methodName, argumentClasses);

                if (m != null) {

                    context.setCurrentType(m.getReturnType());
                    context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

                    coreExpression = toSetSourceString(context, target);
                    if (coreExpression == null || coreExpression.length() < 1)
                        throw new UnsupportedCompilationException("can't find suitable getter method");

                    coreExpression += ";";
                    lastExpression = "null";

                    return coreExpression;
                }

                return "";
            } else {

                getterClass = m.getReturnType();
            }

            // TODO:  This is a hacky workaround until javassist supports varargs method invocations

            boolean varArgs = m.isVarArgs();

            if (varArgs) {
                throw new UnsupportedCompilationException("Javassist does not currently support varargs method calls");
            }

            result = new StringBuilder("." + m.getName() + "(");

            if ((children != null) && (children.length > 0)) {
                Class<?>[] parms = m.getParameterTypes();
                String prevCast = (String) context.remove(ExpressionCompiler.PRE_CAST);

                for (int i = 0; i < children.length; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }

                    Class<?> prevType = context.getCurrentType();

                    context.setCurrentObject(context.getRoot());
                    context.setCurrentType(context.getRoot() != null ? context.getRoot().getClass() : null);
                    context.setCurrentAccessor(null);
                    context.setPreviousType(null);

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

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "(" + ExpressionCompiler.getCastString(parms[i])
                                            + ")ognl.OgnlOps#toArray(" + parmString + ", " + parms[i].getComponentType().getName()
                                            + ".class, true)",
                                    parms[i]
                            );

                        } else if (parms[i].isPrimitive()) {

                            Class<?> wrapClass = OgnlRuntime.getPrimitiveWrapperClass(parms[i]);

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "((" + wrapClass.getName()
                                            + ")ognl.OgnlOps#convertValue(" + parmString + ","
                                            + wrapClass.getName() + ".class, true))."
                                            + OgnlRuntime.getNumericValueGetter(wrapClass),
                                    parms[i]
                            );

                        } else if (parms[i] != Object.class) {
                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "(" + parms[i].getName() + ")ognl.OgnlOps#convertValue(" + parmString + "," + parms[i].getName() + ".class)",
                                    parms[i]
                            );
                        } else if ((children[i] instanceof NodeType
                                && ((NodeType) children[i]).getGetterClass() != null
                                && Number.class.isAssignableFrom(((NodeType) children[i]).getGetterClass()))
                                || (valueClass != null && valueClass.isPrimitive())) {
                            parmString = " ($w) " + parmString;
                        }
                    }

                    result.append(parmString);
                }

                if (prevCast != null) {
                    context.put(ExpressionCompiler.PRE_CAST, prevCast);
                }
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        try {
            Object contextObj = getValueBody(context, target);
            context.setCurrentObject(contextObj);
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        result.append(")").append(post);

        if (m.getReturnType() == void.class) {
            coreExpression = result + ";";
            lastExpression = "null";
        }

        context.setCurrentType(m.getReturnType());
        context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

        return result.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        /*System.out.println("current type: " + context.getCurrentType() + " target:" + target + " " + context.getCurrentObject()
                           + " last child? " + lastChild(context));*/
        Method m = OgnlRuntime.getWriteMethod(context.getCurrentType() != null ?
                        context.getCurrentType() : target.getClass(),
                methodName, getChildrenClasses(context, children));
        if (m == null) {
            throw new UnsupportedCompilationException("Unable to determine setter method generation for " + methodName);
        }

        String post = "";
        StringBuilder result = new StringBuilder("." + m.getName() + "(");

        if (m.getReturnType() != void.class && m.getReturnType().isPrimitive() && (!(parent instanceof ASTTest))) {
            Class<?> wrapper = OgnlRuntime.getPrimitiveWrapperClass(m.getReturnType());

            ExpressionCompiler.addCastString(context, "new " + wrapper.getName() + "(");
            post = ")";
            getterClass = wrapper;
        }

        boolean varArgs = m.isVarArgs();

        if (varArgs) {
            throw new UnsupportedCompilationException("Javassist does not currently support varargs method calls");
        }

        try {
            /* if (lastChild(context) && m.getParameterTypes().length > 0 && _children.length <= 0)
                throw new UnsupportedCompilationException("Unable to determine setter method generation for " + m); */

            if ((children != null) && (children.length > 0)) {
                Class<?>[] parms = m.getParameterTypes();
                String prevCast = (String) context.remove(ExpressionCompiler.PRE_CAST);

                for (int i = 0; i < children.length; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }

                    Class<?> prevType = context.getCurrentType();

                    context.setCurrentObject(context.getRoot());
                    context.setCurrentType(context.getRoot() != null ? context.getRoot().getClass() : null);
                    context.setCurrentAccessor(null);
                    context.setPreviousType(null);

                    Object value = children[i].getValue(context, context.getRoot());
                    String parmString = children[i].toSetSourceString(context, context.getRoot());

                    if (context.getCurrentType() == Void.TYPE || context.getCurrentType() == void.class)
                        throw new UnsupportedCompilationException("Method argument can't be a void type.");

                    if (parmString == null || parmString.trim().length() < 1) {
                        if (children[i] instanceof ASTProperty || children[i] instanceof ASTMethod
                                || children[i] instanceof ASTStaticMethod || children[i] instanceof ASTChain)
                            throw new UnsupportedCompilationException("ASTMethod setter child returned null from a sub property expression.");

                        parmString = "null";
                    }

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

                    parmString = cast + parmString;

                    Class<?> valueClass = value != null ? value.getClass() : null;
                    if (NodeType.class.isAssignableFrom(children[i].getClass()))
                        valueClass = ((NodeType) children[i]).getGetterClass();

                    if (valueClass != parms[i]) {
                        if (parms[i].isArray()) {
                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "(" + ExpressionCompiler.getCastString(parms[i])
                                            + ")ognl.OgnlOps#toArray(" + parmString + ", "
                                            + parms[i].getComponentType().getName()
                                            + ".class)",
                                    parms[i]
                            );

                        } else if (parms[i].isPrimitive()) {
                            Class<?> wrapClass = OgnlRuntime.getPrimitiveWrapperClass(parms[i]);

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "((" + wrapClass.getName()
                                            + ")ognl.OgnlOps#convertValue(" + parmString + ","
                                            + wrapClass.getName() + ".class, true))."
                                            + OgnlRuntime.getNumericValueGetter(wrapClass),
                                    parms[i]
                            );

                        } else if (parms[i] != Object.class) {
                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                    "(" + parms[i].getName() + ")ognl.OgnlOps#convertValue("
                                            + parmString + "," + parms[i].getName() + ".class)",
                                    parms[i]
                            );

                        } else if ((children[i] instanceof NodeType
                                && ((NodeType) children[i]).getGetterClass() != null
                                && Number.class.isAssignableFrom(((NodeType) children[i]).getGetterClass()))
                                || (valueClass != null && valueClass.isPrimitive())) {
                            parmString = " ($w) " + parmString;
                        }
                    }

                    result.append(parmString);
                }

                if (prevCast != null) {
                    context.put(ExpressionCompiler.PRE_CAST, prevCast);
                }
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        try {
            Object contextObj = getValueBody(context, target);
            context.setCurrentObject(contextObj);
        } catch (Throwable t) {
            // ignore
        }

        context.setCurrentType(m.getReturnType());
        context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

        return result + ")" + post;
    }

    private static Class<?> getClassMatchingAllChildren(OgnlContext context, Node[] _children) {
        Class<?>[] cc = getChildrenClasses(context, _children);
        Class<?> componentType = null;
        for (Class<?> ic : cc) {
            if (ic == null) {
                componentType = Object.class; // fall back to object...
                break;
            } else {
                if (componentType == null) {
                    componentType = ic;
                } else {
                    if (!componentType.isAssignableFrom(ic)) {
                        if (ic.isAssignableFrom(componentType)) {
                            componentType = ic; // just swap... ic is more generic...
                        } else {
                            Class<?> pc;
                            while ((pc = componentType.getSuperclass()) != null) { // TODO hmm - it could also be that an interface matches...
                                if (pc.isAssignableFrom(ic)) {
                                    componentType = pc; // use this matching parent class
                                    break;
                                }
                            }
                            if (!componentType.isAssignableFrom(ic)) {
                                // parents didn't match. the types might be primitives. Fall back to object.
                                componentType = Object.class;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (componentType == null)
            componentType = Object.class;
        return componentType;
    }

    private static Class<?>[] getChildrenClasses(OgnlContext context, Node[] _children) {
        if (_children == null)
            return null;
        Class<?>[] argumentClasses = new Class[_children.length];
        for (int i = 0; i < _children.length; i++) {
            Node child = _children[i];
            if (child instanceof ASTList) {    // special handling for ASTList - it creates a List
                argumentClasses[i] = List.class;
            } else if (child instanceof NodeType) {
                argumentClasses[i] = ((NodeType) child).getGetterClass();
            } else if (child instanceof ASTCtor) {
                try {
                    argumentClasses[i] = ((ASTCtor) child).getCreatedClass(context);
                } catch (ClassNotFoundException nfe) {
                    throw OgnlOps.castToRuntime(nfe);
                }
            } else if (child instanceof ASTTest) {
                argumentClasses[i] = getClassMatchingAllChildren(context, ((ASTTest) child).children);
            } else {
                throw new UnsupportedOperationException("Don't know how to handle child: " + child);
            }
        }
        return argumentClasses;
    }

    @Override
    public boolean isSimpleMethod(OgnlContext context) {
        return true;
    }
}
