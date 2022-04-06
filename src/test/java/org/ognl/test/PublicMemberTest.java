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
 * This is a test program for public access in OGNL.
 * Shows the failures and a summary.
 */
public class PublicMemberTest extends TestCase
{
    public String                   _publicProperty = "public value";
    public final String             _publicFinalProperty = "public final value";
    public static String            _publicStaticProperty = "public static value";
    public static final String      _publicStaticFinalProperty  = "public static final value";
    protected OgnlContext           context;


    /*===================================================================
      Public static methods
      ===================================================================*/
    public static TestSuite suite()
    {
        return new TestSuite(PublicMemberTest.class);
    }

    /*===================================================================
      Constructors
      ===================================================================*/
    public PublicMemberTest(String name)
    {
        super(name);
    }

    /*===================================================================
      Public methods
      ===================================================================*/
    public String getPublicProperty()
    {
        return _publicProperty;
    }

    public String getPublicFinalProperty()
    {
        return _publicFinalProperty;
    }

    public static String getPublicStaticProperty()
    {
        return _publicStaticProperty;
    }

    public static String getPublicStaticFinalProperty()
    {
        return _publicStaticFinalProperty;
    }

    public void testPublicAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("publicProperty", context, this), getPublicProperty());
    }

    public void testPublicField() throws OgnlException
    {
        assertEquals(Ognl.getValue("_publicProperty", context, this), _publicProperty);
    }

    public void testPublicFinalAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("publicFinalProperty", context, this), getPublicFinalProperty());
    }

    public void testPublicFinalField() throws OgnlException
    {
        assertEquals(Ognl.getValue("_publicFinalProperty", context, this), _publicFinalProperty);
    }

    public void testPublicStaticAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("publicStaticProperty", context, this), getPublicStaticProperty());
    }

    public void testPublicStaticFieldNormalAccessFail() throws OgnlException
    {
        try {
            assertEquals(Ognl.getValue("_publicStaticProperty", context, this), _publicStaticProperty);
            fail("Should not be able to access public static _publicStaticProperty through getValue()");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a static field using non-static getValue
        }
    }

    public void testPublicStaticFieldStaticAccess() throws OgnlException
    {
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_publicStaticProperty"), _publicStaticProperty);
    }

    public void testPublicStaticFinalAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("publicStaticFinalProperty", context, this), getPublicStaticFinalProperty());
    }

    public void testPublicStaticFinalFieldNormalAccessFail() throws OgnlException
    {
        try {
            assertEquals(Ognl.getValue("_publicStaticFinalProperty", context, this), _publicStaticFinalProperty);
            fail("Should not be able to access public static _publicStaticFinalProperty through getValue()");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a static field using non-static getValue
        }
    }

    public void testPublicStaticFinalFieldStaticAccess() throws OgnlException
    {
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_publicStaticFinalProperty"), _publicStaticFinalProperty);
    }

    public void testPublicFieldSet() throws OgnlException
    {
        final String originalValue = _publicProperty;
        assertEquals(Ognl.getValue("_publicProperty", context, this), originalValue);
        Ognl.setValue("_publicProperty", context, this, "changevalue");
        assertEquals(Ognl.getValue("_publicProperty", context, this), "changevalue");
        Ognl.setValue("_publicProperty", context, this, originalValue);
        assertEquals(Ognl.getValue("_publicProperty", context, this), originalValue);
    }

    public void testPublicFinalFieldSet() throws OgnlException
    {
        final String originalValue = _publicFinalProperty;
        assertEquals(Ognl.getValue("_publicFinalProperty", context, this), originalValue);
        try {
            Ognl.setValue("_publicFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify final property");
        } catch (OgnlException oex) {
            // Fails as test attempts to modify a final property
        }
        assertEquals(Ognl.getValue("_publicFinalProperty", context, this), originalValue);
    }

    public void testPublicStaticFieldSet() throws OgnlException
    {
        final String originalValue = _publicStaticProperty;
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_publicStaticProperty"), originalValue);
        try {
            Ognl.setValue("_publicStaticProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            // Fails as test attempts to modify a static property
        }
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_publicStaticProperty"), originalValue);
    }

    public void testPublicStaticFinalFieldSet() throws OgnlException
    {
        final String originalValue = _publicStaticFinalProperty;
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_publicStaticFinalProperty"), originalValue);
        try {
            Ognl.setValue("_publicStaticFinalProperty", context, this, "changevalue");
            fail("Should not be able to modify static property");
        } catch (OgnlException oex) {
            // Fails as test attempts to modify a static property
        }
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_publicStaticFinalProperty"), originalValue);
    }

    /*===================================================================
      Overridden methods
      ===================================================================*/
    public void setUp()
    {
          context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, false, false), null, null);  // Prevent non-public access
    }
}
