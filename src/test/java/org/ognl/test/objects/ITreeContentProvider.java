package org.ognl.test.objects;

import java.util.Collection;

/**
 *
 */
public interface ITreeContentProvider extends IContentProvider {

    public Collection getChildren(Object parentElement);

    public boolean hasChildren(Object parentElement);
}
