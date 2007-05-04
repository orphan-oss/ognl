package org.ognl.test.objects;

/**
 *
 */
public class Entry {
    public int size()
    {
        return 1;
    }

    public Copy getCopy()
    {
        return new Copy();
    }
}
