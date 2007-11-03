package org.ognl.test.objects;

/**
 *
 */
public class GenericRoot {

    Root _root = new Root();
    GenericCracker _cracker = new GenericCracker();

    public Root getRoot()
    {
        return _root;
    }

    public void setRoot(Root root)
    {
        _root = root;
    }

    public GenericCracker getCracker()
    {
        return _cracker;
    }

    public void setCracker(GenericCracker cracker)
    {
        _cracker = cracker;
    }
}
