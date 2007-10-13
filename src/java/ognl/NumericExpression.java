/**
 * 
 */
package ognl;

import ognl.enhance.ExpressionCompiler;


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
            
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
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
        } else if (context.getCurrentType() != null && context.getCurrentType().isPrimitive()
                && (ASTConst.class.isInstance(child) || NumericExpression.class.isInstance(child)))
        {
            ret += OgnlRuntime.getNumericLiteral(context.getCurrentType());
        } else if (context.getCurrentType() != null && String.class.isAssignableFrom(context.getCurrentType()))
        {
            ret = "Double.parseDouble(" + ret + ")";
            context.setCurrentType(Double.TYPE);
        }

        if (NumericExpression.class.isInstance(child))
            ret = "(" + ret + ")";
        
        return ret;
    }
}
