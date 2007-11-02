package org.ognl.test.objects;

/**
 *
 */
public class GameGeneric extends BaseGeneric<GameGenericObject, Long> {

    public GameGeneric()
    {
        _value = new GameGenericObject();
    }
}
