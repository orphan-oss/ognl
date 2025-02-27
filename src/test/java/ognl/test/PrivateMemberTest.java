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

class PrivateMemberTest {

    private static final String _privateStaticProperty = "private static value";
    private String _privateProperty = "private value";
    private final String _privateFinalProperty = "private final value";
    private static final String _privateStaticFinalProperty = "private static final value";

    private OgnlContext context;

    private String getPrivateProperty() {
        return _privateProperty;
    }

    private static String getPrivateStaticProperty() {
        return _privateStaticProperty;
    }

    private String getPrivateFinalProperty() {
        return _privateFinalProperty;
    }

    private static String getPrivateStaticFinalProperty() {
        return _privateStaticFinalProperty;
    }

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(this, new DefaultMemberAccess(true, false, false));
    }

    @Test
    void testPrivateAccessor() throws OgnlException {
        Object actual = Ognl.getValue("privateProperty", context, this);
        assertEquals(getPrivateProperty(), actual);
    }

    @Test
    void testPrivateField() throws OgnlException {
        Object actual = Ognl.getValue("_privateProperty", context, this);
        assertEquals(_privateProperty, actual);
    }

    @Test
    void testPrivateFinalAccessor() throws OgnlException {
        Object actual = Ognl.getValue("privateFinalProperty", context, this);
        assertEquals(getPrivateFinalProperty(), actual);
    }

    @Test
    void testPrivateFinalField() throws OgnlException {
        Object actual = Ognl.getValue("_privateFinalProperty", context, this);
        assertEquals(_privateFinalProperty, actual);
    }

    @Test
    void testPrivateStaticAccessor() throws OgnlException {
        Object actual = Ognl.getValue("privateStaticProperty", context, this);
        assertEquals(getPrivateStaticProperty(), actual);
    }

    @Test
    void testPrivateStaticFieldNormalAccess() {
        try {
            Object actual = Ognl.getValue("_privateStaticProperty", context, this);
            assertEquals(_privateStaticProperty, actual);
            fail("Should not be able to access private static _privateStaticProperty through getValue()");
        } catch (OgnlException oex) {
            assertEquals("_privateStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFieldStaticAccess() throws OgnlException {
        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticProperty");
        assertEquals(_privateStaticProperty, actual);
    }

    @Test
    void testPrivateStaticFinalAccessor() throws OgnlException {
        Object actual = Ognl.getValue("privateStaticFinalProperty", context, this);
        assertEquals(actual, getPrivateStaticFinalProperty());
    }

    @Test
    void testPrivateStaticFinalFieldNormalAccess() {
        try {
            Object actual = Ognl.getValue("_privateStaticFinalProperty", context, this);
            assertEquals(_privateStaticFinalProperty, actual);
            fail("Should not be able to access private static _privateStaticFinalProperty through getValue()");
        } catch (OgnlException oex) {
            assertEquals("_privateStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFinalFieldStaticAccess() throws OgnlException {
        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticFinalProperty");
        assertEquals(_privateStaticFinalProperty, actual);
    }

    @Test
    void testPrivateFieldSet() throws OgnlException {
        final String originalValue = _privateProperty;
        Object actual = Ognl.getValue("_privateProperty", context, this);
        assertEquals(originalValue, actual);

        Ognl.setValue("_privateProperty", context, this, "changevalue");

        actual = Ognl.getValue("_privateProperty", context, this);
        assertEquals("changevalue", actual);

        Ognl.setValue("_privateProperty", context, this, originalValue);

        actual = Ognl.getValue("_privateProperty", context, this);
        assertEquals(actual, originalValue);
    }

    @Test
    void testPrivateFinalFieldSet() throws OgnlException {
        final String originalValue = _privateFinalProperty;

        Object actual = Ognl.getValue("_privateFinalProperty", context, this);
        assertEquals(originalValue, actual);

        try {
            Ognl.setValue("_privateFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify final property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateFinalProperty", oex.getMessage());
        }

        actual = Ognl.getValue("_privateFinalProperty", context, this);
        assertEquals(originalValue, actual);
    }

    @Test
    void testPrivateStaticFieldSet() throws OgnlException {
        final String originalValue = _privateStaticProperty;

        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticProperty");
        assertEquals(originalValue, actual);

        try {
            Ognl.setValue("_privateStaticProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateStaticProperty", oex.getMessage());
        }

        actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticProperty");
        assertEquals(originalValue, actual);
    }

    @Test
    void testPrivateStaticFinalFieldSet() throws OgnlException {
        final String originalValue = _privateStaticFinalProperty;

        Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticFinalProperty");
        assertEquals(originalValue, actual);

        try {
            Ognl.setValue("_privateStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateStaticFinalProperty", oex.getMessage());
        }

        actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticFinalProperty");
        assertEquals(originalValue, actual);
    }

    @Test
    void testPrivateFieldSetFail() {
        context = Ognl.createDefaultContext(this, new DefaultMemberAccess(false, true, true), null, null);
        try {
            Ognl.setValue("_privateProperty", context, this, "changevalue");
            fail("Should not be able to set private property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateFinalFieldSetFail() {
        context = Ognl.createDefaultContext(this, new DefaultMemberAccess(false, true, true), null, null);
        try {
            Ognl.setValue("_privateFinalProperty", context, this, "changevalue");
            fail("Should not be able to set private property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFieldSetFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Ognl.setValue("_privateStaticProperty", context, this, "changevalue");
            fail("Should not be able to set private property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFinalFieldSetFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Ognl.setValue("_privateStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to set private property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("privateProperty", context, this);
            assertEquals(actual, getPrivateProperty());
            fail("Should not be able to access private property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest.privateProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateFieldFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("_privateProperty", context, this);
            assertEquals(actual, _privateProperty);
            fail("Should not be able to access private property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateFinalAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("privateFinalProperty", context, this);
            assertEquals(actual, getPrivateFinalProperty());
            fail("Should not be able to access private final property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest.privateFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateFinalFieldFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("_privateFinalProperty", context, this);
            assertEquals(actual, _privateFinalProperty);
            fail("Should not be able to access private final property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("privateStaticProperty", context, this);
            assertEquals(actual, getPrivateStaticProperty());
            fail("Should not be able to access private static property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest.privateStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFieldNormalAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("_privateStaticProperty", context, this);
            assertEquals(_privateStaticProperty, actual);
            fail("Should not be able to access private static property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFieldStaticAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticProperty");
            assertEquals(_privateStaticProperty, actual);
            fail("Should not be able to access private static property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("Could not get static field _privateStaticProperty from class ognl.test.PrivateMemberTest", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFinalAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("privateStaticFinalProperty", context, this);
            assertEquals(actual, getPrivateStaticFinalProperty());
            fail("Should not be able to access private static final property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest.privateStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFinalFieldNormalAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = Ognl.getValue("_privateStaticFinalProperty", context, this);
            assertEquals(_privateStaticFinalProperty, actual);
            fail("Should not be able to access private static final property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.PrivateMemberTest._privateStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testPrivateStaticFinalFieldStaticAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true));
        try {
            Object actual = OgnlRuntime.getStaticField(context, this.getClass().getName(), "_privateStaticFinalProperty");
            assertEquals(_privateStaticFinalProperty, actual);
            fail("Should not be able to access private static final property with private access turned off");
        } catch (OgnlException oex) {
            assertEquals("Could not get static field _privateStaticFinalProperty from class ognl.test.PrivateMemberTest", oex.getMessage());
        }
    }
}
