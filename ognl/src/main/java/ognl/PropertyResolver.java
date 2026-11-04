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

import java.util.List;

/**
 * Decides which members OGNL may use for a property name. Installed globally with
 * {@link OgnlRuntime#setPropertyResolver(PropertyResolver)}.
 * <p>
 * A resolver narrows candidates and never grants access: every candidate is still checked by {@link MemberAccess}.
 * Results are cached per (class, name, kind), so a resolver must be a pure function of its arguments.
 *
 * @see DefaultPropertyResolver
 * @since 3.5.0
 */
public interface PropertyResolver {

    /**
     * @return candidate members in the order OGNL tries them, empty when there are none; never {@code null}
     */
    List<PropertyCandidate> candidates(Class<?> targetClass, String propertyName, PropertyAccessKind kind);
}
