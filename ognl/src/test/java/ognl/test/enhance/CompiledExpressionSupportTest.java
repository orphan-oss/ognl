package ognl.test.enhance;

import ognl.DefaultMemberAccess;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.enhance.CompiledExpressionSupport;
import ognl.test.objects.Bean1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompiledExpressionSupportTest {

    private OgnlContext context;

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
    }

    @Test
    void getCastStringReturnsNullForNull() {
        assertNull(CompiledExpressionSupport.getCastString(null));
    }

    @Test
    void getCastStringRendersClassAndArrayNames() {
        assertEquals("java.lang.Integer", CompiledExpressionSupport.getCastString(Integer.class));
        assertEquals("int[]", CompiledExpressionSupport.getCastString(int[].class));
    }

    @Test
    void addCastStringStoresThenPrepends() {
        CompiledExpressionSupport.addCastString(context, "A");
        assertEquals("A", context.get(CompiledExpressionSupport.PRE_CAST));

        CompiledExpressionSupport.addCastString(context, "B");
        assertEquals("BA", context.get(CompiledExpressionSupport.PRE_CAST));
    }

    @Test
    void shouldCastIsFalseForConstant() throws Exception {
        assertFalse(CompiledExpressionSupport.shouldCast((Node) Ognl.parseExpression("1")));
    }

    @Test
    void shouldCastIsFalseForChainStartingWithConstant() throws Exception {
        assertFalse(CompiledExpressionSupport.shouldCast((Node) Ognl.parseExpression("'abc'.length()")));
    }

    @Test
    void shouldCastIsTrueForProperty() throws Exception {
        assertTrue(CompiledExpressionSupport.shouldCast((Node) Ognl.parseExpression("bean2")));
    }

    @Test
    void getRootExpressionIsEmptyForNullRoot() throws Exception {
        Node expr = (Node) Ognl.parseExpression("bean2");
        assertEquals("", CompiledExpressionSupport.getRootExpression(expr, null, context));
    }

    @Test
    void getRootExpressionIsEmptyForConstant() throws Exception {
        Node expr = (Node) Ognl.parseExpression("1");
        assertEquals("", CompiledExpressionSupport.getRootExpression(expr, new Bean1(), context));
    }
}
