package org.ognl.test.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to test OGNL-136 use of synthetic methods.
 */
public abstract class BaseSyntheticObject {

    protected List getList()
    {
        return new ArrayList();
    }
}
