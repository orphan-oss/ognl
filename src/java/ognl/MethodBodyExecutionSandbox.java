package ognl;

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
class MethodBodyExecutionSandbox {
    private static Permissions userDemandPermissions;
    private static Policy userDemandPolicy;
    private static SecurityManager userDemandSecurityManager;
    private static boolean enabled;
    private static boolean disabled;
    private static Integer residentsCount = 0;

    private static SecurityManager parentSecurityManager;
    private static Policy parentPolicy;

    static void enable(Permissions permissions, Policy policy, SecurityManager securityManager){
        userDemandPermissions = permissions;
        userDemandPolicy = policy;
        userDemandSecurityManager = securityManager;
        enabled = true;
        disabled = false;
    }
    
    static void disable() {
        disabled = true;
    }
    
    static Object executeMethodBody(Object target, Method method, Object[] argsArray) throws InvocationTargetException,
            IllegalAccessException {
        
        if (!enabled) {
            // isn't enabled so simply just invoke the method outside sandbox
            return method.invoke(target, argsArray);
        }

        enter();

        try {
            return method.invoke(target, argsArray);
        } catch (SecurityException ex) {
            // JDK sandbox blocked the execution of method body due to do sensitive actions like exit or exec
            ex.printStackTrace();
            throw new IllegalAccessException("Method [" + method + "] cannot be accessed.");
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
            System.setSecurityManager(parentSecurityManager);
            Policy.setPolicy(parentPolicy);
        }
    }
}
