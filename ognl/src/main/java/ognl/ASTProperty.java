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

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.io.Serial;
import java.lang.reflect.Method;
import java.util.Iterator;

public class ASTProperty<C extends OgnlContext<C>> extends SimpleNode<C> implements NodeType {

    @Serial
    private static final long serialVersionUID = -3670610950640379714L;

    private boolean indexedAccess = false;
    private Class<?> getterClass;
    private Class<?> setterClass;

    public ASTProperty(int id) {
        super(id);
    }

    public void setIndexedAccess(boolean value) {
        indexedAccess = value;
    }

    /**
     * Returns true if this property is itself an index reference.
     *
     * @return true if this property is an index reference, false otherwise.
     */
    public boolean isIndexedAccess() {
        return indexedAccess;
    }

    /**
     * Returns true if this property is described by an IndexedPropertyDescriptor and that if
     * followed by an index specifier it will call the index get/set methods rather than go through
     * property accessors.
     *
     * @param context the OgnlContext within which to perform the operation.
     * @param source  the Object (indexed property) from which to retrieve the indexed property type.
     * @return the int representing the indexed property type of source.
     * @throws OgnlException if source is not an indexed property.
     */
    public int getIndexedPropertyType(C context, Object source)
            throws OgnlException {
        Class<?> type = context.getCurrentType();
        Class<?> prevType = context.getPreviousType();
        try {
            if (!isIndexedAccess()) {
                Object property = getProperty(context, source);

                if (property instanceof String) {
                    return OgnlRuntime.getIndexedPropertyType((source == null)
                            ? null
                            : OgnlRuntime.getCompiler().getInterfaceClass(source.getClass()), (String) property);
                }
            }

            return OgnlRuntime.INDEXED_PROPERTY_NONE;
        } finally {
            context.setCurrentObject(source);
            context.setCurrentType(type);
            context.setPreviousType(prevType);
        }
    }

    public Object getProperty(C context, Object source) throws OgnlException {
        return children[0].getValue(context, context.getRoot());
    }

    protected Object getValueBody(C context, Object source)
            throws OgnlException {
        Object property = getProperty(context, source);

        Object result = OgnlRuntime.getProperty(context, source, property);

        if (result == null) {
            result = OgnlRuntime.getNullHandler(OgnlRuntime.getTargetClass(source)).nullPropertyValue(context, source, property);
        }

        return result;
    }

    protected void setValueBody(C context, Object target, Object value)
            throws OgnlException {
        OgnlRuntime.setProperty(context, target, getProperty(context, target), value);
    }

    public boolean isNodeSimpleProperty(C context)
            throws OgnlException {
        return (children != null) && (children.length == 1) && ((SimpleNode<C>) children[0]).isConstant(context);
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return setterClass;
    }

    public String toString() {
        String result;

        if (isIndexedAccess()) {
            result = "[" + children[0] + "]";
        } else {
            result = ((ASTConst<C>) children[0]).getValue().toString();
        }
        return result;
    }

