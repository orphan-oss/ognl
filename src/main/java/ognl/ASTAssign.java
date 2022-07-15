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
import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;

public class ASTAssign extends SimpleNode {

    private static final long serialVersionUID = -2036484456563256284L;

    public ASTAssign(int id) {
        super(id);
    }

    public ASTAssign(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object result = children[1].getValue(context, source);
        children[0].setValue(context, source, result);
        return result;
    }

    public String toString() {
        return children[0] + " = " + children[1];
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        String result = "";

        String first = children[0].toGetSourceString(context, target);
        String second = "";

        if (children[1] instanceof ASTProperty) {
            second += "((" + OgnlRuntime.getCompiler().getClassName(target.getClass()) + ")$2).";
        }

        second += children[1].toGetSourceString(context, target);

        if (ASTSequence.class.isAssignableFrom(children[1].getClass())) {
            ASTSequence seq = (ASTSequence) children[1];

            context.setCurrentType(Object.class);

            String core = seq.getCoreExpression();
            if (core.endsWith(";"))
                core = core.substring(0, core.lastIndexOf(";"));

            second = OgnlRuntime.getCompiler().createLocalReference(context,
                    "ognl.OgnlOps.returnValue(($w)" + core + ", ($w) " + seq.getLastExpression() + ")",
                    Object.class);
        }

        if (children[1] instanceof NodeType
                && !(children[1] instanceof ASTProperty)
                && ((NodeType) children[1]).getGetterClass() != null && !(children[1] instanceof OrderedReturn)) {

            second = "new " + ((NodeType) children[1]).getGetterClass().getName() + "(" + second + ")";
        }

        if (OrderedReturn.class.isAssignableFrom(children[0].getClass())
                && ((OrderedReturn) children[0]).getCoreExpression() != null) {
            context.setCurrentType(Object.class);

            result = first + second + ")";

            // System.out.println("building ordered ret from child[0] with result of:" + result);

            result = OgnlRuntime.getCompiler().createLocalReference(context,
                    "ognl.OgnlOps.returnValue(($w)" + result + ", ($w)" + ((OrderedReturn) children[0]).getLastExpression() + ")",
                    Object.class);
        }

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        String result = "";

        result += children[0].toSetSourceString(context, target);

        if (children[1] instanceof ASTProperty) {
            result += "((" + OgnlRuntime.getCompiler().getClassName(target.getClass()) + ")$2).";
        }

        String value = children[1].toSetSourceString(context, target);

        if (value == null)
            throw new UnsupportedCompilationException("Value for assignment is null, can't enhance statement to bytecode.");

        if (ASTSequence.class.isAssignableFrom(children[1].getClass())) {
            ASTSequence seq = (ASTSequence) children[1];
            result = seq.getCoreExpression() + result;
            value = seq.getLastExpression();
        }

        if (children[1] instanceof NodeType
                && !(children[1] instanceof ASTProperty)
                && ((NodeType) children[1]).getGetterClass() != null) {

            value = "new " + ((NodeType) children[1]).getGetterClass().getName() + "(" + value + ")";
        }

        return result + value + ")";
    }

    @Override
    public boolean isOperation(OgnlContext context) {
        return true;
    }
}
