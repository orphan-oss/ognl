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

    public String format(String key, Object[] parms)
    {
        return "foo";
    }

    public String format(String key, Object param1, Object param2, Object param3)
    {
        return "blah";
    }

    public String format(String key, Object param1)
    {
        return "first";
    }

    public String format(String key, Object param1, Object param2)
    {
        return "haha";
    }
}
