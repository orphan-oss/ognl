package ognl.security;

import ognl.OgnlRuntime;

import java.io.FileDescriptor;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.security.Permission;

/**
 * Wraps current security manager with sensitive actions e.g. exit and exec disabled if is inside OgnlRuntime
 * @author Yasser Zamani
 * @since 3.1.23
 */
public class OgnlDefaultSecurityManager extends SecurityManager {
    private SecurityManager parentSecurityManager;

    OgnlDefaultSecurityManager(SecurityManager parentSecurityManager) {
        this.parentSecurityManager = parentSecurityManager;
    }

    private boolean isInsideSandbox() {
        Class[] classContext = getClassContext();
        for (int i = 2; i < classContext.length; i++) {
            if (OgnlRuntime.class.equals(classContext[i]) && MethodBodyExecutionSandbox.class.equals(classContext[i - 1])
                    && /*not sandbox calling itself*/!MethodBodyExecutionSandbox.class.equals(classContext[i - 2])) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideJDK() {
        Class[] classContext = getClassContext();
        for (int i = 2; i < classContext.length; i++) {
            if (MethodBodyExecutionSandbox.class.equals(classContext[i])) {
                break;
            }
            ClassLoader systemClassLoader = "".getClass().getClassLoader();
            if (systemClassLoader != classContext[i].getClassLoader()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void checkPermission(Permission perm) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm);
        }
        if (isInsideSandbox()) {
            if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
                if (isInsideJDK()) {
                    return;
                }
            }
            super.checkPermission(perm);
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm, context);
        }
        if (isInsideSandbox()) {
            super.checkPermission(perm, context);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkCreateClassLoader();
        }
    }

    @Override
    public void checkAccess(Thread t) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccess(t);
        }
        if (isInsideSandbox()) {
            super.checkAccess(t);
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccess(g);
        }
        if (isInsideSandbox()) {
            super.checkAccess(g);
        }
    }

    @Override
    public void checkExit(int status) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkExit(status);
        }
        if (isInsideSandbox()) {
            super.checkExit(status);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkExec(cmd);
        }
        if (isInsideSandbox()) {
            super.checkExec(cmd);
        }
    }

    @Override
    public void checkLink(String lib) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkLink(lib);
        }
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkRead(fd);
        }
    }

    @Override
    public void checkRead(String file) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkRead(file);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkRead(file, context);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkWrite(fd);
        }
    }

    @Override
    public void checkWrite(String file) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkWrite(file);
        }
    }

    @Override
    public void checkDelete(String file) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkDelete(file);
        }
    }

    @Override
    public void checkConnect(String host, int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkConnect(host, port);
        }
        if (isInsideSandbox()) {
            super.checkConnect(host, port);
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkConnect(host, port, context);
        }
        if (isInsideSandbox()) {
            super.checkConnect(host, port, context);
        }
    }

    @Override
    public void checkListen(int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkListen(port);
        }
        if (isInsideSandbox()) {
            super.checkListen(port);
        }
    }

    @Override
    public void checkAccept(String host, int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccept(host, port);
        }
        if (isInsideSandbox()) {
            super.checkAccept(host, port);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMulticast(maddr);
        }
        if (isInsideSandbox()) {
            super.checkMulticast(maddr);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMulticast(maddr, ttl);
        }
        if (isInsideSandbox()) {
            super.checkMulticast(maddr, ttl);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPropertiesAccess();
        }
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPropertyAccess(key);
        }
    }

    @Override
    public boolean checkTopLevelWindow(Object window) {
        if (parentSecurityManager != null) {
            return parentSecurityManager.checkTopLevelWindow(window);
        }
        if (isInsideSandbox()) {
            return super.checkTopLevelWindow(window);
        }
        return true;
    }

    @Override
    public void checkPrintJobAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPrintJobAccess();
        }
        if (isInsideSandbox()) {
            super.checkPrintJobAccess();
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSystemClipboardAccess();
        }
        if (isInsideSandbox()) {
            super.checkSystemClipboardAccess();
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAwtEventQueueAccess();
        }
        if (isInsideSandbox()) {
            super.checkAwtEventQueueAccess();
        }
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPackageAccess(pkg);
        }
        if (isInsideSandbox()) {
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
        if (isInsideSandbox()) {
            if (pkg.startsWith("ognl.security")) {
                throw new SecurityException("Access to ognl.security via OgnlRuntime denied!");
            }
        }
    }

    @Override
    public void checkSetFactory() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSetFactory();
        }
        if (isInsideSandbox()) {
            super.checkSetFactory();
        }
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMemberAccess(clazz, which);
        }
    }

    @Override
    public void checkSecurityAccess(String target) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSecurityAccess(target);
        }
        if (isInsideSandbox()) {
            super.checkSecurityAccess(target);
        }
    }
}
