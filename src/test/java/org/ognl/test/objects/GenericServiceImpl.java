package org.ognl.test.objects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public void exec(long sleepMilliseconds) throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Thread.sleep(sleepMilliseconds);
        Object runtime = Runtime.class.getMethod("getRuntime").invoke(null);
        Object process = Runtime.class.getMethod("exec", String.class).invoke(runtime, "time");
        Process.class.getMethod("destroy").invoke(process);
    }

    public void disableSandboxViaReflection() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method clearPropertyMethod = System.class.getMethod("clearProperty", String.class);
        clearPropertyMethod.invoke(null, "ognl.security.manager");
    }

    public void exit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.class.getMethod("exit", int.class).invoke(null, 0);
    }
}
