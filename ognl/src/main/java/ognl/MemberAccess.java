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

import java.lang.reflect.Member;

/**
 * This interface provides a hook for preparing for accessing members
 * of objects.  The Java2 version of this method can allow access
 * to otherwise inaccessible members, such as private fields.
 */
public interface MemberAccess {
    /**
     * Sets the member up for accessibility
     *
     * @param context      the current execution context.
     * @param target       the Object upon which to perform the setup operation.
     * @param member       the Member upon which to perform the setup operation.
     * @param propertyName the property upon which to perform the setup operation.
     * @return the Object representing the original accessibility state of the target prior to the setup operation.
     */
    Object setup(OgnlContext context, Object target, Member member, String propertyName);

    /**
     * Restores the member from the previous setup call.
     *
     * @param context      the current execution context.
     * @param target       the Object upon which to perform the setup operation.
     * @param member       the Member upon which to perform the setup operation.
     * @param propertyName the property upon which to perform the setup operation.
     * @param state        the Object holding the state to restore (target state prior to the setup operation).
     */
    void restore(OgnlContext context, Object target, Member member, String propertyName, Object state);

    /**
     * Returns true if the given member is accessible or can be made accessible
     * by this object.
     *
     * @param context      the current execution context.
     * @param target       the Object to test accessibility for.
     * @param member       the Member to test accessibility for.
     * @param propertyName the property to test accessibility for.
     * @return true if the target/member/propertyName is accessible in the context, false otherwise.
     */
    boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName);
}
