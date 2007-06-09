/**
 * 
 */
package ognl;

import ognl.enhance.UnsupportedCompilationException;

import java.math.BigInteger;


/**
 * Base class for numeric expressions.
 * 
 * @author jkuhnert
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
        try {
            
            value = getValueBody(context, target);
            
            if (value != null) 
                _getterClass = value.getClass();
            
            if (ASTConst.class.isInstance(_children[0])) {
                
                if (BigInteger.class.isInstance(value) 
                        && !(_parent != null && NumericExpression.class.isAssignableFrom(_parent.getClass()))) {
                    
                    return value.toString();
                }
                
                return value.toString();
            }
            
        } catch (Throwable t) {
            if (UnsupportedCompilationException.class.isInstance(t))
                throw (UnsupportedCompilationException)t;
            else
                throw new RuntimeException(t);
        }
        
        String result = null;
        
        if (_parent == null) {
            
            if (BigInteger.class.isInstance(value)) {
                return value.toString();
            }
            
            result = value + OgnlRuntime.getNumericLiteral(_getterClass);
        } else
            result = super.toGetSourceString(context, target);
        
        return result;
    }
}
