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

import ognl.test.OgnlTestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Simple {

    private String stringValue = "test";
    private float floatValue;
    private int intValue;
    private boolean booleanValue;
    private BigInteger bigIntValue = BigInteger.valueOf(0);
    private BigDecimal bigDecValue = new BigDecimal(0.0);

    private Root root = new Root();

    private Bean3 _bean;
    private Bean2 _bean2;

    private Object[] _array;

    private Messages _messages;
    private Object[] values;

    public Simple() {
        Map src = new HashMap();
        src.put("test", "This is a test");

        _messages = new Messages(src);
    }

    public Simple(Bean3 bean) {
        _bean = bean;
    }

    public Simple(Bean2 bean) {
        _bean2 = bean;
    }

    public Simple(Object[] values) {
        this.values = values;
    }

    public Simple(String stringValue, float floatValue, int intValue) {
        super();
        this.stringValue = stringValue;
        this.floatValue = floatValue;
        this.intValue = intValue;
    }

    public void setValues(String stringValue, float floatValue, int intValue) {
        this.stringValue = stringValue;
        this.floatValue = floatValue;
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String value) {
        stringValue = value;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float value) {
        floatValue = value;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int value) {
        intValue = value;
    }

    public boolean getValueIsTrue(Object currValue) {
        return true;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean value) {
        booleanValue = value;
    }

    public BigInteger getBigIntValue() {
        return bigIntValue;
    }

    public void setArray(Object[] values) {
        _array = values;
    }

    public Object[] getArray() {
        return _array;
    }

    public void setBigIntValue(BigInteger value) {
        bigIntValue = value;
    }

    public BigDecimal getBigDecValue() {
        return bigDecValue;
    }

    public void setBigDecValue(BigDecimal value) {
        bigDecValue = value;
    }

    public Root getRootValue() {
        return root;
    }

    public MethodTestMethods getTestMethods() {
        return new MethodTestMethods();
    }

    public Messages getMessages() {
        return _messages;
    }

    public int getOne() {
        return 1;
    }

    public int getTwo() {
        return 2;
    }

    public int getThree() {
        return 3;
    }

    public int getTestValue(int val) {
        return val + 1;
    }

    public boolean isEditorDisabled() {
        return false;
    }

    public boolean isDisabled() {
        return true;
    }

    public boolean getIsTruck() {
        return true;
    }

    public GetterMethods getMethodsTest() {
        return new GetterMethods();
    }

    public String getDisplayValue(int val) {
        return "test";
    }

    public Object[] getValues() {
        return values;
    }

    public boolean equals(Object other) {
        boolean result = false;

        if (other instanceof Simple) {
            Simple os = (Simple) other;

            result = OgnlTestCase.isEqual(os.getStringValue(), getStringValue()) && (os.getIntValue() == getIntValue());
        }
        return result;
    }

    public boolean isThisVarArgsWorking(Object... arguments) {
        return true;
    }

    public String isNullVarArgs() {
        return "null";
    }

    public String isStringVarArgs(String... arguments) {
        return "args";
    }

    public TestInterface get() {
        return new TestInterface() {
            @Override
            public String request() {
                return "null";
            }

            @Override
            public String request(Object... args) {
                return "args";
            }
        };
    }

    interface TestInterface {

        String request();

        String request(Object... args);

    }

}
