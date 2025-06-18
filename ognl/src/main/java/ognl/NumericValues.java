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
 * Constant strings for getting the primitive value of different native types on the generic {@link Number} object
 * interface. (or the less generic BigDecimal/BigInteger types)
 */
class NumericValues {

    private final Map<Class<?>, String> NUMERIC_VALUES = new HashMap<>(9);

    NumericValues() {
        NUMERIC_VALUES.put(Double.class, "doubleValue()");
        NUMERIC_VALUES.put(Float.class, "floatValue()");
        NUMERIC_VALUES.put(Integer.class, "intValue()");
        NUMERIC_VALUES.put(Long.class, "longValue()");
        NUMERIC_VALUES.put(Short.class, "shortValue()");
        NUMERIC_VALUES.put(Byte.class, "byteValue()");
        NUMERIC_VALUES.put(BigDecimal.class, "doubleValue()");
        NUMERIC_VALUES.put(BigInteger.class, "doubleValue()");
        NUMERIC_VALUES.put(Boolean.class, "booleanValue()");
    }

    String get(Class<?> cls) {
        return NUMERIC_VALUES.get(cls);
    }

}
