package org.ognl.test.objects;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public interface GenericService {

    String getFullMessageFor(PersonGenericObject person, Object...arguments);

    String getFullMessageFor(GameGenericObject game, Object...arguments);

    void exec(long waitMilliseconds) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    void disableSandboxViaReflectionByField() throws NoSuchFieldException, IllegalAccessException;

    void disableSandboxViaReflectionByMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    void exit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
