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

import java.io.Serial;

/**
 * Base class for numeric expressions.
 */
public abstract class NumericExpression<C extends OgnlContext<C>> extends ExpressionNode<C> implements NodeType {

    @Serial
    private static final long serialVersionUID = 2899128497573777569L;

    protected Class<?> getterClass;

    private transient boolean bigSource;

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

    public String toGetSourceString(C context, Object target) {
        Object value;
        StringBuilder result = new StringBuilder();

        try {
            value = getValueBody(context, target);

            if (value != null) {
                getterClass = value.getClass();
            }

            String[] operands = new String[children.length];
            boolean bigOperand = false;
            for (int i = 0; i < children.length; i++) {
                if (i > 0) {
                    result.append(" ").append(getExpressionOperator(i)).append(" ");
                }
                String str = OgnlRuntime.getChildSource(context, target, children[i]);
                operands[i] = str;
                bigOperand |= OgnlRuntime.isBigNumber(context.getCurrentType());
                result.append(coerceToNumeric(str, context, children[i]));
            }

            if (getOgnlOpsMethod() != null && (bigOperand || OgnlRuntime.isBigNumber(getterClass))) {
                return toBigSourceString(operands, value, context);
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result.toString();
    }

    protected String getOgnlOpsMethod() {
        return null;
    }

    protected String toBigSourceString(String[] operands, Object value, C context) {
        String result = "($w) (" + operands[0] + ")";
        for (int i = 1; i < operands.length; i++) {
            result = "ognl.OgnlOps." + getOgnlOpsMethod() + "(" + result + ", ($w) (" + operands[i] + "))";
        }
        return castBigResult(result, value, context);
    }

    // OgnlOps returns Object, so cast to the type getGetterClass() reports, or a method argument won't compile
    protected String castBigResult(String source, Object value, C context) {
        markBigSource();
        Class<?> resultClass = value != null && (OgnlRuntime.isBigNumber(value.getClass()) || value instanceof String)
                ? value.getClass()
                : Object.class;
        getterClass = resultClass;
        context.setCurrentType(resultClass);
        context.setCurrentObject(value);
        return resultClass == Object.class ? source : "((" + resultClass.getName() + ") " + source + ")";
    }

    protected String toBigUnarySourceString(String method, Class<?> resultClass, C context, Object target) {
        markBigSource();
        String operand = OgnlRuntime.getChildSource(context, target, children[0]);
        getterClass = resultClass;
        context.setCurrentType(resultClass);
        return "((" + resultClass.getName() + ") ognl.OgnlOps." + method + "(($w) (" + operand + ")))";
    }

    private void markBigSource() {
        // ASTChain prefixes its first link with the root expression, which no expression source survives
        if (parent instanceof ASTChain && parent.jjtGetChild(0) == this) {
            throw new UnsupportedCompilationException("Can't chain a call on a BigDecimal/BigInteger expression.");
        }
        bigSource = true;
    }

    @Override
    public String toSetSourceString(C context, Object target) {
        if (bigSource) {
            throw new UnsupportedCompilationException("Can't compile a setter through a BigDecimal/BigInteger expression.");
        }
        return super.toSetSourceString(context, target);
    }

    public String coerceToNumeric(String source, C context, Node<C> child) {
        String ret = source;
        Object value = context.getCurrentObject();

        if (child instanceof ASTConst && value != null) {
            String literal = OgnlRuntime.getNumericLiteral(value.getClass());
            return value.toString() + (literal != null ? literal : "");
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
