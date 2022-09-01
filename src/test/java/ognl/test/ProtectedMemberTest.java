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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;

/**
 * This is a test program for protected access in OGNL.
 * Shows the failures and a summary.
 */
public class ProtectedMemberTest extends TestCase
{
    protected String                _protectedProperty = "protected value";
    protected final String          _protectedFinalProperty = "protected final value";
    protected static String         _protectedStaticProperty = "protected static value";
    protected static final String   _protectedStaticFinalProperty = "protected static final value";
    protected OgnlContext           context;


    /*===================================================================
      Public static methods
      ===================================================================*/
    public static TestSuite suite()
    {
        return new TestSuite(ProtectedMemberTest.class);
    }

    /*===================================================================
      Constructors
      ===================================================================*/
    public ProtectedMemberTest(String name)
    {
        super(name);
    }

    /*===================================================================
      Protected methods
      ===================================================================*/
    protected String getProtectedProperty()
    {
        return _protectedProperty;
    }

    protected String getProtectedFinalProperty()
    {
        return _protectedFinalProperty;
    }

    protected static String getProtectedStaticProperty()
    {
        return _protectedStaticProperty;
    }

    protected static String getProtectedStaticFinalProperty()
    {
        return _protectedStaticFinalProperty;
    }

    /*===================================================================
      Public methods
      ===================================================================*/
    public void testProtectedAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("protectedProperty", context, this), getProtectedProperty());
    }

    public void testProtectedField() throws OgnlException
    {
        assertEquals(Ognl.getValue("_protectedProperty", context, this), _protectedProperty);
    }

    public void testProtectedFinalAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("protectedFinalProperty", context, this), getProtectedFinalProperty());
    }

    public void testProtectedFinalField() throws OgnlException
    {
        assertEquals(Ognl.getValue("_protectedFinalProperty", context, this), _protectedFinalProperty);
    }

    public void testProtectedStaticAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("protectedStaticProperty", context, this), getProtectedStaticProperty());
        // Succeeds due to calling the static getter to retrieve it.
    }

    public void testProtectedStaticFieldNormalAccess() throws OgnlException
    {
        try {
            assertEquals(Ognl.getValue("_protectedStaticProperty", context, this), _protectedStaticProperty);
            fail("Should not be able to access private static _protectedStaticProperty through getValue()");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a static field using non-static getValue
        }
    }

    public void testProtectedStaticFieldStaticAccess() throws OgnlException
    {
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticProperty"), _protectedStaticProperty);
        // Only succeeds due to directly using the runtime to access the field as a static field.
    }

    public void testProtectedStaticFinalAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("protectedStaticFinalProperty", context, this), getProtectedStaticFinalProperty());
        // Succeeds due to calling the static getter to retrieve it.
    }

    public void testProtectedStaticFinalFieldNormalAccess() throws OgnlException
    {
        try {
            assertEquals(Ognl.getValue("_protectedStaticFinalProperty", context, this), _protectedStaticFinalProperty);
            fail("Should not be able to access private static _protectedStaticFinalProperty through getValue()");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a static field using non-static getValue
        }
    }

    public void testProtectedStaticFinalFieldStaticAccess() throws OgnlException
    {
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticFinalProperty"), _protectedStaticFinalProperty);
        // Only succeeds due to directly using the runtime to access the field as a static field.
    }

    public void testProtectedFieldSet() throws OgnlException
    {
        final String originalValue = _protectedProperty;
        assertEquals(Ognl.getValue("_protectedProperty", context, this), originalValue);
        Ognl.setValue("_protectedProperty", context, this, "changevalue");
        assertEquals(Ognl.getValue("_protectedProperty", context, this), "changevalue");
        Ognl.setValue("_protectedProperty", context, this, originalValue);
        assertEquals(Ognl.getValue("_protectedProperty", context, this), originalValue);
    }

    public void testProtectedFinalFieldSet() throws OgnlException
    {
        final String originalValue = _protectedFinalProperty;
        assertEquals(Ognl.getValue("_protectedFinalProperty", context, this), originalValue);
        try {
            Ognl.setValue("_protectedFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify final property");
        } catch (OgnlException oex) {
            // Fails as test attempts to modify a final property
        }
        assertEquals(Ognl.getValue("_protectedFinalProperty", context, this), originalValue);
    }

    public void testProtectedStaticFieldSet() throws OgnlException
    {
        final String originalValue = _protectedStaticProperty;
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticProperty"), originalValue);
        try {
            Ognl.setValue("_protectedStaticProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            // Fails as test attempts to modify a static property
        }
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticProperty"), originalValue);
    }

    public void testProtectedStaticFinalFieldSet() throws OgnlException
    {
        final String originalValue = _protectedStaticFinalProperty;
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticFinalProperty"), originalValue);
        try {
            Ognl.setValue("_protectedStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            // Fails as test attempts to modify a static property
        }
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticFinalProperty"), originalValue);
    }

    public void testProtectedFieldSetFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            Ognl.setValue("_protectedProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to set a protected field with protected access turned off
        }
    }

    public void testProtectedFinalFieldSetFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            Ognl.setValue("_protectedFinalProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to set a protected field with protected access turned off
        }
    }

    public void testProtectedStaticFieldSetFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            Ognl.setValue("_protectedStaticProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to set a protected field with protected access turned off
        }
    }

    public void testProtectedStaticFinalFieldSetFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            Ognl.setValue("_protectedStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to set protected property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to set a protected field with protected access turned off
        }
    }

    public void testProtectedAccessorFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("protectedProperty", context, this), getProtectedProperty());
            fail("Should not be able to access protected property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected accessor with protected access turned off
        }
    }

    public void testProtectedFieldFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("_protectedProperty", context, this), _protectedProperty);
            fail("Should not be able to access protected property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected field with protected access turned off
        }
    }

    public void testProtectedFinalAccessorFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("protectedFinalProperty", context, this), getProtectedFinalProperty());
            fail("Should not be able to access protected final property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected final accessor with protected access turned off
        }
    }

    public void testProtectedFinalFieldFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("_protectedFinalProperty", context, this), _protectedFinalProperty);
            fail("Should not be able to access protected final property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected field with protected access turned off
        }
    }

    public void testProtectedStaticAccessorFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("protectedStaticProperty", context, this), getProtectedStaticProperty());
            fail("Should not be able to access protected static property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected accessor with protected access turned off
        }
    }

    public void testProtectedStaticFieldNormalAccessFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("_protectedStaticProperty", context, this), _protectedStaticProperty);
            fail("Should not be able to access protected static property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected field with protected access turned off
        }
    }

    public void testProtectedStaticFieldStaticAccessFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticProperty"), _protectedStaticProperty);
            fail("Should not be able to access protected static property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected field with protected access turned off
        }
    }

    public void testProtectedStaticFinalAccessorFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("protectedStaticFinalProperty", context, this), getProtectedStaticFinalProperty());
            fail("Should not be able to access protected static final property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected accessor with protected access turned off
        }
    }

    public void testProtectedStaticFinalFieldNormalAccessFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(Ognl.getValue("_protectedStaticFinalProperty", context, this), _protectedStaticFinalProperty);
            fail("Should not be able to access protected static final property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected final field with protected access turned off
        }
    }

    public void testProtectedStaticFinalFieldStaticAccessFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent protected access
        try {
            assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_protectedStaticFinalProperty"), _protectedStaticFinalProperty);
            fail("Should not be able to access protected static final property with protected access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a protected field with protected access turned off
        }
    }

    /*===================================================================
      Overridden methods
      ===================================================================*/
    public void setUp()
    {
          context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, false), null, null);  // Permit protected access, prevent private and package access
    }
}
