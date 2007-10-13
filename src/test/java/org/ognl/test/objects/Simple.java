//--------------------------------------------------------------------------
//	Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.ognl.test.objects;

import org.ognl.test.OgnlTestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Simple extends Object
{
    private String          stringValue = "test";
    private float           floatValue;
    private int             intValue;
    private boolean         booleanValue;
    private BigInteger      bigIntValue = BigInteger.valueOf(0);
    private BigDecimal      bigDecValue = new BigDecimal(0.0);

    private Root root = new Root();

    private Bean3 _bean;
    private Bean2 _bean2;

    private Object[] _array;

    private Messages _messages;

    public Simple()
    {
        Map src = new HashMap();
        src.put("test", "This is a test");

        _messages = new Messages(src);
    }

    public Simple(Bean3 bean)
    {
        _bean = bean;
    }

    public Simple(Bean2 bean)
    {
        _bean2 = bean;
    }

    public Simple(Object[] values)
    {
        super();
    }

    public Simple(String stringValue, float floatValue, int intValue)
    {
        super();
        this.stringValue = stringValue;
        this.floatValue = floatValue;
        this.intValue = intValue;
    }

    public void setValues(String stringValue, float floatValue, int intValue)
    {
        this.stringValue = stringValue;
        this.floatValue = floatValue;
        this.intValue = intValue;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String value)
    {
        stringValue = value;
    }

    public float getFloatValue()
    {
        return floatValue;
    }

    public void setFloatValue(float value)
    {
        floatValue = value;
    }

    public int getIntValue()
    {
        return intValue;
    }

    public void setIntValue(int value)
    {
        intValue = value;
    }

    public boolean getValueIsTrue(Object currValue)
    {
        return true;
    }

    public boolean getBooleanValue()
    {
        return booleanValue;
    }

    public void setBooleanValue(boolean value)
    {
        booleanValue = value;
    }

    public BigInteger getBigIntValue()
    {
        return bigIntValue;
    }

    public void setArray(Object[] values)
    {
        _array = values;
    }

    public Object[] getArray()
    {
        return _array;
    }

    public void setBigIntValue(BigInteger value)
    {
        bigIntValue = value;
    }

    public BigDecimal getBigDecValue()
    {
        return bigDecValue;
    }

    public void setBigDecValue(BigDecimal value)
    {
        bigDecValue = value;
    }

    public Root getRootValue()
    {
        return root;
    }

    public Messages getMessages()
    {
        return _messages;
    }

    public int getOne()
    {
        return 1;
    }

    public int getTwo()
    {
        return 2;
    }

    public int getThree()
    {
        return 3;
    }

    public int getTestValue(int val)
    {
        return val + 1;
    }

    public boolean isEditorDisabled()
    {
        return false;
    }

    public boolean isDisabled()
    {
        return true;
    }

    public GetterMethods getMethodsTest()
    {
        return new GetterMethods();
    }

    public String getDisplayValue(int val)
    {
        return "test";
    }

    public boolean equals(Object other)
    {
        boolean     result = false;

        if (other instanceof Simple) {
            Simple      os = (Simple)other;

            result = OgnlTestCase.isEqual(os.getStringValue(), getStringValue()) && (os.getIntValue() == getIntValue());
        }
        return result;
    }

    public boolean isThisVarArgsWorking(Object...arguments)
    {
        return true;
    }
}
