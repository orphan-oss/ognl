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

/**
 * Exception thrown if a property is attempted to be extracted from an object that does
 * not have such a property.*
 */
public class NoSuchPropertyException extends OgnlException {

    private static final long serialVersionUID = -4394252641910997725L;

    private Object target;
    private Object name;

    public NoSuchPropertyException(Object target, Object name) {
        super(getReason(target, name));
    }

    public NoSuchPropertyException(Object target, Object name, Throwable reason) {
        super(getReason(target, name), reason);
        this.target = target;
        this.name = name;
    }

    static String getReason(Object target, Object name) {
        String ret;

        if (target == null)
            ret = "null";
        else if (target instanceof Class)
            ret = ((Class<?>) target).getName();
        else
            ret = target.getClass().getName();

        ret += "." + name;

        return ret;
    }

    public Object getTarget() {
        return target;
    }

    public Object getName() {
        return name;
    }
}
