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
import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;

import java.io.Serial;
import java.lang.reflect.Array;

public class ASTChain<C extends OgnlContext<C>> extends SimpleNode<C> implements NodeType, OrderedReturn {

    @Serial
    private static final long serialVersionUID = -4684366534386585181L;

    private final boolean shortCircuit = Boolean.parseBoolean(System.getProperty("ognl.chain.short-circuit", "true"));
    private boolean nullSafe = false;

    private Class<?> getterClass;
    private Class<?> setterClass;
    private String lastExpression;
    private String coreExpression;

    public ASTChain(int id) {
        super(id);
    }

    public ASTChain(OgnlParser p, int id) {
        super(p, id);
    }

    public String getLastExpression() {
        return lastExpression;
    }

    public String getCoreExpression() {
        return coreExpression;
    }

    public void jjtClose() {
        flattenTree();
    }

    /**
     * Sets whether this chain uses null-safe navigation (?. operator).
     *
     * @param nullSafe true if this is a null-safe chain, false otherwise
     */
    public void setNullSafe(boolean nullSafe) {
        this.nullSafe = nullSafe;
    }

    /**
     * Returns whether this chain uses null-safe navigation.
     *
     * @return true if this is a null-safe chain, false otherwise
     */
    public boolean isNullSafe() {
        return nullSafe;
    }

