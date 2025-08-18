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
package ognl.test;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyAccessorTest<C extends OgnlContext<C>> {

    private C context;

    @BeforeEach
    void setUp() {
        this.context = Ognl.createDefaultContext(null, new DefaultMemberAccess<C>(false));
        OgnlRuntime.setPropertyAccessor(Parent.class, new ChildPropertyAccessor());
    }

    @Test
    void shouldAccessProperty_usingCustomAccessor() throws Exception {
        // given
        Parent root = new Parent(new Child("Luk"));
        String expectedResult = "Luk";

        // then
        assertEquals(expectedResult, Ognl.getValue("child", context, root));
    }

    static class Child {
        String name;

        public Child(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Parent {
        Child child;

        public Parent(Child child) {
            this.child = child;
        }

        public Child getChild() {
            return child;
        }
    }

    public static class ChildPropertyAccessor<C extends OgnlContext<C>> implements PropertyAccessor<C> {
        public void setProperty(C context, Object target, Object name, Object value) throws OgnlException {
        }

        public Object getProperty(C context, Object target, Object name) throws OgnlException {
            if (target instanceof Parent && "child".equals(name)) {
                return OgnlRuntime.getProperty(context, ((Parent) target).getChild(), "name");
            }
            return null;
        }

        public String getSourceAccessor(C context, Object target, Object index) {
            return index.toString();
        }

        public String getSourceSetter(C context, Object target, Object index) {
            return index.toString();
        }
    }
}
