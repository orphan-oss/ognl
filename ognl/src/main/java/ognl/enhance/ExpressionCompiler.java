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

import ognl.Node;
import ognl.OgnlContext;

/**
 * @deprecated Use {@link JavassistExpressionCompiler} to extend the Javassist backend and
 * {@link CompiledExpressionSupport} for the static helpers.
 */
@Deprecated(forRemoval = true)
public class ExpressionCompiler<C extends OgnlContext<C>> extends JavassistExpressionCompiler<C> {

    public static final String PRE_CAST = CompiledExpressionSupport.PRE_CAST;

    public static <C extends OgnlContext<C>> void addCastString(C context, String cast) {
        CompiledExpressionSupport.addCastString(context, cast);
    }

    public static String getCastString(Class<?> type) {
        return CompiledExpressionSupport.getCastString(type);
    }

    public static <C extends OgnlContext<C>> String getRootExpression(Node<C> expression, Object root, C context) {
        return CompiledExpressionSupport.getRootExpression(expression, root, context);
    }

    public static <C extends OgnlContext<C>> boolean shouldCast(Node<C> expression) {
        return CompiledExpressionSupport.shouldCast(expression);
    }
}
