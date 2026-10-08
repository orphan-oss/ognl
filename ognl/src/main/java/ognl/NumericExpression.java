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

    private transient boolean opsSource;

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
            boolean opsOperand = false;
            for (int i = 0; i < children.length; i++) {
                if (i > 0) {
                    result.append(" ").append(getExpressionOperator(i)).append(" ");
                }
                String str = OgnlRuntime.getChildSource(context, target, children[i]);
                operands[i] = str;
                opsOperand |= needsOgnlOps(context, children[i]);
                result.append(coerceToNumeric(str, context, children[i]));
            }

            if (getOgnlOpsMethod() != null && (opsOperand || OgnlRuntime.isBigNumber(getterClass))) {
                return toOpsSourceString(operands, value, context);
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result.toString();
    }

    // A Number- or Object-typed operand can still hold a Big value, so the compile-time value counts too
    protected Class<?> bigOperandClass(C context) {
        if (OgnlRuntime.isBigNumber(context.getCurrentType())) {
            return context.getCurrentType();
        }
        Object value = context.getCurrentObject();
        return value != null && OgnlRuntime.isBigNumber(value.getClass()) ? value.getClass() : null;
    }

    // javassist keeps char arithmetic at char width and concatenates boxed Characters, so generated
    // source cannot reproduce OgnlOps. An Object-typed operand can still hold a character, so the
    // compile-time value counts too, the way bigOperandClass treats a Big value
    protected boolean isCharOperand(C context) {
        Class<?> type = context.getCurrentType();
        return Character.class == type || Character.TYPE == type
                || context.getCurrentObject() instanceof Character;
    }

    // An operand that OgnlOps already answered is an Object, which javassist would concatenate rather
    // than add, so the delegation has to carry on up the tree instead of being cast back to a number
    protected boolean needsOgnlOps(C context, Node<C> child) {
        return bigOperandClass(context) != null || isCharOperand(context) || isOpsDelegated(child);
    }

    protected boolean isOpsDelegated(Node<C> child) {
        return child instanceof NumericExpression && ((NumericExpression<C>) child).opsSource;
    }

    protected String getOgnlOpsMethod() {
        return null;
    }

    protected String toOpsSourceString(String[] operands, Object value, C context) {
        return castOpsResult(opsCall(operands), value, context);
    }

    private String opsCall(String[] operands) {
        StringBuilder result = new StringBuilder("($w) (").append(operands[0]).append(")");
        for (int i = 1; i < operands.length; i++) {
            result.insert(0, "ognl.OgnlOps." + getOgnlOpsMethod() + "(")
                    .append(", ($w) (").append(operands[i]).append("))");
        }
        return result.toString();
    }

    protected String castOpsResult(String source, Object value, C context) {
        markOpsSource();
        reportOpsResult(value, context);
        // Cast only where the operand types pin the class. A character operand does not: it admits
        // null, which OgnlOps answers from its non-numeric branches, and the cast would fail
        return value != null && (OgnlRuntime.isBigNumber(value.getClass()) || value instanceof String)
                ? "((" + value.getClass().getName() + ") " + source + ")"
                : source;
    }

    // The source stays an Object, but an enclosing method call resolves its overload on the type
    // reported here, and it has to pick the one the interpreted path picks
    private void reportOpsResult(Object value, C context) {
        getterClass = value != null ? value.getClass() : Object.class;
        context.setCurrentType(getterClass);
        context.setCurrentObject(value);
    }

    protected String toOpsUnarySourceString(String method, Class<?> resultClass, C context, Object target) {
        markOpsSource();
        String operand = OgnlRuntime.getChildSource(context, target, children[0]);
        getterClass = resultClass;
        context.setCurrentType(resultClass);
        return "((" + resultClass.getName() + ") ognl.OgnlOps." + method + "(($w) (" + operand + ")))";
    }

    // Uncast, for an operand whose runtime value can change the result class, such as a null Character
    protected String toOpsUnarySourceString(String method, C context, Object target) {
        markOpsSource();
        String operand = OgnlRuntime.getChildSource(context, target, children[0]);
        try {
            reportOpsResult(getValueBody(context, target), context);
        } catch (OgnlException e) {
            throw OgnlOps.castToRuntime(e);
        }
        return "ognl.OgnlOps." + method + "(($w) (" + operand + "))";
    }

    private void markOpsSource() {
        // Set before the throw below, so the setter pass also bails out with UnsupportedCompilationException
        // instead of handing javassist source it rejects with an uncatchable CannotCompileException
        opsSource = true;

        // ASTChain prefixes its first link with the root expression, which no expression source survives
        if (parent instanceof ASTChain && parent.jjtGetChild(0) == this) {
            throw new UnsupportedCompilationException("Can't chain a call on an OgnlOps-delegated expression.");
        }
    }

    @Override
    public String toSetSourceString(C context, Object target) {
        if (opsSource) {
            throw new UnsupportedCompilationException("Can't compile a setter through an OgnlOps-delegated expression.");
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
