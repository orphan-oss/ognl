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

public abstract class ExpressionNode extends SimpleNode {

    private static final long serialVersionUID = 4880029588563407661L;

    public ExpressionNode(int i) {
        super(i);
    }

    public ExpressionNode(OgnlParser p, int i) {
        super(p, i);
    }

    /**
     * Returns true iff this node is constant without respect to the children.
     */
    public boolean isNodeConstant(OgnlContext context) throws OgnlException {
        return false;
    }

    public boolean isConstant(OgnlContext context) throws OgnlException {
        boolean result = isNodeConstant(context);

        if ((children != null) && (children.length > 0)) {
            result = true;
            for (int i = 0; result && (i < children.length); ++i) {
                if (children[i] instanceof SimpleNode) {
                    result = ((SimpleNode) children[i]).isConstant(context);
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    public String getExpressionOperator(int index) {
        throw new RuntimeException("unknown operator for " + OgnlParserTreeConstants.jjtNodeName[id]);
    }

    public String toString() {
        StringBuilder result = new StringBuilder((parent == null) ? "" : "(");

        if ((children != null) && (children.length > 0)) {
            for (int i = 0; i < children.length; ++i) {
                if (i > 0) {
                    result.append(" ").append(getExpressionOperator(i)).append(" ");
                }
                result.append(children[i].toString());
            }
        }
        if (parent != null) {
            result.append(")");
        }
        return result.toString();
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        StringBuilder result = new StringBuilder((parent == null || NumericExpression.class.isAssignableFrom(parent.getClass())) ? "" : "(");

        if ((children != null) && (children.length > 0)) {
            for (int i = 0; i < children.length; ++i) {
                if (i > 0) {
                    result.append(" ").append(getExpressionOperator(i)).append(" ");
                }

                String value = children[i].toGetSourceString(context, target);

                if ((children[i] instanceof ASTProperty || children[i] instanceof ASTMethod
                        || children[i] instanceof ASTSequence || children[i] instanceof ASTChain)
                        && value != null && value.trim().length() > 0) {

                    String pre = null;
                    if (children[i] instanceof ASTMethod) {
                        pre = (String) context.get("_currentChain");
                    }

                    if (pre == null)
                        pre = "";

                    String cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                    if (cast == null)
                        cast = "";

                    value = cast + ExpressionCompiler.getRootExpression(children[i], context.getRoot(), context) + pre + value;
                }

                result.append(value);
            }
        }

        if (parent != null && !NumericExpression.class.isAssignableFrom(parent.getClass())) {
            result.append(")");
        }

        return result.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        StringBuilder result = new StringBuilder((parent == null) ? "" : "(");

        if ((children != null) && (children.length > 0)) {
            for (int i = 0; i < children.length; ++i) {
                if (i > 0) {
                    result.append(" ").append(getExpressionOperator(i)).append(" ");
                }

                result.append(children[i].toSetSourceString(context, target));
            }
        }
        if (parent != null) {
            result.append(")");
        }

        return result.toString();
    }

    @Override
    public boolean isOperation(OgnlContext context) throws OgnlException {
        return true;
    }
}
