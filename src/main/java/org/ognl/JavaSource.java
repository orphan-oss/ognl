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

import org.ognl.enhance.ExpressionAccessor;

/**
 * Defines an object that can return a representation of itself and any objects it contains
 * in the form of a {@link String} embedded with literal java statements.
 *
 * @author jkuhnert
 */
public interface JavaSource {

    /**
     * Expected to return a java source representation of itself such that
     * it could be turned into a literal java expression to be compiled and
     * executed for {@link ExpressionAccessor#get(OgnlContext, Object)} calls.
     *
     * @param context the OgnlContext within which to perform the operation.
     * @param target  the Object from which to retrieve the get source string.
     * @return Literal java string representation of an object get.
     */
    String toGetSourceString(OgnlContext context, Object target);

    /**
     * Expected to return a java source representation of itself such that
     * it could be turned into a literal java expression to be compiled and
     * executed for {@link ExpressionAccessor#get(OgnlContext, Object)} calls.
     *
     * @param context the OgnlContext within which to perform the operation.
     * @param target  the Object from which to retrieve the set source string.
     * @return Literal java string representation of an object set.
     */
    String toSetSourceString(OgnlContext context, Object target);
}
