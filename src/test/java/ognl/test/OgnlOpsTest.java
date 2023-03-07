package ognl.test;

import ognl.OgnlOps;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OgnlOpsTest {

    @Test
    public void testEqualStringsEqual() {
        final String v1 = "a";
        final String v2 = "a";
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    public void testEqualStringsNotEqual() {
        final String v1 = "a";
        final String v2 = "b";
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    public void testEqualFloatsEqual() {
        final Float v1 = 0.1f;
        final Float v2 = 0.1f;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    public void testEqualFloatsNotEqual() {
        final Float v1 = 0.1f;
        final Float v2 = 0.2f;
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    public void testEqualLongsEqual() {
        final Long v1 = 1L;
        final Long v2 = 1L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    public void testEqualLongsNotEqual() {
        final Long v1 = 1L;
        final Long v2 = 2L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    public void testEqualBigLongsEqual() {
        final Long v1 = 1000000000000000001L;
        final Long v2 = 1000000000000000001L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertTrue(res);
    }

    @Test
    public void testEqualBigLongsNotEqual() {
        final Long v1 = 1000000000000000001L;
        final Long v2 = 1000000000000000002L;
        final boolean res = OgnlOps.equal(v1, v2);
        assertFalse(res);
    }

    @Test
    public void testEqualNullsEqual() {
        assertTrue(OgnlOps.equal(null, null));
    }

    @Test
    public void testEqualNullsNotEqual() {
        final Object v2 = "b";
        assertFalse(OgnlOps.equal(null, v2));
        assertFalse(OgnlOps.equal(v2, null));
    }
}
