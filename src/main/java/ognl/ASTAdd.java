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

import java.io.Serial;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ASTAdd<C extends OgnlContext<C>> extends NumericExpression<C> {

    @Serial
    private static final long serialVersionUID = 6299295217841613060L;

    public ASTAdd(int id) {
        super(id);
    }

    public ASTAdd(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody(C context, Object source) throws OgnlException {
        Object result = children[0].getValue(context, source);

        for (int i = 1; i < children.length; ++i)
            result = OgnlOps.add(result, children[i].getValue(context, source));

        return result;
    }

    public String getExpressionOperator(int index) {
        return "+";
    }

    boolean isWider(NodeType type, NodeType lastType) {
        if (lastType == null)
            return true;

        //System.out.println("checking isWider(" + type.getGetterClass() + " , " + lastType.getGetterClass() + ")");

        if (String.class.isAssignableFrom(lastType.getGetterClass()))
            return false;

        if (String.class.isAssignableFrom(type.getGetterClass()))
            return true;

        if (parent != null && String.class.isAssignableFrom(type.getGetterClass()))
            return true;

        if (String.class.isAssignableFrom(lastType.getGetterClass()) && Object.class == type.getGetterClass())
            return false;

        if (parent != null && String.class.isAssignableFrom(lastType.getGetterClass()))
            return false;
        else if (parent == null && String.class.isAssignableFrom(lastType.getGetterClass()))
            return true;
        else if (parent == null && String.class.isAssignableFrom(type.getGetterClass()))
            return false;

        if (BigDecimal.class.isAssignableFrom(type.getGetterClass())
                || BigInteger.class.isAssignableFrom(type.getGetterClass()))
            return true;

        if (BigDecimal.class.isAssignableFrom(lastType.getGetterClass())
                || BigInteger.class.isAssignableFrom(lastType.getGetterClass()))
            return false;

        if (Double.class.isAssignableFrom(type.getGetterClass()))
            return true;

        if (Integer.class.isAssignableFrom(type.getGetterClass())
                && Double.class.isAssignableFrom(lastType.getGetterClass()))
            return false;

        if (Float.class.isAssignableFrom(type.getGetterClass())
                && Integer.class.isAssignableFrom(lastType.getGetterClass()))
            return true;

        return true;
    }

    public String toGetSourceString(C context, Object target) {
        try {
            StringBuilder result = new StringBuilder();
            NodeType lastType = null;

            // go through once to determine the ultimate type

            if ((children != null) && (children.length > 0)) {
                Class<?> currType = context.getCurrentType();
                Class<?> currAccessor = context.getCurrentAccessor();

                Object cast = context.get(ExpressionCompiler.PRE_CAST);

                for (Node<C> child : children) {
                    child.toGetSourceString(context, target);

                    if (child instanceof NodeType
                            && ((NodeType) child).getGetterClass() != null
                            && isWider((NodeType) child, lastType)) {
                        lastType = (NodeType) child;
                    }
                }

                context.put(ExpressionCompiler.PRE_CAST, cast);

                context.setCurrentType(currType);
                context.setCurrentAccessor(currAccessor);
            }

            // reset context since previous children loop would have changed it

            context.setCurrentObject(target);

            if ((children != null) && (children.length > 0)) {

                for (int i = 0; i < children.length; ++i) {
                    if (i > 0)
                        result.append(" ").append(getExpressionOperator(i)).append(" ");

                    String expr = children[i].toGetSourceString(context, target);

                    if (("null".equals(expr))
                            || (!(children[i] instanceof ASTConst)
                            && (expr == null || expr.trim().length() <= 0))) {
                        expr = "null";
                    }

                    //System.out.println("astadd child class: " + _children[i].getClass().getName() + " and return expr: " + expr);

                    if (children[i] instanceof ASTProperty) {
                        expr = ExpressionCompiler.getRootExpression(children[i], context.getRoot(), context) + expr;
                        context.setCurrentAccessor(context.getRoot().getClass());
                    } else if (children[i] instanceof ASTMethod) {
                        String chain = (String) context.get("_currentChain");
                        String rootExpr = ExpressionCompiler.getRootExpression(children[i], context.getRoot(), context);

                        //System.out.println("astadd chains is >>" + chain + "<< and rootExpr is >>" + rootExpr + "<<");

                        // dirty fix for overly aggressive casting dot operations
                        if (rootExpr.endsWith(".") && chain != null && chain.startsWith(").")) {
                            chain = chain.substring(1);
                        }

                        expr = rootExpr + (chain != null ? chain + "." : "") + expr;
                        context.setCurrentAccessor(context.getRoot().getClass());

                    } else if (children[i] instanceof ExpressionNode) {
                        expr = "(" + expr + ")";
                    } else if ((!(parent instanceof ASTChain))
                            && children[i] instanceof ASTChain) {
                        String rootExpr = ExpressionCompiler.getRootExpression(children[i], context.getRoot(), context);

                        if (!(children[i].jjtGetChild(0) instanceof ASTProperty)
                                && rootExpr.endsWith(")") && expr.startsWith(")"))
                            expr = expr.substring(1);

                        expr = rootExpr + expr;
                        context.setCurrentAccessor(context.getRoot().getClass());

                        String cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                        if (cast == null)
                            cast = "";

                        expr = cast + expr;
                    }

                    // turn quoted characters into quoted strings

                    if (context.getCurrentType() != null && context.getCurrentType() == Character.class
                            && children[i] instanceof ASTConst) {
                        if (expr.indexOf('\'') >= 0)
                            expr = expr.replaceAll("'", "\"");
                        context.setCurrentType(String.class);
                    } else {

                        if (!ASTVarRef.class.isAssignableFrom(children[i].getClass())
                                && !(children[i] instanceof ASTProperty)
                                && !(children[i] instanceof ASTMethod)
                                && !(children[i] instanceof ASTSequence)
                                && !(children[i] instanceof ASTChain)
                                && !NumericExpression.class.isAssignableFrom(children[i].getClass())
                                && !(children[i] instanceof ASTStaticField)
                                && !(children[i] instanceof ASTStaticMethod)
                                && !(children[i] instanceof ASTTest)) {
                            if (lastType != null && String.class.isAssignableFrom(lastType.getGetterClass())) {
                                //System.out.println("Input expr >>" + expr + "<<");
                                if (expr.contains("&quot;"))
                                    expr = expr.replaceAll("&quot;", "\"");
                                if (expr.indexOf('"') >= 0)
                                    expr = expr.replaceAll("\"", "'");
                                expr = "\"" + expr + "\"";
                                //System.out.println("Expr now >>" + expr + "<<");
                            }
                        }
                    }

                    result.append(expr);

                    // hanlde addition for numeric types when applicable or just string concatenation

                    if ((lastType == null || !String.class.isAssignableFrom(lastType.getGetterClass()))
                            && !ASTConst.class.isAssignableFrom(children[i].getClass())
                            && !NumericExpression.class.isAssignableFrom(children[i].getClass())) {
                        if (context.getCurrentType() != null && Number.class.isAssignableFrom(context.getCurrentType())
                                && !(children[i] instanceof ASTMethod)) {
                            if (children[i] instanceof ASTVarRef
                                    || children[i] instanceof ASTProperty
                                    || children[i] instanceof ASTChain)
                                result.append(".");

                            result.append(OgnlRuntime.getNumericValueGetter(context.getCurrentType()));
                            context.setCurrentType(OgnlRuntime.getPrimitiveWrapperClass(context.getCurrentType()));
                        }
                    }

                    if (lastType != null)
                        context.setCurrentAccessor(lastType.getGetterClass());
                }
            }

            if (parent == null || ASTSequence.class.isAssignableFrom(parent.getClass())) {
                if (getterClass != null && String.class.isAssignableFrom(getterClass))
                    getterClass = Object.class;
            } else {
                context.setCurrentType(getterClass);
            }

            try {
                Object contextObj = getValueBody(context, target);
                context.setCurrentObject(contextObj);
            } catch (Throwable t) {
                throw OgnlOps.castToRuntime(t);
            }

            return result.toString();

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }
}
