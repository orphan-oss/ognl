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

public class ASTInstanceof extends SimpleNode implements NodeType {

    private static final long serialVersionUID = 4988707282372901939L;

    private String targetType;

    public ASTInstanceof(int id) {
        super(id);
    }

    public ASTInstanceof(OgnlParser p, int id) {
        super(p, id);
    }

    void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException {
        Object value = children[0].getValue(context, source);
        return OgnlRuntime.isInstance(context, value, targetType) ? Boolean.TRUE : Boolean.FALSE;
    }

    public String toString() {
        return children[0] + " instanceof " + targetType;
    }

    public Class<?> getGetterClass() {
        return boolean.class;
    }

    public Class<?> getSetterClass() {
        return null;
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        try {

            String ret;

            if (children[0] instanceof ASTConst)
                ret = ((Boolean) getValueBody(context, target)).toString();
            else
                ret = children[0].toGetSourceString(context, target) + " instanceof " + targetType;

            context.setCurrentType(Boolean.TYPE);

            return ret;

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        return toGetSourceString(context, target);
    }
}
