package org.ognl.test.objects;

import ognl.security.MethodBodyExecutionSandbox;

import java.io.IOException;
import java.lang.reflect.Field;
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

    public void disableSandboxViaReflectionByField() throws NoSuchFieldException, IllegalAccessException {
        Field disabledField = MethodBodyExecutionSandbox.class.getDeclaredField("disabled");
        disabledField.setAccessible(true);
        disabledField.set(null, true);
    }

    public void disableSandboxViaReflectionByMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method disableMethod = MethodBodyExecutionSandbox.class.getMethod("disable");
        disableMethod.invoke(null);
    }

    public void exit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.class.getMethod("exit", int.class).invoke(null, 0);
    }
}
