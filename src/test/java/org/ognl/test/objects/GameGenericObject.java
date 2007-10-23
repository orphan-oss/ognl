package org.ognl.test.objects;

/**
 *
 */
public class GameGenericObject implements GenericObject {

    public GameGenericObject()
    {
        super();
    }

    public int getId()
    {
        return 20;
    }

    public String getDisplayName()
    {
        return "Halo 3";
    }

    public String getHappy()
    {
        return "happy";
    }
}
