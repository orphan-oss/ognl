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

import java.util.*;

/**
	This tests the interface inheritence test.  This test implements
	MyMap->Map but extends Object, therefore should be coded using
	MapPropertyAccessor instead of ObjectPropertyAccessor.
 */
public class MyMapImpl extends Object implements MyMap
{
	private Map				map = new HashMap();

	public void clear()
	{
		map.clear();
	}

	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	public Set entrySet()
	{
		return map.entrySet();
	}

	public boolean equals(Object o)
	{
		return map.equals(o);
	}

	public Object get(Object key)
	{
		return map.get(key);
	}

	public int hashCode()
	{
		return map.hashCode();
	}

	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	public Set keySet()
	{
		return map.keySet();
	}

	public Object put(Object key, Object value)
	{
		return map.put(key, value);
	}

	public void putAll(Map t)
	{
		map.putAll(t);
	}

	public Object remove(Object key)
	{
		return map.remove(key);
	}

	public int size()
	{
		return map.size();
	}

	public Collection values()
	{
		return map.values();
	}

	public String getDescription()
	{
		return "MyMap implementation";
	}
}
