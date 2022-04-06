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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

class PrimitiveDefaults {

    private final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new HashMap<Class<?>, Object>(13);

    PrimitiveDefaults() {
        PRIMITIVE_DEFAULTS.put(Boolean.TYPE, Boolean.FALSE);
        PRIMITIVE_DEFAULTS.put(Boolean.class, Boolean.FALSE);
        PRIMITIVE_DEFAULTS.put(Byte.TYPE, (byte) 0);
        PRIMITIVE_DEFAULTS.put(Byte.class, (byte) 0);
        PRIMITIVE_DEFAULTS.put(Short.TYPE, (short) 0);
        PRIMITIVE_DEFAULTS.put(Short.class, (short) 0);
        PRIMITIVE_DEFAULTS.put(Character.TYPE, (char) 0);
        PRIMITIVE_DEFAULTS.put(Integer.TYPE, 0);
        PRIMITIVE_DEFAULTS.put(Long.TYPE, 0L);
        PRIMITIVE_DEFAULTS.put(Float.TYPE, 0.0f);
        PRIMITIVE_DEFAULTS.put(Double.TYPE, 0.0);
        PRIMITIVE_DEFAULTS.put(BigInteger.class, BigInteger.ZERO);
        PRIMITIVE_DEFAULTS.put(BigDecimal.class, BigDecimal.ZERO);
    }

    Object get(Class<?> cls) {
        return PRIMITIVE_DEFAULTS.get(cls);
    }

}
