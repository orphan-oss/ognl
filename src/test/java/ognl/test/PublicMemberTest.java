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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PublicMemberTest {

    public String _publicProperty = "public value";
    public final String _publicFinalProperty = "public final value";
    public static String _publicStaticProperty = "public static value";
    public static final String _publicStaticFinalProperty = "public static final value";
    protected OgnlContext context;

    @BeforeEach
    void setUp() {
        // Prevent non-public access
        context = Ognl.createDefaultContext(this, new DefaultMemberAccess(false, false, false));
    }

    public String getPublicProperty() {
        return _publicProperty;
    }

    public String getPublicFinalProperty() {
        return _publicFinalProperty;
    }

    public static String getPublicStaticProperty() {
        return _publicStaticProperty;
    }

    public static String getPublicStaticFinalProperty() {
        return _publicStaticFinalProperty;
    }

    @Test
    void testPublicAccessor() throws OgnlException {
        Object actual = Ognl.getValue("publicProperty", context, this);
        assertEquals(getPublicProperty(), actual);
    }

    @Test
    void testPublicField() throws OgnlException {
        Object actual = Ognl.getValue("_publicProperty", context, this);
        assertEquals(_publicProperty, actual);
    }

    @Test
    void testPublicFinalAccessor() throws OgnlException {
        Object actual = Ognl.getValue("publicFinalProperty", context, this);
        assertEquals(getPublicFinalProperty(), actual);
    }

    @Test
    void testPublicFinalField() throws OgnlException {
        Object actual = Ognl.getValue("_publicFinalProperty", context, this);
        assertEquals(_publicFinalProperty, actual);
    }

    @Test
    void testPublicStaticAccessor() throws OgnlException {
        Object actual = Ognl.getValue("publicStaticProperty", context, this);
        assertEquals(getPublicStaticProperty(), actual);
    }

    @Test
    void testPublicStaticFieldNormalAccessFail() {
        try {
            Object actual = Ognl.getValue("_publicStaticProperty", context, this);
            assertEquals(_publicStaticProperty, actual);
            fail("Should not be able to access public static _publicStaticProperty through getValue()");
        } catch (OgnlException oex) {
            assertEquals("_publicStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testPublicStaticFieldStaticAccess() throws OgnlException {
        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_publicStaticProperty");
        assertEquals(_publicStaticProperty, actual);
    }

    @Test
    void testPublicStaticFinalAccessor() throws OgnlException {
        Object actual = Ognl.getValue("publicStaticFinalProperty", context, this);
        assertEquals(getPublicStaticFinalProperty(), actual);
    }

    @Test
    void testPublicStaticFinalFieldNormalAccessFail() {
        try {
            Object actual = Ognl.getValue("_publicStaticFinalProperty", context, this);
            assertEquals(_publicStaticFinalProperty, actual);
            fail("Should not be able to access public static _publicStaticFinalProperty through getValue()");
        } catch (OgnlException oex) {
            assertEquals("_publicStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPublicStaticFinalFieldStaticAccess() throws OgnlException {
        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_publicStaticFinalProperty");
        assertEquals(_publicStaticFinalProperty, actual);
    }

    @Test
    void testPublicFieldSet() throws OgnlException {
        final String originalValue = _publicProperty;
        Object actual = Ognl.getValue("_publicProperty", context, this);
        assertEquals(originalValue, actual);

        Ognl.setValue("_publicProperty", context, this, "changevalue");
        actual = Ognl.getValue("_publicProperty", context, this);
        assertEquals("changevalue", actual);

        Ognl.setValue("_publicProperty", context, this, originalValue);
        actual = Ognl.getValue("_publicProperty", context, this);
        assertEquals(originalValue, actual);
    }

    @Test
    void testPublicFinalFieldSet() throws OgnlException {
        final String originalValue = _publicFinalProperty;
        Object actual = Ognl.getValue("_publicFinalProperty", context, this);
        assertEquals(originalValue, actual);

        try {
            Ognl.setValue("_publicFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify final property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PublicMemberTest._publicFinalProperty", oex.getMessage());
        }

        actual = Ognl.getValue("_publicFinalProperty", context, this);
        assertEquals(originalValue, actual);
    }

    @Test
    void testPublicStaticFieldSet() throws OgnlException {
        final String originalValue = _publicStaticProperty;
        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_publicStaticProperty");
        assertEquals(originalValue, actual);

        try {
            Ognl.setValue("_publicStaticProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PublicMemberTest._publicStaticProperty", oex.getMessage());
        }

        actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_publicStaticProperty");
        assertEquals(originalValue, actual);
    }

    @Test
    void testPublicStaticFinalFieldSet() throws OgnlException {
        final String originalValue = _publicStaticFinalProperty;

        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_publicStaticFinalProperty");
        assertEquals(originalValue, actual);

        try {
            Ognl.setValue("_publicStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PublicMemberTest._publicStaticFinalProperty", oex.getMessage());
        }

        actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_publicStaticFinalProperty");
        assertEquals(originalValue, actual);
    }

}
