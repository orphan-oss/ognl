package ognl;

import junit.framework.TestCase;

import java.util.List;

public class IsTruckTest extends TestCase {

    public void testIsTruckMethod() throws Exception{
        boolean actual = (Boolean) Ognl.getValue("isTruck", new TruckHolder());

        assertTrue(actual);
    }

}

class TruckHolder {

    public boolean getIsTruck() {
        return true;
    }

}
