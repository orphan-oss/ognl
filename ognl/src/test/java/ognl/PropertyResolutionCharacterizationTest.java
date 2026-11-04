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

import ognl.PropertyResolutionFixtures.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"rawtypes", "unchecked"})
class PropertyResolutionCharacterizationTest {

    @Test
    void lombokNameReadsAndWrites() throws Exception {
        LombokBean bean = new LombokBean();
        OgnlContext ctx = ctx(bean);
        Ognl.setValue("uRange", ctx, bean, "a");
        assertEquals("a", Ognl.getValue("uRange", ctx, bean));
    }

    @Test
    void specNameReadsAndWrites() throws Exception {
        SpecBean bean = new SpecBean();
        OgnlContext ctx = ctx(bean);
        Ognl.setValue("uRange", ctx, bean, "a");
        assertEquals("a", Ognl.getValue("uRange", ctx, bean));
    }

    @Test
    void lenientHasReads() throws Exception {
        LenientBean bean = new LenientBean();
        assertEquals(false, Ognl.getValue("children", ctx(bean), bean));
    }

    @Test
    void lenientPlainNameReads() throws Exception {
        LenientBean bean = new LenientBean();
        assertEquals("lbl", Ognl.getValue("label", ctx(bean), bean));
    }

    @Test
    void ignoreReadMethodsHidesLenientRead() {
        LenientBean bean = new LenientBean();
        OgnlContext ctx = ctx(bean);
        ctx.setIgnoreReadMethods(true);
        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue("label", ctx, bean));
    }

    @Test
    void publicFieldReadsAndWrites() throws Exception {
        FieldBean bean = new FieldBean();
        OgnlContext ctx = ctx(bean);
        Ognl.setValue("name", ctx, bean, "n");
        assertEquals("n", bean.name);
        assertEquals("n", Ognl.getValue("name", ctx, bean));
    }

    @Test
    void superclassFieldReads() throws Exception {
        SubFieldBean bean = new SubFieldBean();
        bean.name = "n";
        assertEquals("n", Ognl.getValue("name", ctx(bean), bean));
    }

    @Test
    void finalFieldWriteFails() {
        FieldBean bean = new FieldBean();
        OgnlContext ctx = ctx(bean);
        assertThrows(NoSuchPropertyException.class, () -> Ognl.setValue("fixed", ctx, bean, "x"));
        assertEquals("f", bean.fixed);
    }

    @Test
    void staticFieldReadThrowsPlainOgnlException() {
        FieldBean bean = new FieldBean();
        OgnlContext ctx = ctx(bean);
        OgnlException ex = assertThrows(OgnlException.class, () -> Ognl.getValue("shared", ctx, bean));
        assertEquals(OgnlException.class, ex.getClass());
    }

    @Test
    void methodWinsOverField() throws Exception {
        BothBean bean = new BothBean();
        OgnlContext ctx = ctx(bean);
        Ognl.setValue("value", ctx, bean, "x");
        assertEquals("m:x", bean.value);
        assertEquals(1, bean.setValueCalls);
        assertEquals("g:m:x", Ognl.getValue("value", ctx, bean));
    }

    @Test
    void deniedSetterFallsBackToField() throws Exception {
        BothBean bean = new BothBean();
        OgnlContext ctx = ctx(bean, "setValue");
        Ognl.setValue("value", ctx, bean, "x");
        assertEquals("x", bean.value);
        assertEquals(0, bean.setValueCalls);
    }

    @Test
    void deniedGetterGoesToFieldNotLenient() throws Exception {
        BothBean bean = new BothBean();
        bean.value = "raw";
        assertEquals("raw", Ognl.getValue("value", ctx(bean, "getValue"), bean));
    }

    @Test
    void fieldConversionFailureDoesNotFallBackToLenient() {
        ConvBean bean = new ConvBean();
        OgnlContext ctx = ctx(bean);
        OgnlException ex = assertThrows(OgnlException.class, () -> Ognl.setValue("count", ctx, bean, new Object()));
        assertEquals(NumberFormatException.class, ex.getCause().getClass());
        assertNull(bean.lastLenient);
    }

    @Test
    void indexedIntReadAndWrite() throws Exception {
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        assertEquals("b", Ognl.getValue("items[1]", ctx, bean));
        assertEquals(1, bean.getItemsIntCalls);
        Ognl.setValue("items[1]", ctx, bean, "z");
        assertEquals("z", bean.items[1]);
    }

    @Test
    void indexedOverloadByIndexType() throws Exception {
        IndexedBean bean = new IndexedBean();
        assertEquals("key:key", Ognl.getValue("items['key']", ctx(bean), bean));
    }

    @Test
    void objectIndexedReadAndWrite() throws Exception {
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        Ognl.setValue("attr['k']", ctx, bean, "v");
        assertEquals("v", Ognl.getValue("attr['k']", ctx, bean));
    }

    @Test
    void recordComponentReads() throws Exception {
        Point point = new Point(1, 2);
        assertEquals(1, Ognl.getValue("x", ctx(point), point));
    }

    @Test
    void hasGetAndSetPropertyToday() throws Exception {
        assertHas(new LombokBean(), "uRange", true, true);
        assertHas(new SpecBean(), "uRange", true, true);
        assertHas(new LenientBean(), "label", false, false);
        assertHas(new FieldBean(), "name", true, true);
        assertHas(new FieldBean(), "shared", false, false);
        assertHas(new FieldBean(), "fixed", true, false);
        assertHas(new Point(1, 2), "x", true, false);
    }

    @Test
    void recordAccessorCountsWhenGetterIsDenied() throws Exception {
        RecordWithGetter bean = new RecordWithGetter("v");
        assertTrue(OgnlRuntime.hasGetProperty(ctx(bean, "getFoo"), bean, "foo"));
    }

    @Test
    void compiledReadMatchesInterpreted() throws Exception {
        LombokBean lombok = new LombokBean();
        lombok.setURange("u");
        assertCompiledMatches(lombok, "uRange");

        FieldBean field = new FieldBean();
        field.name = "n";
        assertCompiledMatches(field, "name");

        ObjectIndexedBean objectIndexed = new ObjectIndexedBean();
        objectIndexed.attrs.put("key", "v");
        assertCompiledMatches(objectIndexed, "attr['key']");

        IndexedBean indexed = new IndexedBean();
        OgnlContext ctx = ctx(indexed);
        Node compiled = Ognl.compileExpression(ctx, indexed, "items[1]");
        assertEquals(Ognl.getValue("items[1]", ctx, indexed), compiled.getAccessor().get(ctx, indexed));
    }

    @Test
    void defaultCompiledKeepsPre755LenientChoice() throws Exception {
        PlainAndIsBean plainAndIs = new PlainAndIsBean();
        OgnlContext ctx = ctx(plainAndIs);
        assertEquals("is", Ognl.getValue("foo", ctx, plainAndIs));
        Node compiled = Ognl.compileExpression(ctx, plainAndIs, "foo");
        assertFalse(PropertyResolutionFixtures.getterDelegatesToInterpreted(compiled, ctx, plainAndIs));
        assertEquals("plain", compiled.getAccessor().get(ctx, plainAndIs));
    }

    @Test
    void defaultCompiledKeepsPre755IgnoreReadMethodsBypass() throws Exception {
        LenientBean lenient = new LenientBean();
        OgnlContext ctx = ctx(lenient);
        ctx.setIgnoreReadMethods(true);
        Node compiled = Ognl.compileExpression(ctx, lenient, "label");
        assertFalse(PropertyResolutionFixtures.getterDelegatesToInterpreted(compiled, ctx, lenient));
        assertEquals("lbl", compiled.getAccessor().get(ctx, lenient));
    }

    private static OgnlContext ctx(Object root, String... deniedMemberNames) {
        return PropertyResolutionFixtures.context(root, deniedMemberNames);
    }

    private static void assertHas(Object target, String name, boolean get, boolean set) throws Exception {
        OgnlContext ctx = ctx(target);
        assertEquals(get, OgnlRuntime.hasGetProperty(ctx, target, name), "hasGet " + name);
        assertEquals(set, OgnlRuntime.hasSetProperty(ctx, target, name), "hasSet " + name);
    }

    private static void assertCompiledMatches(Object bean, String expression) throws Exception {
        OgnlContext ctx = ctx(bean);
        Object interpreted = Ognl.getValue(expression, ctx, bean);
        Node compiled = Ognl.compileExpression(ctx, bean, expression);
        assertFalse(PropertyResolutionFixtures.getterDelegatesToInterpreted(compiled, ctx, bean), expression + " fell back to interpreted");
        assertEquals(interpreted, compiled.getAccessor().get(ctx, bean));
    }
}
