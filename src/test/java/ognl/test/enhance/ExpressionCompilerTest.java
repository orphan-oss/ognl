/**
 *
 */
package ognl.test.enhance;

import ognl.DefaultMemberAccess;
import ognl.ExpressionSyntaxException;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.enhance.ExpressionCompiler;
import ognl.enhance.OgnlExpressionCompiler;
import ognl.test.objects.Bean1;
import ognl.test.objects.GenericRoot;
import ognl.test.objects.IndexedMapObject;
import ognl.test.objects.Inherited;
import ognl.test.objects.Root;
import ognl.test.objects.TestInherited1;
import ognl.test.objects.TestInherited2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests functionality of {@link ExpressionCompiler}.
 */
public class ExpressionCompilerTest {

    private OgnlExpressionCompiler compiler;
    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        compiler = new ExpressionCompiler();
    }

    @Test
    void test_Get_Property_Access()
            throws Throwable {
        Node expr = (Node) Ognl.parseExpression("bean2");
        Bean1 root = new Bean1();

        compiler.compileExpression(context, expr, root);

        assertNotNull(expr.getAccessor().get(context, root));
    }

    @Test
    void test_Get_Indexed_Property()
            throws Throwable {
        Node expr = (Node) Ognl.parseExpression("bean2.bean3.indexedValue[25]");
        Bean1 root = new Bean1();

        assertNull(Ognl.getValue(expr, context, root));

        compiler.compileExpression(context, expr, root);

        assertNull(expr.getAccessor().get(context, root));
    }

    @Test
    void test_Set_Indexed_Property()
            throws Throwable {
        Node expr = (Node) Ognl.parseExpression("bean2.bean3.indexedValue[25]");
        Bean1 root = new Bean1();

        assertNull(Ognl.getValue(expr, context, root));

        compiler.compileExpression(context, expr, root);

        expr.getAccessor().set(context, root, "test string");

        assertEquals("test string", expr.getAccessor().get(context, root));
    }

    @Test
    void test_Expression()
            throws Throwable {
        Node expr = (Node) Ognl.parseExpression("bean2.bean3.value <= 24");
        Bean1 root = new Bean1();

        assertEquals(Boolean.FALSE, Ognl.getValue(expr, context, root));

        compiler.compileExpression(context, expr, root);

        assertEquals(Boolean.FALSE, expr.getAccessor().get(context, root));
    }

    @Test
    void test_Get_Context_Property()
            throws Throwable {
        context.put("key", "foo");
        Node expr = (Node) Ognl.parseExpression("bean2.bean3.map[#key]");
        Bean1 root = new Bean1();

        assertEquals("bar", Ognl.getValue(expr, context, root));

        compiler.compileExpression(context, expr, root);

        assertEquals("bar", expr.getAccessor().get(context, root));

        context.put("key", "bar");

        assertEquals("baz", Ognl.getValue(expr, context, root));
        assertEquals("baz", expr.getAccessor().get(context, root));
    }

    @Test
    void test_Set_Context_Property()
            throws Throwable {
        context.put("key", "foo");
        Node expr = (Node) Ognl.parseExpression("bean2.bean3.map[#key]");
        Bean1 root = new Bean1();

        compiler.compileExpression(context, expr, root);

        assertEquals("bar", expr.getAccessor().get(context, root));

        context.put("key", "bar");
        assertEquals("baz", expr.getAccessor().get(context, root));

        expr.getAccessor().set(context, root, "bam");
        assertEquals("bam", expr.getAccessor().get(context, root));
    }

    @Test
    void test_Property_Index() throws Throwable {
        Root root = new Root();
        Node expr = Ognl.compileExpression(context, root, "{index + 1}");

        Object ret = expr.getAccessor().get(context, root);

        assertInstanceOf(Collection.class, ret);
    }

    @Test
    void test_Root_Expression_Inheritance()
            throws Throwable {
        Inherited obj1 = new TestInherited1();
        Inherited obj2 = new TestInherited2();

        Node expr = Ognl.compileExpression(context, obj1, "myString");

        assertEquals("inherited1", expr.getAccessor().get(context, obj1));
        assertEquals("inherited2", expr.getAccessor().get(context, obj2));
    }

    @Test
    void test_Create_Empty_Collection() throws Throwable {
        Node expr = Ognl.compileExpression(context, null, "{}");

        Object ret = expr.getAccessor().get(context, null);

        assertNotNull(ret);
        assertTrue(Collection.class.isAssignableFrom(ret.getClass()));
    }

    public String getKey() {
        return "key";
    }

    @Test
    void test_Indexed_Property() throws Throwable {
        Node expression = Ognl.compileExpression(context, this, "key");
        assertEquals("key", expression.getAccessor().get(context, this));
    }

    IndexedMapObject mapObject = new IndexedMapObject("propertyValue");

    public IndexedMapObject getObject() {
        return mapObject;
    }

    @SuppressWarnings("unused")
    public String getPropertyKey() {
        return "property";
    }

    @Test
    void test_Indexed_Map_Property() throws Throwable {
        assertEquals("propertyValue", Ognl.getValue("object[propertyKey]", this));

        context.clear();
        Node expression = Ognl.compileExpression(context, this, "object[#this.propertyKey]");
        assertEquals("propertyValue", expression.getAccessor().get(context, this));

        context.clear();
        expression = Ognl.compileExpression(context, this, "object[propertyKey]");
        assertEquals("propertyValue", expression.getAccessor().get(context, this));
    }

    @Test
    void test_Set_Generic_Property() throws Exception {
        GenericRoot root = new GenericRoot();

        Node node = Ognl.compileExpression(context, root, "cracker.param");
        assertNull(node.getAccessor().get(context, root));

        node.getAccessor().set(context, root, 0);
        assertEquals(0, node.getAccessor().get(context, root));

        node.getAccessor().set(context, root, 12);
        assertEquals(12, node.getAccessor().get(context, root));
    }

    /**
     * Test ApplyExpressionMaxLength() mechanism for OGNL expression parsing.
     */
    @Test
    void test_ApplyExpressionMaxLength() {
        final String shortFakeExpression = new String(new char[10]).replace('\0', 'S');
        final String mediumFakeExpression = new String(new char[100]).replace('\0', 'S');
        final String longFakeExpression = new String(new char[1000]).replace('\0', 'S');
        final String veryLongFakeExpression = new String(new char[10000]).replace('\0', 'S');

        try {
            // ---------------------------------------------------------------------
            // Test initial default state.  Any length expression should work
            try {
                Ognl.parseExpression(shortFakeExpression);
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm illegal length values are rejected
            try {
                Ognl.applyExpressionMaxLength(Integer.MIN_VALUE);
                fail("applyExpressionMaxLength illegal value " + Integer.MIN_VALUE + " was permitted ?");
            } catch (IllegalArgumentException iaex) {
                // Expected result
            } catch (Exception ex) {
                fail("applyExpressionMaxLength illegal value " + Integer.MIN_VALUE + " failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm maximum length value are accepted
            try {
                Ognl.applyExpressionMaxLength(Integer.MAX_VALUE);
            } catch (Exception ex) {
                fail("applyExpressionMaxLength value " + Integer.MAX_VALUE + " failed unexpectedly - Error: " + ex);
            }

            // Test state with maximum length limit.  Any length expression should work
            try {
                Ognl.parseExpression(shortFakeExpression);
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm all lengths up to and equal to the largest testing string are accepted
            try {
                Ognl.applyExpressionMaxLength(veryLongFakeExpression.length());
            } catch (Exception ex) {
                fail("applyExpressionMaxLength value " + veryLongFakeExpression.length() + " failed unexpectedly - Error: " + ex);
            }

            // Test state with veryLongFakeExpression.length() limit.  All tested length expressions should work
            try {
                Ognl.parseExpression(shortFakeExpression);
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm all lengths less than the largest testing string length are accepted
            try {
                Ognl.applyExpressionMaxLength(veryLongFakeExpression.length() - 1);
            } catch (Exception ex) {
                fail("applyExpressionMaxLength value " + (veryLongFakeExpression.length() - 1) + " failed unexpectedly - Error: " + ex);
            }

            // Test state with veryLongFakeExpression.length() -1 limit.  Only veryLongFakeExpression should fail
            try {
                Ognl.parseExpression(shortFakeExpression);
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") succeeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm all lengths greater than the shortest testing string length are rejected
            try {
                Ognl.applyExpressionMaxLength(shortFakeExpression.length());
            } catch (Exception ex) {
                fail("applyExpressionMaxLength value " + shortFakeExpression.length() + " failed unexpectedly - Error: " + ex);
            }

            // Test state with shortFakeExpression.length() limit.  Only shortFakeExpression should succeed
            try {
                Ognl.parseExpression(shortFakeExpression);
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm even the shortest testing string length is rejected
            try {
                Ognl.applyExpressionMaxLength(shortFakeExpression.length() - 1);
            } catch (Exception ex) {
                fail("applyExpressionMaxLength value " + (shortFakeExpression.length() - 1) + " failed unexpectedly - Error: " + ex);
            }

            // Test state with shortFakeExpression.length() limit.  Only shortFakeExpression should succeed
            try {
                Ognl.parseExpression(shortFakeExpression);
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm only the empty string is not rejected
            try {
                Ognl.applyExpressionMaxLength(0);
            } catch (Exception ex) {
                fail("applyExpressionMaxLength value 0 failed unexpectedly - Error: " + ex);
            }

            // Test state with 0 length limit.  Only the empty string should succeed
            try {
                Ognl.parseExpression("");
            } catch (ExpressionSyntaxException esx) {
                // Expected for an empty expression (acceptable state).
            } catch (Exception ex) {
                fail("Parse of empty string failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(shortFakeExpression);
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of shortFakeExpression (" + shortFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(mediumFakeExpression);
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of mediumFakeExpression (" + mediumFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(longFakeExpression);
                fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of longFakeExpression (" + longFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of longFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }

            try {
                Ognl.parseExpression(veryLongFakeExpression);
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") succeded unexpectedly after limit set below its length ?");
            } catch (OgnlException oex) {
                if (oex.getCause() instanceof SecurityException) {
                    assertInstanceOf(SecurityException.class, oex.getCause());
                } else {
                    fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly after limit set below its length - Error: " + oex);
                }
            } catch (Exception ex) {
                fail("Parse of veryLongFakeExpression (" + veryLongFakeExpression.length() + ") failed unexpectedly - Error: " + ex);
            }
        } finally {
            try {
                Ognl.applyExpressionMaxLength(null);  // Reset to default state before leaving test.
            } catch (Exception ex) {
                // ignore, do not care for cleanup
            }
        }
    }

    /**
     * Test freezing and thawing of maximum expression length mechanism for OGNL expression parsing.
     */
    @Test
    void test_FreezeThawExpressionMaxLength() {
        try {
            // ---------------------------------------------------------------------
            // Test initial default state.  Can change maximum length to valid values without any issues
            try {
                Ognl.applyExpressionMaxLength(0);
                Ognl.applyExpressionMaxLength(Integer.MAX_VALUE);
                Ognl.applyExpressionMaxLength(0);
                Ognl.applyExpressionMaxLength(10000);
                Ognl.applyExpressionMaxLength(1000);
                Ognl.applyExpressionMaxLength(100);
                Ognl.applyExpressionMaxLength(10);
            } catch (Exception ex) {
                fail("applyExpressionMaxLength in default (initial) state with legal values failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Test thawing permitted even if never frozen, does not prevent setting afterward
            try {
                Ognl.thawExpressionMaxLength();
                Ognl.applyExpressionMaxLength(Integer.MAX_VALUE);
                Ognl.thawExpressionMaxLength();
            } catch (IllegalStateException ise) {
                fail("applyExpressionMaxLength was blocked when thawed ?");
                // Expected result
            } catch (Exception ex) {
                fail("applyExpressionMaxLength (thaw attempt) failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Test freezing maximum length
            try {
                Ognl.freezeExpressionMaxLength();
                Ognl.applyExpressionMaxLength(Integer.MAX_VALUE);
                fail("applyExpressionMaxLength was not blocked when frozen ?");
            } catch (IllegalStateException ise) {
                // Expected result
            } catch (Exception ex) {
                fail("applyExpressionMaxLength (freeze attempt) failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Test repetative freezing
            try {
                Ognl.freezeExpressionMaxLength();
                Ognl.freezeExpressionMaxLength();
            } catch (Exception ex) {
                fail("freezeExpressionMaxLength failed during repetative freeze operations - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Confirm still frozen, then thaw and demonstrate set permitted
            try {
                Ognl.applyExpressionMaxLength(Integer.MAX_VALUE);
                fail("applyExpressionMaxLength was not blocked when frozen ?");
            } catch (IllegalStateException ise) {
                // Expected result
            } catch (Exception ex) {
                fail("applyExpressionMaxLength (when frozen) failed unexpectedly - Error: " + ex);
            }
            try {
                Ognl.thawExpressionMaxLength();
                Ognl.applyExpressionMaxLength(Integer.MAX_VALUE);
            } catch (IllegalStateException ise) {
                fail("applyExpressionMaxLength was blocked when thawed ?");
                // Expected result
            } catch (Exception ex) {
                fail("applyExpressionMaxLength (thaw attempt) failed unexpectedly - Error: " + ex);
            }

            // ---------------------------------------------------------------------
            // Test repetitive thawing
            try {
                Ognl.thawExpressionMaxLength();
                Ognl.thawExpressionMaxLength();
            } catch (Exception ex) {
                fail("thawExpressionMaxLength failed during repetative thaw operations - Error: " + ex);
            }
        } finally {
            try {
                Ognl.thawExpressionMaxLength();  // Reset to default state before leaving test.
            } catch (Exception ex) {
                // ignore, do not care for cleanup
            }
            try {
                Ognl.applyExpressionMaxLength(null);  // Reset to default state before leaving test.
            } catch (Exception ex) {
                // ignore, do not care for cleanup
            }
        }
    }

}
