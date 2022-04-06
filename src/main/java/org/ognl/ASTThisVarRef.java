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

public class ASTThisVarRef extends ASTVarRef {

    private static final long serialVersionUID = 5225752191165475979L;

    public ASTThisVarRef(int id) {
        super(id);
    }

    public ASTThisVarRef(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        return context.getCurrentObject();
    }

    protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException {
        context.setCurrentObject(value);
    }

    public String toString() {
        return "#this";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Unable to compile this references.");
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Unable to compile this references.");
    }
}
