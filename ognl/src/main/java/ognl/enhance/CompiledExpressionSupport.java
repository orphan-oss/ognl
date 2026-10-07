/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * and/or LICENSE file distributed with this work for additional
 * information regarding copyright ownership.  The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
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
package ognl.enhance;

import ognl.ASTChain;
import ognl.ASTConst;
import ognl.ASTCtor;
import ognl.ASTList;
import ognl.ASTProperty;
import ognl.ASTRootVarRef;
import ognl.ASTStaticField;
import ognl.ASTStaticMethod;
import ognl.ASTVarRef;
import ognl.ExpressionNode;
import ognl.Node;
import ognl.OgnlContext;
import ognl.OgnlRuntime;

/**
 * Backend-neutral helpers for building the java source strings that compiled expressions are generated from.
 */
public final class CompiledExpressionSupport {

    /**
     * Key used to store any java source string casting statements in the {@link OgnlContext} during
     * class compilation.
     */
    public static final String PRE_CAST = "_preCast";

    private CompiledExpressionSupport() {
    }


    /**
     * Used by {@link OgnlExpressionCompiler#castExpression(OgnlContext, Node, String)} to store the cast java
     * source string in to the current {@link OgnlContext}. This will either add to the existing
     * string present if it already exists or create a new instance and store it using the static key
     * of {@link #PRE_CAST}.
     *
     * @param context The current execution context.
     * @param cast    The java source string to store in to the context.
     */
    public static <C extends OgnlContext<C>> void addCastString(C context, String cast) {
        String value = (String) context.get(PRE_CAST);

        if (value != null)
            value = cast + value;
        else
            value = cast;

        context.put(PRE_CAST, value);
    }


    /**
     * Returns the appropriate casting expression (minus parens) for the specified class type.
     *
     * <p>
     * For instance, if given an {@link Integer} object the string <code>"java.lang.Integer"</code>
     * would be returned. For an array of primitive ints <code>"int[]"</code> and so on..
     * </p>
     *
     * @param type The class to cast a string expression for.
     * @return The converted raw string version of the class name.
     */
    public static String getCastString(Class<?> type) {
        if (type == null)
            return null;

        return type.isArray() ? type.getComponentType().getName() + "[]" : type.getName();
    }

    /**
     * Convenience method called by many different property/method resolving AST types to get a root expression
     * resolving string for the given node.  The callers are mostly ignorant and rely on this method to properly
     * determine if the expression should be cast at all and take the appropriate actions if it should.
     *
     * @param expression The node to check and generate a root expression to if necessary.
     * @param root       The root object for this execution.
     * @param context    The current execution context.
     * @return Either an empty string or a root path java source string compatible with javassist compilations
     * from the root object up to the specified {@link Node}.
     */
    public static <C extends OgnlContext<C>> String getRootExpression(Node<C> expression, Object root, C context) {
        String rootExpr = "";

        if (!shouldCast(expression))
            return rootExpr;

        if ((!(expression instanceof ASTList)
                && !(expression instanceof ASTVarRef)
                && !(expression instanceof ASTStaticMethod)
                && !(expression instanceof ASTStaticField)
                && !(expression instanceof ASTConst)
                && !(expression instanceof ExpressionNode)
                && !(expression instanceof ASTCtor)
                && root != null)
                ||
                (root != null && expression instanceof ASTRootVarRef)) {

            Class<?> castClass = OgnlRuntime.getCompiler().getRootExpressionClass(expression, context);

            if (castClass.isArray() || expression instanceof ASTRootVarRef) {
                rootExpr = "((" + getCastString(castClass) + ")$2)";

                if (expression instanceof ASTProperty && !((ASTProperty<C>) expression).isIndexedAccess()) {
                    rootExpr += ".";
                }
            } else if ((expression instanceof ASTProperty && ((ASTProperty<C>) expression).isIndexedAccess()) || expression instanceof ASTChain) {
                rootExpr = "((" + getCastString(castClass) + ")$2)";
            } else {
                rootExpr = "((" + getCastString(castClass) + ")$2).";
            }
        }

        return rootExpr;
    }

    /**
     * Used by {@link #getRootExpression(Node, Object, OgnlContext)} to determine if the expression
     * needs to be cast at all.
     *
     * @param expression The node to check against.
     * @return Yes if the node type should be cast - false otherwise.
     */
    public static <C extends OgnlContext<C>> boolean shouldCast(Node<C> expression) {
        if (expression instanceof ASTChain) {
            Node<C> child = expression.jjtGetChild(0);
            if (child instanceof ASTConst
                    || child instanceof ASTStaticMethod
                    || child instanceof ASTStaticField
                    || (child instanceof ASTVarRef && !(child instanceof ASTRootVarRef)))
                return false;
        }

        return !(expression instanceof ASTConst);
    }
}
