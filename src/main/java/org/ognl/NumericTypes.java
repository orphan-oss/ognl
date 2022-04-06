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

/**
 * This interface defines some useful constants for describing the various possible
 * numeric types of OGNL.
 */
public interface NumericTypes {
    // Order does matter here... see the getNumericType methods in ognl.g.

    /**
     * Type tag meaning boolean.
     */
    int BOOL = 0;
    /**
     * Type tag meaning byte.
     */
    int BYTE = 1;
    /**
     * Type tag meaning char.
     */
    int CHAR = 2;
    /**
     * Type tag meaning short.
     */
    int SHORT = 3;
    /**
     * Type tag meaning int.
     */
    int INT = 4;
    /**
     * Type tag meaning long.
     */
    int LONG = 5;
    /**
     * Type tag meaning java.math.BigInteger.
     */
    int BIGINT = 6;
    /**
     * Type tag meaning float.
     */
    int FLOAT = 7;
    /**
     * Type tag meaning double.
     */
    int DOUBLE = 8;
    /**
     * Type tag meaning java.math.BigDecimal.
     */
    int BIGDEC = 9;
    /**
     * Type tag meaning something other than a number.
     */
    int NONNUMERIC = 10;

    /**
     * The smallest type tag that represents reals as opposed to integers.  You can see
     * whether a type tag represents reals or integers by comparing the tag to this
     * constant: all tags less than this constant represent integers, and all tags
     * greater than or equal to this constant represent reals.  Of course, you must also
     * check for NONNUMERIC, which means it is not a number at all.
     */
    int MIN_REAL_TYPE = FLOAT;
}