    public String toGetSourceString(C context, Object target) {
        if (context.getCurrentObject() == null)
            throw new UnsupportedCompilationException("Current target is null.");

        String result = "";
        Method m = null;

        try {
            if (isIndexedAccess()) {
                Object value = children[0].getValue(context, context.getRoot());

                if (value == null || DynamicSubscript.class.isAssignableFrom(value.getClass()))
                    throw new UnsupportedCompilationException("Value passed as indexed property was null or not supported.");

                // Get root cast string if the child is a type that needs it (like a nested ASTProperty)

                String srcString = children[0].toGetSourceString(context, context.getRoot());
                srcString = ExpressionCompiler.getRootExpression(children[0], context.getRoot(), context) + srcString;

                if (children[0] instanceof ASTChain) {
                    String cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                    if (cast != null)
                        srcString = cast + srcString;
                }

                if (children[0] instanceof ASTConst && context.getCurrentObject() instanceof String)
                    srcString = "\"" + srcString + "\"";

                // System.out.println("indexed getting with child srcString: " + srcString + " value class: " + value.getClass() + " and child: " + _children[0].getClass());

                if (context.get("_indexedMethod") != null) {
                    m = (Method) context.remove("_indexedMethod");
                    getterClass = m.getReturnType();

                    Object indexedValue = OgnlRuntime.callMethod(context, target, m.getName(), new Object[]{value});

                    context.setCurrentType(getterClass);
                    context.setCurrentObject(indexedValue);
                    context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

                    return "." + m.getName() + "(" + srcString + ")";
                } else {
                    PropertyAccessor<C> p = OgnlRuntime.getPropertyAccessor(target.getClass());

                    Object currObj = context.getCurrentObject();
                    Class<?> currType = context.getCurrentType();
                    Class<?> prevType = context.getPreviousType();

                    Object indexVal = p.getProperty(context, target, value);

                    // reset current object for accessor

                    context.setCurrentObject(currObj);
                    context.setCurrentType(currType);
                    context.setPreviousType(prevType);

                    if (children[0] instanceof ASTConst && context.getCurrentObject() instanceof Number)
                        context.setCurrentType(OgnlRuntime.getPrimitiveWrapperClass(context.getCurrentObject().getClass()));

                    result = p.getSourceAccessor(context, target, srcString);
                    getterClass = context.getCurrentType();
                    context.setCurrentObject(indexVal);

                    return result;
                }
            }

            String name = ((ASTConst<C>) children[0]).getValue().toString();

            if (!Iterator.class.isAssignableFrom(context.getCurrentObject().getClass())
                    || (Iterator.class.isAssignableFrom(context.getCurrentObject().getClass()) && !name.contains("next"))) {
                Object currObj = target;

                try {
                    target = getValue(context, context.getCurrentObject());
                } catch (NoSuchPropertyException e) {
                    try {
                        target = getValue(context, context.getRoot());
                    } catch (NoSuchPropertyException ex) {
                        // ignore
                    }
                } finally {
                    context.setCurrentObject(currObj);
                }
            }

            PropertyDescriptor pd = OgnlRuntime.getPropertyDescriptor(context.getCurrentObject().getClass(), name);

            if (pd != null && pd.getReadMethod() != null
                    && !context.getMemberAccess().isAccessible(context, context.getCurrentObject(), pd.getReadMethod(), name)) {
                throw new UnsupportedCompilationException("Member access forbidden for property " + name + " on class " + context.getCurrentObject().getClass());
            }

            if (this.getIndexedPropertyType(context, context.getCurrentObject()) > 0 && pd != null) {
                // if an indexed method accessor need to use special property descriptors to find methods

                if (pd instanceof IndexedPropertyDescriptor) {
                    m = ((IndexedPropertyDescriptor) pd).getIndexedReadMethod();
                } else {
                    if (pd instanceof ObjectIndexedPropertyDescriptor)
                        m = ((ObjectIndexedPropertyDescriptor) pd).getIndexedReadMethod();
                    else
                        throw new OgnlException("property '" + name + "' is not an indexed property");
                }

                if (parent == null) {
                    // the above pd will be the wrong result sometimes, such as methods like getValue(int) vs String[] getValue()
                    m = OgnlRuntime.getReadMethod(context.getCurrentObject().getClass(), name);

                    result = m.getName() + "()";
                    getterClass = m.getReturnType();
                } else {
                    context.put("_indexedMethod", m);
                }
            } else {

                /*    System.out.println("astproperty trying to get " + name + " on object target: " + context.getCurrentObject().getClass().getName()
       + " current type " + context.getCurrentType() + " current accessor " + context.getCurrentAccessor()
   + " prev type " + context.getPreviousType() + " prev accessor " + context.getPreviousAccessor());*/

                PropertyAccessor<C> pa = OgnlRuntime.getPropertyAccessor(context.getCurrentObject().getClass());

                if (context.getCurrentObject().getClass().isArray()) {
                    if (pd == null) {
                        pd = OgnlRuntime.getProperty(context.getCurrentObject().getClass(), name);

                        if (pd != null && pd.getReadMethod() != null) {
                            m = pd.getReadMethod();
                            result = pd.getName();
                        } else {
                            getterClass = int.class;
                            context.setCurrentAccessor(context.getCurrentObject().getClass());
                            context.setCurrentType(int.class);
                            result = "." + name;
                        }
                    }
                } else {
                    if (pd != null && pd.getReadMethod() != null) {
                        m = pd.getReadMethod();
                        result = "." + m.getName() + "()";
                    } else if (pa != null) {
                        Object currObj = context.getCurrentObject();
                        Class<?> currType = context.getCurrentType();
                        Class<?> prevType = context.getPreviousType();

                        String srcString = children[0].toGetSourceString(context, context.getRoot());

                        if (children[0] instanceof ASTConst && context.getCurrentObject() instanceof String) {
                            srcString = "\"" + srcString + "\"";
                        }

                        context.setCurrentObject(currObj);
                        context.setCurrentType(currType);
                        context.setPreviousType(prevType);

                        result = pa.getSourceAccessor(context, context.getCurrentObject(), srcString);

                        getterClass = context.getCurrentType();
                    }
                }
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        // set known property types for NodeType interface when possible

        if (m != null) {
            getterClass = m.getReturnType();

            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));
        }

        context.setCurrentObject(target);

        return result;
    }

