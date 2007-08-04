package ognl.enhance;



/**
 * Thrown during bytecode enhancement conversions of ognl expressions to indicate
 * that a certain expression isn't currently supported as a pure java bytecode enhanced
 * version. 
 * 
 * <p>
 *  If this exception is thrown it is expected that ognl will fall back to default ognl 
 *  evaluation of the expression.
 * </p>
 * 
 * @author jkuhnert
 */
public class UnsupportedCompilationException extends RuntimeException
{
    
    public UnsupportedCompilationException(String message)
    {
        super(message);
    }

    public UnsupportedCompilationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
