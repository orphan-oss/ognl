package org.ognl.test.objects;

import java.util.Map;

/**
 *
 */
public class Messages {

    Map _source;

    public Messages(Map source)
    {
        _source = source;
    }
    
    public String getMessage(String key)
    {
        return (String)_source.get(key);
    }
}
