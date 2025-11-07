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
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for Issue #103: Class Reference Parser Fails with "or" in Package Names
 * This test verifies that OGNL can parse class references containing reserved keywords
 * like "or", "and", "not", etc. in package names.
 */
class PackageKeywordTest {

    private Root root;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        root = new Root();
        context = Ognl.createDefaultContext(root, new DefaultMemberAccess(true));
    }

    /**
     * Baseline test: java.util.UUID can be referenced (no keywords in package)
     */
    @Test
    void javaUtilUUID() throws Exception {
        Object result = Ognl.getValue("@java.util.UUID@randomUUID()", context, root);
        assertNotNull(result);
        assertEquals(UUID.class, result.getClass());
    }

    /**
     * Verify parsing works with actual Java class containing no keywords
     */
    @Test
    void classReferenceWithAtomicInteger() throws Exception {
        Object result = Ognl.getValue("@java.util.concurrent.atomic.AtomicInteger@class", context, root);
        assertEquals(java.util.concurrent.atomic.AtomicInteger.class, result);
    }

    /**
     * Issue #103: Parse expression with "or" keyword in package name.
     * Before fix: ExpressionSyntaxException at parse time
     * After fix: Should parse successfully
     */
    @Test
    void parseExpressionWithOrInPackage() throws Exception {
        try {
            Ognl.parseExpression("@jp.or.example.IdUtils@generateId()");
        } catch (ognl.ExpressionSyntaxException e) {
            fail("Parser should accept 'or' as part of package name: " + e.getMessage());
        }
    }

    /**
     * Verify "and" keyword is accepted in package names
     */
    @Test
    void parseExpressionWithAndInPackage() throws Exception {
        try {
            Ognl.parseExpression("@com.and.example.Utils@method()");
        } catch (ognl.ExpressionSyntaxException e) {
            fail("Parser should accept 'and' as part of package name: " + e.getMessage());
        }
    }

    /**
     * Verify "not" keyword is accepted in package names
     */
    @Test
    void parseExpressionWithNotInPackage() throws Exception {
        try {
            Ognl.parseExpression("@org.not.example.Utils@method()");
        } catch (ognl.ExpressionSyntaxException e) {
            fail("Parser should accept 'not' as part of package name: " + e.getMessage());
        }
    }

    /**
     * Verify "in" keyword is accepted in package names
     */
    @Test
    void parseExpressionWithInInPackage() throws Exception {
        try {
            Ognl.parseExpression("@org.example.in.Utils@method()");
        } catch (ognl.ExpressionSyntaxException e) {
            fail("Parser should accept 'in' as part of package name: " + e.getMessage());
        }
    }

    /**
     * Verify multiple keywords can be combined in package names
     */
    @Test
    void parseExpressionWithMultipleKeywordsInPackage() throws Exception {
        try {
            Ognl.parseExpression("@org.not.and.or.Utils@field");
        } catch (ognl.ExpressionSyntaxException e) {
            fail("Parser should accept multiple keywords in package name: " + e.getMessage());
        }
    }
}
