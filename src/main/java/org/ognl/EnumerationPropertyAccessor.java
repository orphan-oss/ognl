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

import java.util.Enumeration;

/**
 * Implementation of PropertyAccessor that provides "property" reference to
 * "nextElement" (aliases to "next" also) and "hasMoreElements" (also aliased
 * to "hasNext").
 */
public class EnumerationPropertyAccessor extends ObjectPropertyAccessor implements PropertyAccessor {

    public Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException {
        Object result;
        Enumeration<?> e = (Enumeration<?>) target;

        if (name instanceof String) {
            if (name.equals("next") || name.equals("nextElement")) {
                result = e.nextElement();
            } else {
                if (name.equals("hasNext") || name.equals("hasMoreElements")) {
                    result = e.hasMoreElements() ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    result = super.getProperty(context, target, name);
                }
            }
        } else {
            result = super.getProperty(context, target, name);
        }
        return result;
    }

    public void setProperty(OgnlContext context, Object target, Object name, Object value) throws OgnlException {
        throw new IllegalArgumentException("can't set property " + name + " on Enumeration");
    }
}
