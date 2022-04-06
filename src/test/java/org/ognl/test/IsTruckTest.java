package org.ognl.test;

import junit.framework.TestCase;
import org.ognl.*;

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
