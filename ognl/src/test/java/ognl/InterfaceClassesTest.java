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

import ognl.enhance.ExpressionCompiler;
import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterfaceClassesTest {

    @Test
    void defaultsMatchTheCompiler() {
        ExpressionCompiler<?> compiler = new ExpressionCompiler();
        List<Class<?>> classes = List.of(ArrayList.class, HashMap.class, HashSet.class, ArrayDeque.class,
                new ArrayList<>().iterator().getClass(), int.class, List.class, String.class, Object.class);

        for (Class<?> clazz : classes) {
            assertEquals(compiler.getInterfaceClass(clazz), InterfaceClasses.of(clazz), clazz.getName());
        }
    }

    @Test
    void abstractListIteratorIsAnIterator() {
        List<String> list = new AbstractList<>() {
            @Override
            public String get(int index) {
                return "a";
            }

            @Override
            public int size() {
                return 1;
            }
        };
        Class<?> itr = list.iterator().getClass();

        assertEquals("java.util.AbstractList$Itr", itr.getName());
        assertEquals(Iterator.class, InterfaceClasses.of(itr));
    }

    @Test
    void knownAnswers() {
        assertEquals(List.class, InterfaceClasses.of(ArrayList.class));
        assertEquals(Map.class, InterfaceClasses.of(HashMap.class));
        assertEquals(Set.class, InterfaceClasses.of(HashSet.class));
        assertEquals(Collection.class, InterfaceClasses.of(ArrayDeque.class));
        assertEquals(int.class, InterfaceClasses.of(int.class));
        assertEquals(String.class, InterfaceClasses.of(String.class));
    }
}
