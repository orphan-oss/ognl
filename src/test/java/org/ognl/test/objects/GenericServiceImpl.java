package org.ognl.test.objects;

import java.io.IOException;

/**
 *
 */
public class GenericServiceImpl implements GenericService {

    public String getFullMessageFor(GameGenericObject game, Object... arguments)
    {
        game.getHappy();
        
        return game.getDisplayName();
    }

    public String getFullMessageFor(PersonGenericObject person, Object... arguments)
    {
        return person.getDisplayName();
    }

    public void exec(long sleepMilliseconds) throws IOException, InterruptedException {
        Thread.sleep(sleepMilliseconds);
        Runtime.getRuntime().exec("time").destroy();
    }
}
