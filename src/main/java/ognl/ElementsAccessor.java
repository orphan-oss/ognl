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

import java.util.Enumeration;

/**
 * This interface defines a method for getting the "elements" of an object, which means
 * any objects that naturally would be considered to be contained by the object.  So for a
 * collection, you would expect this method to return all the objects in that collection;
 * while for an ordinary object you would expect this method to return just that object.
 *
 * <p> An implementation of this interface will often require that its target objects all
 * be of some particular type.  For example, the MapElementsAccessor class requires that
 * its targets all implement the Map interface.
 */
public interface ElementsAccessor {
    /**
     * Returns an iterator over the elements of the given target object.
     *
     * @param target the object to get the elements of
     * @return an iterator over the elements of the given object
     * @throws OgnlException if there is an error getting the given object's elements
     */
    Enumeration<?> getElements(Object target) throws OgnlException;
}
