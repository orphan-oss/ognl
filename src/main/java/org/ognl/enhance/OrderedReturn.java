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

import org.ognl.Node;

/**
 * Marks an ognl expression {@link Node} as needing to have the return portion of a
 * getter method happen in a specific part of the generated expression vs just having
 * the whole expression returned in one chunk.
 */
public interface OrderedReturn {

    /**
     * Get the core expression to execute first before any return foo logic is started.
     *
     * @return The core standalone expression that shouldn't be pre-pended with a return keyword.
     */
    String getCoreExpression();

    /**
     * Gets the last expression to be pre-pended with a return &lt;expression&gt; block.
     *
     * @return The expression representing the return portion of a statement;
     */
    String getLastExpression();
}
