package ognl.enhance;

/**
 * Implementation of {@link LocalReference}. 
 */
public class LocalReferenceImpl implements LocalReference {

    String _name;
    Class _type;
    String _expression;

    public LocalReferenceImpl(String name, String expression, Class type)
    {
        _name = name;
        _type = type;
        _expression = expression;
    }

    public String getName()
    {
        return _name;
    }

    public String getExpression()
    {
        return _expression;
    }

    public Class getType()
    {
        return _type;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalReferenceImpl that = (LocalReferenceImpl) o;

        if (_expression != null ? !_expression.equals(that._expression) : that._expression != null) return false;
        if (_name != null ? !_name.equals(that._name) : that._name != null) return false;
        if (_type != null ? !_type.equals(that._type) : that._type != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (_name != null ? _name.hashCode() : 0);
        result = 31 * result + (_type != null ? _type.hashCode() : 0);
        result = 31 * result + (_expression != null ? _expression.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "LocalReferenceImpl[" +
               "_name='" + _name + '\'' +
               '\n' +
               ", _type=" + _type +
               '\n' +
               ", _expression='" + _expression + '\'' +
               '\n' +
               ']';
    }
}
