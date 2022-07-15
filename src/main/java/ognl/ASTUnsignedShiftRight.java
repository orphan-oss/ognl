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

public class ASTUnsignedShiftRight extends NumericExpression {

    private static final long serialVersionUID = 7787910329305946213L;

    public ASTUnsignedShiftRight(int id) {
        super(id);
    }

    public ASTUnsignedShiftRight(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object v1 = children[0].getValue(context, source);
        Object v2 = children[1].getValue(context, source);
        return OgnlOps.unsignedShiftRight(v1, v2);
    }

    public String getExpressionOperator(int index) {
        return ">>>";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        String result;

        try {
            String child1 = OgnlRuntime.getChildSource(context, target, children[0]);
            child1 = coerceToNumeric(child1, context, children[0]);

            String child2 = OgnlRuntime.getChildSource(context, target, children[1]);
            child2 = coerceToNumeric(child2, context, children[1]);

            Object v1 = children[0].getValue(context, target);
            int type = OgnlOps.getNumericType(v1);

            if (type <= OgnlOps.INT) {
                child1 = "(int)" + child1;
                child2 = "(int)" + child2;
            }

            result = child1 + " >>> " + child2;

            context.setCurrentType(Integer.TYPE);
            context.setCurrentObject(getValueBody(context, target));
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return result;
    }
}
