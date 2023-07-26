package ognl.test;

import ognl.Ognl;
import ognl.OgnlContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;

public class NullRootTest {

    @Test
    public void testNullValue() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = new HashMap<>();
        root.put("key1", null);
        String expr = "key1.key2.key3";
        assertNull(Ognl.getValue(expr, context, root));
    }

    @Test
    public void testEmptyRoot() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = new HashMap<>();
        String expr = "key1.key2.key3";
        assertNull(Ognl.getValue(expr, context, root));
    }

    @Test
    public void testNullRoot() throws Exception {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = null;
        String expr = "key1.key2.key3";
        assertNull(Ognl.getValue(expr, context, root));
    }
}
