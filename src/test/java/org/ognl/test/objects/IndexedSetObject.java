package org.ognl.test.objects;

import java.util.HashMap;

/**
 * Test for OGNL-119.
 */
public class IndexedSetObject {

    private final HashMap<String,Object> things = new HashMap<String,Object>();

    public IndexedSetObject() {
        things.put("x", new Container(1));
    }

    public Object getThing(String index) {
        return things.get(index);
    }

    public void setThing(String index, Object value) {
        things.put(index, value);
    }

    public static class Container {
        private int val;
        public Container(int val) { this.val = val; }
        public int getVal() { return val; }
        public void setVal(int val) { this.val = val; }
    }
}
