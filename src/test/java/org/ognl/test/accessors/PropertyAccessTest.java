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
package org.ognl.test.accessors;

import junit.framework.TestCase;
import org.ognl.DefaultMemberAccess;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.OgnlException;
import org.ognl.OgnlRuntime;
import org.ognl.test.OgnlTestCase;
import org.ognl.test.objects.BeanProvider;
import org.ognl.test.objects.BeanProviderAccessor;
import org.ognl.test.objects.EvenOdd;
import org.ognl.test.objects.Root;

public class PropertyAccessTest extends TestCase {

    public void testPropertyAccess() throws OgnlException {
        //  given
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));

        Root root = new Root();
        root.getBeans().setBean("evenOdd", new EvenOdd());

        // when
        Object result = Ognl.getValue("beans.evenOdd.next", context, root);

        // then
        assertEquals("even", result);
    }

    public void setUp() {
        OgnlRuntime.setPropertyAccessor(BeanProvider.class, new BeanProviderAccessor());
    }

}
