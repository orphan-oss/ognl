package ognl;

import java.security.Permissions;
import java.security.Policy;
import java.security.SecurityPermission;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Guarantee a singleton shared thread-safe security manager and sandbox for user's methods body execution
 * @author Yasser Zamani
 * @since 3.1.23
 */
class MethodBodyExecutionSandbox {
    private Permissions userDemandPermissions;
    private Policy userDemandPolicy;
    private SecurityManager userDemandSecurityManager;
    private AtomicInteger useCount;
    private SecurityManager parentSecurityManager;
    private Policy parentPolicy;

    MethodBodyExecutionSandbox(Permissions userDemandPermissions, Policy userDemandPolicy,
                               SecurityManager userDemandSecurityManager){
        this.userDemandPermissions = userDemandPermissions;
        this.userDemandPolicy = userDemandPolicy;
        this.userDemandSecurityManager = userDemandSecurityManager;
        this.useCount = new AtomicInteger(0);
    }

    synchronized void increaseUseCount() {
        if (useCount.get() == 0) {
            // not installed into JVM so install one instance
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
                    return;
                }
                try {
                    Policy.setPolicy(userDemandPolicy == null ? new OgnlPolicy(parentPolicy, userDemandPermissions) : userDemandPolicy);
                } catch (SecurityException ex) {
                    // user has applied a policy that doesn't allow setPolicy so we have to honor user's sandbox
                    return;
                }
                try {
                    System.setSecurityManager(userDemandSecurityManager == null ? new OgnlSecurityManager(parentSecurityManager)
                            : userDemandSecurityManager);
                } catch (SecurityException ex) {
                    // user has applied a policy that doesn't allow setSecurityManager so we have to restore previous
                    // policy and honor user's sandbox
                    Policy.setPolicy(parentPolicy);
                    return;
                }
            }
        }

        useCount.incrementAndGet();
    }

    synchronized void decreaseUseCount() {
        if (useCount.get() == 0) {
            return;
        }
        if (useCount.decrementAndGet() == 0) {
            // no user so roll back to previous state to save performance
            System.setSecurityManager(parentSecurityManager);
            Policy.setPolicy(parentPolicy);
        }
    }

    Integer getUseCount() {
        return useCount.get();
    }
}
