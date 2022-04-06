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
package org.ognl;

import org.ognl.enhance.UnsupportedCompilationException;

/**
 * Base class for types that compare values.
 */
public abstract class ComparisonExpression extends BooleanExpression {

    private static final long serialVersionUID = -687171907698242382L;

    public ComparisonExpression(int id) {
        super(id);
    }

    public ComparisonExpression(OgnlParser p, int id) {
        super(p, id);
    }

    public abstract String getComparisonFunction();

    public String toGetSourceString(OgnlContext context, Object target) {
        if (target == null)
            throw new UnsupportedCompilationException("Current target is null, can't compile.");

        try {

            Object value = getValueBody(context, target);

            if (value != null && Boolean.class.isAssignableFrom(value.getClass()))
                getterClass = Boolean.TYPE;
            else if (value != null)
                getterClass = value.getClass();
            else
                getterClass = Boolean.TYPE;

            // iterate over children to make numeric type detection work properly

            OgnlRuntime.getChildSource(context, target, children[0]);
            OgnlRuntime.getChildSource(context, target, children[1]);

//            System.out.println("comparison expression currentType: " + context.getCurrentType() + " previousType: " + context.getPreviousType());

            boolean conversion = OgnlRuntime.shouldConvertNumericTypes(context);

            String result = conversion ? "(" + getComparisonFunction() + "( ($w) (" : "(";

            result += OgnlRuntime.getChildSource(context, target, children[0])
                    + " "
                    + (conversion ? "), ($w) " : getExpressionOperator(0)) + " "
                    + OgnlRuntime.getChildSource(context, target, children[1]);

            result += conversion ? ")" : "";

            context.setCurrentType(Boolean.TYPE);

            result += ")";

            return result;
        } catch (NullPointerException e) {

            // expected to happen in some instances

            throw new UnsupportedCompilationException("evaluation resulted in null expression.");
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }
}
