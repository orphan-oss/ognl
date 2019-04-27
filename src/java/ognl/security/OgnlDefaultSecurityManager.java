package ognl.security;

import ognl.OgnlRuntime;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.AccessControlException;
import java.security.Permission;

/**
 * Wraps current security manager with all actions disabled if is inside OgnlRuntime
 * @author Yasser Zamani
 * @since 3.1.23
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

    @Override
    public void checkCreateClassLoader() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkCreateClassLoader();
        }
        if (isAccessDenied()) {
            super.checkCreateClassLoader();
        }
    }

    @Override
    public void checkAccess(Thread t) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccess(t);
        }
        if (isAccessDenied()) {
            super.checkAccess(t);
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccess(g);
        }
        if (isAccessDenied()) {
            super.checkAccess(g);
        }
    }

    @Override
    public void checkExit(int status) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkExit(status);
        }
        if (isAccessDenied()) {
            super.checkExit(status);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkExec(cmd);
        }
        if (isAccessDenied()) {
            super.checkExec(cmd);
        }
    }

    @Override
    public void checkLink(String lib) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkLink(lib);
        }
        if (isAccessDenied()) {
            super.checkLink(lib);
        }
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkRead(fd);
        }
        if (isAccessDenied()) {
            super.checkRead(fd);
        }
    }

    @Override
    public void checkRead(String file) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkRead(file);
        }
        if (isAccessDenied()) {
            super.checkRead(file);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkRead(file, context);
        }
        if (isAccessDenied()) {
            super.checkRead(file, context);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkWrite(fd);
        }
        if (isAccessDenied()) {
            super.checkWrite(fd);
        }
    }

    @Override
    public void checkWrite(String file) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkWrite(file);
        }
        if (isAccessDenied()) {
            super.checkWrite(file);
        }
    }

    @Override
    public void checkDelete(String file) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkDelete(file);
        }
        if (isAccessDenied()) {
            super.checkDelete(file);
        }
    }

    @Override
    public void checkConnect(String host, int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkConnect(host, port);
        }
        if (isAccessDenied()) {
            super.checkConnect(host, port);
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkConnect(host, port, context);
        }
        if (isAccessDenied()) {
            super.checkConnect(host, port, context);
        }
    }

    @Override
    public void checkListen(int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkListen(port);
        }
        if (isAccessDenied()) {
            super.checkListen(port);
        }
    }

    @Override
    public void checkAccept(String host, int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccept(host, port);
        }
        if (isAccessDenied()) {
            super.checkAccept(host, port);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMulticast(maddr);
        }
        if (isAccessDenied()) {
            super.checkMulticast(maddr);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMulticast(maddr, ttl);
        }
        if (isAccessDenied()) {
            super.checkMulticast(maddr, ttl);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPropertiesAccess();
        }
        if (isAccessDenied()) {
            super.checkPropertiesAccess();
        }
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPropertyAccess(key);
        }
        if (isAccessDenied()) {
            super.checkPropertyAccess(key);
        }
    }

    @Override
    public boolean checkTopLevelWindow(Object window) {
        if (parentSecurityManager != null) {
            return parentSecurityManager.checkTopLevelWindow(window);
        }
        if (isAccessDenied()) {
            return super.checkTopLevelWindow(window);
        }
        return true;
    }

    @Override
    public void checkPrintJobAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPrintJobAccess();
        }
        if (isAccessDenied()) {
            super.checkPrintJobAccess();
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSystemClipboardAccess();
        }
        if (isAccessDenied()) {
            super.checkSystemClipboardAccess();
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAwtEventQueueAccess();
        }
        if (isAccessDenied()) {
            super.checkAwtEventQueueAccess();
        }
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPackageAccess(pkg);
        }
        if (isAccessDenied()) {
            if (pkg.startsWith("ognl.security")) {
                throw new SecurityException("Access to ognl.security via OgnlRuntime denied!");
            }
        }
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPackageDefinition(pkg);
        }
        if (isAccessDenied()) {
            if (pkg.startsWith("ognl.security")) {
                throw new SecurityException("Access to ognl.security via OgnlRuntime denied!");
            }
            super.checkPackageDefinition(pkg);
        }
    }

    @Override
    public void checkSetFactory() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSetFactory();
        }
        if (isAccessDenied()) {
            super.checkSetFactory();
        }
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMemberAccess(clazz, which);
        }
        if (isAccessDenied()) {
            super.checkMemberAccess(clazz, which);
        }
    }

    @Override
    public void checkSecurityAccess(String target) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSecurityAccess(target);
        }
        if (isAccessDenied()) {
            super.checkSecurityAccess(target);
        }
    }
}
