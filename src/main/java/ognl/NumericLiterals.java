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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Numeric primitive literal string expressions.
 */
class NumericLiterals {

    private final Map<Class<? extends Number>, String> NUMERIC_LITERALS = new HashMap<>(10);

    NumericLiterals() {
        NUMERIC_LITERALS.put(Integer.class, "");
        NUMERIC_LITERALS.put(Integer.TYPE, "");
        NUMERIC_LITERALS.put(Long.class, "l");
        NUMERIC_LITERALS.put(Long.TYPE, "l");
        NUMERIC_LITERALS.put(Float.class, "f");
        NUMERIC_LITERALS.put(Float.TYPE, "f");
        NUMERIC_LITERALS.put(Double.class, "d");
        NUMERIC_LITERALS.put(Double.TYPE, "d");

        NUMERIC_LITERALS.put(BigInteger.class, "d");
        NUMERIC_LITERALS.put(BigDecimal.class, "d");
    }

    String get(Class<? extends Number> clazz) {
        return NUMERIC_LITERALS.get(clazz);
    }

}
