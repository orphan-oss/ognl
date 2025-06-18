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

import java.util.LinkedHashMap;
import java.util.Map;

public class ASTMap extends SimpleNode {

    private static final long serialVersionUID = -849999202189860682L;

    private String className;

    public ASTMap(int id) {
        super(id);
    }

    public ASTMap(OgnlParser p, int id) {
        super(p, id);
    }

    protected void setClassName(String value) {
        className = value;
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException {
        Map<Object, Object> answer;

        if (className == null) {
            answer = new LinkedHashMap<>();
        } else {
            try {
                answer = (Map<Object, Object>) OgnlRuntime.classForName(context, className).newInstance();
            } catch (Exception ex) {
                throw new OgnlException("Map implementor '" + className + "' not found", ex);
            }
        }

        for (int i = 0; i < jjtGetNumChildren(); ++i) {
            ASTKeyValue kv = (ASTKeyValue) children[i];
            Node k = kv.getKey(), v = kv.getValue();

            answer.put(k.getValue(context, source), (v == null) ? null : v.getValue(context, source));
        }

        return answer;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("#");

        if (className != null) {
            result.append("@").append(className).append("@");
        }

        result.append("{ ");
        for (int i = 0; i < jjtGetNumChildren(); ++i) {
            ASTKeyValue kv = (ASTKeyValue) children[i];

            if (i > 0) {
                result.append(", ");
            }
            result.append(kv.getKey()).append(" : ").append(kv.getValue());
        }
        return result + " }";
    }

    public String toGetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Map expressions not supported as native java yet.");
    }

    public String toSetSourceString(OgnlContext context, Object target) {
        throw new UnsupportedCompilationException("Map expressions not supported as native java yet.");
    }
}
