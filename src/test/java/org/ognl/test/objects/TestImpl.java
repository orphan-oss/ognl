package org.ognl.test.objects;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestImpl extends TestClass {

    public Map<String, String> getTheMap()
    {
        Map<String, String> map = new HashMap();
        map.put("key", "value");
        return map;
    }
}
