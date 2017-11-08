package ognl.internal;

import ognl.ClassCacheInspector;

import java.util.Arrays;

/**
 * Implementation of {@link ClassCache}.
 */
public class ClassCacheImpl implements ClassCache {

    /* this MUST be a power of 2 */
    private static final int TABLE_SIZE = 512;
    /* ...and now you see why. The table size is used as a mask for generating hashes */
    private static final int TABLE_SIZE_MASK = TABLE_SIZE - 1;

    private Entry[] _table;
    private ClassCacheInspector _classInspector;
    private int _size = 0;

    public ClassCacheImpl()
    {
        _table = new Entry[TABLE_SIZE];
    }

    public void setClassInspector(ClassCacheInspector inspector)
    {
        _classInspector = inspector;
    }

    public void clear()
    {
        for (int i=0; i < _table.length; i++)
        {
            _table[i] = null;
        }

        _size = 0;
    }

    public int getSize()
    {
        return _size;
    }

    public final Object get(Class key)
    {
        Object result = null;
        int i = key.hashCode() & TABLE_SIZE_MASK;

        for (Entry entry = _table[i]; entry != null; entry = entry.next)
        {
            if (entry.key == key)
            {
                result = entry.value;
                break;
            }
        }

        return result;
    }

    public final Object put(Class key, Object value)
    {
        if (_classInspector != null && !_classInspector.shouldCache(key))
            return value;

        Object result = null;
        int i = key.hashCode() & TABLE_SIZE_MASK;
        Entry entry = _table[i];

        if (entry == null)
        {
            _table[i] = new Entry(key, value);
            _size++;
        } else
        {
            if (entry.key == key)
            {
                result = entry.value;
                entry.value = value;
            } else
            {
                while (true)
                {
                    if (entry.key == key)
                    {
                        /* replace value */
                        result = entry.value;
                        entry.value = value;
                        break;
                    } else
                    {
                        if (entry.next == null)
                        {
                            /* add value */
                            entry.next = new Entry(key, value);
                            break;
                        }
                    }
                    entry = entry.next;
                }
            }
        }

        return result;
    }

    public String toString()
    {
        return "ClassCacheImpl[" +
               "_table=" + (_table == null ? null : Arrays.asList(_table)) +
               '\n' +
               ", _classInspector=" + _classInspector +
               '\n' +
               ", _size=" + _size +
               '\n' +
               ']';
    }
}
