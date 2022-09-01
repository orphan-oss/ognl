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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASTList extends SimpleNode implements NodeType {

    private static final long serialVersionUID = 5819304155523588899L;

    public ASTList(int id) {
        super(id);
    }

    public ASTList(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException {
        List<Object> answer = new ArrayList<>(jjtGetNumChildren());
        for (int i = 0; i < jjtGetNumChildren(); ++i) {
            answer.add(children[i].getValue(context, source));
        }
        return answer;
    }

    public Class<?> getGetterClass() {
        return null;
    }

    public Class<?> getSetterClass() {
        return null;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("{ ");

        for (int i = 0; i < jjtGetNumChildren(); ++i) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(children[i].toString());
        }
        return result + " }";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        StringBuilder result = new StringBuilder();
        boolean array = parent instanceof ASTCtor && ((ASTCtor) parent).isArray();

        context.setCurrentType(List.class);
        context.setCurrentAccessor(List.class);

        if (!array) {
            if (jjtGetNumChildren() < 1)
                return "java.util.Arrays.asList( new Object[0])";

            result.append("java.util.Arrays.asList( new Object[] ");
        }

        result.append("{ ");

        try {

            for (int i = 0; i < jjtGetNumChildren(); ++i) {
                if (i > 0) {
                    result.append(", ");
                }

                Class<?> prevType = context.getCurrentType();

                Object objValue = children[i].getValue(context, context.getRoot());
                String value = children[i].toGetSourceString(context, target);

                // to undo type setting of constants when used as method parameters
                if (children[i] instanceof ASTConst) {

                    context.setCurrentType(prevType);
                }

                value = ExpressionCompiler.getRootExpression(children[i], target, context) + value;

                String cast = "";
                if (ExpressionCompiler.shouldCast(children[i])) {

                    cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
                }
                if (cast == null)
                    cast = "";

                if (!(children[i] instanceof ASTConst))
                    value = cast + value;

                Class<?> ctorClass = (Class<?>) context.get("_ctorClass");
                if (array && ctorClass != null && !ctorClass.isPrimitive()) {

                    Class<?> valueClass = value.getClass();
                    if (NodeType.class.isAssignableFrom(children[i].getClass()))
                        valueClass = ((NodeType) children[i]).getGetterClass();

                    if (valueClass != null && ctorClass.isArray()) {

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                "(" + ExpressionCompiler.getCastString(ctorClass)
                                        + ")ognl.OgnlOps.toArray(" + value + ", " + ctorClass.getComponentType().getName()
                                        + ".class, true)",
                                ctorClass
                        );

                    } else if (ctorClass != Object.class) {
                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                "(" + ctorClass.getName() + ")ognl.OgnlOps.convertValue(" + value + "," + ctorClass.getName() + ".class)",
                                ctorClass
                        );
                    } else if ((children[i] instanceof NodeType
                            && ((NodeType) children[i]).getGetterClass() != null
                            && Number.class.isAssignableFrom(((NodeType) children[i]).getGetterClass()))
                            || Objects.requireNonNull(valueClass).isPrimitive()) {
                        value = " ($w) (" + value + ")";
                    }

                } else if (ctorClass == null || !ctorClass.isPrimitive()) {

                    value = " ($w) (" + value + ")";
                }

                if (objValue == null || value.length() <= 0)
                    value = "null";

                result.append(value);
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        context.setCurrentType(List.class);
        context.setCurrentAccessor(List.class);

        result.append("}");

        if (!array)
            result.append(")");

        return result.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Can't generate setter for ASTList.");
    }
}
