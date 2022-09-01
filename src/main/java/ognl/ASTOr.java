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

public class ASTOr extends BooleanExpression {

    private static final long serialVersionUID = -5221348144886054097L;

    public ASTOr(int id) {
        super(id);
    }

    public ASTOr(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object result = null;
        int last = children.length - 1;
        for (int i = 0; i <= last; ++i) {
            result = children[i].getValue(context, source);
            if (i != last && OgnlOps.booleanValue(result))
                break;
        }
        return result;
    }

    protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException {
        int last = children.length - 1;
        for (int i = 0; i < last; ++i) {
            Object v = children[i].getValue(context, target);
            if (OgnlOps.booleanValue(v))
                return;
        }
        children[last].setValue(context, target, value);
    }

    public String getExpressionOperator(int index) {
        return "||";
    }

    public Class<?> getGetterClass() {
        return null;
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        if (children.length != 2)
            throw new UnsupportedCompilationException("Can only compile boolean expressions with two children.");

        String result = "(";

        try {

            String first = OgnlRuntime.getChildSource(context, target, children[0]);
            if (!OgnlRuntime.isBoolean(first))
                first = OgnlRuntime.getCompiler().createLocalReference(context, first, context.getCurrentType());

            Class<?> firstType = context.getCurrentType();

            String second = OgnlRuntime.getChildSource(context, target, children[1]);
            if (!OgnlRuntime.isBoolean(second))
                second = OgnlRuntime.getCompiler().createLocalReference(context, second, context.getCurrentType());

            Class<?> secondType = context.getCurrentType();

            boolean mismatched = (firstType.isPrimitive() && !secondType.isPrimitive())
                    || (!firstType.isPrimitive() && secondType.isPrimitive());

            result += "ognl.OgnlOps.booleanValue(" + first + ")";
            result += " ? ";
            result += (mismatched ? " ($w) " : "") + first;
            result += " : ";
            result += (mismatched ? " ($w) " : "") + second;
            result += ")";

            context.setCurrentObject(target);
            context.setCurrentType(Boolean.TYPE);

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        if (children.length != 2)
            throw new UnsupportedCompilationException("Can only compile boolean expressions with two children.");

        String pre = (String) context.get("_currentChain");
        if (pre == null)
            pre = "";

        String result = "";

        try {

            children[0].getValue(context, target);

            String first = ExpressionCompiler.getRootExpression(children[0], context.getRoot(), context)
                    + pre + children[0].toGetSourceString(context, target);
            if (!OgnlRuntime.isBoolean(first))
                first = OgnlRuntime.getCompiler().createLocalReference(context, first, Object.class);

            children[1].getValue(context, target);

            String second = ExpressionCompiler.getRootExpression(children[1], context.getRoot(), context)
                    + pre + children[1].toSetSourceString(context, target);
            if (!OgnlRuntime.isBoolean(second))
                second = OgnlRuntime.getCompiler().createLocalReference(context, second, context.getCurrentType());

            result += "ognl.OgnlOps.booleanValue(" + first + ")";

            result += " ? ";

            result += first;
            result += " : ";

            result += second;

            context.setCurrentObject(target);

            context.setCurrentType(Boolean.TYPE);

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result;
    }
}
