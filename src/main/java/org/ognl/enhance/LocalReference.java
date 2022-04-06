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
package org.ognl.enhance;

/**
 * Container class for {@link OgnlExpressionCompiler} generated local method
 * block references.
 */
public interface LocalReference {

    /**
     * The name of the assigned variable reference.
     *
     * @return The name of the reference as it will be when compiled.
     */
    String getName();

    /**
     * The expression that sets the value, ie the part after <code>&lt;class type&gt; refName = &lt;expression&gt;</code>.
     *
     * @return The setting expression.
     */
    String getExpression();

    /**
     * The type of reference.
     *
     * @return The type.
     */
    Class<?> getType();
}
