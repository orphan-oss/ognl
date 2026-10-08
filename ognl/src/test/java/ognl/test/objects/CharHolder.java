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

import java.math.BigDecimal;
import java.math.BigInteger;

public class CharHolder {

    public static String statInteger(Integer value) {
        return "statInteger:" + value;
    }

    public static String statBigDecimal(BigDecimal value) {
        return "statBigDecimal:" + value;
    }

    public static String statBigInteger(BigInteger value) {
        return "statBigInteger:" + value;
    }

    private BigDecimal bigDecimalValue = new BigDecimal("2.5");

    private BigInteger bigIntegerValue = BigInteger.valueOf(7);

    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    private char primChar = 'b';

    private Character boxedChar = 'b';

    public char getPrimChar() {
        return primChar;
    }

    public void setPrimChar(char primChar) {
        this.primChar = primChar;
    }

    public Character getBoxedChar() {
        return boxedChar;
    }

    public void setBoxedChar(Character boxedChar) {
        this.boxedChar = boxedChar;
    }

    public Object getObjectChar() {
        return boxedChar;
    }

    private Object objectValue = 'a';

    public Object getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(Object objectValue) {
        this.objectValue = objectValue;
    }

    public String overload(int value) {
        return "int:" + value;
    }

    public String overload(Object value) {
        return "obj:" + value.getClass().getSimpleName();
    }

    public String takesInteger(Integer value) {
        return "Integer:" + value;
    }

    public int plusOne(int value) {
        return value + 1;
    }
}
