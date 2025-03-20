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
package ognl.test;

import ognl.Ognl;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectionSelectionTest {

    private Root root;

    @BeforeEach
    void setUp() {
        root = new Root();
    }

    @Test
    void testProjectionClass() throws Exception {
        Object actual = Ognl.getValue("array.{class}", root);
        List<Class<Integer>> expected = Arrays.asList(Integer.class, Integer.class, Integer.class, Integer.class);
        assertEquals(expected, actual);
    }

    @Test
    void testSelection() throws Exception {
        Object actual = Ognl.getValue("map.array.{? #this > 2 }", root);
        assertEquals(Arrays.asList(3, 4), actual);

        actual = Ognl.getValue("map.array.{^ #this > 2 }", root);
        assertEquals(List.of(3), actual);

        actual = Ognl.getValue("map.array.{$ #this > 2 }", root);
        assertEquals(List.of(4), actual);

        actual = Ognl.getValue("map.array[*].{?true} instanceof java.util.Collection", root);
        assertEquals(Boolean.TRUE, actual);
    }

    @Test
    void testFactorial() throws Exception {
        Object actual = Ognl.getValue("#fact=1, 30H.{? #fact = #fact * (#this+1), false }, #fact", root);
        assertEquals(new BigInteger("265252859812191058636308480000000"), actual);
    }
}
