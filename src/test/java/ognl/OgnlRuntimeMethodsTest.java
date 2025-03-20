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

import ognl.test.objects.BaseGeneric;
import ognl.test.objects.Bean2;
import ognl.test.objects.FormImpl;
import ognl.test.objects.GameGeneric;
import ognl.test.objects.GameGenericObject;
import ognl.test.objects.GenericCracker;
import ognl.test.objects.GenericService;
import ognl.test.objects.GenericServiceImpl;
import ognl.test.objects.GetterMethods;
import ognl.test.objects.IComponent;
import ognl.test.objects.IForm;
import ognl.test.objects.ListSource;
import ognl.test.objects.ListSourceImpl;
import ognl.test.objects.OtherEnum;
import ognl.test.objects.Root;
import ognl.test.objects.SetterReturns;
import ognl.test.objects.SubclassSyntheticObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests various methods / functionality of {@link OgnlRuntime}.
 */
class OgnlRuntimeMethodsTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
    }

    @Test
    void test_Get_Super_Or_Interface_Class() {
        ListSource list = new ListSourceImpl();

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "total");
        assertNotNull(m);

        assertEquals(ListSource.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    @Test
    void test_Get_Private_Class() {
        List<String> list = Arrays.asList("hello", "world");

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "iterator");
        assertNotNull(m);

        assertEquals(Iterable.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    @Test
    void test_Complicated_Inheritance() {
        IForm form = new FormImpl();

        Method m = OgnlRuntime.getWriteMethod(form.getClass(), "clientId");
        assertNotNull(m);

        assertEquals(IComponent.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, form.getClass()));
    }

    @Test
    void test_Get_Read_Method() {
        Method m = OgnlRuntime.getReadMethod(Bean2.class, "pageBreakAfter");
        assertNotNull(m);

        assertEquals("isPageBreakAfter", m.getName());
    }

    @Test
    void test_Get_Read_Field() {
        Method m = OgnlRuntime.getReadMethod(Bean2.class, "code");
        assertNull(m);

        Field field = OgnlRuntime.getField(Bean2.class, "code");
        assertNotNull(field);
        assertEquals("code", field.getName());
    }

    @SuppressWarnings("unused")
    static class TestGetters {
        public boolean isEditorDisabled() {
            return false;
        }

        public boolean isDisabled() {
            return true;
        }

        public boolean isNotAvailable() {
            return false;
        }

        public boolean isAvailable() {
            return true;
        }
    }

    @Test
    void test_Get_Read_Method_Multiple() {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "disabled");
        assertNotNull(m);

        assertEquals("isDisabled", m.getName());
    }

    @Test
    void test_Get_Read_Method_Multiple_Boolean_Getters() {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "available");
        assertNotNull(m);

        assertEquals("isAvailable", m.getName());

        m = OgnlRuntime.getReadMethod(TestGetters.class, "notAvailable");
        assertNotNull(m);

        assertEquals("isNotAvailable", m.getName());
    }

    @Test
    void test_Find_Method_Mixed_Boolean_Getters() {
        Method m = OgnlRuntime.getReadMethod(GetterMethods.class, "allowDisplay");
        assertNotNull(m);

        assertEquals("getAllowDisplay", m.getName());
    }

    @Test
    void test_Get_Appropriate_Method() throws Exception {
        ListSource list = new ListSourceImpl();

        Object ret = OgnlRuntime.callMethod(context, list, "addValue", new String[]{null});

        assertNotNull(ret);
    }

    @Test
    void test_Call_Static_Method_Invalid_Class() {
        try {
            OgnlRuntime.callStaticMethod(context, "made.up.Name", "foo", null);

            fail("ClassNotFoundException should have been thrown by previous reference to <made.up.Name> class.");
        } catch (Exception et) {
            assertInstanceOf(MethodFailedException.class, et);
            assertTrue(et.getMessage().contains("made.up.Name"));
        }
    }

    @Test
    void test_Setter_Returns() throws Exception {
        SetterReturns root = new SetterReturns();

        Method m = OgnlRuntime.getWriteMethod(root.getClass(), "value");
        assertNotNull(m);

        Ognl.setValue("value", context, root, "12__");
        assertEquals("12__", Ognl.getValue("value", context, root));
    }

    @Test
    void test_Call_Method_VarArgs() throws Exception {
        GenericService service = new GenericServiceImpl();

        GameGenericObject argument = new GameGenericObject();

        Object[] args = new Object[2];
        args[0] = argument;

        assertEquals("Halo 3", OgnlRuntime.callMethod(context, service, "getFullMessageFor", args));
    }

    @Test
    void test_Class_Cache_Inspector() throws Exception {
        OgnlRuntime.clearCache();
        OgnlRuntime.clearAdditionalCache();  // Testing no exception only.
        assertEquals(0, OgnlRuntime.cache.propertyDescriptorCache.getSize());
        assertEquals(0, OgnlRuntime.cache.genericMethodParameterTypesCache.getSize());

        Root root = new Root();

        Node expr = Ognl.compileExpression(context, root, "property.bean3.value != null");

        assertTrue((Boolean) expr.getAccessor().get(context, root));

        int size = OgnlRuntime.cache.propertyDescriptorCache.getSize();
        assertTrue(size > 0);

        OgnlRuntime.clearCache();
        OgnlRuntime.clearAdditionalCache();  // Testing no exception only.
        assertEquals(0, OgnlRuntime.cache.propertyDescriptorCache.getSize());
        assertEquals(0, OgnlRuntime.cache.genericMethodParameterTypesCache.getSize());

        // now register class cache prevention

        OgnlRuntime.setClassCacheInspector(new TestCacheInspector());

        expr = Ognl.compileExpression(context, root, "property.bean3.value != null");
        assertTrue((Boolean) expr.getAccessor().get(context, root));

        assertEquals((size - 1), OgnlRuntime.cache.propertyDescriptorCache.getSize());
    }

    static class TestCacheInspector implements ClassCacheInspector {
        public boolean shouldCache(Class<?> type) {
            return type != null && type != Root.class;
        }
    }

    @Test
    void test_Set_Generic_Parameter_Types() {
        Method m = OgnlRuntime.getSetMethod(context, GenericCracker.class, "param");
        assertNotNull(m);

        Class<?>[] types = m.getParameterTypes();
        assertEquals(1, types.length);
        assertEquals(Integer.class, types[0]);
    }

    @Test
    void test_Get_Generic_Parameter_Types() {
        Method m = OgnlRuntime.getGetMethod(GenericCracker.class, "param");
        assertNotNull(m);

        assertEquals(Integer.class, m.getReturnType());
    }

    @Test
    void test_Find_Parameter_Types() {
        Method m = OgnlRuntime.getSetMethod(context, GameGeneric.class, "ids");
        assertNotNull(m);

        Class<?>[] types = OgnlRuntime.findParameterTypes(GameGeneric.class, m);
        assertEquals(1, types.length);
        assertEquals(Long[].class, types[0]);
    }

    @Test
    void test_Find_Parameter_Types_Superclass() {
        Method m = OgnlRuntime.getSetMethod(context, BaseGeneric.class, "ids");
        assertNotNull(m);

        Class<?>[] types = OgnlRuntime.findParameterTypes(BaseGeneric.class, m);
        assertEquals(1, types.length);
        assertEquals(Serializable[].class, types[0]);
    }

    @Test
    void test_Get_Declared_Methods_With_Synthetic_Methods() {
        List<Method> result = OgnlRuntime.getDeclaredMethods(SubclassSyntheticObject.class, "list", false);

        // synthetic method would be "public volatile java.util.List ognl.test.objects.SubclassSyntheticObject.getList()",
        // causing method return size to be 3

        assertEquals(2, result.size());
    }

    @Test
    void test_Get_Property_Descriptors_With_Synthetic_Methods() throws Exception {
        PropertyDescriptor pd = OgnlRuntime.getPropertyDescriptor(SubclassSyntheticObject.class, "list");

        assertNotNull(pd);
        assertTrue(OgnlRuntime.isMethodCallable(pd.getReadMethod()));
    }

    public static class GenericParent<T> {
        @SuppressWarnings("unused")
        void save(T entity) {
        }
    }

    public static class StringChild extends GenericParent<String> {
    }

    public static class LongChild extends GenericParent<Long> {
    }

    /**
     * Tests OGNL parameter discovery.
     */
    @Test
    void testOGNLParameterDiscovery() throws NoSuchMethodException {
        Method saveMethod = GenericParent.class.getDeclaredMethod("save", Object.class);

        Class<?>[] longClass = OgnlRuntime.findParameterTypes(LongChild.class, saveMethod);
        assertNotSame(String.class, longClass[0]);
        assertSame(Long.class, longClass[0]);

        Class<?>[] stringClass = OgnlRuntime.findParameterTypes(StringChild.class, saveMethod);
        assertNotSame(Long.class, stringClass[0], "The cached parameter types from previous calls are used");
        assertSame(String.class, stringClass[0]);
    }

    @Test
    void testBangOperator() throws Exception {
        Object value = Ognl.getValue("!'false'", context, new Object());
        assertEquals(Boolean.TRUE, value);
    }

    @Test
    void testGetStaticField() throws Exception {
        Object obj = OgnlRuntime.getStaticField(context, "ognl.test.objects.Root", "SIZE_STRING");
        assertEquals(Root.SIZE_STRING, obj);
    }

    @Test
    void testGetStaticFieldEnum() throws Exception {
        Object obj = OgnlRuntime.getStaticField(context, "ognl.test.objects.OtherEnum", "ONE");
        assertEquals(OtherEnum.ONE, obj);
    }

    @Test
    void testGetStaticFieldEnumStatic() throws Exception {
        Object obj = OgnlRuntime.getStaticField(context, "ognl.test.objects.OtherEnum", "STATIC_STRING");
        assertEquals(OtherEnum.STATIC_STRING, obj);
    }

    /**
     * This test indirectly confirms an error output (syserr) is no longer produced when OgnlRuntime
     * encounters the condition reported in issue #17. {@link OgnlRuntime#findBestMethod(List, Class, String, Class[])}
     * can find two appropriate methods with the same score where one is abstract and one is concrete.  Either
     * choice in that scenario actually worked when invoked, but produced the unwanted syserr output.
     */
    @Test
    void testAbstractConcreteMethodScoringNoSysErr() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        ObjectMethodAccessor methodAccessor = new ObjectMethodAccessor();
        ConcreteTestClass concreteTestClass = new ConcreteTestClass();
        Object result = methodAccessor.callMethod(context, concreteTestClass, "testMethod", new Object[]{"Test", 1});
        // The "Two methods with same score(0) ..." error output should no longer be seen with the above call.
        assertEquals("Test" + 1, result, "Result not concatenation of parameters ?");
    }

    /**
     * Abstract test class for issue #42 - equal score syserr output for abstract class/method hierarchy.
     *
     * @param <T>
     */
    abstract static class AbstractTestClass<T> {
        @SuppressWarnings("unused")
        public abstract String testMethod(T element, int i);
    }

    /**
     * Concrete test class for issue #42 - equal score syserr output for abstract class/method hierarchy.
     */
    static class ConcreteTestClass extends AbstractTestClass<String> {
        public String testMethod(String element, int i) {
            return element + i;
        }
    }

    /**
     * Protected class for synthetic/bridge method tests.
     */
    protected static class ProtectedParent {
        @SuppressWarnings("unused")
        public void setName(String name) {
        }

        public String getName() {
            return "name";
        }
    }

    /**
     * Public descendant class for synthetic/bridge method tests.
     */
    public static class PublicChild extends ProtectedParent {
    }

    /**
     * Test that synthetic bridge read methods can be found successfully.
     * <p>
     * Note: Only bridge methods should qualify, non-bridge synthetic methods should not.
     */
    @Test
    void testSyntheticBridgeReadMethod() {
        assertNotNull(OgnlRuntime.getReadMethod(PublicChild.class, "name"));
    }

    /**
     * Test that synthetic bridge write methods can be found successfully.
     * <p>
     * Note: Only bridge methods should qualify, non-bridge synthetic methods should not.
     */
    @Test
    void testSyntheticBridgeWriteMethod() {
        assertNotNull(OgnlRuntime.getWriteMethod(PublicChild.class, "name", new Class[]{String.class}));
    }

    /**
     * Public class for "is callable" method tests.
     */
    public static class SimplePublicClass {
        String name = "name contents";

        public String getName() {
            return name;
        }
    }

    /**
     * Public class with non-public nested class for "is callable" method tests.
     */
    public static class SimpleNestingClass {
        static class NestedClass {
            // do not use "final"
            @SuppressWarnings("final")
            private String name = "nested name contents";
        }

        public String getNestedName() {
            return new NestedClass().name;  // Should force creation of a synthetic method for NestedClass (to access its name field).
        }
    }

    /**
     * Test that normal non-synthetic methods are considered callable by both isMethodCallable() and isMethodCallable_BridgeOrNonSynthetic().
     */
    @Test
    void testConfirmStandardMethodCallability() {
        Method method = null;
        try {
            method = SimplePublicClass.class.getDeclaredMethod("getName", (Class<?>[]) null);
        } catch (NoSuchMethodException nsme) {
            fail("SimplePublicClass.getName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull(method, "getName() method retrieval failed ?");
        assertFalse(method.isBridge() || method.isSynthetic(), "SimplePublicClass.getName() is a synthetic or bridge method ?");
        assertTrue(OgnlRuntime.isMethodCallable(method), "SimplePublicClass.getName() is not considered callable by isMethodCallable() ?");
        assertTrue(OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method), "SimplePublicClass.getName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?");
    }

    /**
     * Test that bridge methods ARE considered callable by isMethodCallable_BridgeOrNonSynthetic() ONLY, and NOT by isMethodCallable().
     */
    @Test
    void testConfirmBridgeMethodCallability() {
        Method method = null;
        try {
            method = PublicChild.class.getDeclaredMethod("getName", (Class<?>[]) null);
        } catch (NoSuchMethodException nsme) {
            fail("PublicChild.getName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull(method, "getName() method retrieval failed ?");
        assertTrue(method.isBridge(), "PublicChild.getName() is not a bridge method ?");
        assertFalse(OgnlRuntime.isMethodCallable(method), "PublicChild.getName() is considered callable by isMethodCallable() ?");
        assertTrue(OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method), "PublicChild.getName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?");

        try {
            Class<?>[] argumentTypes = {String.class};
            method = PublicChild.class.getDeclaredMethod("setName", argumentTypes);
        } catch (NoSuchMethodException nsme) {
            fail("PublicChild.setName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull(method, "setName() method retrieval failed ?");
        assertTrue(method.isBridge(), "PublicChild.setName() is not a bridge method ?");
        assertFalse(OgnlRuntime.isMethodCallable(method), "PublicChild.setName() is considered callable by isMethodCallable() ?");
        assertTrue(OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method), "PublicChild.setName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?");
    }

    /**
     * Test that no synthetic method is created.
     */
    @Test
    void testConfirmNoSyntheticMethod() throws Exception {
        Method[] methods = SimpleNestingClass.NestedClass.class.getDeclaredMethods();
        assertNotNull(methods, "Nested class has no methods ?");
        // This assertion varies if called with coverage tools, as they inject synthetic methods.
        // assertEquals("Nested class has no methods ?", 0, methods.length);

        Field field = SimpleNestingClass.NestedClass.class.getDeclaredField("name");
        field.setAccessible(true);
        assertEquals("nested name contents", field.get(new SimpleNestingClass.NestedClass()));

        assertEquals("nested name contents", new SimpleNestingClass().getNestedName());
    }

    /**
     * Public class for "setFieldValue" method tests.
     */
    public static class SimpleFieldClass {
        public static String NAME = "name";
        public final List<String> numbers = Arrays.asList("one", "two", "three");
        public String gender = "male";
        public String email = "test@test.com";
        private String address = "1 Glen st";
    }

    @Test
    void testSetFieldValueWhenCheckAccess() throws OgnlException, NoSuchFieldException {

        SimpleFieldClass simpleField = new SimpleFieldClass();

        // verify that the static & final field is NOT accessible and bypass set field value
        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "NAME", "new name", true));
        assertEquals("name", SimpleFieldClass.NAME);

        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "numbers", Collections.singletonList("four"), true));
        assertEquals(3, simpleField.numbers.size());

        // verify that the field is accessible and set field value successfully
        Field genderField = SimpleFieldClass.class.getDeclaredField("gender");
        assertTrue(context.getMemberAccess().isAccessible(context, simpleField, genderField, null));
        assertTrue(OgnlRuntime.setFieldValue(context, simpleField, "gender", "female", true));
        assertEquals("female", simpleField.gender);

        // verify that the field is NOT accessible, and bypass set field value
        Field addressField = SimpleFieldClass.class.getDeclaredField("address");
        assertFalse(context.getMemberAccess().isAccessible(context, simpleField, addressField, null));
        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "address", "2 Glen st", true));
        assertEquals("1 Glen st", simpleField.address);
    }

    @Test
    void testSetFieldValueWhenNotCheckAccess() throws OgnlException, NoSuchFieldException {
        ExcludedObjectMemberAccess memberAccess = new ExcludedObjectMemberAccess(false);
        OgnlContext context = Ognl.createDefaultContext(null, memberAccess);
        SimpleFieldClass simpleField = new SimpleFieldClass();

        // verify that the static & final field is NOT accessible and bypass set field value
        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "NAME", "new name", true));
        assertEquals("name", SimpleFieldClass.NAME);

        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "numbers", Collections.singletonList("four"), true));
        assertEquals(3, simpleField.numbers.size());

        // verify that the field is accessible and set field value successfully
        Field genderField = SimpleFieldClass.class.getDeclaredField("gender");
        assertTrue(context.getMemberAccess().isAccessible(context, simpleField, genderField, null));
        assertTrue(OgnlRuntime.setFieldValue(context, simpleField, "gender", "female", true));
        assertEquals("female", simpleField.gender);

        // verify that even the field is NOT accessible, and it processes to set field value successfully
        Field emailField = SimpleFieldClass.class.getDeclaredField("email");
        memberAccess.exclude(emailField);
        assertFalse(memberAccess.isAccessible(context, simpleField, emailField, null));
        OgnlRuntime.setFieldValue(context, simpleField, "email", "admin@admin.com", true);
        assertEquals("test@test.com", simpleField.email);

        OgnlRuntime.setFieldValue(context, simpleField, "email", "admin@admin.com", false);
        assertEquals("admin@admin.com", simpleField.email);

        // verify that even the field is NOT accessible, and it processes to set field value but throws NoSuchPropertyException (as for private field)
        Field addressField = SimpleFieldClass.class.getDeclaredField("address");
        memberAccess.exclude(addressField);
        assertFalse(memberAccess.isAccessible(context, simpleField, addressField, null));
        try {
            OgnlRuntime.setFieldValue(context, simpleField, "address", "2 Glen st", true);
        } catch (NoSuchPropertyException e) {
            assertEquals("ognl.TestOgnlRuntime$SimpleFieldClass.address", e.getMessage());
            assertEquals("1 Glen st", simpleField.address);
        }
    }

}
