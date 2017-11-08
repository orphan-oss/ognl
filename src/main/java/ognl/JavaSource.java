/**
 * 
 */
package ognl;

import ognl.enhance.ExpressionAccessor;

/**
 * Defines an object that can return a representation of itself and any objects it contains
 * in the form of a {@link String} embedded with literal java statements.
 * 
 * @author jkuhnert
 */
public interface JavaSource
{
    
    /**
     * Expected to return a java source representation of itself such that 
     * it could be turned into a literal java expression to be compiled and
     * executed for {@link ExpressionAccessor#get(OgnlContext, Object)} calls.
     * 
     * @return Literal java string representation of an object get.
     */
    String toGetSourceString(OgnlContext context, Object target);
    
    /**
     * Expected to return a java source representation of itself such that 
     * it could be turned into a literal java expression to be compiled and
     * executed for {@link ExpressionAccessor#get(OgnlContext, Object)} calls.
     * 
     * @return Literal java string representation of an object get.
     */
    String toSetSourceString(OgnlContext context, Object target);
}
