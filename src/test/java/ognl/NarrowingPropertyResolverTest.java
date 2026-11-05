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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static ognl.PropertyAccessKind.READ;
import static ognl.PropertyAccessKind.WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"rawtypes", "unchecked"})
class NarrowingPropertyResolverTest {

    private MethodAccessor previousIndexedBeanAccessor;

    @AfterEach
    void reset() {
        OgnlRuntime.setPropertyResolver(null);
        if (previousIndexedBeanAccessor != null) {
            OgnlRuntime.setMethodAccessor(IndexedBean.class, previousIndexedBeanAccessor);
        }
    }

    public static class PrivateParentIndexed {
        public int parentCalls;

        @SuppressWarnings("unused")
        private Object getItems(int i) {
            parentCalls++;
            return "parent:" + i;
        }
    }

    public static class PublicChildIndexed extends PrivateParentIndexed {
        public int childCalls;

        public Object getItems(int i) {
            childCalls++;
            return "child:" + i;
        }

        public Object[] getItems() {
            return new Object[]{"a", "b"};
        }
    }

    public static class PublicParentIndexed {
        public Object getItems(int i) {
            return "parent:" + i;
        }

        public Object[] getItems() {
            return new Object[]{"a", "b"};
        }
    }

    public static class OverridingChildIndexed extends PublicParentIndexed {
        @Override
        public Object getItems(int i) {
            return "child:" + i;
        }
    }

    public static class HiddenFieldBase {
        public String name = "base";
    }

    public static class HiddenFieldSub extends HiddenFieldBase {
        public String name = "sub";
    }

    private static class NamedIndexedAccessDenied extends DefaultMemberAccess {
        NamedIndexedAccessDenied() {
            super(false);
        }

        @Override
        public boolean isAccessible(OgnlContext context, Object target, java.lang.reflect.Member member, String propertyName) {
            if (member.getName().equals("getItems") && propertyName != null) {
                return false;
            }
            return super.isAccessible(context, target, member, propertyName);
        }
    }

    private static class DenyingMethodAccessor implements MethodAccessor {
        @Override
        public Object callStaticMethod(OgnlContext context, Class targetClass, String methodName, Object[] args) {
            return "DENIED";
        }

        @Override
        public Object callMethod(OgnlContext context, Object target, String methodName, Object[] args) {
            return "DENIED";
        }
    }

    private static void keep(BiPredicate<PropertyAccessKind, PropertyCandidate> predicate) {
        OgnlRuntime.setPropertyResolver((c, n, k) -> DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                .filter(candidate -> predicate.test(k, candidate)).collect(Collectors.toList()));
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
        List<Step> order = Arrays.asList(Step.FIELD, Step.ACCESSOR, Step.LENIENT);
        OgnlRuntime.setPropertyResolver((c, n, k) -> DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                .sorted(Comparator.comparingInt(candidate -> order.indexOf(candidate.step()))).collect(Collectors.toList()));
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
        Ognl.setValue("value", ctx, bothBean, "x");
        assertEquals("x", bothBean.value);
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
    void narrowedOverloadIsUnreachable() {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ
                || isStringKeyed(c));
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        OgnlException ex = assertThrows(OgnlException.class, () -> Ognl.getValue("items[1]", ctx, bean));
        assertTrue(ex.getMessage().contains("items"), ex.getMessage());
        assertEquals(0, bean.getItemsIntCalls);
    }

