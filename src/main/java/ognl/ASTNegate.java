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

public class ASTNegate extends NumericExpression {

    private static final long serialVersionUID = -541105956940549394L;

    public ASTNegate(int id) {
        super(id);
    }

    public ASTNegate(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        return OgnlOps.negate(children[0].getValue(context, source));
    }

    public String toString() {
        return "-" + children[0];
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        String source = children[0].toGetSourceString(context, target);

        if (!(children[0] instanceof ASTNegate)) {
            return "-" + source;
        } else {
            return "-(" + source + ")";
        }
    }

    @Override
    public boolean isOperation(OgnlContext context) throws OgnlException {
        if (children.length == 1) {
            SimpleNode child = (SimpleNode) children[0];
            return child.isOperation(context) || !child.isConstant(context);
        }
        return super.isOperation(context);
    }
}
