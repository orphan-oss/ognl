//--------------------------------------------------------------------------
//	Copyright (c) 2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
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
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;

/**
 * This is a test program for private access in OGNL.
 * Shows the failures and a summary.
 */
public class PrivateMemberTest extends TestCase
{
    private static String           _privateStaticProperty = "private static value";
    private String                  _privateProperty = "private value";
    protected OgnlContext           context;


	/*===================================================================
		Public static methods
	  ===================================================================*/
    public static TestSuite suite()
    {
        return new TestSuite(PrivateMemberTest.class);
    }

	/*===================================================================
		Constructors
	  ===================================================================*/
	public PrivateMemberTest(String name)
	{
	    super(name);
	}

	/*===================================================================
		Private methods
	  ===================================================================*/
    private String getPrivateProperty()
    {
        return _privateProperty;
    }

    private static String getPrivateStaticProperty() {
        return _privateStaticProperty;
    }

  /*===================================================================
    Public methods
    ===================================================================*/
    public void testPrivateAccessor() throws OgnlException
    {
        assertEquals(Ognl.getValue("privateProperty", context, this), getPrivateProperty());
    }

    public void testPrivateField() throws OgnlException
    {
        assertEquals(Ognl.getValue("_privateProperty", context, this), _privateProperty);
    }

    public void testPrivateStaticAccessor() throws OgnlException
    {
        // Test following PR#59/PR#60 (MemberAccess support private static field).
        assertEquals(Ognl.getValue("privateStaticProperty", context, this), getPrivateStaticProperty());
        // Succeeds due to calling the static getter to retrieve it.
    }

    public void testPrivateStaticFieldNormalAccess() throws OgnlException
    {
        // Test following PR#59/PR#60 (MemberAccess support private static field).
        try {
            assertEquals(Ognl.getValue("_privateStaticProperty", context, this), _privateStaticProperty);
            fail("Should not be able to access private static _privateStaticProperty through getValue()");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a static field using non-static getValue
        }
    }

    public void testPrivateStaticFieldStaticAccess() throws OgnlException
    {
        // Test following PR#59/PR#60 (MemberAccess support private static field).
        assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_privateStaticProperty"), _privateStaticProperty);
        // Only succeeds due to directly using the runtime to access the field as a static field.
    }

    public void testPrivateAccessorFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true), null, null);  // Prevent private access
        try {
          assertEquals(Ognl.getValue("privateProperty", context, this), getPrivateProperty());
          fail("Should not be able to access private property with private access turned off");
        } catch (OgnlException oex) {
          // Fails as test attempts to access a private accessor with private access turned off
        }
    }

    public void testPrivateFieldFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true), null, null);  // Prevent private access
        try {
          assertEquals(Ognl.getValue("_privateProperty", context, this), _privateProperty);
          fail("Should not be able to access private property with private access turned off");
        } catch (OgnlException oex) {
          // Fails as test attempts to access a private accessor with private access turned off
        }
    }

    public void testPrivateStaticAccessorFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true), null, null);  // Prevent private access
        // Test following PR#59/PR#60 (MemberAccess support private static field).
        try {
          assertEquals(Ognl.getValue("privateStaticProperty", context, this), getPrivateStaticProperty());
          fail("Should not be able to access private static property with private access turned off");
        } catch (OgnlException oex) {
          // Fails as test attempts to access a private accessor with private access turned off
        }
    }

    public void testPrivateStaticFieldNormalAccessFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true), null, null);  // Prevent private access
        // Test following PR#59/PR#60 (MemberAccess support private static field).
        try {
            assertEquals(Ognl.getValue("_privateStaticProperty", context, this), _privateStaticProperty);
            fail("Should not be able to access private static property with private access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a private accessor with private access turned off
        }
    }

    public void testPrivateStaticFieldStaticAccessFail() throws OgnlException
    {
        context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false, true, true), null, null);  // Prevent private access
        // Test following PR#59/PR#60 (MemberAccess support private static field).
        try {
            assertEquals(OgnlRuntime.getStaticField(context, this.getClass().getName() , "_privateStaticProperty"), _privateStaticProperty);
            fail("Should not be able to access private static property with private access turned off");
        } catch (OgnlException oex) {
            // Fails as test attempts to access a private accessor with private access turned off
        }
    }

	/*===================================================================
		Overridden methods
	  ===================================================================*/
	public void setUp()
	{
        context = (OgnlContext)Ognl.createDefaultContext(null, new DefaultMemberAccess(true), null, null);
	}
}
