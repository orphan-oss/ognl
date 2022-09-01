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
