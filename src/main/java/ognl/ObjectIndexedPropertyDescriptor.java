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
package ognl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * <p>PropertyDescriptor subclass that describes an indexed set of read/write
 * methods to get a property. Unlike IndexedPropertyDescriptor this allows
 * the "key" to be an arbitrary object rather than just an int.  Consequently
 * it does not have a "readMethod" or "writeMethod" because it only expects
 * a pattern like:</p>
 * <pre>
 *    public void set<i>Property</i>(<i>KeyType</i>, <i>ValueType</i>);
 *    public <i>ValueType</i> get<i>Property</i>(<i>KeyType</i>);
 * </pre>
 * <p>and does not require the methods that access it as an array.  OGNL can
 * get away with this without losing functionality because if the object
 * does expose the properties they are most probably in a Map and that case
 * is handled by the normal OGNL property accessors.
 * </p>
 * <p>For example, if an object were to have methods that accessed and "attributes"
 * property it would be natural to index them by String rather than by integer
 * and expose the attributes as a map with a different property name:
 * <pre>
 *    public void setAttribute(String name, Object value);
 *    public Object getAttribute(String name);
 *    public Map getAttributes();
 * </pre>
 * <p>Note that the index get/set is called get/set <code>Attribute</code>
 * whereas the collection getter is called <code>Attributes</code>.  This
 * case is handled unambiguously by the OGNL property accessors because the
 * set/get<code>Attribute</code> methods are detected by this object and the
 * "attributes" case is handled by the <code>MapPropertyAccessor</code>.
 * Therefore OGNL expressions calling this code would be handled in the
 * following way:
 * </p>
 * <table>
 *  <caption>OGNL Expression</caption>
 *  <tr><th>OGNL Expression</th>
 *      <th>Handling</th>
 *  </tr>
 *  <tr>
 *      <td><code>attribute["name"]</code></td>
 *      <td>Handled by an index getter, like <code>getAttribute(String)</code>.</td>
 *  </tr>
 *  <tr>
 *      <td><code>attribute["name"] = value</code></td>
 *      <td>Handled by an index setter, like <code>setAttribute(String, Object)</code>.</td>
 *  </tr>
 *  <tr>
 *      <td><code>attributes["name"]</code></td>
 *      <td>Handled by <code>MapPropertyAccessor</code> via a <code>Map.get()</code>.  This
 *          will <b>not</b> go through the index get accessor.
 *      </td>
 *  </tr>
 *  <tr>
 *      <td><code>attributes["name"] = value</code></td>
 *      <td>Handled by <code>MapPropertyAccessor</code> via a <code>Map.put()</code>.  This
 *          will <b>not</b> go through the index set accessor.
 *      </td>
 *  </tr>
 * </table>
 */
public class ObjectIndexedPropertyDescriptor extends PropertyDescriptor {

    private final Method indexedReadMethod;
    private final Method indexedWriteMethod;
    private final Class<?> propertyType;

    public ObjectIndexedPropertyDescriptor(String propertyName, Class<?> propertyType, Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {
        super(propertyName, null, null);
        this.propertyType = propertyType;
        this.indexedReadMethod = indexedReadMethod;
        this.indexedWriteMethod = indexedWriteMethod;
    }

    public Method getIndexedReadMethod() {
        return indexedReadMethod;
    }

    public Method getIndexedWriteMethod() {
        return indexedWriteMethod;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }
}
