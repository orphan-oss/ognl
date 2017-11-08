package ognl.enhance;

/**
 * Container class for {@link OgnlExpressionCompiler} generated local method
 * block references.
 */
public interface LocalReference {

    /**
     * The name of the assigned variable reference.
     *
     * @return The name of the reference as it will be when compiled.
     */
    String getName();

    /**
     * The expression that sets the value, ie the part after <code><class type> refName = <expression></code>.
     * @return The setting expression.
     */
    String getExpression();

    /**
     * The type of reference.
     * @return The type.
     */
    Class getType();
}
