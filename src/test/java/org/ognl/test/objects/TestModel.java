package org.ognl.test.objects;

/**
 *
 */
public class TestModel {

    public Copy getCopy()
    {
        return new Copy();
    }

    public Model getUnassignedCopyModel()
    {
        return new Model();
    }

    public boolean isCanApproveCopy()
    {
        return true;
    }

    public Entry getEntry()
    {
        return new Entry();
    }
}
