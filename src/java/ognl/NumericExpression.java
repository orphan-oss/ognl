/**
 * 
 */
package ognl;

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.UnsupportedCompilationException;


/**
 * Base class for numeric expressions. 
 */
public abstract class NumericExpression extends ExpressionNode implements NodeType
{
    protected Class _getterClass;
    
    public NumericExpression(int id) {
        super(id);
    }
    
    public NumericExpression(OgnlParser p, int id) {
        super(p, id);
    }
    
    public Class getGetterClass()
    {
        if (_getterClass != null)
            return _getterClass;
        
        return Double.TYPE;
    }
    
    public Class getSetterClass()
    {
        return null;
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        Object value = null;
        String result = "";

        try {

            value = getValueBody(context, target);
            
            if (value != null)
                _getterClass = value.getClass();

            for (int i=0; i < _children.length; i++)
            {
                if (i > 0)
                    result += " " + getExpressionOperator(i) + " ";

                String str = OgnlRuntime.getChildSource(context, target, _children[i]);

                result += coerceToNumeric(str, context, _children[i]);
            }
            
        } catch (Throwable t) {
            if (UnsupportedCompilationException.class.isInstance(t))
                throw (UnsupportedCompilationException)t;
            else
                throw new RuntimeException(t);
        }

        return result;
    }

    public String coerceToNumeric(String source, OgnlContext context, Node child)
    {
        String ret = source;
        Object value = context.getCurrentObject();

        if (ASTConst.class.isInstance(child) && value != null)
        {
            return value.toString();
        }

        if (context.getCurrentType() != null && !context.getCurrentType().isPrimitive()
            && context.getCurrentObject() != null && Number.class.isInstance(context.getCurrentObject()))
        {
            ret = "((" + ExpressionCompiler.getCastString(context.getCurrentObject().getClass()) + ")" + ret + ")";
            ret += "." + OgnlRuntime.getNumericValueGetter(context.getCurrentObject().getClass());
        } else if (context.getCurrentType() != null && context.getCurrentType().isPrimitive()) {
            
            ret += OgnlRuntime.getNumericLiteral(context.getCurrentType());
        }

        if (NumericExpression.class.isInstance(child))
            ret = "(" + ret + ")";
        
        return ret;
    }
}
