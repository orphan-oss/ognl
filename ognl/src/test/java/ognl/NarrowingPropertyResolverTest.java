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

import ognl.PropertyCandidate.Step;
import ognl.PropertyResolutionFixtures.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import static ognl.PropertyAccessKind.READ;
import static ognl.PropertyAccessKind.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"rawtypes", "unchecked"})
class NarrowingPropertyResolverTest {

    @AfterEach
    void reset() {
        OgnlRuntime.setPropertyResolver(null);
    }

    private static void keep(BiPredicate<PropertyAccessKind, PropertyCandidate> predicate) {
        OgnlRuntime.setPropertyResolver((c, n, k) -> DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                .filter(candidate -> predicate.test(k, candidate)).toList());
    }

    private static void drop(PropertyAccessKind kind, Step step) {
        keep((k, c) -> k != kind || c.step() != step);
    }

    private static void drop(Step step) {
        keep((k, c) -> c.step() != step);
    }

    private static boolean isStringKeyed(PropertyCandidate candidate) {
        Class<?>[] types = ((java.lang.reflect.Method) candidate.member()).getParameterTypes();
        return types.length == 1 && types[0] == String.class;
    }

    private static OgnlContext ctx(Object root, String... denied) {
        return PropertyResolutionFixtures.context(root, denied);
    }

    @Test
    void droppingAccessorReadFallsToField() throws Exception {
        drop(READ, Step.ACCESSOR);
        BothBean bean = new BothBean();
        bean.value = "v";
        assertEquals("v", Ognl.getValue("value", ctx(bean), bean));
    }

