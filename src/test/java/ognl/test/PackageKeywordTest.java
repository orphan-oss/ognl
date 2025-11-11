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
import ognl.ExpressionSyntaxException;
import ognl.Ognl;

/**
 * Tests for parsing class references containing reserved keywords in package names.
 * This addresses Issue #103 where expressions like @jp.or.example.IdUtils@method()
 * would fail to parse because "or" is a reserved keyword in OGNL.
 */
public class PackageKeywordTest extends TestCase {

    /**
     * Test that a package name containing "or" keyword can be parsed.
     * This is a common pattern in Japanese domain names (.jp.or.).
     */
    public void testPackageNameWithOrKeyword() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@jp.or.example.Utils@staticMethod()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse expression with 'or' keyword in package name: " + e.getMessage());
        }
    }

    /**
     * Test that a package name containing "and" keyword can be parsed.
     */
    public void testPackageNameWithAndKeyword() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@com.and.example.Utils@staticMethod()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse expression with 'and' keyword in package name: " + e.getMessage());
        }
    }

    /**
     * Test that a package name containing "not" keyword can be parsed.
     */
    public void testPackageNameWithNotKeyword() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@com.not.example.Utils@staticMethod()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse expression with 'not' keyword in package name: " + e.getMessage());
        }
    }

    /**
     * Test that a package name containing "in" keyword can be parsed.
     */
    public void testPackageNameWithInKeyword() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@com.in.example.Utils@staticMethod()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse expression with 'in' keyword in package name: " + e.getMessage());
        }
    }

    /**
     * Test that a package name containing multiple reserved keywords can be parsed.
     */
    public void testPackageNameWithMultipleKeywords() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@jp.or.in.and.Utils@staticMethod()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse expression with multiple keywords in package name: " + e.getMessage());
        }
    }

    /**
     * Test that a static field reference with keyword in package works.
     */
    public void testStaticFieldWithKeywordInPackage() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@jp.or.example.Constants@FIELD_NAME");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse static field reference with 'or' keyword in package name: " + e.getMessage());
        }
    }

    /**
     * Test that instanceof with keyword in class name works.
     */
    public void testInstanceofWithKeywordInClassName() throws Exception {
        try {
            Object expression = Ognl.parseExpression("value instanceof jp.or.example.MyClass");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse instanceof expression with 'or' keyword in class name: " + e.getMessage());
        }
    }

    /**
     * Test that constructor call with keyword in package works.
     */
    public void testConstructorWithKeywordInPackage() throws Exception {
        try {
            Object expression = Ognl.parseExpression("new jp.or.example.MyClass()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse constructor expression with 'or' keyword in package name: " + e.getMessage());
        }
    }

    /**
     * Test various other reserved keywords that might appear in package names.
     */
    public void testOtherReservedKeywordsInPackages() throws Exception {
        String[] keywords = {"bor", "xor", "band", "eq", "neq", "lt", "lte", "gt", "gte", "shl", "shr", "ushr"};

        for (String keyword : keywords) {
            try {
                String expressionStr = "@com." + keyword + ".example.Utils@method()";
                Object expression = Ognl.parseExpression(expressionStr);
                assertNotNull(expression);
            } catch (ExpressionSyntaxException e) {
                fail("Failed to parse expression with '" + keyword + "' keyword in package name: " + e.getMessage());
            }
        }
    }

    /**
     * Test that the standard java.util.UUID example still works.
     */
    public void testStandardJavaPackageStillWorks() throws Exception {
        try {
            Object expression = Ognl.parseExpression("@java.util.UUID@randomUUID()");
            assertNotNull(expression);
        } catch (ExpressionSyntaxException e) {
            fail("Failed to parse standard Java package reference: " + e.getMessage());
        }
    }
}
