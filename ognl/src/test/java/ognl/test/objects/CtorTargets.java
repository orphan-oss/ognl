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
package ognl.test.objects;

import java.util.Arrays;

public class CtorTargets {

    public static class WithChar {
        private final char value;

        public WithChar(char value) {
            this.value = value;
        }

        public boolean equals(Object o) {
            return o instanceof WithChar && ((WithChar) o).value == value;
        }

        public int hashCode() {
            return value;
        }

        public String toString() {
            return "WithChar(" + (int) value + ")";
        }
    }

    public static class WithObjectArray {
        private final Object[] values;

        public WithObjectArray(Object[] values) {
            this.values = values;
        }

        public boolean equals(Object o) {
            return o instanceof WithObjectArray && Arrays.equals(((WithObjectArray) o).values, values);
        }

        public int hashCode() {
            return Arrays.hashCode(values);
        }

        public String toString() {
            return "WithObjectArray" + Arrays.toString(values);
        }
    }

    public static class WithCharArray {
        private final char[] values;

        public WithCharArray(char[] values) {
            this.values = values;
        }

        public boolean equals(Object o) {
            return o instanceof WithCharArray && Arrays.equals(((WithCharArray) o).values, values);
        }

        public int hashCode() {
            return Arrays.hashCode(values);
        }

        public String toString() {
            return "WithCharArray(" + new String(values) + ")";
        }
    }
}
