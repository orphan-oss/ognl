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

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.test.objects.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NestedMethodTest {

    private Component component;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        component = new Component();
        context = Ognl.createDefaultContext(component);
    }

    @Test
    void testToDisplayPictureUrl() throws Exception {
        Object actual = Ognl.getValue("toDisplay.pictureUrl", context, component);
        assertEquals(component.getToDisplay().getPictureUrl(), actual);
    }

    @Test
    void testPageCreateRelativeAsset() throws Exception {
        Object actual = Ognl.getValue("page.createRelativeAsset(toDisplay.pictureUrl)", context, component);
        assertEquals(component.getPage().createRelativeAsset(component.getToDisplay().getPictureUrl()), actual);
    }
}
