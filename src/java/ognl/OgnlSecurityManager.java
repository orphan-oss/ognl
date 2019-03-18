package ognl;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * @author Yasser Zamani
 * @since 3.1.23
 */
class OgnlSecurityManager extends SecurityManager {
    private SecurityManager parentSecurityManager;

    OgnlSecurityManager(SecurityManager parentSecurityManager) {
        this.parentSecurityManager = parentSecurityManager;
    }

    private boolean isInsideOgnlContext() {
        for (Class clazz :
                getClassContext()) {
            if (OgnlRuntime.class.equals(clazz)) {
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
        if (isInsideOgnlContext()) {
            super.checkPermission(perm);
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPermission(perm, context);
        }
        if (isInsideOgnlContext()) {
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
        if (isInsideOgnlContext()) {
            super.checkAccess(t);
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccess(g);
        }
        if (isInsideOgnlContext()) {
            super.checkAccess(g);
        }
    }

    @Override
    public void checkExit(int status) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkExit(status);
        }
        if (isInsideOgnlContext()) {
            super.checkExit(status);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkExec(cmd);
        }
        if (isInsideOgnlContext()) {
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
        if (isInsideOgnlContext()) {
            super.checkConnect(host, port);
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkConnect(host, port, context);
        }
        if (isInsideOgnlContext()) {
            super.checkConnect(host, port, context);
        }
    }

    @Override
    public void checkListen(int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkListen(port);
        }
        if (isInsideOgnlContext()) {
            super.checkListen(port);
        }
    }

    @Override
    public void checkAccept(String host, int port) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAccept(host, port);
        }
        if (isInsideOgnlContext()) {
            super.checkAccept(host, port);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMulticast(maddr);
        }
        if (isInsideOgnlContext()) {
            super.checkMulticast(maddr);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkMulticast(maddr, ttl);
        }
        if (isInsideOgnlContext()) {
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
        if (isInsideOgnlContext()) {
            return super.checkTopLevelWindow(window);
        }
        return true;
    }

    @Override
    public void checkPrintJobAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPrintJobAccess();
        }
        if (isInsideOgnlContext()) {
            super.checkPrintJobAccess();
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSystemClipboardAccess();
        }
        if (isInsideOgnlContext()) {
            super.checkSystemClipboardAccess();
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkAwtEventQueueAccess();
        }
        if (isInsideOgnlContext()) {
            super.checkAwtEventQueueAccess();
        }
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPackageAccess(pkg);
        }
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkPackageDefinition(pkg);
        }
    }

    @Override
    public void checkSetFactory() {
        if (parentSecurityManager != null) {
            parentSecurityManager.checkSetFactory();
        }
        if (isInsideOgnlContext()) {
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
        if (isInsideOgnlContext()) {
            super.checkSecurityAccess(target);
        }
    }
}
