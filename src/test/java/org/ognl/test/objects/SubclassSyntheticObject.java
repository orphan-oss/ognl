package org.ognl.test.objects;

import java.util.ArrayList;

/**
 * Simple subclass.
 */
public class SubclassSyntheticObject extends BaseSyntheticObject {

    public ArrayList getList()
    {
        return new ArrayList();
    }
}
