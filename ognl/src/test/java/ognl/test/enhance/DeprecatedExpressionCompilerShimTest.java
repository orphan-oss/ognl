package ognl.test.enhance;

import ognl.DefaultMemberAccess;
import ognl.Node;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.enhance.CompiledExpressionSupport;
import ognl.enhance.ExpressionCompiler;
import ognl.enhance.JavassistExpressionCompiler;
import ognl.enhance.OgnlExpressionCompiler;
import ognl.test.objects.Bean1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings({"deprecation", "removal", "unchecked", "rawtypes"})
public class DeprecatedExpressionCompilerShimTest {

    private static class LegacySubclass<C extends OgnlContext<C>> extends ExpressionCompiler<C> {
    }

    @Test
    void shimIsAJavassistCompiler() {
        assertInstanceOf(JavassistExpressionCompiler.class, new ExpressionCompiler<>());
    }

    @Test
    void shimStaticsDelegateToNeutralSupport() throws Exception {
        assertEquals(CompiledExpressionSupport.PRE_CAST, ExpressionCompiler.PRE_CAST);
        assertEquals("int[]", ExpressionCompiler.getCastString(int[].class));

        Node expr = (Node) Ognl.parseExpression("1");
        assertEquals(CompiledExpressionSupport.shouldCast(expr), ExpressionCompiler.shouldCast(expr));
    }

    @Test
    void legacySubclassStillCompilesWhenInstalled() throws Throwable {
        OgnlExpressionCompiler original = OgnlRuntime.getCompiler();
        try {
            OgnlRuntime.setCompiler(new LegacySubclass());
            OgnlContext context = Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
            Bean1 root = new Bean1();
            Node expr = Ognl.compileExpression(context, root, "bean2");
            assertNotNull(expr.getAccessor().get(context, root));
        } finally {
            OgnlRuntime.setCompiler(original);
        }
    }
}
