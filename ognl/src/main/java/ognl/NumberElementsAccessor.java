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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Implementation of ElementsAccessor that returns an iterator over integers from 0 up to
 * the given target.
 */
public class NumberElementsAccessor implements ElementsAccessor, NumericTypes {

    public Enumeration<?> getElements(final Object target) {
        return new Enumeration<>() {
            private final int type = OgnlOps.getNumericType(target);
            private final long finish = OgnlOps.longValue(target);
            private long next = 0;

            public boolean hasMoreElements() {
                return next < finish;
            }

            public Object nextElement() {
                if (next >= finish)
                    throw new NoSuchElementException();
                return OgnlOps.newInteger(type, next++);
            }
        };
    }
}
