package org.ognl.test.objects;

/**
 *
 */
public class ComponentImpl implements IComponent {

    String _clientId;
    int _count = 0;

    public String getClientId()
    {
        return _clientId;  
    }

    public void setClientId(String id)
    {
        _clientId = id;
    }

    public int getCount(String index)
    {
        return _count;
    }

    public void setCount(String index, int count)
    {
        _count = count;
    }
}
