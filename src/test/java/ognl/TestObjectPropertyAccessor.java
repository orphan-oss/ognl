package ognl;

import junit.framework.TestCase;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Tests various methods / functionality of {@link ObjectPropertyAccessor}.
 */
public class TestObjectPropertyAccessor extends TestCase {
    private Map context;
    private ObjectPropertyAccessor propertyAccessor;

    public void setUp() throws Exception {
        super.setUp();
        context = Ognl.createDefaultContext(null, new ExcludedObjectMemberAccess(false));
        propertyAccessor = new ObjectPropertyAccessor();
    }

    /**
     * Public class for "setPossibleProperty" method tests.
     */
    public static class SimplePublicClass {
        private String gender = "male";
        public String email = "test@test.com";
        private String name = "name";
        private String age = "18";

        public void setGender(String gender) {
            this.gender = gender;
        }

        private void setEmail(String email) {
            this.email = email;
        }

        private void setName(String email) {
            this.email = email;
        }

        public void setname(String name) {
            this.name = name;
        }

        private void setAge(String age) {
            this.age = age;
        }

        public void setage(String age) {
            this.age = age;
        }
    }

    public void testSetPossibleProperty() throws OgnlException, IntrospectionException {
        OgnlContext context = (OgnlContext) this.context;
        SimplePublicClass simplePublic = new SimplePublicClass();

        // 1. when set method is accessible and set method
        assertNotSame(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "gender", "female"));
        assertEquals("female", simplePublic.gender);

        // 2. when set method is NOT accessible and fallback to set field (field is accessible)
        assertNotSame(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "email", "admin@admin.com"));
        assertEquals("admin@admin.com", simplePublic.email);

        // 3. when set method is NOT accessible, field is NOT accessible, fallback to write method (write method is accessible)
        assertEquals("setName", OgnlRuntime.getSetMethod(context, SimplePublicClass.class, "name").getName());
        assertEquals("setname", OgnlRuntime.getWriteMethod(SimplePublicClass.class, "name", null).getName());
        assertNotSame(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "name", "new name"));
        assertEquals("new name", simplePublic.name);

        // 4. when set method is NOT accessible, field is NOT accessible, fallback to write method (write method is NOT accessible)
        Method ageWriteMethod = OgnlRuntime.getWriteMethod(SimplePublicClass.class, "age", null);
        ((ExcludedObjectMemberAccess) context.getMemberAccess()).exclude(ageWriteMethod);

        assertEquals("setage", ageWriteMethod.getName());
        assertFalse(context.getMemberAccess().isAccessible(context, simplePublic, ageWriteMethod, "age"));
        assertEquals("setAge", OgnlRuntime.getSetMethod(context, SimplePublicClass.class, "age").getName());
        assertEquals(OgnlRuntime.NotFound, propertyAccessor.setPossibleProperty(context, simplePublic, "age", "99"));
        assertEquals("18", simplePublic.age);
    }
}
