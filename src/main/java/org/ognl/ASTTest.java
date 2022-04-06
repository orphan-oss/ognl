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

import org.ognl.enhance.UnsupportedCompilationException;

public class ASTTest extends ExpressionNode {

    private static final long serialVersionUID = -6924826770978283631L;

    public ASTTest(int id) {
        super(id);
    }

    public ASTTest(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object test = children[0].getValue(context, source);
        int branch = OgnlOps.booleanValue(test) ? 1 : 2;
        return children[branch].getValue(context, source);
    }

    protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException {
        Object test = children[0].getValue(context, target);
        int branch = OgnlOps.booleanValue(test) ? 1 : 2;
        children[branch].setValue(context, target, value);
    }

    public String getExpressionOperator(int index) {
        return (index == 1) ? "?" : ":";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        if (target == null)
            throw new UnsupportedCompilationException("evaluation resulted in null expression.");

        if (children.length != 3)
            throw new UnsupportedCompilationException("Can only compile test expressions with two children." + children.length);

        String result = "";

        try {

            String first = OgnlRuntime.getChildSource(context, target, children[0]);
            if (!OgnlRuntime.isBoolean(first) && !context.getCurrentType().isPrimitive())
                first = OgnlRuntime.getCompiler().createLocalReference(context, first, context.getCurrentType());

            if (children[0] instanceof ExpressionNode) {
                first = "(" + first + ")";
            }

            String second = OgnlRuntime.getChildSource(context, target, children[1]);
            Class<?> secondType = context.getCurrentType();

            if (!OgnlRuntime.isBoolean(second) && !context.getCurrentType().isPrimitive())
                second = OgnlRuntime.getCompiler().createLocalReference(context, second, context.getCurrentType());

            if (children[1] instanceof ExpressionNode) {
                second = "(" + second + ")";
            }

            String third = OgnlRuntime.getChildSource(context, target, children[2]);
            Class<?> thirdType = context.getCurrentType();

            if (!OgnlRuntime.isBoolean(third) && !context.getCurrentType().isPrimitive())
                third = OgnlRuntime.getCompiler().createLocalReference(context, third, context.getCurrentType());
            if (children[2] instanceof ExpressionNode) {
                third = "(" + third + ")";
            }

            boolean mismatched = (secondType.isPrimitive() && !thirdType.isPrimitive())
                    || (!secondType.isPrimitive() && thirdType.isPrimitive());

            result += "org.ognl.OgnlOps.booleanValue(" + first + ")";
            result += " ? ";
            result += (mismatched ? " ($w) " : "") + second;
            result += " : ";
            result += (mismatched ? " ($w) " : "") + third;

            context.setCurrentObject(target);
            context.setCurrentType(mismatched ? Object.class : secondType);

            return result;
        } catch (NullPointerException e) {
            // expected to happen in some instances
            throw new UnsupportedCompilationException("evaluation resulted in null expression.");
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }
}
