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

public class Bean2 extends Object
{
    private Bean3       bean3 = new Bean3();

    private boolean _pageBreakAfter = false;

    public String code = "code";

    public Long getId()
    {
        return 1l;
    }

    public Bean3 getBean3()
    {
        return bean3;
    }

    public long getMillis()
    {
        return 1000 * 60 * 2;
    }

    public boolean isCarrier()
    {
        return false;
    }

    public boolean isPageBreakAfter()
    {
        return _pageBreakAfter;
    }

    public void setPageBreakAfter(boolean value)
    {
        _pageBreakAfter = value;
    }

    public void togglePageBreakAfter()
    {
        _pageBreakAfter ^= true;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bean2 bean2 = (Bean2) o;

        if (_pageBreakAfter != bean2._pageBreakAfter) return false;

        return true;
    }

    public int hashCode()
    {
        return (_pageBreakAfter ? 1 : 0);
    }
}
