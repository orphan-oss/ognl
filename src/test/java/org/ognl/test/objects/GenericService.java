package org.ognl.test.objects;

import java.io.IOException;

/**
 *
 */
public interface GenericService {

    String getFullMessageFor(PersonGenericObject person, Object...arguments);

    String getFullMessageFor(GameGenericObject game, Object...arguments);

    void exec(long waitMilliseconds) throws IOException, InterruptedException;
}
