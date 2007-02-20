/**
 * 
 */
package org.ognl.test.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of {@link BeanProvider}.
 */
public class BeanProviderImpl implements Serializable, BeanProvider
{
    private Map _map = new HashMap();
    
    public BeanProviderImpl() {}
    
    public Object getBean(String name)
    {
        return _map.get(name);
    }
    
    public void setBean(String name, Object bean)
    {
        _map.put(name, bean);
    }
}
