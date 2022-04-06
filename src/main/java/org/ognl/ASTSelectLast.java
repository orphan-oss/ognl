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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ASTSelectLast extends SimpleNode {

    private static final long serialVersionUID = 2555624711061241877L;

    public ASTSelectLast(int id) {
        super(id);
    }

    public ASTSelectLast(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Node expr = children[0];
        List<Object> answer = new ArrayList<>();
        ElementsAccessor elementsAccessor = OgnlRuntime.getElementsAccessor(OgnlRuntime.getTargetClass(source));

        for (Enumeration<?> e = elementsAccessor.getElements(source); e.hasMoreElements(); ) {
            Object next = e.nextElement();

            if (OgnlOps.booleanValue(expr.getValue(context, next))) {
                answer.clear();
                answer.add(next);
            }
        }
        return answer;
    }

    public String toString() {
        return "{$ " + children[0] + " }";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Eval expressions not supported as native java yet.");
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Eval expressions not supported as native java yet.");
    }
}
