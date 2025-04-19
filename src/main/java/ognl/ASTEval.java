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

import ognl.enhance.UnsupportedCompilationException;

import java.io.Serial;

public class ASTEval<C extends OgnlContext<C>> extends SimpleNode<C> {

    @Serial
    private static final long serialVersionUID = 1664536168007480363L;

    public ASTEval(int id) {
        super(id);
    }

    public ASTEval(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(C context, Object source)
            throws OgnlException {
        Object result, expr = children[0].getValue(context, source), previousRoot = context.getRoot();

        source = children[1].getValue(context, source);
        @SuppressWarnings("unchecked")
        Node<C> node = (expr instanceof Node) ? (Node<C>) expr : (Node<C>) Ognl.parseExpression(expr.toString());
        try {
            context.setRoot(source);
            result = node.getValue(context, source);
        } finally {
            context.setRoot(previousRoot);
        }
        return result;
    }

    protected void setValueBody(C context, Object target, Object value)
            throws OgnlException {
        Object expr = children[0].getValue(context, target), previousRoot = context.getRoot();

        target = children[1].getValue(context, target);
        @SuppressWarnings("unchecked")
        Node<C> node = (expr instanceof Node) ? (Node<C>) expr : (Node<C>) Ognl.parseExpression(expr.toString());
        try {
            context.setRoot(target);
            node.setValue(context, target, value);
        } finally {
            context.setRoot(previousRoot);
        }
    }

    @Override
    public boolean isEvalChain(C context) throws OgnlException {
        return true;
    }

    public String toString() {
        return "(" + children[0] + ")(" + children[1] + ")";
    }

    public String toGetSourceString(C context, Object target) {
        throw new UnsupportedCompilationException("Eval expressions not supported as native java yet.");
    }

    public String toSetSourceString(C context, Object target) {
        throw new UnsupportedCompilationException("Map expressions not supported as native java yet.");
    }
}
