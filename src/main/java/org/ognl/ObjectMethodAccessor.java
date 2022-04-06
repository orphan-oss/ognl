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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that uses reflection on the target object's class to find a
 * field or a pair of set/get methods with the given property name.
 */
public class ObjectMethodAccessor implements MethodAccessor {

    public Object callStaticMethod(OgnlContext context, Class<?> targetClass, String methodName, Object[] args) throws MethodFailedException {
        List<Method> methods = OgnlRuntime.getMethods(targetClass, methodName, true);

        return OgnlRuntime.callAppropriateMethod(context, targetClass, null, methodName, null, methods, args);
    }

    public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) throws MethodFailedException {
        Class<?> targetClass = (target == null) ? null : target.getClass();
        List<Method> methods = OgnlRuntime.getMethods(targetClass, methodName, false);

        if ((methods == null) || (methods.size() == 0)) {
            methods = OgnlRuntime.getMethods(targetClass, methodName, true);
        }

        return OgnlRuntime.callAppropriateMethod(context, target, target, methodName, null, methods, args);
    }
}