    @Test
    void droppingFieldMakesFieldOnlyPropertyMissing() {
        drop(Step.FIELD);
        FieldBean bean = new FieldBean();
        OgnlContext ctx = ctx(bean);
        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue("name", ctx, bean));
        assertThrows(NoSuchPropertyException.class, () -> Ognl.setValue("name", ctx, bean, "n"));
        assertNull(bean.name);
    }

    @Test
    void droppingLenientHidesLenientRead() {
        drop(Step.LENIENT);
        LenientBean bean = new LenientBean();
        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue("label", ctx(bean), bean));
    }

    @Test
    void exactSetterOnlyStopsFieldAndLenient() {
        keep((k, c) -> k != WRITE || c.step() == Step.ACCESSOR);
        BothBean bean = new BothBean();
        OgnlContext ctx = ctx(bean, "setValue");
        assertThrows(NoSuchPropertyException.class, () -> Ognl.setValue("value", ctx, bean, "x"));
        assertNull(bean.value);
    }

    @Test
    void orderIsHonoured() throws Exception {
        List<Step> order = List.of(Step.FIELD, Step.ACCESSOR, Step.LENIENT);
        OgnlRuntime.setPropertyResolver((c, n, k) -> DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                .sorted(Comparator.comparingInt(candidate -> order.indexOf(candidate.step()))).toList());
        BothBean bean = new BothBean();
        Ognl.setValue("value", ctx(bean), bean, "x");
        assertEquals("x", bean.value);
        assertEquals(0, bean.setValueCalls);
    }

    @Test
    void duplicateMemberInvokedOnce() throws Exception {
        BothBean bean = new BothBean();
        Ognl.setValue("value", ctx(bean), bean, "x");
        assertEquals("m:x", bean.value);
        assertEquals(1, bean.setValueCalls);
    }

    @Test
    void ignoreReadMethodsWithLenientOnlyList() {
        keep((k, c) -> c.step() == Step.LENIENT);
        LenientBean bean = new LenientBean();
        OgnlContext ctx = ctx(bean);
        ctx.setIgnoreReadMethods(true);
        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue("label", ctx, bean));
    }

    @Test
    void hasPropertiesFollowNarrowing() throws Exception {
        GetterOnlyBean getterOnly = new GetterOnlyBean();
        assertTrue(OgnlRuntime.hasGetProperty(ctx(getterOnly), getterOnly, "title"));
        assertFalse(OgnlRuntime.hasSetProperty(ctx(getterOnly), getterOnly, "title"));

        drop(Step.FIELD);
        FieldBean fieldBean = new FieldBean();
        assertFalse(OgnlRuntime.hasGetProperty(ctx(fieldBean), fieldBean, "name"));
        assertFalse(OgnlRuntime.hasSetProperty(ctx(fieldBean), fieldBean, "name"));
        assertTrue(OgnlRuntime.hasGetProperty(ctx(getterOnly), getterOnly, "title"));

        drop(READ, Step.ACCESSOR);
        assertFalse(OgnlRuntime.hasGetProperty(ctx(getterOnly), getterOnly, "title"));

        drop(WRITE, Step.ACCESSOR);
        BothBean bothBean = new BothBean();
        OgnlContext ctx = ctx(bothBean);
        assertFalse(OgnlRuntime.hasSetMethod(ctx, bothBean, BothBean.class, "value"));
        assertTrue(OgnlRuntime.hasSetProperty(ctx, bothBean, "value"));
    }

    @Test
    void recordAccessorNeedsCandidate() throws Exception {
        Point point = new Point(1, 2);
        assertTrue(OgnlRuntime.hasGetProperty(ctx(point), point, "x"));

        drop(Step.LENIENT);
        OgnlContext ctx = ctx(point);
        assertThrows(NoSuchPropertyException.class, () -> Ognl.getValue("x", ctx, point));
        assertFalse(OgnlRuntime.hasGetProperty(ctx, point, "x"));
    }

    @Test
    void firstUsableCandidateMatchesEvaluation() throws Exception {
        BothBean bean = new BothBean();
        OgnlContext ctx = ctx(bean, "setValue");
        Optional<PropertyCandidate> candidate = OgnlRuntime.firstUsableCandidate(ctx, bean, "value", WRITE);
        assertEquals(Optional.of(PropertyCandidate.field(BothBean.class.getField("value"))), candidate);
        Ognl.setValue("value", ctx, bean, "x");
        assertEquals("x", bean.value);
    }

    @Test
    void firstUsableCandidateEmptyWhenNothingUsable() {
        FieldBean bean = new FieldBean();
        assertEquals(Optional.empty(), OgnlRuntime.firstUsableCandidate(ctx(bean), bean, "fixed", WRITE));
    }

    @Test
    void droppingIndexedMakesPropertyNotIndexed() throws Exception {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ && k != PropertyAccessKind.INDEXED_WRITE);
        IndexedBean bean = new IndexedBean();
        assertEquals(OgnlRuntime.INDEXED_PROPERTY_NONE, OgnlRuntime.getIndexedPropertyType(IndexedBean.class, "items"));
        assertEquals("b", Ognl.getValue("items[1]", ctx(bean), bean));
        assertEquals(0, bean.getItemsIntCalls);
    }

    @Test
    void narrowedOverloadIsUnreachable() throws Exception {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ
                || isStringKeyed(c));
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        assertEquals("key:1", Ognl.getValue("items[1]", ctx, bean));
        assertEquals(0, bean.getItemsIntCalls);
    }

    @Test
    void duplicatedNarrowedListStaysNarrowed() throws Exception {
        OgnlRuntime.setPropertyResolver((c, n, k) -> {
            List<PropertyCandidate> kept = DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                    .filter(candidate -> k != PropertyAccessKind.INDEXED_READ
                            || isStringKeyed(candidate))
                    .toList();
            return k == PropertyAccessKind.INDEXED_READ
                    ? java.util.Collections.nCopies(3, kept.get(0)) : kept;
        });
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        assertEquals("key:1", Ognl.getValue("items[1]", ctx, bean));
        assertEquals(0, bean.getItemsIntCalls);
    }

    @Test
    void narrowedIndexedWrite() {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_WRITE
                || !((java.lang.reflect.Method) c.member()).getName().equals("setAttr"));
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        assertThrows(OgnlException.class, () -> Ognl.setValue("attr['k']", ctx, bean, "v"));
        assertTrue(bean.attrs.isEmpty());
    }
}