    Method getIndexedWriteMethod(PropertyDescriptor pd) {
        if (pd instanceof IndexedPropertyDescriptor) {
            return ((IndexedPropertyDescriptor) pd).getIndexedWriteMethod();
        } else if (pd instanceof ObjectIndexedPropertyDescriptor) {
            return ((ObjectIndexedPropertyDescriptor) pd).getIndexedWriteMethod();
        }

        return null;
    }

    public String toSetSourceString(C context, Object target) {
        String result = "";
        Method m = null;

        if (context.getCurrentObject() == null)
            throw new UnsupportedCompilationException("Current target is null.");

        try {

            if (isIndexedAccess()) {
                Object value = children[0].getValue(context, context.getRoot());

                if (value == null)
                    throw new UnsupportedCompilationException("Value passed as indexed property is null, can't enhance statement to bytecode.");

                String srcString = children[0].toGetSourceString(context, context.getRoot());
                srcString = ExpressionCompiler.getRootExpression(children[0], context.getRoot(), context) + srcString;

                if (children[0] instanceof ASTChain) {
                    String cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                    if (cast != null)
                        srcString = cast + srcString;
                }

                if (children[0] instanceof ASTConst && context.getCurrentObject() instanceof String) {
                    srcString = "\"" + srcString + "\"";
                }

                if (context.get("_indexedMethod") != null) {
                    m = (Method) context.remove("_indexedMethod");
                    PropertyDescriptor pd = (PropertyDescriptor) context.remove("_indexedDescriptor");

                    boolean lastChild = lastChild(context);
                    if (lastChild) {
                        m = getIndexedWriteMethod(pd);

                        if (m == null)
                            throw new UnsupportedCompilationException("Indexed property has no corresponding write method.");
                    }

                    setterClass = m.getParameterTypes()[0];

                    Object indexedValue = null;
                    if (!lastChild)
                        indexedValue = OgnlRuntime.callMethod(context, target, m.getName(), new Object[]{value});

                    context.setCurrentType(setterClass);
                    context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

                    if (!lastChild) {
                        context.setCurrentObject(indexedValue);
                        return "." + m.getName() + "(" + srcString + ")";
                    } else {
                        return "." + m.getName() + "(" + srcString + ", $3)";
                    }
                } else {
                    PropertyAccessor<C> p = OgnlRuntime.getPropertyAccessor(target.getClass());

                    Object currObj = context.getCurrentObject();
                    Class<?> currType = context.getCurrentType();
                    Class<?> prevType = context.getPreviousType();

                    Object indexVal = p.getProperty(context, target, value);

                    // reset current object for accessor

                    context.setCurrentObject(currObj);
                    context.setCurrentType(currType);
                    context.setPreviousType(prevType);

                    if (children[0] instanceof ASTConst && context.getCurrentObject() instanceof Number)
                        context.setCurrentType(OgnlRuntime.getPrimitiveWrapperClass(context.getCurrentObject().getClass()));

                    result = lastChild(context) ? p.getSourceSetter(context, target, srcString) : p.getSourceAccessor(context, target, srcString);

                    getterClass = context.getCurrentType();
                    context.setCurrentObject(indexVal);

                    return result;
                }
            }

            String name = ((ASTConst<C>) children[0]).getValue().toString();

            if (!Iterator.class.isAssignableFrom(context.getCurrentObject().getClass())
                    || (Iterator.class.isAssignableFrom(context.getCurrentObject().getClass()) && !name.contains("next"))) {

                Object currObj = target;

                try {
                    target = getValue(context, context.getCurrentObject());
                } catch (NoSuchPropertyException e) {
                    try {
                        target = getValue(context, context.getRoot());
                    } catch (NoSuchPropertyException ignored) {
                    }
                } finally {

                    context.setCurrentObject(currObj);
                }
            }

            PropertyDescriptor pd = OgnlRuntime.getPropertyDescriptor(OgnlRuntime.getCompiler().getInterfaceClass(context.getCurrentObject().getClass()), name);

            if (pd != null) {
                Method pdMethod = lastChild(context) ? pd.getWriteMethod() : pd.getReadMethod();

                if (pdMethod != null && !context.getMemberAccess().isAccessible(context, context.getCurrentObject(), pdMethod, name)) {
                    throw new UnsupportedCompilationException("Member access forbidden for property " + name + " on class " + context.getCurrentObject().getClass());
                }
            }

            if (pd != null && this.getIndexedPropertyType(context, context.getCurrentObject()) > 0) {
                // if an indexed method accessor need to use special property descriptors to find methods

                if (pd instanceof IndexedPropertyDescriptor ipd) {
                    m = lastChild(context) ? ipd.getIndexedWriteMethod() : ipd.getIndexedReadMethod();
                } else {
                    if (pd instanceof ObjectIndexedPropertyDescriptor opd) {
                        m = lastChild(context) ? opd.getIndexedWriteMethod() : opd.getIndexedReadMethod();
                    } else {
                        throw new OgnlException("property '" + name + "' is not an indexed property");
                    }
                }

                if (parent == null) {
                    // the above pd will be the wrong result sometimes, such as methods like getValue(int) vs String[] getValue()

                    m = OgnlRuntime.getWriteMethod(context.getCurrentObject().getClass(), name);
                    Class<?> parm = m.getParameterTypes()[0];
                    String cast = parm.isArray() ? ExpressionCompiler.getCastString(parm) : parm.getName();

                    result = m.getName() + "((" + cast + ")$3)";
                    setterClass = parm;
                } else {
                    context.put("_indexedMethod", m);
                    context.put("_indexedDescriptor", pd);
                }

            } else {
                PropertyAccessor<C> pa = OgnlRuntime.getPropertyAccessor(context.getCurrentObject().getClass());

                if (target != null)
                    setterClass = target.getClass();

                if (parent != null && pd != null && pa == null) {
                    m = pd.getReadMethod();
                    result = m.getName() + "()";
                } else {
                    if (context.getCurrentObject().getClass().isArray()) {
                        result = "";
                    } else if (pa != null) {
                        Object currObj = context.getCurrentObject();
                        //Class currType = context.getCurrentType();
                        //Class prevType = context.getPreviousType();

                        String srcString = children[0].toGetSourceString(context, context.getRoot());

                        if (children[0] instanceof ASTConst && context.getCurrentObject() instanceof String) {
                            srcString = "\"" + srcString + "\"";
                        }

                        context.setCurrentObject(currObj);
                        //context.setCurrentType(currType);
                        //context.setPreviousType(prevType);

                        if (!lastChild(context)) {
                            result = pa.getSourceAccessor(context, context.getCurrentObject(), srcString);
                        } else {
                            result = pa.getSourceSetter(context, context.getCurrentObject(), srcString);
                        }

                        getterClass = context.getCurrentType();
                    }
                }
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        context.setCurrentObject(target);

        if (m != null) {
            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));
        }

        return result;
    }
}
