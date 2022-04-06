//--------------------------------------------------------------------------
//  Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//  Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.ognl.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ognl.DefaultMemberAccess;
import org.ognl.Ognl;
import org.ognl.OgnlContext;
import org.ognl.OgnlException;
import org.ognl.OgnlRuntime;

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
