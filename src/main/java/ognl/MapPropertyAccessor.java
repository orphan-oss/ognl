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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of PropertyAccessor that sets and gets properties by storing and looking up values
 * in Maps.
 */
public class MapPropertyAccessor<C extends OgnlContext<C>> implements PropertyAccessor<C> {

    public Object getProperty(C context, Object target, Object name) throws OgnlException {
        Object result;
        Map<?, ?> map = (Map<?, ?>) target;
        Node currentNode = context.getCurrentNode().jjtGetParent();
        boolean indexedAccess = false;

        if (currentNode == null) {
            throw new OgnlException("node is null for '" + name + "'");
        }
        if (!(currentNode instanceof ASTProperty)) {
            currentNode = currentNode.jjtGetParent();
        }
        if (currentNode instanceof ASTProperty) {
            indexedAccess = ((ASTProperty) currentNode).isIndexedAccess();
        }

        if ((name instanceof String) && !indexedAccess) {
            if (name.equals("size")) {
                result = map.size();
            } else {
                if (name.equals("keys") || name.equals("keySet")) {
                    result = map.keySet();
                } else {
                    if (name.equals("values")) {
                        result = map.values();
                    } else {
                        if (name.equals("isEmpty")) {
                            result = map.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            result = map.get(name);
                        }
                    }
                }
            }
        } else {
            result = map.get(name);
        }

        return result;
    }

    public void setProperty(C context, Object target, Object name, Object value) throws OgnlException {
        Map<Object, Object> map = (Map<Object, Object>) target;
        map.put(name, value);
    }

    public String getSourceAccessor(C context, Object target, Object index) {
        Node currentNode = context.getCurrentNode().jjtGetParent();
        boolean indexedAccess = false;

        if (currentNode == null)
            throw new RuntimeException("node is null for '" + index + "'");

        if (!(currentNode instanceof ASTProperty))
            currentNode = currentNode.jjtGetParent();

        if (currentNode instanceof ASTProperty)
            indexedAccess = ((ASTProperty) currentNode).isIndexedAccess();

        String indexStr = index.toString();

        context.setCurrentAccessor(Map.class);
        context.setCurrentType(Object.class);

        if (index instanceof String && !indexedAccess) {
            String key = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);

            switch (key) {
                case "size":
                    context.setCurrentType(int.class);
                    return ".size()";
                case "keys":
                case "keySet":
                    context.setCurrentType(Set.class);
                    return ".keySet()";
                case "values":
                    context.setCurrentType(Collection.class);
                    return ".values()";
                case "isEmpty":
                    context.setCurrentType(boolean.class);
                    return ".isEmpty()";
            }
        }

        return ".get(" + indexStr + ")";
    }

    public String getSourceSetter(C context, Object target, Object index) {
        context.setCurrentAccessor(Map.class);
        context.setCurrentType(Object.class);

        String indexStr = index.toString();

        if (index instanceof String) {
            String key = (indexStr.indexOf('"') >= 0 ? indexStr.replaceAll("\"", "") : indexStr);

            switch (key) {
                case "size":
                    return "";
                case "keys":
                case "keySet":
                    return "";
                case "values":
                    return "";
                case "isEmpty":
                    return "";
            }
        }

        return ".put(" + indexStr + ", $3)";
    }
}
