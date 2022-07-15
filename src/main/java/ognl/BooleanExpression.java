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
import ognl.enhance.UnsupportedCompilationException;

/**
 * Base class for boolean expressions.
 */
public abstract class BooleanExpression extends ExpressionNode implements NodeType {

    private static final long serialVersionUID = 8933433183011657435L;

    protected Class<?> getterClass;

    public BooleanExpression(int id) {
        super(id);
    }

    public BooleanExpression(OgnlParser p, int id) {
        super(p, id);
    }

    public Class<?> getGetterClass() {
        return getterClass;
    }

    public Class<?> getSetterClass() {
        return null;
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        try {
            Object value = getValueBody(context, target);

            if (value != null && Boolean.class.isAssignableFrom(value.getClass())) {
                getterClass = Boolean.TYPE;
            } else if (value != null) {
                getterClass = value.getClass();
            } else {
                getterClass = Boolean.TYPE;
            }

            String ret = super.toGetSourceString(context, target);

            if ("(false)".equals(ret)) {
                return "false";
            } else if ("(true)".equals(ret)) {
                return "true";
            }

            return ret;

        } catch (NullPointerException e) {
            // expected to happen in some instances
            e.printStackTrace();
            throw new UnsupportedCompilationException("evaluation resulted in null expression.");
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }

}
