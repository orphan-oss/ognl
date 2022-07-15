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

/**
 * Implementation of ElementsAccessor that returns a single-element iterator, containing
 * the original target object.
 */
public class ObjectElementsAccessor implements ElementsAccessor {

    public Enumeration<?> getElements(Object target) {

        final Object object = target;

        return new Enumeration<Object>() {
            private boolean seen = false;

            public boolean hasMoreElements() {
                return !seen;
            }

            public Object nextElement() {
                Object result = null;

                if (!seen) {
                    result = object;
                    seen = true;
                }
                return result;
            }
        };
    }
}
