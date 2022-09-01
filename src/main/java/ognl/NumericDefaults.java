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

class NumericDefaults {

    private final Map<Class<?>, Object> NUMERIC_DEFAULTS = new HashMap<>(10);

    NumericDefaults() {
        NUMERIC_DEFAULTS.put(Boolean.class, Boolean.FALSE);
        NUMERIC_DEFAULTS.put(Byte.class, (byte) 0);
        NUMERIC_DEFAULTS.put(Short.class, (short) 0);
        NUMERIC_DEFAULTS.put(Character.class, (char) 0);
        NUMERIC_DEFAULTS.put(Integer.class, 0);
        NUMERIC_DEFAULTS.put(Long.class, 0L);
        NUMERIC_DEFAULTS.put(Float.class, 0.0f);
        NUMERIC_DEFAULTS.put(Double.class, 0.0);

        NUMERIC_DEFAULTS.put(BigInteger.class, BigInteger.ZERO);
        NUMERIC_DEFAULTS.put(BigDecimal.class, BigDecimal.ZERO);
    }

    Object get(Class<?> cls) {
        return NUMERIC_DEFAULTS.get(cls);
    }

}
