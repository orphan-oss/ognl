package org.ognl.test.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TreeContentProvider implements ITreeContentProvider {



    public Collection getChildren(Object parentElement)
    {
        return Collections.EMPTY_LIST;
    }

    public boolean hasChildren(Object parentElement)
    {
        return true;
    }

    public List getElements()
    {
        return Collections.EMPTY_LIST;
    }
}
