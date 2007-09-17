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

import java.util.*;

public class Indexed extends BaseIndexed
{
    private String[] _values = new String[] { "foo", "bar", "baz" };

    private List _list = new ArrayList();
    private ListSource _source = new ListSourceImpl();

    private Map _props = new HashMap();

    public Indexed()
    {
        _list.add(new Integer(1));
        _list.add(new Integer(2));
        _list.add(new Integer(3));

        _source.addValue(new Bean2());
    }

    public Indexed(String[] values)
    {
        _values = values;
    }
    
    /* Indexed property "_values" */
    public String[] getValues()
    {
        return _values;
    }
    
    public void setValues(String[] value)
    {
        _values = value;
    }

    /**
        This method returns the string from the array and appends "xxx" to
        distinguish the "get" method from the direct array access.
     */
    public String getValues(int index)
    {
        return _values[index] + "xxx";
    }
    
    public void setValues(int index, String value)
    {
        if (value.endsWith("xxx")) {
            _values[index] = value.substring(0, value.length() - 3);
        } else {
            _values[index] = value;
        }
    }

    public Collection getList()
    {
        return _list;
    }

    public String getTitle(int count)
    {
        return "Title count " + count;
    }
    
    public ListSource getSource()
    {
        return _source;
    }

    public void setProperty(String property, Object value)
    {
        _props.put(property, value);
    }

    public Object getProperty(String property)
    {
        return _props.get(property);
    }
}
