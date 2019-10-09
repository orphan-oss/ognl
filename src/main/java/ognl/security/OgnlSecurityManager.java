package ognl.security;

import java.io.FilePermission;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps current security manager with JDK security manager if is inside OgnlRuntime user's methods body execution.
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
 * @since 3.1.24
 */
public class OgnlSecurityManager extends SecurityManager {
    private static final String OGNL_SANDBOX_CLASS_NAME = "ognl.security.UserMethod";
    private static final Class<?> CLASS_LOADER_CLASS = ClassLoader.class;
    private static final Class<?> FILE_PERMISSION_CLASS = FilePermission.class;

    private SecurityManager parentSecurityManager;
    private List<Long> residents = new ArrayList<Long>();
    private SecureRandom rnd = new SecureRandom();

    public OgnlSecurityManager(SecurityManager parentSecurityManager) {
        this.parentSecurityManager = parentSecurityManager;
    }

    private boolean isAccessDenied(Permission perm) {
        Class[] classContext = getClassContext();
        Boolean isInsideClassLoader = null;
        for (Class c : classContext) {
            if (isInsideClassLoader == null && CLASS_LOADER_CLASS.isAssignableFrom(c)) {
                if (FILE_PERMISSION_CLASS.equals(perm.getClass()) && "read".equals(perm.getActions())) {
                    // TODO: might be risky but we have to - fix it if any POC discovered
                    // relax a bit with containers class loaders lazy class load
                    return false;
                } else {
                    isInsideClassLoader = false;
                }
            }
            if (OGNL_SANDBOX_CLASS_NAME.equals(c.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkPermission(Permission perm) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm);
        }
        if (isAccessDenied(perm)) {
            super.checkPermission(perm);
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm, context);
        }
        if (isAccessDenied(perm)) {
            super.checkPermission(perm, context);
        }
    }

    public Long enter() {
        synchronized (this) {
            long token = rnd.nextLong();
            if (residents.size() == 0) {
                if (install()) {
                    residents.add(token);
                    return token;
                } else {
                    return null;
                }
            }
            residents.add(token);
            return token;
        }
    }

    public void leave(long token) throws SecurityException {
        synchronized (this) {
            if (!residents.contains(token)) {
                throw new SecurityException();
            }
            residents.remove(token);
            if (residents.size() == 0) {
                // no user so roll back to previous state to save performance
                uninstall();
            }
        }
    }

    private boolean install() {
        try {
            System.setSecurityManager(this);
        } catch (SecurityException ex) {
            // user has applied a policy that doesn't allow setSecurityManager so we have to honor user's sandbox
            return false;
        }

        return true;
    }

    private void uninstall() {
        System.setSecurityManager(parentSecurityManager);
    }
}
