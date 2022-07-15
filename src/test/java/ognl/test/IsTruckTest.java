package ognl.test;

import junit.framework.TestCase;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;

public class IsTruckTest extends TestCase {

    public void testIsTruckMethod() throws Exception{
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        boolean actual = (Boolean) Ognl.getValue("isTruck", context, new TruckHolder());

        assertTrue(actual);
    }

}

class TruckHolder {

    public boolean getIsTruck() {
        return true;
    }

}
