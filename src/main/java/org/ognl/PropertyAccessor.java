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
package org.ognl;

/**
 * This interface defines methods for setting and getting a property from a target object. A
 * "property" in this case is a named data value that takes the generic form of an Object---the same
 * definition as is used by beans. But the operational semantics of the term will vary by
 * implementation of this interface: a bean-style implementation will get and set properties as
 * beans do, by reflection on the target object's class, but other implementations are possible,
 * such as one that uses the property name as a key into a map.
 * <p>
 * An implementation of this interface will often require that its target objects all be of some
 * particular type. For example, the MapPropertyAccessor class requires that its targets all
 * implement the java.util.Map interface.
 * <p>
 * Note that the "name" of a property is represented by a generic Object. Some implementations may
 * require properties' names to be Strings, while others may allow them to be other types---for
 * example, ArrayPropertyAccessor treats Number names as indexes into the target object, which must
 * be an array.
 */
public interface PropertyAccessor {

    /**
     * Extracts and returns the property of the given name from the given target object.
     *
     * @param context The current execution context.
     * @param target  the object to get the property from
     * @param name    the name of the property to get.
     * @return the current value of the given property in the given object
     * @throws OgnlException if there is an error locating the property in the given object
     */
    Object getProperty(OgnlContext context, Object target, Object name) throws OgnlException;

    /**
     * Sets the value of the property of the given name in the given target object.
     *
     * @param context The current execution context.
     * @param target  the object to set the property in
     * @param name    the name of the property to set
     * @param value   the new value for the property.
     * @throws OgnlException if there is an error setting the property in the given object
     */
    void setProperty(OgnlContext context, Object target, Object name, Object value) throws OgnlException;

    /**
     * Returns a java string representing the textual method that should be called to access a
     * particular element. (ie "get")
     *
     * @param context The current execution context.
     * @param target  The current object target on the expression tree being evaluated.
     * @param index   The index object that will be placed inside the string to access the value.
     * @return The source accessor method to call.
     */
    String getSourceAccessor(OgnlContext context, Object target, Object index);

    /**
     * Returns a java string representing the textual method that should be called to set a
     * particular element. (ie "set")
     *
     * @param context The current execution context.
     * @param target  The current object target on the expression tree being evaluated.
     * @param index   The index object that will be placed inside the string to set the value.
     * @return The source setter method to call.
     */
    String getSourceSetter(OgnlContext context, Object target, Object index);
}
