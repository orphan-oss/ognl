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

public class ASTSequence extends SimpleNode implements NodeType, OrderedReturn {

    private static final long serialVersionUID = 7862664419715024875L;

    private Class<?> getterClass;
    private String lastExpression;
    private String coreExpression;

    public ASTSequence(int id) {
        super(id);
    }

    public ASTSequence(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object result = null;
        for (Node child : children) {
            result = child.getValue(context, source);
        }
        return result; // The result is just the last one we saw.
    }

    protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException {
        int last = children.length - 1;
        for (int i = 0; i < last; ++i) {
            children[i].getValue(context, target);
        }
        children[last].setValue(context, target, value);
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return null;
    }

    public String getLastExpression() {
        return lastExpression;
    }

    public String getCoreExpression() {
        return coreExpression;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < children.length; ++i) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(children[i]);
        }
        return result.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        return "";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        String result = "";

        NodeType _lastType = null;

        for (int i = 0; i < children.length; ++i) {
            String seqValue = children[i].toGetSourceString(context, target);

            if ((i + 1) < children.length && children[i] instanceof ASTOr) {
                seqValue = "(" + seqValue + ")";
            }

            if (i > 0 && children[i] instanceof ASTProperty && seqValue != null && seqValue.trim().length() > 0) {
                String pre = (String) context.get("_currentChain");
                if (pre == null) {
                    pre = "";
                }
                seqValue = ExpressionCompiler.getRootExpression(children[i], context.getRoot(), context) + pre + seqValue;
                context.setCurrentAccessor(context.getRoot().getClass());
            }

            if ((i + 1) >= children.length) {
                coreExpression = result;
                lastExpression = seqValue;
            }

            if (seqValue != null && seqValue.trim().length() > 0 && (i + 1) < children.length) {
                result += seqValue + ";";
            } else if (seqValue != null && seqValue.trim().length() > 0) {
                result += seqValue;
            }

            // set last known type from last child with a type
            if (children[i] instanceof NodeType && ((NodeType) children[i]).getGetterClass() != null) {
                _lastType = (NodeType) children[i];
            }
        }

        if (_lastType != null) {
            getterClass = _lastType.getGetterClass();
        }

        return result;
    }

    public boolean isSequence(OgnlContext context) {
        return true;
    }

}
