package org.ognl.test.objects;

/**
 *
 */
public class GenericServiceImpl implements GenericService {

    public String getFullMessageFor(PersonGenericObject person, Object... arguments)
    {
        return person.getDisplayName();
    }

    public String getFullMessageFor(GameGenericObject game, Object... arguments)
    {
        return game.getDisplayName();
    }
}
