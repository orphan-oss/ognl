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
package org.ognl.internal.entry;

import org.ognl.OgnlRuntime;
import org.ognl.internal.CacheException;

import java.lang.reflect.Method;

public class MethodPermCacheEntryFactory implements CacheEntryFactory<Method, Boolean> {

    private SecurityManager securityManager;

    public MethodPermCacheEntryFactory(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public Boolean create(Method key) throws CacheException {
        try {
            securityManager.checkPermission(OgnlRuntime.getPermission(key));
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

}
