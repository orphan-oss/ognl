package ognl.security;

import ognl.OgnlRuntime;

import java.security.AccessControlException;
import java.security.Permission;

/**
 * Wraps current security manager with all actions disabled if is inside OgnlRuntime
 * @author Yasser Zamani
 * @since 3.1.24
 */
final class OgnlDefaultSecurityManager extends SecurityManager {
    private SecurityManager parentSecurityManager;

    OgnlDefaultSecurityManager(SecurityManager parentSecurityManager) {
        this.parentSecurityManager = parentSecurityManager;
    }

    private boolean isAccessDenied() {
        Class[] classContext = getClassContext();
        boolean isInsideUserMethod = false;
        for (int i = 2; i < classContext.length; i++) {
            if (OgnlRuntime.class.equals(classContext[i]) && MethodBodyExecutionSandbox.class.equals(classContext[i - 1])
                    && /*not sandbox calling itself*/!MethodBodyExecutionSandbox.class.equals(classContext[i - 2])) {
                isInsideUserMethod = true;
                break;
            }
        }
        return isInsideUserMethod;
    }

    private void accessDeny(Permission perm) {
        throw new AccessControlException("OgnlRuntime access denied "+ perm, perm);
    }

    @Override
    public void checkPermission(Permission perm) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm);
        }
        if (isAccessDenied()) {
            accessDeny(perm);
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm, context);
        }
        if (isAccessDenied()) {
            accessDeny(perm);
        }
    }
}
