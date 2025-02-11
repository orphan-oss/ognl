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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ognl.test.OgnlTestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests various methods / functionality of {@link OgnlRuntime}.
 */
public class TestOgnlRuntime {

    private OgnlContext context;

    @Before
    public void setUp() throws Exception {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @Test
    public void test_Get_Super_Or_Interface_Class() {
        ListSource list = new ListSourceImpl();

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "total");
        assertNotNull(m);

        assertEquals(ListSource.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    @Test
    public void test_Get_Private_Class() {
        List<String> list = Arrays.asList("hello", "world");

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "iterator");
        assertNotNull(m);

        assertEquals(Iterable.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    @Test
    public void test_Complicated_Inheritance() {
        IForm form = new FormImpl();

        Method m = OgnlRuntime.getWriteMethod(form.getClass(), "clientId");
        assertNotNull(m);

        assertEquals(IComponent.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, form.getClass()));
    }

    @Test
    public void test_Get_Read_Method() {
        Method m = OgnlRuntime.getReadMethod(Bean2.class, "pageBreakAfter");
        assertNotNull(m);

        assertEquals("isPageBreakAfter", m.getName());
    }

    @Test
    public void test_Get_Read_Field() {
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
    public void test_Get_Read_Method_Multiple() {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "disabled");
        assertNotNull(m);

        assertEquals("isDisabled", m.getName());
    }

    @Test
    public void test_Get_Read_Method_Multiple_Boolean_Getters() {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "available");
        assertNotNull(m);

        assertEquals("isAvailable", m.getName());

        m = OgnlRuntime.getReadMethod(TestGetters.class, "notAvailable");
        assertNotNull(m);

        assertEquals("isNotAvailable", m.getName());
    }

    @Test
    public void test_Find_Method_Mixed_Boolean_Getters() {
        Method m = OgnlRuntime.getReadMethod(GetterMethods.class, "allowDisplay");
        assertNotNull(m);

        assertEquals("getAllowDisplay", m.getName());
    }

    @Test
    public void test_Get_Appropriate_Method()
            throws Exception {
        ListSource list = new ListSourceImpl();
        OgnlContext context = this.context;

        Object ret = OgnlRuntime.callMethod(context, list, "addValue", new String[] {null});

        assert ret != null;
    }

    @Test
    public void test_Call_Static_Method_Invalid_Class() {

        try {

            OgnlContext context = this.context;
            OgnlRuntime.callStaticMethod(context, "made.up.Name", "foo", null);

            fail("ClassNotFoundException should have been thrown by previous reference to <made.up.Name> class.");
        } catch (Exception et) {

            assertTrue(et instanceof MethodFailedException);
            assertTrue(et.getMessage().contains("made.up.Name"));
        }
    }

    @Test
    public void test_Setter_Returns()
            throws Exception {
        OgnlContext context = this.context;
        SetterReturns root = new SetterReturns();

        Method m = OgnlRuntime.getWriteMethod(root.getClass(), "value");
        assertNotNull(m);

        Ognl.setValue("value", context, root, "12__");
        assertEquals(Ognl.getValue("value", context, root), "12__");
    }

    @Test
    public void test_Call_Method_VarArgs()
            throws Exception {
        OgnlContext context = this.context;
        GenericService service = new GenericServiceImpl();

        GameGenericObject argument = new GameGenericObject();

        Object[] args = new Object[2];
        args[0] = argument;

        assertEquals("Halo 3", OgnlRuntime.callMethod(context, service, "getFullMessageFor", args));
    }

    @Test
    public void test_Call_Method_In_JDK_Sandbox() {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue()) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Call_Method_In_JDK_Sandbox() -invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = new Object[1];
        args[0] = 0;

        boolean temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, service, "exec", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof InvocationTargetException);
            assertTrue(((InvocationTargetException) ex.getCause()).getTargetException().getMessage().contains("execute"));
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }
    }

    @Test
    public void test_Call_Method_In_JDK_Sandbox_Thread_Safety()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue()) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Call_Method_In_JDK_Sandbox_Thread_Safety() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        final OgnlContext context = this.context;
        final GenericService service = new GenericServiceImpl();

        boolean temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            final int NUM_THREADS = 100;
            final int MAX_WAIT_MS = 300;
            ExecutorService exec = Executors.newFixedThreadPool(NUM_THREADS);
            final CountDownLatch allThreadsWaitOnThis = new CountDownLatch(1);
            final AtomicInteger numThreadsFailedTest = new AtomicInteger(0);
            for (int i = 0; i < NUM_THREADS; ++i) {
                exec.submit(() -> {
                    try {
                        allThreadsWaitOnThis.await();
                    } catch (InterruptedException ignored) {
                    }

                    try {
                        Thread.sleep((long) (Math.random() * MAX_WAIT_MS));
                    } catch (InterruptedException ignored) {
                    }

                    Object[] args = new Object[1];
                    args[0] = Math.random() * MAX_WAIT_MS;

                    try {
                        OgnlRuntime.callMethod(context, service, "exec", args);
                        numThreadsFailedTest.incrementAndGet();
                    } catch (Exception ex) {
                        if (!((ex.getCause() instanceof InvocationTargetException &&
                                ((InvocationTargetException) ex.getCause()).getTargetException().getMessage().contains("execute"))
                                ||
                                (ex.getCause() instanceof SecurityException &&
                                        ex.getCause().getMessage().contains("createClassLoader")))) {
                            numThreadsFailedTest.incrementAndGet();
                        }
                    }
                });
            }

            // release all the threads
            allThreadsWaitOnThis.countDown();

            // wait for them all to finish
            Thread.sleep(MAX_WAIT_MS * 3);
            exec.shutdown();
            exec.awaitTermination(MAX_WAIT_MS * 3, TimeUnit.MILLISECONDS);
            assertTrue(exec.isTerminated());
            assertEquals(0, numThreadsFailedTest.get());
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }
    }

    @Test
    public void test_Disable_JDK_Sandbox() {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue()) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Disable_JDK_Sandbox() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = new Object[]{};

        boolean temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, service, "disableSandboxViaReflectionByProperty", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof InvocationTargetException);
            assertTrue(((InvocationTargetException) ex.getCause()).getTargetException().getMessage().contains(OgnlRuntime.OGNL_SECURITY_MANAGER));
            assertTrue(((InvocationTargetException) ex.getCause()).getTargetException().getMessage().contains("write"));
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }

        temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, service, "disableSandboxViaReflectionByField", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause().getMessage().contains("accessDeclaredMembers"));
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }

        temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, service, "disableSandboxViaReflectionByMethod", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof InvocationTargetException);
            assertTrue(((InvocationTargetException) ex.getCause()).getTargetException() instanceof SecurityException);
            assertNull(((InvocationTargetException) ex.getCause()).getTargetException().getMessage());
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }
    }

    @Test
    public void test_Exit_JDK_Sandbox() {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue()) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Exit_JDK_Sandbox() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = new Object[]{};

        boolean temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, service, "exit", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof InvocationTargetException);
            assertTrue(((InvocationTargetException) ex.getCause()).getTargetException().getMessage().contains("exit"));
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }
    }

    @Test
    public void test_Call_Method_In_JDK_Sandbox_Privileged() throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue()) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Call_Method_In_JDK_Sandbox_Privileged() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = new Object[]{};

        boolean temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, service, "doNotPrivileged", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof SecurityException);
            assertTrue(ex.getCause().getMessage().contains("FilePermission"));
            assertTrue(ex.getCause().getMessage().contains("read"));
            assertTrue(ex.getCause().getMessage().contains("test.properties"));
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }

        temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            Object result = OgnlRuntime.callMethod(context, service, "doPrivileged", args);
            assertNotNull(result);
            assertNotSame(-1, result);
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }
    }

    @Test
    public void test_Class_Loader_Direct_Access() {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue()) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Class_Loader_Direct_Access() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = this.context;
        ClassLoader classLoader = getClass().getClassLoader();

        Object[] args = new Object[1];
        args[0] = "test.properties";

        boolean temporaryEnabled = false;
        try {
            System.setProperty(OgnlRuntime.OGNL_SECURITY_MANAGER, "");
            temporaryEnabled = true;
        } catch (Exception ignore) {
            // already enabled
        }

        try {
            OgnlRuntime.callMethod(context, classLoader, "getResourceAsStream", args);
            fail("JDK sandbox should block execution");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof IllegalAccessException);
            if (OgnlRuntime.getUseStricterInvocationValue()) {
                // Blocked by stricter invocation check first, if active.
                assertTrue("Didn't find expected stricter invocation mode exception message ?",
                        ex.getCause().getMessage().endsWith("] cannot be called from within OGNL invokeMethod() under stricter invocation mode."));
            } else {
                // Otherwise, blocked by OGNL SecurityManager sandbox.
                assertEquals("OGNL direct access to class loader denied!", ex.getCause().getMessage());
            }
        } finally {
            if (temporaryEnabled) {
                System.clearProperty(OgnlRuntime.OGNL_SECURITY_MANAGER);
            }
        }
    }

    @Test
    public void test_Class_Cache_Inspector()
            throws Exception {
        OgnlRuntime.clearCache();
        OgnlRuntime.clearAdditionalCache();  // Testing no exception only.
        assertEquals(0, OgnlRuntime.cache.propertyDescriptorCache.getSize());
        assertEquals(0, OgnlRuntime.cache.genericMethodParameterTypesCache.getSize());

        Root root = new Root();
        OgnlContext context = this.context;
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
    public void test_Set_Generic_Parameter_Types() {
        OgnlContext context = this.context;

        Method m = OgnlRuntime.getSetMethod(context, GenericCracker.class, "param");
        assertNotNull(m);

        Class<?>[] types = m.getParameterTypes();
        assertEquals(1, types.length);
        assertEquals(Integer.class, types[0]);
    }

    @Test
    public void test_Get_Generic_Parameter_Types() {

        Method m = OgnlRuntime.getGetMethod(GenericCracker.class, "param");
        assertNotNull(m);

        assertEquals(Integer.class, m.getReturnType());
    }

    @Test
    public void test_Find_Parameter_Types() {
        OgnlContext context = this.context;

        Method m = OgnlRuntime.getSetMethod(context, GameGeneric.class, "ids");
        assertNotNull(m);

        Class<?>[] types = OgnlRuntime.findParameterTypes(GameGeneric.class, m);
        assertEquals(1, types.length);
        assertEquals(Long[].class, types[0]);
    }

    @Test
    public void test_Find_Parameter_Types_Superclass() {
        OgnlContext context = this.context;

        Method m = OgnlRuntime.getSetMethod(context, BaseGeneric.class, "ids");
        assertNotNull(m);

        Class<?>[] types = OgnlRuntime.findParameterTypes(BaseGeneric.class, m);
        assertEquals(1, types.length);
        assertEquals(Serializable[].class, types[0]);
    }

    @Test
    public void test_Get_Declared_Methods_With_Synthetic_Methods() {
        List<Method> result = OgnlRuntime.getDeclaredMethods(SubclassSyntheticObject.class, "list", false);

        // synthetic method would be "public volatile java.util.List ognl.test.objects.SubclassSyntheticObject.getList()",
        // causing method return size to be 3

        assertEquals(2, result.size());
    }

    @Test
    public void test_Get_Property_Descriptors_With_Synthetic_Methods()
            throws Exception {
        PropertyDescriptor pd = OgnlRuntime.getPropertyDescriptor(SubclassSyntheticObject.class, "list");

        assert pd != null;
        assert OgnlRuntime.isMethodCallable(pd.getReadMethod());
    }

    private static class GenericParent<T> {
        @SuppressWarnings("unused")
        public void save(T entity) {
        }
    }

    private static class StringChild extends GenericParent<String> {

    }

    private static class LongChild extends GenericParent<Long> {

    }

    /**
     * Tests OGNL parameter discovery.
     */
    @Test
    public void testOGNLParameterDiscovery() throws NoSuchMethodException {
        Method saveMethod = GenericParent.class.getMethod("save", Object.class);
        System.out.println(saveMethod);

        Class<?>[] longClass = OgnlRuntime.findParameterTypes(LongChild.class, saveMethod);
        assertNotSame(longClass[0], String.class);
        assertSame(longClass[0], Long.class);

        Class<?>[] stringClass = OgnlRuntime.findParameterTypes(StringChild.class, saveMethod);
        assertNotSame("The cached parameter types from previous calls are used", stringClass[0], Long.class);
        assertSame(stringClass[0], String.class);
    }

    @Test
    public void testBangOperator() throws Exception {
        Object value = Ognl.getValue("!'false'", context, new Object());
        assertEquals(Boolean.TRUE, value);
    }

    @Test
    public void testGetStaticField() throws Exception {
        OgnlContext context = this.context;
        Object obj = OgnlRuntime.getStaticField(context, "ognl.test.objects.Root", "SIZE_STRING");
        assertEquals(Root.SIZE_STRING, obj);
    }

    @Test
    public void testGetStaticFieldEnum() throws Exception {
        OgnlContext context = this.context;
        Object obj = OgnlRuntime.getStaticField(context, "ognl.test.objects.OtherEnum", "ONE");
        assertEquals(OtherEnum.ONE, obj);
    }

    @Test
    public void testGetStaticFieldEnumStatic() throws Exception {
        OgnlContext context = this.context;
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
    public void testAbstractConcreteMethodScoringNoSysErr() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        ObjectMethodAccessor methodAccessor = new ObjectMethodAccessor();
        ConcreteTestClass concreteTestClass = new ConcreteTestClass();
        Object result = methodAccessor.callMethod(context, concreteTestClass, "testMethod", new Object[]{"Test", 1});
        // The "Two methods with same score(0) ..." error output should no longer be seen with the above call.
        assertEquals("Result not concatenation of parameters ?", "Test" + 1, result);
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
    public void testSyntheticBridgeReadMethod() {
        assertNotNull(OgnlRuntime.getReadMethod(PublicChild.class, "name"));
    }

    /**
     * Test that synthetic bridge write methods can be found successfully.
     * <p>
     * Note: Only bridge methods should qualify, non-bridge synthetic methods should not.
     */
    @Test
    public void testSyntheticBridgeWriteMethod() {
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
    public void testConfirmStandardMethodCallability() {
        Method method = null;
        try {
            method = SimplePublicClass.class.getDeclaredMethod("getName", (Class<?>[]) null);
        } catch (NoSuchMethodException nsme) {
            fail("SimplePublicClass.getName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull("getName() method retrieval failed ?", method);
        assertFalse("SimplePublicClass.getName() is a synthetic or bridge method ?", method.isBridge() || method.isSynthetic());
        assertTrue("SimplePublicClass.getName() is not considered callable by isMethodCallable() ?", OgnlRuntime.isMethodCallable(method));
        assertTrue("SimplePublicClass.getName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?", OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method));
    }

    /**
     * Test that bridge methods ARE considered callable by isMethodCallable_BridgeOrNonSynthetic() ONLY, and NOT by isMethodCallable().
     */
    @Test
    public void testConfirmBridgeMethodCallability() {
        Method method = null;
        try {
            method = PublicChild.class.getDeclaredMethod("getName", (Class<?>[]) null);
        } catch (NoSuchMethodException nsme) {
            fail("PublicChild.getName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull("getName() method retrieval failed ?", method);
        assertTrue("PublicChild.getName() is not a bridge method ?", method.isBridge());
        assertFalse("PublicChild.getName() is considered callable by isMethodCallable() ?", OgnlRuntime.isMethodCallable(method));
        assertTrue("PublicChild.getName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?", OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method));

        try {
            Class<?>[] argumentTypes = {String.class};
            method = PublicChild.class.getDeclaredMethod("setName", argumentTypes);
        } catch (NoSuchMethodException nsme) {
            fail("PublicChild.setName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull("setName() method retrieval failed ?", method);
        assertTrue("PublicChild.setName() is not a bridge method ?", method.isBridge());
        assertFalse("PublicChild.setName() is considered callable by isMethodCallable() ?", OgnlRuntime.isMethodCallable(method));
        assertTrue("PublicChild.setName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?", OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method));
    }

    /**
     * Test that no synthetic method is created.
     */
    @Test
    public void testConfirmNoSyntheticMethod() throws Exception {
        Method[] methods = SimpleNestingClass.NestedClass.class.getDeclaredMethods();
        assertNotNull("Nested class has no methods ?", methods);
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

    public void testSetFieldValueWhenCheckAccess() throws OgnlException, NoSuchFieldException {
        OgnlContext context = (OgnlContext) this.context;
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

    public void testSetFieldValueWhenNotCheckAccess() throws OgnlException, NoSuchFieldException {
        ExcludedObjectMemberAccess memberAccess = new ExcludedObjectMemberAccess(false);
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, memberAccess);
        SimpleFieldClass simpleField = new SimpleFieldClass();

        // verify that the static & final field is NOT accessible and bypass set field value
        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "NAME", "new name"));
        assertEquals("name", SimpleFieldClass.NAME);

        assertFalse(OgnlRuntime.setFieldValue(context, simpleField, "numbers", Collections.singletonList("four")));
        assertEquals(3, simpleField.numbers.size());

        // verify that the field is accessible and set field value successfully
        Field genderField = SimpleFieldClass.class.getDeclaredField("gender");
        assertTrue(context.getMemberAccess().isAccessible(context, simpleField, genderField, null));
        assertTrue(OgnlRuntime.setFieldValue(context, simpleField, "gender", "female"));
        assertEquals("female", simpleField.gender);

        // verify that even the field is NOT accessible, and it processes to set field value successfully
        Field emailField = SimpleFieldClass.class.getDeclaredField("email");
        memberAccess.exclude(emailField);
        assertFalse(memberAccess.isAccessible(context, simpleField, emailField, null));
        OgnlRuntime.setFieldValue(context, simpleField, "email", "admin@admin.com");
        assertEquals("admin@admin.com", simpleField.email);

        // verify that even the field is NOT accessible, and it processes to set field value but throws NoSuchPropertyException (as for private field)
        Field addressField = SimpleFieldClass.class.getDeclaredField("address");
        memberAccess.exclude(addressField);
        assertFalse(memberAccess.isAccessible(context, simpleField, addressField, null));
        try {
            OgnlRuntime.setFieldValue(context, simpleField, "address", "2 Glen st");
        } catch (NoSuchPropertyException e) {
            assertEquals("ognl.TestOgnlRuntime$SimpleFieldClass.address", e.getMessage());
            assertEquals("1 Glen st", simpleField.address);
        }
    }
}
