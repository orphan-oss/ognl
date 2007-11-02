package org.ognl.test.objects;

/**
 * Test for OGNL-131.
 */
public class SearchCriteria {

    String _displayName;

    public SearchCriteria(String name)
    {
        _displayName = name;
    }

    public String getDisplayName()
    {
        return _displayName;
    }
}
