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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexed extends BaseIndexed {
    private String[] _values = new String[]{"foo", "bar", "baz"};

    private List _list = new ArrayList();
    private ListSource _source = new ListSourceImpl();

    private Map _props = new HashMap();

    public Indexed() {
        _list.add(new Integer(1));
        _list.add(new Integer(2));
        _list.add(new Integer(3));

        _source.addValue(new Bean2());
    }

    public Indexed(String[] values) {
        _values = values;
    }

    /* Indexed property "_values" */
    public String[] getValues() {
        return _values;
    }

    public void setValues(String[] value) {
        _values = value;
    }

    /**
     * This method returns the string from the array and appends "xxx" to
     * distinguish the "get" method from the direct array access.
     */
    public String getValues(int index) {
        return _values[index] + "xxx";
    }

    public void setValues(int index, String value) {
        if (value.endsWith("xxx")) {
            _values[index] = value.substring(0, value.length() - 3);
        } else {
            _values[index] = value;
        }
    }

    public Collection getList() {
        return _list;
    }

    public String getTitle(int count) {
        return "Title count " + count;
    }

    public ListSource getSource() {
        return _source;
    }

    public void setProperty(String property, Object value) {
        _props.put(property, value);
    }

    public Object getProperty(String property) {
        return _props.get(property);
    }
}
