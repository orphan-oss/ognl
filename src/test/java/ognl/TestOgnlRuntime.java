package ognl;

import junit.framework.TestCase;
import org.ognl.test.objects.*;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests various methods / functionality of {@link ognl.OgnlRuntime}.
 */
public class TestOgnlRuntime extends TestCase {


    private Map context;

    public void setUp() throws Exception {
        super.setUp();
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    public void test_Get_Super_Or_Interface_Class() throws Exception {
        ListSource list = new ListSourceImpl();

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "total");
        assertNotNull(m);

        assertEquals(ListSource.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    public void test_Get_Private_Class() throws Exception {
        List list = Arrays.asList(new String[]{"hello", "world"});

        Method m = OgnlRuntime.getReadMethod(list.getClass(), "iterator");
        assertNotNull(m);

        assertEquals(Iterable.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, list.getClass()));
    }

    public void test_Complicated_Inheritance() throws Exception {
        IForm form = new FormImpl();

        Method m = OgnlRuntime.getWriteMethod(form.getClass(), "clientId");
        assertNotNull(m);

        assertEquals(IComponent.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, form.getClass()));
    }

    public void test_Get_Read_Method()
            throws Exception {
        Method m = OgnlRuntime.getReadMethod(Bean2.class, "pageBreakAfter");
        assertNotNull(m);

        assertEquals("isPageBreakAfter", m.getName());
    }

    public void test_Get_Read_Field()
            throws Exception {
        Method m = OgnlRuntime.getReadMethod(Bean2.class, "code");
        assertNull(m);

        Field field = OgnlRuntime.getField(Bean2.class, "code");
        assertNotNull(field);
        assertEquals("code", field.getName());
    }

    class TestGetters {
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

    public void test_Get_Read_Method_Multiple()
            throws Exception {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "disabled");
        assertNotNull(m);

        assertEquals("isDisabled", m.getName());
    }

    public void test_Get_Read_Method_Multiple_Boolean_Getters()
            throws Exception {
        Method m = OgnlRuntime.getReadMethod(TestGetters.class, "available");
        assertNotNull(m);

        assertEquals("isAvailable", m.getName());

        m = OgnlRuntime.getReadMethod(TestGetters.class, "notAvailable");
        assertNotNull(m);

        assertEquals("isNotAvailable", m.getName());
    }

    public void test_Find_Method_Mixed_Boolean_Getters()
            throws Exception {
        Method m = OgnlRuntime.getReadMethod(GetterMethods.class, "allowDisplay");
        assertNotNull(m);

        assertEquals("getAllowDisplay", m.getName());
    }

    public void test_Get_Appropriate_Method()
            throws Exception {
        ListSource list = new ListSourceImpl();
        OgnlContext context = (OgnlContext) this.context;

        Object ret = OgnlRuntime.callMethod(context, list, "addValue", new String[]{null});

        assert ret != null;
    }

    public void test_Call_Static_Method_Invalid_Class() {

        try {

            OgnlContext context = (OgnlContext) this.context;
            OgnlRuntime.callStaticMethod(context, "made.up.Name", "foo", null);

            fail("ClassNotFoundException should have been thrown by previous reference to <made.up.Name> class.");
        } catch (Exception et) {

            assertTrue(MethodFailedException.class.isInstance(et));
            assertTrue(et.getMessage().indexOf("made.up.Name") > -1);
        }
    }

    public void test_Setter_Returns()
            throws Exception {
        OgnlContext context = (OgnlContext) this.context;
        SetterReturns root = new SetterReturns();

        Method m = OgnlRuntime.getWriteMethod(root.getClass(), "value");
        assertTrue(m != null);

        Ognl.setValue("value", context, root, "12__");
        assertEquals(Ognl.getValue("value", context, root), "12__");
    }

    public void test_Call_Method_VarArgs()
            throws Exception {
        OgnlContext context = (OgnlContext) this.context;
        GenericService service = new GenericServiceImpl();

        GameGenericObject argument = new GameGenericObject();

        Object[] args = OgnlRuntime.getObjectArrayPool().create(2);
        args[0] = argument;

        assertEquals("Halo 3", OgnlRuntime.callMethod(context, service, "getFullMessageFor", args));
    }

    public void test_Call_Method_In_JDK_Sandbox()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Call_Method_In_JDK_Sandbox() -invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = (OgnlContext) this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = OgnlRuntime.getObjectArrayPool().create(1);
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

