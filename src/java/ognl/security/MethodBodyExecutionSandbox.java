package ognl.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permissions;
import java.security.Policy;
import java.security.SecurityPermission;

/**
 * Guarantee a singleton shared thread-safe security manager and sandbox for user's methods body execution
 * @author Yasser Zamani
 * @since 3.1.23
 */
public class MethodBodyExecutionSandbox {
    private static Permissions userDemandPermissions;
    private static Policy userDemandPolicy;
    private static SecurityManager userDemandSecurityManager;
    private static boolean enabled;
    private static boolean disabled;
    private static Integer residentsCount = 0;

    private static SecurityManager parentSecurityManager;
    private static Policy parentPolicy;

    /**
     * Enables JDK sandbox via {@link OgnlSecurityManager} for user's invoking methods body execution.
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
     * @param permissions further Permissions or pass <code>null</code> to use minimum required permissions
     * @param policy your own one or pass <code>null</code> to use {@link OgnlPolicy}
     * @param securityManager your own one or pass <code>null</code> to use {@link OgnlSecurityManager}
     *
     * @since 3.1.23
     */
    public static void enable(Permissions permissions, Policy policy, SecurityManager securityManager) {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getPolicy"));
            sm.checkPermission(new SecurityPermission("setPolicy"));
            sm.checkPermission(new RuntimePermission("setSecurityManager"));
        }

        userDemandPermissions = permissions;
        userDemandPolicy = policy;
        userDemandSecurityManager = securityManager;
        enabled = true;
        disabled = false;
    }

    public static void disable() {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            sm.checkPermission(new SecurityPermission("setPolicy"));
            sm.checkPermission(new RuntimePermission("setSecurityManager"));
        }

        disabled = true;
    }
    
    public static Object executeMethodBody(Object target, Method method, Object[] argsArray) throws InvocationTargetException,
            IllegalAccessException {
        
        if (!enabled) {
            // isn't enabled so simply just invoke the method outside sandbox
            return method.invoke(target, argsArray);
        }

        enter();

        try {
            return method.invoke(target, argsArray);
        } finally {
            leave();
        }
    }

    private static void enter() {
        synchronized (MethodBodyExecutionSandbox.class) {
            if (residentsCount == 0) {
                if (installSandboxIntoJVM()) {
                    residentsCount++;
                }
            } else {
                residentsCount++;
            }
        }
    }

    private static void leave() {
        synchronized (MethodBodyExecutionSandbox.class) {
            residentsCount--;
            if (residentsCount == 0) {
                // no user so roll back to previous state to save performance
                uninstallSandboxFromJVM();

                //disable if user demand
                if (disabled) {
                    enabled = false;
                }
            }
        }
    }
    
    private static boolean installSandboxIntoJVM() {
        // try to synchronize with potential other external policy appliers
        synchronized (Policy.class) {
            parentSecurityManager = System.getSecurityManager();

            try {
                if (parentSecurityManager != null) {
                    parentSecurityManager.checkPermission(new SecurityPermission("getPolicy"));
                    parentSecurityManager.checkPermission(new SecurityPermission("setPolicy"));
                    parentSecurityManager.checkPermission(new RuntimePermission("setSecurityManager"));
                }
                parentPolicy = Policy.getPolicy();
            } catch (SecurityException ex) {
                // user has applied a policy that doesn't allow getPolicy so we have to honor user's sandbox
                return false;
            }
            try {
                Policy.setPolicy(userDemandPolicy == null ? new OgnlPolicy(parentPolicy, userDemandPermissions) : userDemandPolicy);
            } catch (SecurityException ex) {
                // user has applied a policy that doesn't allow setPolicy so we have to honor user's sandbox
                return false;
            }
            try {
                System.setSecurityManager(userDemandSecurityManager == null ? new OgnlSecurityManager(parentSecurityManager)
                        : userDemandSecurityManager);
            } catch (SecurityException ex) {
                // user has applied a policy that doesn't allow setSecurityManager so we have to restore previous
                // policy and honor user's sandbox
                Policy.setPolicy(parentPolicy);
                return false;
            }
        }
        
        return true;
    }

    private static void uninstallSandboxFromJVM() {
        // try to synchronize with potential other external policy appliers
        synchronized (Policy.class) {
            Policy.setPolicy(parentPolicy);
            System.setSecurityManager(parentSecurityManager);
        }
    }
}
