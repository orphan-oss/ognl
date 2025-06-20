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
package ognl;

import ognl.test.objects.Root;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OgnlContextTest {

    private static OgnlContext createOgnlContext() {
        return new OgnlContext(new DefaultMemberAccess(false), null, null);
    }

    @Test
    void traceEvaluation_shouldBeEnabled() {
        OgnlContext context = createOgnlContext();
        context.setTraceEvaluations(true);

        assertTrue(context.isTraceEvaluations());
        assertEquals(Boolean.TRUE, context.get("_traceEvaluations"));
    }

    @Test
    void keepLastEvaluation_shouldBeEnabled() {
        OgnlContext context = createOgnlContext();
        context.setKeepLastEvaluation(true);
        assertTrue(context.isKeepLastEvaluation());
        assertEquals(Boolean.TRUE, context.get("_keepLastEvaluation"));
    }

    @Test
    void allValues_shouldBeStored() {
        OgnlContext context = createOgnlContext();
        Map<String, Object> values = new HashMap<>();
        values.put("key1", "value1");
        values.put("key2", "value2");

        context.setValues(values);

        assertEquals(values, context.getValues());
    }

    @Test
    void classResolver_shouldNotBeNull() {
        OgnlContext context = createOgnlContext();

        assertNotNull(context.getClassResolver());
        assertEquals(DefaultClassResolver.class, context.getClassResolver().getClass());
    }

    @Test
    void typeConverted_shouldNotBeNull() {
        OgnlContext context = createOgnlContext();

        assertNotNull(context.getTypeConverter());
        assertEquals(DefaultTypeConverter.class, context.getTypeConverter().getClass());
    }

    @Test
    void memberAccess_shouldNotBeNull() {
        OgnlContext context = createOgnlContext();

        assertNotNull(context.getMemberAccess());
        assertEquals(DefaultMemberAccess.class, context.getMemberAccess().getClass());
    }

    @Test
    void root_shouldInitAccessorAndType() {
        OgnlContext context = createOgnlContext();

        Root root = new Root();
        context.setRoot(root);

        assertNotNull(context.getRoot());
        assertNotNull(context.getCurrentObject());
        assertNull(context.getCurrentNode());

        assertNull(context.getCurrentAccessor());
        assertNull(context.getFirstAccessor());
        assertNull(context.getPreviousAccessor());

        assertNotNull(context.getCurrentType());
        assertEquals(Root.class, context.getCurrentType());
        assertNotNull(context.getFirstType());
        assertEquals(Root.class, context.getFirstType());
        assertNull(context.getPreviousType());
        assertEquals(root, context.get("root"));
    }

    @Test
    void currentEvaluation_shouldNotBeNull() throws OgnlException {
        OgnlContext context = createOgnlContext();
        Root root = new Root();
        context.setRoot(root);

        Object result = Ognl.getValue("index", context, root);

        assertNotNull(result);
        assertEquals(1, result);
        assertNull(context.getCurrentEvaluation());
        assertNull(context.getRootEvaluation());
        assertNull(context.getLastEvaluation());
    }

    @Test
    void ignoreReadMethod() {
        OgnlContext context = createOgnlContext();
        assertFalse(context.isIgnoreReadMethods());
        assertEquals(Boolean.FALSE, context.get("_ignoreReadMethods"));
        context.setIgnoreReadMethods(true);
        assertTrue(context.isIgnoreReadMethods());
        assertEquals(Boolean.TRUE, context.get("_ignoreReadMethods"));
        assertEquals(Boolean.TRUE, context.put("_ignoreReadMethods", false));
        assertFalse(context.isIgnoreReadMethods());
        assertEquals(Boolean.FALSE, context.get("_ignoreReadMethods"));
        assertThrows(IllegalArgumentException.class, () -> context.remove("_ignoreReadMethods"));
    }

    @Test
    void reservedKeywords() {
        // given
        OgnlContext context = createOgnlContext();
        Object root = new Object();

        // when
        context.put("root", root);

        // then
        assertSame(root, context.get("root"));
        assertNull(context.getValues().get("root"));

        // when
        context.put("this", root);

        // then
        assertSame(root, context.get("this"));
        assertNull(context.getValues().get("this"));

        // when
        assertFalse(context.isTraceEvaluations());
        context.put("_traceEvaluations", Boolean.TRUE);

        // then
        assertSame(Boolean.TRUE, context.get("_traceEvaluations"));
        assertTrue(context.isTraceEvaluations());
        assertNull(context.getValues().get("_traceEvaluations"));

        // given
        Evaluation evaluation = new Evaluation(new ASTConst(0), root);

        // when
        assertNull(context.getLastEvaluation());
        context.put("_lastEvaluation", evaluation);

        // then
        assertSame(evaluation, context.get("_lastEvaluation"));
        assertSame(evaluation, context.getLastEvaluation());
        assertNull(context.getValues().get("_lastEvaluation"));

        // when
        assertFalse(context.isKeepLastEvaluation());
        context.put("_keepLastEvaluation", Boolean.TRUE);

        // then
        assertSame(Boolean.TRUE, context.get("_keepLastEvaluation"));
        assertTrue(context.isKeepLastEvaluation());
        assertNull(context.getValues().get("_keepLastEvaluation"));
    }

    @Test
    void memberAccessIsRequired() {
        try {
            new OgnlContext((MemberAccess) null, null, null);
        } catch (Exception e) {
            assertInstanceOf(IllegalArgumentException.class, e);
            assertEquals("MemberAccess implementation must be provided - null not permitted!", e.getMessage());
        }
    }

    @Test
    void defaultClassResolverAndTypeConverter() {
        // given & when
        OgnlContext context = new OgnlContext(new DefaultMemberAccess(false), null, null);

        // then
        assertTrue(context.getValues().isEmpty());
        assertTrue(context.isEmpty());
        assertInstanceOf(DefaultClassResolver.class, context.getClassResolver());
        assertInstanceOf(DefaultTypeConverter.class, context.getTypeConverter());
    }
}
