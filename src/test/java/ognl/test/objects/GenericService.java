package ognl.test.objects;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public interface GenericService {

    String getFullMessageFor(PersonGenericObject person, Object... arguments);

    String getFullMessageFor(GameGenericObject game, Object... arguments);

    void exec(long waitMilliseconds) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    void disableSandboxViaReflectionByProperty() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    void disableSandboxViaReflectionByField() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException;

    void disableSandboxViaReflectionByMethod() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    void exit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    int doNotPrivileged() throws IOException;

    int doPrivileged() throws IOException;
}
