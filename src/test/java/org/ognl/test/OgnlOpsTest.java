package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.OgnlOps;

public class OgnlOpsTest extends TestCase {
    public void testEqualStringsEqual() throws Exception {
        final String v1 = "a";
        final String v2 = "a";
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualStringsNotEqual() throws Exception {
        final String v1 = "a";
        final String v2 = "b";
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualFloatsEqual() throws Exception {
        final Float v1 = 0.1f;
        final Float v2 = 0.1f;
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualFloatsNotEqual() throws Exception {
        final Float v1 = 0.1f;
        final Float v2 = 0.2f;
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualLongsEqual() throws Exception {
        final Long v1 = 1l;
        final Long v2 = 1l;
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualLongsNotEqual() throws Exception {
        final Long v1 = 1l;
        final Long v2 = 2l;
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualBigLongsEqual() throws Exception {
        final Long v1 = 1000000000000000001l;
        final Long v2 = 1000000000000000001l;
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualBigLongsNotEqual() throws Exception {
        final Long v1 = 1000000000000000001l;
        final Long v2 = 1000000000000000002l;
        final boolean res = OgnlOps.equal(v1, v2);
        assertEquals(v1.equals(v2), res);
    }

    public void testEqualNullsEqual() throws Exception {
        final Object v1 = null;
        final Object v2 = null;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    public void testEqualNullsNotEqual() throws Exception {
        final Object v1 = null;
        final Object v2 = "b";
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
        final boolean res2 = OgnlOps.equal(v2, v1);
        assertFalse(res2);
    }
}