    public void test_Call_Method_In_JDK_Sandbox_Thread_Safety()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Call_Method_In_JDK_Sandbox_Thread_Safety() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        final OgnlContext context = (OgnlContext) this.context;
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
                exec.submit(new Runnable() {
                    public void run() {
                        try {
                            allThreadsWaitOnThis.await();
                        } catch (InterruptedException ignored) {
                        }

                        try {
                            Thread.sleep((long) (Math.random() * MAX_WAIT_MS));
                        } catch (InterruptedException ignored) {
                        }

                        Object[] args = OgnlRuntime.getObjectArrayPool().create(1);
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

    public void test_Disable_JDK_Sandbox()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Disable_JDK_Sandbox() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = (OgnlContext) this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = OgnlRuntime.getObjectArrayPool().create(0);

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

    public void test_Exit_JDK_Sandbox()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Exit_JDK_Sandbox() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = (OgnlContext) this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = OgnlRuntime.getObjectArrayPool().create(0);

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

    public void test_Call_Method_In_JDK_Sandbox_Privileged()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Call_Method_In_JDK_Sandbox_Privileged() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = (OgnlContext) this.context;
        GenericService service = new GenericServiceImpl();

        Object[] args = OgnlRuntime.getObjectArrayPool().create(0);

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

    public void test_Class_Loader_Direct_Access()
            throws Exception {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            System.out.println("OGNL SecurityManager sandbox disabled by JVM option.  Skipping test_Class_Loader_Direct_Access() invocation test.");
            return;  // JVM option was set to disable sandbox, do not attempt invocation.
        }

        OgnlContext context = (OgnlContext) this.context;
        ClassLoader classLoader = getClass().getClassLoader();

        Object[] args = OgnlRuntime.getObjectArrayPool().create(1);
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
            if (OgnlRuntime.getUseStricterInvocationValue() == true) {
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

    public void test_Class_Cache_Inspector()
            throws Exception {
        OgnlRuntime.clearCache();
        OgnlRuntime.clearAdditionalCache();  // Testing no exception only.
        assertEquals(0, OgnlRuntime.cache.propertyDescriptorCache.getSize());
        assertEquals(0, OgnlRuntime.cache.genericMethodParameterTypesCache.getSize());

        Root root = new Root();
        OgnlContext context = (OgnlContext) this.context;
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

    class TestCacheInspector implements ClassCacheInspector {

        public boolean shouldCache(Class type) {
            if (type == null || type == Root.class)
                return false;

            return true;
        }
    }

    public void test_Set_Generic_Parameter_Types()
            throws Exception {
        OgnlContext context = (OgnlContext) this.context;

        Method m = OgnlRuntime.getSetMethod(context, GenericCracker.class, "param");
        assertNotNull(m);

        Class[] types = m.getParameterTypes();
        assertEquals(1, types.length);
        assertEquals(Integer.class, types[0]);
    }

    public void test_Get_Generic_Parameter_Types() {

        Method m = OgnlRuntime.getGetMethod(GenericCracker.class, "param");
        assertNotNull(m);

        assertEquals(Integer.class, m.getReturnType());
    }

    public void test_Find_Parameter_Types()
            throws Exception {
        OgnlContext context = (OgnlContext) this.context;

        Method m = OgnlRuntime.getSetMethod(context, GameGeneric.class, "ids");
        assertNotNull(m);

        Class[] types = OgnlRuntime.findParameterTypes(GameGeneric.class, m);
        assertEquals(1, types.length);
        assertEquals(new Long[0].getClass(), types[0]);
    }

    public void test_Find_Parameter_Types_Superclass()
            throws Exception {
        OgnlContext context = (OgnlContext) this.context;

        Method m = OgnlRuntime.getSetMethod(context, BaseGeneric.class, "ids");
        assertNotNull(m);

        Class[] types = OgnlRuntime.findParameterTypes(BaseGeneric.class, m);
        assertEquals(1, types.length);
        assertEquals(new Serializable[0].getClass(), types[0]);
    }

    public void test_Get_Declared_Methods_With_Synthetic_Methods()
            throws Exception {
        List result = OgnlRuntime.getDeclaredMethods(SubclassSyntheticObject.class, "list", false);

        // synthetic method would be "public volatile java.util.List org.ognl.test.objects.SubclassSyntheticObject.getList()",
        // causing method return size to be 3

        assertEquals(2, result.size());
    }

    public void test_Get_Property_Descriptors_With_Synthetic_Methods()
            throws Exception {
        PropertyDescriptor pd = OgnlRuntime.getPropertyDescriptor(SubclassSyntheticObject.class, "list");

        assert pd != null;
        assert OgnlRuntime.isMethodCallable(pd.getReadMethod());
    }

    private static class GenericParent<T> {
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
    public void testOGNLParameterDiscovery() throws NoSuchMethodException {
        Method saveMethod = GenericParent.class.getMethod("save", Object.class);
        System.out.println(saveMethod);

        Class[] longClass = OgnlRuntime.findParameterTypes(LongChild.class, saveMethod);
        assertNotSame(longClass[0], String.class);
        assertSame(longClass[0], Long.class);

        Class[] stringClass = OgnlRuntime.findParameterTypes(StringChild.class, saveMethod);
        assertNotSame("The cached parameter types from previous calls are used", stringClass[0], Long.class);
        assertSame(stringClass[0], String.class);
    }

    public void testBangOperator() throws Exception {
        Object value = Ognl.getValue("!'false'", context, new Object());
        assertEquals(Boolean.TRUE, value);
    }

    public void testGetStaticField() throws Exception {
        OgnlContext context = (OgnlContext) this.context;
        Object obj = OgnlRuntime.getStaticField(context, "org.ognl.test.objects.Root", "SIZE_STRING");
        assertEquals(Root.SIZE_STRING, obj);
    }

    public void testGetStaticFieldEnum() throws Exception {
        OgnlContext context = (OgnlContext) this.context;
        Object obj = OgnlRuntime.getStaticField(context, "org.ognl.test.objects.OtherEnum", "ONE");
        assertEquals(OtherEnum.ONE, obj);
    }

    public void testGetStaticFieldEnumStatic() throws Exception {
        OgnlContext context = (OgnlContext) this.context;
        Object obj = OgnlRuntime.getStaticField(context, "org.ognl.test.objects.OtherEnum", "STATIC_STRING");
        assertEquals(OtherEnum.STATIC_STRING, obj);
    }

    /**
     * This test indirectly confirms an error output (syserr) is no longer produced when OgnlRuntime
     * encounters the condition reported in issue #17.  {@link OgnlRuntime#findBestMethod(List, Class, String, Class[])}
     * can findtwo appropriate methods with the same score where one is abstract and one is concrete.  Either
     * choice in that scenario actually worked when invoked, but produced the unwanted syserr output.
     */
    public void testAbstractConcreteMethodScoringNoSysErr() throws Exception {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
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
    abstract class AbstractTestClass<T> {
        public abstract String testMethod(T element, int i);
    }

    /**
     * Concrete test class for issue #42 - equal score syserr output for abstract class/method hierarchy.
     */
    class ConcreteTestClass extends AbstractTestClass<String> {
        public String testMethod(String element, int i) {
            return element + i;
        }
    }

    /**
     * Protected class for synthetic/bridge method tests.
     */
    protected static class ProtectedParent {
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
     *
     * @throws Exception
     */
    public void testSyntheticBridgeReadMethod() throws Exception {
        assertNotNull(OgnlRuntime.getReadMethod(PublicChild.class, "name"));
    }

    /**
     * Test that synthetic bridge write methods can be found successfully.
     * <p>
     * Note: Only bridge methods should qualify, non-bridge synthetic methods should not.
     *
     * @throws Exception
     */
    public void testSyntheticBridgeWriteMethod() throws Exception {
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
        class NestedClass {
            private String name = "nested name contents";
        }

        public String getNestedName() {
            return new NestedClass().name;  // Should force creation of a synthetic method for NestedClass (to access its name field).
        }
    }

    /**
     * Test that normal non-synthetic methods are considered callable by both isMethodCallable() and isMethodCallable_BridgeOrNonSynthetic().
     */
    public void testConfirmStandardMethodCallability() {
        Method method = null;
        try {
            method = SimplePublicClass.class.getDeclaredMethod("getName", (Class<?>[]) null);
        } catch (NoSuchMethodException nsme) {
            fail("SimplePublicClass.getName() method retrieval by reflection failed (NoSuchMethodException) ?");
        }
        assertNotNull("getName() method retrieval failed ?", method);
        assertTrue("SimplePublicClass.getName() is a synthetic or bridge method ?", !(method.isBridge() || method.isSynthetic()));
        assertTrue("SimplePublicClass.getName() is not considered callable by isMethodCallable() ?", OgnlRuntime.isMethodCallable(method));
        assertTrue("SimplePublicClass.getName() is not considered callable by isMethodCallable_BridgeOrNonSynthetic() ?", OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method));
    }

    /**
     * Test that bridge methods ARE considered callable by isMethodCallable_BridgeOrNonSynthetic() ONLY, and NOT by isMethodCallable().
     */
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
     * Test that non-bridge synthetic methods are NOT considered callable by either isMethodCallable() or isMethodCallable_BridgeOrNonSynthetic().
     */
    public void testConfirmSyntheticMethodNonCallablility() {
        Method method;
        Method[] methods = SimpleNestingClass.NestedClass.class.getDeclaredMethods();
        assertNotNull("Nested class has no methods ?", methods);
        assertTrue("Nested class has no methods ?", methods.length > 0);
        method = methods[0];
        assertNotNull("Nested class method at index 0 is null ?", method);
        assertTrue("SimpleAbstractClass.getName() is a synthetic method ?", method.isSynthetic());
        assertFalse("SimpleAbstractClass.getName() is a bridge method ?", method.isBridge());
        assertFalse("SimpleAbstractClass.getName() is considered callable by isMethodCallable() ?", OgnlRuntime.isMethodCallable(method));
        assertFalse("SimpleAbstractClass.getName() is considered callable by isMethodCallable_BridgeOrNonSynthetic() ?", OgnlRuntime.isMethodCallable_BridgeOrNonSynthetic(method));
    }
}
