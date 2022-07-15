/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * and/or LICENSE file distributed with this work for additional
 * information regarding copyright ownership.  The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
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
package ognl.security;

import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;

/**
 * A signature for {@link OgnlSecurityManager#isAccessDenied(java.security.Permission)}. Also executes user methods with not any permission.
 *
 * @since 3.1.24
 */
public class UserMethod implements PrivilegedExceptionAction<Object> {

    private final Object target;
    private final Method method;
    private final Object[] argsArray;

    public UserMethod(Object target, Method method, Object[] argsArray) {
        this.target = target;
        this.method = method;
        this.argsArray = argsArray;
    }

    public Object run() throws Exception {
        return method.invoke(target, argsArray);
    }
}
