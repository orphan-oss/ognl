/*
 * Copyright 2020 OGNL Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ognl.test;

import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Simple;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OgnlContextCreateTest {

    @Test
    void createContext() throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(null).withValues(prepareValues());

        assertEquals("test100", Ognl.getValue("#test", context, new Simple()));
    }

    @Test
    void createContextWithRoot() throws OgnlException {
        Simple root = new Simple();

        OgnlContext context = Ognl.createDefaultContext(root, prepareValues());

        assertEquals("test100", Ognl.getValue("#test", context, root));
    }

    @Test
    void createContextWithNullRoot() throws OgnlException {
        Simple root = new Simple();

        OgnlContext context = Ognl.createDefaultContext(null, prepareValues());

        assertEquals("test100", Ognl.getValue("#test", context, root));
    }

    @Test
    void createContextWithClassResolver() throws OgnlException {
        Simple root = new Simple();

        OgnlContext context = Ognl.createDefaultContext(root, new MyClassResolver());

        assertEquals("static", Ognl.getValue("@ognl.test.MyClass@getValue()", context, root));
    }

    @Test
    void addContextWithClassResolver() throws OgnlException {
        Simple root = new Simple();
        OgnlContext oldContext = Ognl.createDefaultContext(root, new MyClassResolver());

        OgnlContext context = Ognl.addDefaultContext(root, oldContext);

        assertEquals("static", Ognl.getValue("@ognl.test.MyClass@getValue()", context, root));
    }

    @Test
    void createContextWithNullRootAndClassResolver() throws OgnlException {
        Simple root = new Simple();

        OgnlContext context = Ognl.createDefaultContext(null, new MyClassResolver());

        assertEquals("static", Ognl.getValue("@ognl.test.MyClass@getValue()", context, root));
    }

    @Test
    void addContextWithNullRootAndClassResolver() throws OgnlException {
        Simple root = new Simple();
        OgnlContext oldContext = Ognl.createDefaultContext(null, new MyClassResolver());

        OgnlContext context = Ognl.addDefaultContext(null, oldContext);

        assertEquals("static", Ognl.getValue("@ognl.test.MyClass@getValue()", context, root));
    }

    @Test
    void createContextWithClassResolverAndTypeConverter() throws OgnlException {
        Simple root = new Simple();
        OgnlContext context = Ognl.createDefaultContext(root, new MyClassResolver(), new MyTypeConverter());

        Simple actual = (Simple) Ognl.getValue("@ognl.test.MyClass@getValue()", context, root, Simple.class);

        assertNotNull(actual);
        assertArrayEquals(new Object[]{"static"}, actual.getValues());
    }

    @Test
    void addContextWithClassResolverAndTypeConverter() throws OgnlException {
        Simple root = new Simple();
        OgnlContext oldContext = Ognl.createDefaultContext(root, new MyClassResolver(), new MyTypeConverter());

        OgnlContext context = Ognl.addDefaultContext(null, oldContext);

        Simple actual = (Simple) Ognl.getValue("@ognl.test.MyClass@getValue()", context, root, Simple.class);

        assertNotNull(actual);
        assertArrayEquals(new Object[]{"static"}, actual.getValues());
    }

    @Test
    void addContextWithClassResolverAndNoTypeConverter() throws OgnlException {
        Simple root = new Simple();
        OgnlContext oldContext = Ognl.createDefaultContext(root, null, new MyTypeConverter());

        OgnlContext context = Ognl.addDefaultContext(null, new MyClassResolver(), oldContext);

        Simple actual = (Simple) Ognl.getValue("@ognl.test.MyClass@getValue()", context, root, Simple.class);

        assertNotNull(actual);
        assertArrayEquals(new Object[]{"static"}, actual.getValues());
    }

    @Test
    void addContextWithNoClassResolverAndNoTypeConverter() throws OgnlException {
        Simple root = new Simple();
        OgnlContext oldContext = Ognl.createDefaultContext(root);

        OgnlContext context = Ognl.addDefaultContext(null, new MyClassResolver(), new MyTypeConverter(), oldContext);

        Simple actual = (Simple) Ognl.getValue("@ognl.test.MyClass@getValue()", context, root, Simple.class);

        assertNotNull(actual);
        assertArrayEquals(new Object[]{"static"}, actual.getValues());
    }

    @Test
    void createContextWithNullRootAndClassResolverAndTypeConverter() throws OgnlException {
        Simple root = new Simple();
        OgnlContext context = Ognl.createDefaultContext(null, new MyClassResolver(), new MyTypeConverter());

        Simple actual = (Simple) Ognl.getValue("@ognl.test.MyClass@getValue()", context, root, Simple.class);

        assertNotNull(actual);
        assertArrayEquals(new Object[]{"static"}, actual.getValues());
    }

    @Test
    void addContextWithNullRootAndClassResolverAndTypeConverter() throws OgnlException {
        Simple root = new Simple();
        OgnlContext oldContext = Ognl.createDefaultContext(null, new MyClassResolver(), new MyTypeConverter());

        OgnlContext context = Ognl.addDefaultContext(null, oldContext);

        Simple actual = (Simple) Ognl.getValue("@ognl.test.MyClass@getValue()", context, root, Simple.class);

        assertNotNull(actual);
        assertArrayEquals(new Object[]{"static"}, actual.getValues());
    }

    private Map<String, Object> prepareValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("test", "test100");
        return values;
    }

    private static class MyClassResolver extends DefaultClassResolver {
        @Override
        @SuppressWarnings("unchecked")
        public <T> Class<T> classForName(String className, OgnlContext context) throws ClassNotFoundException {
            if (className.equals("ognl.test.MyClass")) {
                return (Class<T>) MyClass.class;
            }
            return super.classForName(className, context);
        }
    }

    private static class MyClass {
        public static String getValue() {
            return "static";
        }
    }

    private class MyTypeConverter extends DefaultTypeConverter {
        @Override
        public Object convertValue(OgnlContext context, Object value, Class<?> toType) {
            if (toType == Simple.class) {
                return new Simple(new Object[]{value});
            }
            return super.convertValue(context, value, toType);
        }
    }
}
