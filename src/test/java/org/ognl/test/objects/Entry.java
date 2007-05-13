package org.ognl.test.objects;

/**
 *
 */
public class Entry {

    private int _size = 1;

    public int size()
    {
        return _size;
    }

    public Copy getCopy()
    {
        return new Copy();
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Entry entry = (Entry) o;
        return _size == entry._size;
    }

    public int hashCode()
    {
        return _size;
    }
}
