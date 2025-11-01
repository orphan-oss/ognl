package ognl.test;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NullRootTest {

    @Test
    public void testNullValue() {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = new HashMap<>();
        root.put("key1", null);
        String expr = "key1.key2.key3";
        try {
            Ognl.getValue(expr, context, root);
            fail();
        } catch (OgnlException e) {
            assertEquals("source is null for getProperty(null, \"key2\")", e.getMessage());
        }
    }

    @Test
    public void testEmptyRoot() {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = new HashMap<>();
        String expr = "key1.key2.key3";
        try {
            Ognl.getValue(expr, context, root);
        } catch (OgnlException e) {
            assertEquals("source is null for getProperty(null, \"key2\")", e.getMessage());
        }
    }

    @Test
    public void testNullRoot() {
        OgnlContext context = Ognl.createDefaultContext(null);
        Map<String, Object> root = null;
        String expr = "key1.key2.key3";
        try {
            Ognl.getValue(expr, context, root);
            fail();
        } catch (OgnlException e) {
            assertEquals("source is null for getProperty(null, \"key1\")", e.getMessage());
        }
    }
}
