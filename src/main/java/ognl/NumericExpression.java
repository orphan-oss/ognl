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

/**
 * Base class for numeric expressions.
 */
public abstract class NumericExpression extends ExpressionNode implements NodeType {

    private static final long serialVersionUID = 411246929049244018L;

    protected Class<?> getterClass;

    public NumericExpression(int id) {
        super(id);
    }

    public NumericExpression(OgnlParser p, int id) {
        super(p, id);
    }

    public Class<?> getGetterClass() {
        if (getterClass != null)
            return getterClass;

        return Double.TYPE;
    }

    public Class<?> getSetterClass() {
        return null;
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        Object value;
        StringBuilder result = new StringBuilder();

        try {
            value = getValueBody(context, target);

            if (value != null) {
                getterClass = value.getClass();
            }

            for (int i = 0; i < children.length; i++) {
                if (i > 0) {
                    result.append(" ").append(getExpressionOperator(i)).append(" ");
                }
                String str = OgnlRuntime.getChildSource(context, target, children[i]);
                result.append(coerceToNumeric(str, context, children[i]));
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result.toString();
    }

    public String coerceToNumeric(String source, OgnlContext context, Node child) {
        String ret = source;
        Object value = context.getCurrentObject();

        if (child instanceof ASTConst && value != null) {
            return value.toString();
        }

        if (context.getCurrentType() != null && !context.getCurrentType().isPrimitive()
                && context.getCurrentObject() != null && context.getCurrentObject() instanceof Number) {
            ret = "((" + ExpressionCompiler.getCastString(context.getCurrentObject().getClass()) + ")" + ret + ")";
            ret += "." + OgnlRuntime.getNumericValueGetter(context.getCurrentObject().getClass());
        } else if (context.getCurrentType() != null && context.getCurrentType().isPrimitive()
                && (child instanceof ASTConst || child instanceof NumericExpression)) {
            ret += OgnlRuntime.getNumericLiteral(context.getCurrentType());
        } else if (context.getCurrentType() != null && String.class.isAssignableFrom(context.getCurrentType())) {
            ret = "Double.parseDouble(" + ret + ")";
            context.setCurrentType(Double.TYPE);
        }

        if (child instanceof NumericExpression)
            ret = "(" + ret + ")";

        return ret;
    }
}
