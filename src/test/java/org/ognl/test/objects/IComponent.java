package org.ognl.test.objects;

/**
 *
 */
public interface IComponent {

    String getClientId();

    void setClientId(String id);

    int getCount(String index);
    
    void setCount(String index, int count);
}
