package ognl.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Guarantee a singleton shared thread-safe security manager and sandbox for user's methods body execution.
 *
 * Add the `-Dognl.security.manager` to JVM options to enable.
 *
 * <p> Note: Due to potential performance and concurrency issues, try this only if you afraid your app can have an
 * unknown "expression injection" flaw or you afraid you cannot prevent those in your app's internal sandbox
 * comprehensively e.g. you cannot discover and maintain all attack vectors over time because of many dependencies
 * and also their change over time.</p>
 *
 * <p> This tries to provide an option to you to enable a security manager that disables any sensitive action e.g.
 * exec and exit even if attacker had a successful "expression injection" in any unknown way into your app. However,
 * also honors previous security manager and policies if any set, as parent, and rolls back to them after method
 * execution finished.</p>
 *
 * @author Yasser Zamani
 * @since 3.1.23
 */
public class MethodBodyExecutionSandbox {
    private static int residentsCount = 0;

    private static SecurityManager parentSecurityManager;

    public static Object executeMethodBody(Object target, Method method, Object[] argsArray) throws InvocationTargetException,
            IllegalAccessException {
        
        if (!enter()) {
            // isn't enabled or cannot installed due to current security manager policy
            return method.invoke(target, argsArray);
        }

        try {
            return method.invoke(target, argsArray);
        } finally {
            leave();
        }
    }

    private static boolean enter() {
        synchronized (MethodBodyExecutionSandbox.class) {
            if (residentsCount == 0) {
                boolean sandboxEnabled = false;
                try {
                    sandboxEnabled = System.getProperty("ognl.security.manager") != null;
                } catch (SecurityException ignored) {
                    // user has applied a policy that doesn't allow read property so we have to honor user's sandbox
                }
                if (sandboxEnabled && installSandboxIntoJVM()) {
                    residentsCount++;
                    return true;
                }
            } else {
                residentsCount++;
                return true;
            }
            return false;
        }
    }

    private static void leave() {
        synchronized (MethodBodyExecutionSandbox.class) {
            residentsCount--;
            if (residentsCount == 0) {
                // no user so roll back to previous state to save performance
                uninstallSandboxFromJVM();
            }
        }
    }
    
    private static boolean installSandboxIntoJVM() {
        parentSecurityManager = System.getSecurityManager();
        try {
            System.setSecurityManager(new OgnlDefaultSecurityManager(parentSecurityManager));
        } catch (SecurityException ex) {
            // user has applied a policy that doesn't allow setSecurityManager so we have to honor user's sandbox
            return false;
        }

        return true;
    }

    private static void uninstallSandboxFromJVM() {
        System.setSecurityManager(parentSecurityManager);
    }
}
