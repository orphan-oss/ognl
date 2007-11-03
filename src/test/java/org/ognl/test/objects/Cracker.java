package org.ognl.test.objects;

import java.io.Serializable;

/**
 * Generic test object.
 */
public interface Cracker<T extends Serializable>{

    T getParam();
    
    void setParam(T param);
}
