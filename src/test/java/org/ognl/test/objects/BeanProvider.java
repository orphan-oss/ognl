/**
 * 
 */
package org.ognl.test.objects;


/**
 * Test interface to be used with a custom propery accessor.
 */
public interface BeanProvider
{
    
    /**
     * Gets a bean by name.
     * @param name
     * @return
     */
    Object getBean(String name);
    
    /**
     * Sets a new bean mapping.
     * @param name
     * @param bean
     */
    void setBean(String name, Object bean);
}