    @Test
    void offeredOverloadSelectedByArgumentRuns() throws Exception {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ
                || isStringKeyed(c));
        IndexedBean bean = new IndexedBean();
        assertEquals("key:key", Ognl.getValue("items['key']", ctx(bean), bean));
        assertEquals(0, bean.getItemsIntCalls);
    }

    @Test
    void duplicatedNarrowedListStaysNarrowed() {
        OgnlRuntime.setPropertyResolver((c, n, k) -> {
            List<PropertyCandidate> kept = DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                    .filter(candidate -> k != PropertyAccessKind.INDEXED_READ
                            || isStringKeyed(candidate))
                    .collect(Collectors.toList());
            return k == PropertyAccessKind.INDEXED_READ
                    ? java.util.Collections.nCopies(3, kept.get(0)) : kept;
        });
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        assertThrows(OgnlException.class, () -> Ognl.getValue("items[1]", ctx, bean));
        assertEquals(0, bean.getItemsIntCalls);
    }

    @Test
    void indexedReadGoesThroughMethodAccessor() throws Exception {
        previousIndexedBeanAccessor = OgnlRuntime.getMethodAccessor(IndexedBean.class);
        OgnlRuntime.setMethodAccessor(IndexedBean.class, new DenyingMethodAccessor());
        IndexedBean bean = new IndexedBean();
        assertEquals("DENIED", Ognl.getValue("items[1]", ctx(bean), bean));

        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ || !isStringKeyed(c));
        assertEquals("DENIED", Ognl.getValue("items[1]", ctx(bean), bean));
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



    private static boolean isIntKeyed(PropertyCandidate candidate) {
        return ((java.lang.reflect.Method) candidate.member()).getParameterTypes()[0] == int.class;
    }


    @Test
    void narrowedIndexedWriteOverloadIsUnreachable() {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_WRITE || isIntKeyed(c));
        OverloadedIndexedBean bean = new OverloadedIndexedBean();
        OgnlContext ctx = ctx(bean);
        assertThrows(OgnlException.class, () -> Ognl.setValue("items['key']", ctx, bean, "v"));
        assertEquals(0, bean.stringWrites);
        assertNull(bean.lastWrite);
    }



    @Test
    void readOrderIsHonoured() throws Exception {
        List<Step> order = Arrays.asList(Step.FIELD, Step.ACCESSOR, Step.LENIENT);
        OgnlRuntime.setPropertyResolver((c, n, k) -> DefaultPropertyResolver.INSTANCE.candidates(c, n, k).stream()
                .sorted(Comparator.comparingInt(candidate -> order.indexOf(candidate.step()))).collect(Collectors.toList()));
        BothBean bean = new BothBean();
        bean.value = "v";
        assertEquals("v", Ognl.getValue("value", ctx(bean), bean));
    }




    @Test
    void privateParentOverloadDoesNotStandInForChild() {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ
                || c.member().getDeclaringClass() == PrivateParentIndexed.class);
        PublicChildIndexed bean = new PublicChildIndexed();
        OgnlContext ctx = ctx(bean);
        assertThrows(OgnlException.class, () -> Ognl.getValue("items[1]", ctx, bean));
        assertEquals(0, bean.childCalls);
        assertEquals(0, bean.parentCalls);
    }

    @Test
    void overriddenParentOverloadCoversChild() throws Exception {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ
                || c.member().getDeclaringClass() == PublicParentIndexed.class);
        OverridingChildIndexed bean = new OverridingChildIndexed();
        assertEquals("child:1", Ognl.getValue("items[1]", ctx(bean), bean));
    }

    @Test
    void offeredSuperclassFieldIsReadInsteadOfHidingField() throws Exception {
        java.lang.reflect.Field baseName = HiddenFieldBase.class.getField("name");
        OgnlRuntime.setPropertyResolver((c, n, k) -> c == HiddenFieldSub.class && n.equals("name") && k == READ
                ? Collections.singletonList(PropertyCandidate.field(baseName))
                : DefaultPropertyResolver.INSTANCE.candidates(c, n, k));
        HiddenFieldSub bean = new HiddenFieldSub();
        OgnlContext ctx = ctx(bean);
        assertEquals("base", Ognl.getValue("name", ctx, bean));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void firstUsableIndexedCandidateChecksAccessLikeDispatch() throws Exception {
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = Ognl.createDefaultContext(bean, new NamedIndexedAccessDenied());
        assertEquals("b", Ognl.getValue("items[1]", ctx, bean));
        assertTrue(OgnlRuntime.firstUsableCandidate(ctx, bean, "items", PropertyAccessKind.INDEXED_READ).isPresent());
    }

    @Test
    void narrowedObjectIndexedRead() {
        keep((k, c) -> k != PropertyAccessKind.INDEXED_READ);
        ObjectIndexedBean bean = new ObjectIndexedBean();
        bean.attrs.put("key", "v");
        OgnlContext ctx = ctx(bean);
        assertThrows(OgnlException.class, () -> Ognl.getValue("attr['key']", ctx, bean));
        assertEquals(0, bean.getAttrCalls);
    }



    @Test
    void defaultCompiledUnchanged() throws Exception {
        BothBean bean = new BothBean();
        bean.value = "v";
        OgnlContext ctx = ctx(bean);
        Node compiled = Ognl.compileExpression(ctx, bean, "value");
        assertFalse(PropertyResolutionFixtures.getterDelegatesToInterpreted(compiled, ctx, bean));
        assertEquals("g:v", compiled.getAccessor().get(ctx, bean));
        assertFalse(PropertyResolutionFixtures.setterDelegatesToInterpreted(compiled, ctx, bean, "x"));
        assertEquals("m:x", bean.value);
        assertEquals(1, bean.setValueCalls);
    }

    @Test
    void getMethodValueSkipsNarrowedOutGetter() {
        drop(READ, Step.ACCESSOR);
        BothBean bean = new BothBean();
        bean.value = "v";
        OgnlContext ctx = ctx(bean);
        assertEquals(OgnlRuntime.NotFound, assertDoesNotThrowValue(() -> OgnlRuntime.getMethodValue(ctx, bean, "value", true)));
        assertThrows(NoSuchMethodException.class, () -> OgnlRuntime.getMethodValue(ctx, bean, "value", false));
    }

    @Test
    void setMethodValueSkipsNarrowedOutSetter() throws Exception {
        drop(WRITE, Step.ACCESSOR);
        BothBean bean = new BothBean();
        OgnlContext ctx = ctx(bean);
        assertFalse(OgnlRuntime.setMethodValue(ctx, bean, "value", "x", true));
        assertFalse(OgnlRuntime.setMethodValue(ctx, bean, "value", "x", false));
        assertEquals(0, bean.setValueCalls);
        assertNull(bean.value);
    }

    @Test
    void getFieldValueSkipsNarrowedOutField() {
        drop(READ, Step.FIELD);
        BothBean bean = new BothBean();
        bean.value = "v";
        OgnlContext ctx = ctx(bean);
        assertEquals(OgnlRuntime.NotFound, assertDoesNotThrowValue(() -> OgnlRuntime.getFieldValue(ctx, bean, "value", true)));
        assertThrows(NoSuchFieldException.class, () -> OgnlRuntime.getFieldValue(ctx, bean, "value", false));
    }

    @Test
    void setFieldValueSkipsNarrowedOutField() throws Exception {
        drop(WRITE, Step.FIELD);
        BothBean bean = new BothBean();
        OgnlContext ctx = ctx(bean);
        assertFalse(OgnlRuntime.setFieldValue(ctx, bean, "value", "x", true));
        assertFalse(OgnlRuntime.setFieldValue(ctx, bean, "value", "x", false));
        assertNull(bean.value);
    }

    @Test
    void stepNotFittingKindIsRejectedClearly() throws Exception {
        PropertyCandidate field = PropertyCandidate.field(BothBean.class.getField("value"));
        PropertyCandidate indexed = PropertyCandidate.indexed(IndexedBean.class.getMethod("getItems", int.class));
        PropertyResolver resolver = (c, n, k) -> Collections.singletonList(k == PropertyAccessKind.INDEXED_READ ? field : indexed);
        OgnlRuntime.setPropertyResolver(resolver);
        IndexedBean bean = new IndexedBean();
        OgnlContext ctx = ctx(bean);
        for (PropertyAccessKind kind : Arrays.asList(PropertyAccessKind.INDEXED_READ, READ, WRITE)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> OgnlRuntime.firstUsableCandidate(ctx, bean, "items", kind));
            assertTrue(ex.getMessage().contains(resolver.getClass().getName())
                    && ex.getMessage().contains(IndexedBean.class.getName())
                    && ex.getMessage().contains("'items'")
                    && ex.getMessage().contains(kind.name()), ex.getMessage());
        }
    }

    private interface ThrowingSupplier {
        Object get() throws Exception;
    }

    private static Object assertDoesNotThrowValue(ThrowingSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
