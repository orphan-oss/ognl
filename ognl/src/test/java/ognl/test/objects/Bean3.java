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

import java.util.HashMap;
import java.util.Map;

public class Bean3 extends Object {
    private int value = 100;

    private Map map;

    {
        map = new HashMap();
        map.put("foo", "bar");
        map.put("bar", "baz");
    }

    private String _nullValue;
    private Object _indexValue;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Object getIndexedValue(int index) {
        return _indexValue;
    }

    public void setIndexedValue(int index, Object value) {
        _indexValue = value;
    }

    public Map getMap() {
        return map;
    }

    public void setNullValue(String value) {
        _nullValue = value;
    }

    public String getNullValue() {
        return _nullValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_indexValue == null) ? 0 : _indexValue.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Bean3 other = (Bean3) obj;
        if (_indexValue == null) {
            if (other._indexValue != null) return false;
        } else if (!_indexValue.equals(other._indexValue)) return false;
        return true;
    }
}
