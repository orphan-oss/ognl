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

import java.math.BigDecimal;
import java.math.BigInteger;

public class ASTConst extends SimpleNode implements NodeType {

    private static final long serialVersionUID = 6025967712275552286L;

    private Object value;
    private Class<?> getterClass;

    public ASTConst(int id) {
        super(id);
    }

    public ASTConst(OgnlParser p, int id) {
        super(p, id);
    }

    /**
     * Called from parser actions.
     *
     * @param value the Object representing the value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException {
        return this.value;
    }

    public boolean isNodeConstant(OgnlContext context) throws OgnlException {
        return true;
    }

    public Class<?> getGetterClass() {
        if (getterClass == null)
            return null;

        return getterClass;
    }

    public Class<?> getSetterClass() {
        return null;
    }

    public String toString() {
        String result;

        if (value == null) {
            result = "null";
        } else {
            if (value instanceof String) {
                result = '\"' + OgnlOps.getEscapeString(value.toString()) + '\"';
            } else {
                if (value instanceof Character) {
                    result = '\'' + OgnlOps.getEscapedChar((Character) value) + '\'';
                } else {
                    result = value.toString();

                    if (value instanceof Long) {
                        result = result + "L";
                    } else {
                        if (value instanceof BigDecimal) {
                            result = result + "B";
                        } else {
                            if (value instanceof BigInteger) {
                                result = result + "H";
                            } else {
                                if (value instanceof Node) {
                                    result = ":[ " + result + " ]";
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        if (value == null && parent instanceof ExpressionNode) {
            context.setCurrentType(null);
            return "null";
        } else if (value == null) {
            context.setCurrentType(null);
            return "";
        }

        getterClass = value.getClass();

        Object retval;
        if (parent instanceof ASTProperty) {
            context.setCurrentObject(value);

            return value.toString();
        } else if (Number.class.isAssignableFrom(value.getClass())) {
            context.setCurrentType(OgnlRuntime.getPrimitiveWrapperClass(value.getClass()));
            context.setCurrentObject(value);

            return value.toString();
        } else if (!(parent != null && NumericExpression.class.isAssignableFrom(parent.getClass())) && String.class.isAssignableFrom(value.getClass())) {
            context.setCurrentType(String.class);

            retval = '\"' + OgnlOps.getEscapeString(value.toString()) + '\"';

            context.setCurrentObject(retval.toString());

            return retval.toString();
        } else if (value instanceof Character) {
            Character val = (Character) value;

            context.setCurrentType(Character.class);

            if (Character.isLetterOrDigit(val))
                retval = "'" + value + "'";
            else
                retval = "'" + OgnlOps.getEscapedChar((Character) value) + "'";

            context.setCurrentObject(retval);
            return retval.toString();
        }

        if (Boolean.class.isAssignableFrom(value.getClass())) {
            getterClass = Boolean.TYPE;

            context.setCurrentType(Boolean.TYPE);
            context.setCurrentObject(value);

            return value.toString();
        }

        return value.toString();
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        if (parent == null)
            throw new UnsupportedCompilationException("Can't modify constant values.");

        return toGetSourceString(context, target);
    }
}
