/**
 * 
 */
package ognl.enhance;

import ognl.Node;
import ognl.OgnlContext;

/**
 * @author jkuhnert
 *
 */
public interface OgnlExpressionCompiler
{

    void compileExpression(OgnlContext context, Node expression, Object root)
        throws Exception;
    
   String getClassName(Class clazz);
   
   Class getInterfaceClass(Class clazz);
   
   String castExpression(OgnlContext context, Node expression, String body);
}
