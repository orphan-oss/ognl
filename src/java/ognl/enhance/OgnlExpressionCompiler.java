/**
 *
 */
package ognl.enhance;

import ognl.Node;
import ognl.OgnlContext;

import java.lang.reflect.Method;

/**
 */
public interface OgnlExpressionCompiler {

    String ROOT_TYPE = "-ognl-root-type";

    void compileExpression(OgnlContext context, Node expression, Object root)
            throws Exception;

    String getClassName(Class clazz);

    Class getInterfaceClass(Class clazz);

    Class getSuperOrInterfaceClass(Method m, Class clazz);

    Class getRootExpressionClass(Node rootNode, OgnlContext context);

    String castExpression(OgnlContext context, Node expression, String body);

    String createLocalReference(OgnlContext context, String expression, Class type);
}
