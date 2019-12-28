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
package ognl;

import java.lang.reflect.AccessibleObject;

/**
 * This interface provides a mechanism for indirect reflection access processing
 *   of AccessibleObject instances by OGNL.  It can be used to provide different
 *   behaviour as JDK reflection mechanisms evolve.
 *
 * @since 3.1.24
 */
public abstract interface AccessibleObjectHandler
{
    /**
     * Provides an appropriate implementation to change the accessibility of accessibleObject.
     *
     * @param accessibleObject the AccessibleObject upon which to apply the flag.
     * @param flag the new accessible flag value.
     */
    void setAccessible(AccessibleObject accessibleObject, boolean flag);
}
