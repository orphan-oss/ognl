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

import java.lang.reflect.Array;
import java.util.Enumeration;

/**
 * Implementation of ElementsAccessor that returns an iterator over a Java array.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ArrayElementsAccessor implements ElementsAccessor {
    public Enumeration<?> getElements(final Object target) {
        return new Enumeration<Object>() {
            private final int count = Array.getLength(target);
            private int index = 0;

            public boolean hasMoreElements() {
                return index < count;
            }

            public Object nextElement() {
                return Array.get(target, index++);
            }
        };
    }
}
