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
import ognl.SimpleNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaExpressionTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() throws Exception {
        this.context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    private SimpleNode getExpression(Object root, String expressionStr) throws Exception {
        // validate expression
        Ognl.parseExpression(expressionStr);
        // compile expression
        return (SimpleNode) Ognl.compileExpression(context, root, expressionStr);
    }

    @Test
    void shouldReadArrayLength() throws Exception {
        // given
        Object root = new Object[]{};
        String expressionStr = "#a=:[33](20).longValue().{0}.toArray().length";
        int expectedResult = 33;

        // when
        SimpleNode expression = getExpression(root, expressionStr);

        // then
        assertEquals(expectedResult, Ognl.getValue(expression, context, root));
    }

    @Test
    void shouldEvaluateLambda1() throws Exception {
        // given
        Object root = null;
        String expressionStr = "#fact=:[#this <=1 ? 1 : #fact(#this-1) * #this], #fact(30)";
        int expectedResult = 1409286144;

        // when
        SimpleNode expression = getExpression(root, expressionStr);

        // then
        assertEquals(expectedResult, Ognl.getValue(expression, context, root));
    }

    @Test
    void shouldEvaluateLambda2() throws Exception {
        // given
        Object root = null;
        String expressionStr = "#fact=:[#this <= 1 ? 1 : #fact(#this-1) * #this], #fact(30L)";
        long expectedResult = -8764578968847253504L;

        // when
        SimpleNode expression = getExpression(root, expressionStr);

        // then
        assertEquals(expectedResult, Ognl.getValue(expression, context, root));
    }

    @Test
    void shouldEvaluateLambda3() throws Exception {
        // given
        Object root = null;
        String expressionStr = "#fact=:[#this <= 1 ? 1 : #fact(#this-1) * #this], #fact(30h)";
        BigInteger expectedResult = new BigInteger("265252859812191058636308480000000");

        // when
        SimpleNode expression = getExpression(root, expressionStr);

        // then
        assertEquals(expectedResult, Ognl.getValue(expression, context, root));
    }

    @Test
    void shouldEvaluateLambda4() throws Exception {
        // given
        Object root = null;
        String expressionStr = "#bump = :[ #this.{ #this + 1 } ], (#bump)({ 1, 2, 3 })";
        List<Integer> expectedResult = Arrays.asList(2, 3, 4);

        // when
        SimpleNode expression = getExpression(root, expressionStr);

        // then
        assertEquals(expectedResult, Ognl.getValue(expression, context, root));
    }

    @Test
    void shouldEvaluateLambda5() throws Exception {
        // given
        Object root = null;
        String expressionStr = "#call = :[ \"calling \" + [0] + \" on \" + [1] ], (#call)({ \"x\", \"y\" })";
        String expectedResult = "calling x on y";

        // when
        SimpleNode expression = getExpression(root, expressionStr);

        // then
        assertEquals(expectedResult, Ognl.getValue(expression, context, root));
    }

}
