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

public class ASTBitOr extends NumericExpression {

    private static final long serialVersionUID = -7692570501162791771L;

    public ASTBitOr(int id) {
        super(id);
    }

    public ASTBitOr(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object result = children[0].getValue(context, source);
        for (int i = 1; i < children.length; ++i)
            result = OgnlOps.binaryOr(result, children[i].getValue(context, source));
        return result;
    }

    public String getExpressionOperator(int index) {
        return "|";
    }
}
