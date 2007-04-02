/**
 *
 */
package ognl.enhance;

import ognl.Node;
import ognl.OgnlContext;

import java.lang.reflect.Method;

/**
 * @author jkuhnert
 */
public interface OgnlExpressionCompiler {
    
    void compileExpression(OgnlContext context, Node expression, Object root)
            throws Exception;

    String getClassName(Class clazz);

    Class getInterfaceClass(Class clazz);

    Class getSuperOrInterfaceClass(Method m, Class clazz);

    String castExpression(OgnlContext context, Node expression, String body);

    String createLocalReference(OgnlContext context, String expression, Class type);
}
