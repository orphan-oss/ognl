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

public class BaseObjectIndexed extends Object
{
    private Map     attributes = new HashMap();

    public BaseObjectIndexed()
    {
        super();
    }

    public Map getAttributes()
    {
        return attributes;
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value)
    {
        attributes.put(name, value);
    }

    /* allow testing property name where types do not match */
    public Object getOtherAttribute(String name)
    {
        return null;
    }

    public void setOtherAttribute(Object someObject, Object foo)
    {
        /* do nothing */
    }


    /* test whether get only is found */
    public Object getSecondaryAttribute(Object name)
    {
        return attributes.get(name);
    }
}