    protected Object getValueBody(C context, Object source) throws OgnlException {
        Object result = source;

        // null-safe operator: return null immediately if source is null
        if (nullSafe && result == null) {
            return null;
        }

        // short-circuit the chain only in case if the root is null and this isn't IN operator
        if (shortCircuit && result == null && !(parent instanceof ASTIn)) {
            return null;
        }

        for (int i = 0, ilast = children.length - 1; i <= ilast; ++i) {
            // null-safe operator: return null if intermediate result is null
            if (nullSafe && result == null) {
                return null;
            }

            // short-circuit the chain only in case if the root is null and accessing property
            if (shortCircuit && result == null && (children[i] instanceof ASTProperty)) {
                return null;
            }

            boolean handled = false;

            if (i < ilast) {
                if (children[i] instanceof ASTProperty<C> propertyNode) {
                    int indexType = propertyNode.getIndexedPropertyType(context, result);

                    if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (children[i + 1] instanceof ASTProperty<C> indexNode)) {
                        if (indexNode.isIndexedAccess()) {
                            Object index = indexNode.getProperty(context, result);

                            if (index instanceof DynamicSubscript) {
                                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                                    Object array = propertyNode.getValue(context, result);
                                    int len = Array.getLength(array);

                                    switch (((DynamicSubscript) index).getFlag()) {
                                        case DynamicSubscript.ALL:
                                            result = Array.newInstance(array.getClass().getComponentType(), len);
                                            System.arraycopy(array, 0, result, 0, len);
                                            handled = true;
                                            i++;
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
                                } else {
                                    if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) {
                                        throw new OgnlException(
                                                "DynamicSubscript '" + indexNode
                                                        + "' not allowed for object indexed property '" + propertyNode
                                                        + "'");
                                    }
                                }
                            }
                            if (!handled) {
                                result = OgnlRuntime.getIndexedProperty(context, result,
                                        propertyNode.getProperty(context, result).toString(),
                                        index);
                                handled = true;
                                i++;
                            }
                        }
                    }
                }
            }
            if (!handled) {
                result = children[i].getValue(context, result);
            }
        }
        return result;
    }

    protected void setValueBody(C context, Object target, Object value)
            throws OgnlException {
        boolean handled = false;

        for (int i = 0, ilast = children.length - 2; i <= ilast; ++i) {
            if (children[i] instanceof ASTProperty<C> propertyNode) {
                int indexType = propertyNode.getIndexedPropertyType(context, target);

                if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (children[i + 1] instanceof ASTProperty<C> indexNode)) {
                    if (indexNode.isIndexedAccess()) {
                        Object index = indexNode.getProperty(context, target);

                        if (index instanceof DynamicSubscript) {
                            if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                                Object array = propertyNode.getValue(context, target);
                                int len = Array.getLength(array);

                                switch (((DynamicSubscript) index).getFlag()) {
                                    case DynamicSubscript.ALL:
                                        System.arraycopy(target, 0, value, 0, len);
                                        handled = true;
                                        i++;
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
                            } else {
                                if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) {
                                    throw new OgnlException("DynamicSubscript '" + indexNode
                                            + "' not allowed for object indexed property '" + propertyNode
                                            + "'");
                                }
                            }
                        }
                        if (!handled && i == ilast) {
                            OgnlRuntime.setIndexedProperty(context, target,
                                    propertyNode.getProperty(context, target).toString(),
                                    index, value);
                            handled = true;
                            i++;
                        } else if (!handled) {
                            target = OgnlRuntime.getIndexedProperty(context, target,
                                    propertyNode.getProperty(context, target).toString(),
                                    index);
                            i++;
                            continue;
                        }
                    }
                }
            }
            if (!handled) {
                target = children[i].getValue(context, target);
            }
        }
        if (!handled) {
            children[children.length - 1].setValue(context, target, value);
        }
    }

    public boolean isSimpleNavigationChain(C context)
            throws OgnlException {
        boolean result = false;

        if ((children != null) && (children.length > 0)) {
            result = true;
            for (int i = 0; result && (i < children.length); i++) {
                if (children[i] instanceof SimpleNode) {
                    result = ((SimpleNode<C>) children[i]).isSimpleProperty(context);
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return setterClass;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        if ((children != null) && (children.length > 0)) {
            for (int i = 0; i < children.length; i++) {
                if (i > 0 && shouldAppendNavigationOperator(children[i])) {
                    result.append(nullSafe ? "?." : ".");
                }
                result.append(children[i].toString());
            }
        }
        return result.toString();
    }

    private boolean shouldAppendNavigationOperator(Node child) {
        return !(child instanceof ASTProperty) || !((ASTProperty<C>) child).isIndexedAccess();
    }

    public String toGetSourceString(C context, Object target) {
        String prevChain = (String) context.get("_currentChain");

        if (target != null) {
            context.setCurrentObject(target);
            context.setCurrentType(target.getClass());
        }

        // For null-safe chains, wrap in null check
        if (nullSafe && target == null) {
            return "null";
        }

        String result = "";
        NodeType _lastType = null;
        boolean ordered = false;
        boolean constructor = false;
        try {
            if (children != null) {
                for (Node<C> child : children) {
                    String value = child.toGetSourceString(context, context.getCurrentObject());

                    if (child instanceof ASTCtor)
                        constructor = true;

                    if (child instanceof NodeType
                            && ((NodeType) child).getGetterClass() != null) {
                        _lastType = (NodeType) child;
                    }

                    if (!(child instanceof ASTVarRef) && !constructor
                            && !(child instanceof OrderedReturn && ((OrderedReturn) child).getLastExpression() != null)
                            && (!(parent instanceof ASTSequence))) {
                        value = OgnlRuntime.getCompiler().castExpression(context, child, value);
                    }

                    if (child instanceof OrderedReturn or && or.getLastExpression() != null) {
                        ordered = true;

                        if (or.getCoreExpression() == null || or.getCoreExpression().trim().length() <= 0)
                            result = "";
                        else
                            result += or.getCoreExpression();

                        lastExpression = or.getLastExpression();

                        if (context.get(ExpressionCompiler.PRE_CAST) != null) {
                            lastExpression = context.remove(ExpressionCompiler.PRE_CAST) + lastExpression;
                        }
                    } else if (child instanceof ASTOr
                            || child instanceof ASTAnd
                            || child instanceof ASTCtor
                            || (child instanceof ASTStaticField && parent == null)) {
                        context.put("_noRoot", "true");
                        result = value;
                    } else {
                        result += value;
                    }

                    context.put("_currentChain", result);
                }
            }
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        if (_lastType != null) {
            getterClass = _lastType.getGetterClass();
            setterClass = _lastType.getSetterClass();
        }

        if (ordered) {
            coreExpression = result;
        }

        context.put("_currentChain", prevChain);

        return result;
    }

    public String toSetSourceString(C context, Object target) {
        String prevChain = (String) context.get("_currentChain");
        String prevChild = (String) context.get("_lastChild");

        if (prevChain != null)
            throw new UnsupportedCompilationException("Can't compile nested chain expressions.");

        if (target != null) {
            context.setCurrentObject(target);
            context.setCurrentType(target.getClass());
        }

        String result = "";
        NodeType _lastType = null;
        boolean constructor = false;
        try {
            if ((children != null) && (children.length > 0)) {
                if (children[0] instanceof ASTConst) {
                    throw new UnsupportedCompilationException("Can't modify constant values.");
                }

                for (int i = 0; i < children.length; i++) {
                    if (i == (children.length - 1)) {
                        context.put("_lastChild", "true");
                    }

                    String value = children[i].toSetSourceString(context, context.getCurrentObject());

                    if (children[i] instanceof ASTCtor)
                        constructor = true;

                    if (children[i] instanceof NodeType
                            && ((NodeType) children[i]).getGetterClass() != null) {
                        _lastType = (NodeType) children[i];
                    }

                    if (!(children[i] instanceof ASTVarRef) && !constructor
                            && !(children[i] instanceof OrderedReturn && ((OrderedReturn) children[i]).getLastExpression() != null)
                            && (!(parent instanceof ASTSequence))) {
                        value = OgnlRuntime.getCompiler().castExpression(context, children[i], value);
                    }

                    if (children[i] instanceof ASTOr
                            || children[i] instanceof ASTAnd
                            || children[i] instanceof ASTCtor
                            || children[i] instanceof ASTStaticField) {
                        context.put("_noRoot", "true");
                        result = value;
                    } else
                        result += value;

                    context.put("_currentChain", result);
                }
            }
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        context.put("_lastChild", prevChild);
        context.put("_currentChain", prevChain);

        if (_lastType != null)
            setterClass = _lastType.getSetterClass();

        return result;
    }

    @Override
    public boolean isChain(C context) {
        return true;
    }
}
