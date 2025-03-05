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

class ProtectedMemberTest {

    protected String _protectedProperty = "protected value";
    protected final String _protectedFinalProperty = "protected final value";
    protected static String _protectedStaticProperty = "protected static value";
    protected static final String _protectedStaticFinalProperty = "protected static final value";
    protected OgnlContext context;

    @BeforeEach
    void setUp() {
        // Permit protected access, prevent private and package access
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, false));
    }

    protected String getProtectedProperty() {
        return _protectedProperty;
    }

    protected String getProtectedFinalProperty() {
        return _protectedFinalProperty;
    }

    protected static String getProtectedStaticProperty() {
        return _protectedStaticProperty;
    }

    protected static String getProtectedStaticFinalProperty() {
        return _protectedStaticFinalProperty;
    }

    @Test
    void testProtectedAccessor() throws OgnlException {
        assertEquals(getProtectedProperty(), Ognl.getValue("protectedProperty", context, this));
    }

    @Test
    void testProtectedField() throws OgnlException {
        assertEquals(_protectedProperty, Ognl.getValue("_protectedProperty", context, this));
    }

    @Test
    void testProtectedFinalAccessor() throws OgnlException {
        assertEquals(getProtectedFinalProperty(), Ognl.getValue("protectedFinalProperty", context, this));
    }

    @Test
    void testProtectedFinalField() throws OgnlException {
        assertEquals(_protectedFinalProperty, Ognl.getValue("_protectedFinalProperty", context, this));
    }

    @Test
    void testProtectedStaticAccessor() throws OgnlException {
        assertEquals(getProtectedStaticProperty(), Ognl.getValue("protectedStaticProperty", context, this));
    }

    @Test
    void testProtectedStaticFieldNormalAccess() {
        try {
            assertEquals(_protectedStaticProperty, Ognl.getValue("_protectedStaticProperty", context, this));
            fail("Should not be able to access private static _protectedStaticProperty through getValue()");
        } catch (OgnlException oex) {
            assertEquals("_protectedStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFieldStaticAccess() throws OgnlException {
        assertEquals(_protectedStaticProperty, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticProperty"));
    }

    @Test
    void testProtectedStaticFinalAccessor() throws OgnlException {
        assertEquals(getProtectedStaticFinalProperty(), Ognl.getValue("protectedStaticFinalProperty", context, this));
    }

    @Test
    void testProtectedStaticFinalFieldNormalAccess() {
        try {
            assertEquals(_protectedStaticFinalProperty, Ognl.getValue("_protectedStaticFinalProperty", context, this));
            fail("Should not be able to access private static _protectedStaticFinalProperty through getValue()");
        } catch (OgnlException oex) {
            assertEquals("_protectedStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFinalFieldStaticAccess() throws OgnlException {
        assertEquals(_protectedStaticFinalProperty, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticFinalProperty"));
    }

    @Test
    void testProtectedFieldSet() throws OgnlException {
        final String originalValue = _protectedProperty;
        assertEquals(originalValue, Ognl.getValue("_protectedProperty", context, this));
        Ognl.setValue("_protectedProperty", context, this, "changevalue");
        assertEquals("changevalue", Ognl.getValue("_protectedProperty", context, this));
        Ognl.setValue("_protectedProperty", context, this, originalValue);
        assertEquals(originalValue, Ognl.getValue("_protectedProperty", context, this));
    }

    @Test
    void testProtectedFinalFieldSet() throws OgnlException {
        final String originalValue = _protectedFinalProperty;
        assertEquals(originalValue, Ognl.getValue("_protectedFinalProperty", context, this));
        try {
            Ognl.setValue("_protectedFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify final property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedFinalProperty", oex.getMessage());
        }
        assertEquals(originalValue, Ognl.getValue("_protectedFinalProperty", context, this));
    }

    @Test
    void testProtectedStaticFieldSet() throws OgnlException {
        final String originalValue = _protectedStaticProperty;
        assertEquals(originalValue, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticProperty"));
        try {
            Ognl.setValue("_protectedStaticProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedStaticProperty", oex.getMessage());
        }
        assertEquals(originalValue, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticProperty"));
    }

    @Test
    void testProtectedStaticFinalFieldSet() throws OgnlException {
        final String originalValue = _protectedStaticFinalProperty;
        assertEquals(originalValue, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticFinalProperty"));
        try {
            Ognl.setValue("_protectedStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedStaticFinalProperty", oex.getMessage());
        }
        assertEquals(originalValue, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticFinalProperty"));
    }

    @Test
    void testProtectedFieldSetFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            Ognl.setValue("_protectedProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedFinalFieldSetFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            Ognl.setValue("_protectedFinalProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFieldSetFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            Ognl.setValue("_protectedStaticProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFinalFieldSetFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            Ognl.setValue("_protectedStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(getProtectedProperty(), Ognl.getValue("protectedProperty", context, this));
            fail("Should not be able to access protected property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest.protectedProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedFieldFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(_protectedProperty, Ognl.getValue("_protectedProperty", context, this));
            fail("Should not be able to access protected property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedFinalAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(getProtectedFinalProperty(), Ognl.getValue("protectedFinalProperty", context, this));
            fail("Should not be able to access protected final property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest.protectedFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedFinalFieldFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(_protectedFinalProperty, Ognl.getValue("_protectedFinalProperty", context, this));
            fail("Should not be able to access protected final property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(getProtectedStaticProperty(), Ognl.getValue("protectedStaticProperty", context, this));
            fail("Should not be able to access protected static property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest.protectedStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFieldNormalAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(_protectedStaticProperty, Ognl.getValue("_protectedStaticProperty", context, this));
            fail("Should not be able to access protected static property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedStaticProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFieldStaticAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(_protectedStaticProperty, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticProperty"));
            fail("Should not be able to access protected static property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("Could not get static field _protectedStaticProperty from class ognl.test.ProtectedMemberTest", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFinalAccessorFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(getProtectedStaticFinalProperty(), Ognl.getValue("protectedStaticFinalProperty", context, this));
            fail("Should not be able to access protected static final property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest.protectedStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFinalFieldNormalAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(_protectedStaticFinalProperty, Ognl.getValue("_protectedStaticFinalProperty", context, this));
            fail("Should not be able to access protected static final property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("ognl.test.ProtectedMemberTest._protectedStaticFinalProperty", oex.getMessage());
        }
    }

    @Test
    void testProtectedStaticFinalFieldStaticAccessFail() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false));  // Prevent protected access
        try {
            assertEquals(_protectedStaticFinalProperty, OgnlRuntime.getStaticField(context, this.getClass().getName(), "_protectedStaticFinalProperty"));
            fail("Should not be able to access protected static final property with protected access turned off");
        } catch (OgnlException oex) {
            assertEquals("Could not get static field _protectedStaticFinalProperty from class ognl.test.ProtectedMemberTest", oex.getMessage());
        }
    }
}
