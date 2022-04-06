package org.ognl.test.objects;

import org.ognl.security.OgnlSecurityManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.util.List;
import org.ognl.OgnlRuntime;

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
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        Thread.sleep(sleepMilliseconds);
        Object runtime = Runtime.class.getMethod("getRuntime").invoke(null);
        Object process = Runtime.class.getMethod("exec", String.class).invoke(runtime, "time");
        Process.class.getMethod("destroy").invoke(process);
    }

    public void disableSandboxViaReflectionByProperty() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        Method clearPropertyMethod = System.class.getMethod("clearProperty", String.class);
        clearPropertyMethod.invoke(null, "org.ognl.security.manager");
    }

    public void disableSandboxViaReflectionByField() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        Method getOgnlSecurityManagerMethod = OgnlSecurityManagerFactory.class.getMethod("getOgnlSecurityManager");
        Object ognlSecurityManager = getOgnlSecurityManagerMethod.invoke(null);
        Field residentsField = ognlSecurityManager.getClass().getDeclaredField("residents");
        residentsField.setAccessible(true);
        List<Long> residents = (List<Long>) residentsField.get(ognlSecurityManager);
        Object[] residentsTokens = residents.toArray();
        for (Object token : residentsTokens
                ) {
            ognlSecurityManager.getClass().getMethod("leave", long.class).invoke(ognlSecurityManager, token);
        }
    }

    public void disableSandboxViaReflectionByMethod() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        Method getOgnlSecurityManagerMethod = OgnlSecurityManagerFactory.class.getMethod("getOgnlSecurityManager");
        Object ognlSecurityManager = getOgnlSecurityManagerMethod.invoke(null);
        SecureRandom rnd = new SecureRandom();
        ognlSecurityManager.getClass().getMethod("leave", long.class).invoke(ognlSecurityManager, rnd.nextLong());
    }

    public void exit() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        System.class.getMethod("exit", int.class).invoke(null, 0);
    }

    public int doNotPrivileged() throws IOException {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        InputStream is = getClass().getClassLoader().getResource("test.properties").openStream();
        int result = is.read();
        is.close();
        return result;
    }

    public int doPrivileged() throws IOException {
        if (OgnlRuntime.getDisableOgnlSecurityManagerOnInitValue() == true) {
            throw new IllegalStateException("Cannot call test method when OGNL SecurityManager disabled on initialization");
        }
        InputStream is = null;
        try {
            is = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                public InputStream run() throws Exception {
                    return getClass().getClassLoader().getResource("test.properties").openStream();
                }
            });
        } catch (PrivilegedActionException e) {
            if (e.getException() instanceof IOException) {
                throw (IOException) e.getException();
            }
        }
        int result = is.read();
        is.close();
        return result;
    }
}
