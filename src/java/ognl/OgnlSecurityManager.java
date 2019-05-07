// --------------------------------------------------------------------------
// Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// Neither the name of the Drew Davidson nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
// OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
// AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
// THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
// DAMAGE.
// --------------------------------------------------------------------------
package ognl;

import java.io.FileDescriptor;
import java.io.FilePermission;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.security.Permission;
import java.security.SecurityPermission;


/**
 * SecurityManager class for OGNL.
 * 
 * Warning:  It is <b>strongly recommended</b> that users utilize their own <b>application-specific</b>
 *   security manager, when required, in preference to this OGNL Security Manager.  This
 *   security manager implementation only provides <b>limited protection</b> for OGNL processing
 *   threads when enabled by a user.
 * 
 * This OGNL Security Manager is a subordinate security manager as it usually defers to the previous
 *   security manager, if one was provided.  If no explicit previous security manager is provided,
 *   it will use the default system security manager (assuming one is defined) as the previous
 *   security manager.
 *   Note: The currently installed system Policy will still influence results for many permission
 *     checks, so it may be necessary to make adjustments for a given application.
 * 
 * It can only be configured and installed - once - and supports configuration by JVM options or
 *   using explicit defaults.
 * Once configured/installed it is not possible to modify the configuration.  Subsequent calls to
 *   configure/install will result in {@link SecurityException}.
 * 
 * Enable using the JVM option: -Dognl.enableSecurityManager=true 
 * Disable using the JVM option: -Dognl.enableSecurityManager=false
 *   Note:  By default the OGNL Security Manager is disabled (no JVM option present).
 * 
 * Enable <b>"strict mode"</b> using the JVM option: -Dognl.securityManagerStrictMode=true
 * Disable <b>"strict mode"</b> using the JVM option: -Dognl.securityManagerStrictMode=false
 *   Note:  Strict mode is disabled by default, enabling it makes the OGNL security manager 
 *          more strict (and more likely to reject calls and/or cause failures).
 * 
 * The OGNL Security Manager can have its subordinate behaviour (OGNL-specific) effectively
 *   "toggled" at the Thread level in a relatively efficient manner.  This should limit impact
 *   to threads not currently processing OGNL calls when this security manager is installed.
 *   Note: Some access (AWT event queue, clipboard, printing, exit, exec, write, delete, network)
 *     should be rejected - while the OGNL Security Manager is active for a given thread - even
 *     if the previous security manager would normally permit those operations.
 * 
 * Note: The OGNL Security Manager will not permit another security manager to be installed
 *   once it is installed, unless the previousSecurityManager permits that operation 
 *   (in which case the OGNL Security Manager will also permit such requests).
 * 
 * The thread-level activation/de-activation mechanism of class is derived in part from examples
 *   at the following locations:
 *     <a href="https://alphaloop.blogspot.com/2014/08/a-per-thread-java-security-manager.html">Blog</a>
 *     <a href="https://github.com/alphaloop/selective-security-manager">Github project</a>
 * Thanks to alphaloop (Blog author) for promoting the thread-level ideas incorporated into this class.
 * 
 * @since 3.1.24
 */
public class OgnlSecurityManager extends SecurityManager {
    public static final String ENABLE_SECURITY_MANAGER = "ognl.enableSecurityManager";
    public static final String ENABLE_SECURITY_MANAGER_STRICTMODE = "ognl.securityManagerStrictMode";
    private static final Permission REFLECT_PERMISSION_SUPRESS_ACCESSCHECKS = new ReflectPermission("suppressAccessChecks");
    private static final Permission RUNTIME_PERMISSION_ACCESS_DECLAREDMEMBERS = new RuntimePermission("accessDeclaredMembers");
    private static final Permission RUNTIME_PERMISSION_SET_SECURITYMANAGER = new RuntimePermission("setSecurityManager");
    private static final Permission SECURITY_PERMISSION_GETPROPERTY_PACKAGEACCESS = new SecurityPermission("getProperty.package.access");
    private static final Permission FILE_PERMISSION_CURRENTDIRECTORY_AND_BELOW_READ = new FilePermission("-", "read");
    private static final Permission OGNL_INVOKE_PERMISSION = new OgnlInvokePermission("*");


    // ThreadLocal state allows GONLSecurityManager to be "effectively toggled" on an individual thread basis.
    private static final ThreadLocal<Boolean> enabledInCurrentThread = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;  // Initally disabled for each thread
            }
            @Override
            public void set(Boolean value) {
                if (value == null) {
                    throw new IllegalArgumentException("enabledInCurrentThread cannot be nulled");
                }
                super.set(value);
            }
        };
    private static final ThreadLocal<Integer> invocationDepthWhileEnabled = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                // Note: Using valueOf() to take advantage of its cached values
                return Integer.valueOf(0);  // Initally zeroed for each thread
            }
            @Override
            public void set(Integer value) {
                if (value == null) {
                    throw new IllegalArgumentException("invocationDepthWhileEnabled cannot be nulled");
                }
                super.set(value);
            }
        };
    private static volatile OgnlSecurityManager ognlSecurityManager = null;  // Singleton
    private final SecurityManager previousSecurityManager;
    private final boolean strictMode;


    // --------------------------------------------------------------------
    // OgnlSecurityManager specific methods.
    //
    // Note: It is not safe to use any System.out or Log output calls 
    //   (even for debugging) within this module.  Doing so may result in
    //   a StackOverflowError exception being thrown due to permission 
    //   scheck recursion.
    // --------------------------------------------------------------------

    /**
     * Generate a new OGNLSecurityManager instance.
     * 
     * @param previousSecurityManager
     */
    private OgnlSecurityManager(final SecurityManager previousSecurityManager, boolean strictMode)
    {
        super();
        this.previousSecurityManager = previousSecurityManager;
        this.strictMode = strictMode;
    }

    /**
     * Check if an OgnlSecurityManager instance has been installed or not.
     * 
     * @return boolean
     */
    public static boolean isOgnlSecurityManagerInstalled()
    {
        return ognlSecurityManager != null;
    }

    /**
     * Check if the current Thread call stack contains an earlier call by OgnlRuntime,
     *   ignoring (not counting) the first ignoreCount instances (since this method is
     *   running within OgnlMethodBlcoker we don't need to include this method itself).
     * 
     * Returns true if-and-only-if the current Thread call stack contains more than
     *   ignoreCount references to OgnlRuntime.
     * 
     * If the Thread call stack cannot be checked, this method will throw an {@link IllegalStateException}.
     * 
     * Note: The caller must know and account for the expected calling sequence
     *   for this method to be effective.
     * 
     * Note: This method may be EXPENSIVE, so avoid using it in frequently executed
     *   call sequences.
     * 
     * @param ignoreCount 
     * 
     * @return boolean
     */
    static final boolean ognlRuntimeExceedsCallStackIgnoreCount(int ignoreCount) {
        final StackTraceElement[] stackTraceElementArray;

        if (ignoreCount < 0 ) {
            throw new IllegalArgumentException("ognlRuntimeInThreadCallStack() does not support ignoreCount: " + ignoreCount + " ( < 0)");
        }
        try {
            int ognlRuntimeClassInStackCount = 0;
            stackTraceElementArray = Thread.currentThread().getStackTrace();
            for (int index = 0; index < stackTraceElementArray.length; index++) {
                if (OgnlRuntime.class.equals(stackTraceElementArray[index].getClass())) {
                    ognlRuntimeClassInStackCount++;
                    if (ognlRuntimeClassInStackCount > ignoreCount) {
                      return true;
                    }
                }
            }
        } catch (SecurityException se) {
            throw new IllegalStateException("ognlRuntimeInThreadCallStack() fails when stacktrace access denied", se);
        }

        return false;
    }

    /**
     * Create and install an OgnlSecurityManager instance only if configured to 
     *   do so by JVM Option.
     * 
     * Note: Will throw a {@link java.lang.SecurityException} if unable to install.
     * 
     * @return boolean true if installed, false otherwise
     * @throws SecurityException
     */
    public static synchronized boolean installOgnlSecurityManagerViaJVMOption() throws SecurityException {
        boolean enableSM;
        boolean enableStrictMode;
        try {
            enableSM = Boolean.parseBoolean(System.getProperty(ENABLE_SECURITY_MANAGER));
        } catch (SecurityException se) {
            enableSM = false;  // Cannot access the property, so don't install
        }
        try {
            enableStrictMode = Boolean.parseBoolean(System.getProperty(ENABLE_SECURITY_MANAGER_STRICTMODE));
        } catch (SecurityException se) {
            enableStrictMode = false;  // Cannot access the property, so don't install
        }
        if (enableSM) {
            return installOgnlSecurityManager(enableStrictMode);
        } else {
            return false;
        }
    }

    /**
     * Create and install an OgnlSecurityManager instance, using currently installed
     *   {@link java.lang.SecurityManager} as the previousSecurityManager (if present).
     * 
     * Note: Will throw a {@link java.lang.SecurityException} if unable to install.
     * 
     * @param strictMode
     * 
     * @return boolean true if installed, false otherwise
     * @throws SecurityException
     */
    public static synchronized boolean installOgnlSecurityManager(boolean strictMode) throws SecurityException {
        return installOgnlSecurityManager(System.getSecurityManager(), strictMode);
    }

    /**
     * Create and install an OgnlSecurityManager instance, using the provided
     *   {@link java.lang.SecurityManager} as the previousSecurityManager (if non-null).
     * 
     * This can be used to manually install the OGNLSecurityManager.
     * Note: Will throw a {@link java.lang.SecurityException} if unable to install.
     * 
     * @param securityManager
     * @param strictMode
     * 
     * @return boolean true if installed, false otherwise
     * @throws SecurityException
     */
    public static synchronized boolean installOgnlSecurityManager(SecurityManager securityManager, boolean strictMode) throws SecurityException {
        if (OgnlSecurityManager.ognlSecurityManager != null) {
            throw new SecurityException("OGNL security manager already installed, re-installation not permitted");
        }
        final OgnlSecurityManager tempOgnlSecurityManagerRef = new OgnlSecurityManager(securityManager, strictMode);
        System.setSecurityManager(tempOgnlSecurityManagerRef);
        OgnlSecurityManager.ognlSecurityManager = tempOgnlSecurityManagerRef;
        return true;
    }

    /**
     * Check if the OgnlSecurityManager is currently enabled for the current thread.
     * 
     * @return boolean
     */
    static boolean isEnabledForCurrentThread() {
        return ognlSecurityManager != null && enabledInCurrentThread.get().booleanValue();
    }

    /**
     * Enable OgnlSecurityManager for the current thread.
     * 
     * Returns true if enable succeeded, false otherwise
     * 
     * Note: Attempting to enable when already enabled will return false (failure)
     *    since you cannot enable something already enabled.
     * 
     * @return boolean
     */
    static boolean enableForCurrentThread() {
        boolean success = false;
        if (ognlSecurityManager != null) {
            final boolean enabled = enabledInCurrentThread.get().booleanValue();
            if (!enabled) {
                enabledInCurrentThread.set(Boolean.TRUE);
                invocationDepthWhileEnabled.set(Integer.valueOf(0));
                success = true;
            }
        }

        return success;
    }

    /**
     * Disable OgnlSecurityManager for the current thread.
     * 
     * Returns true if disable succeeded, false otherwise
     * 
     * Note: Cannot disable the OgnlSecurityManager for the thread until the 
     *   invocationDepth returns to 0 (original caller complete).
     * 
     * @return boolean
     */
    static boolean disableForCurrentThread() {
        boolean success = false;
        if (ognlSecurityManager != null) {
            final boolean enabled = enabledInCurrentThread.get().booleanValue();
            if (enabled) {
                final int value = invocationDepthWhileEnabled.get().intValue();
                if (value < 1) {
                    enabledInCurrentThread.set(Boolean.FALSE);
                    invocationDepthWhileEnabled.set(Integer.valueOf(0));
                    success = true;
                }
            }
        }

        return success;
    }

    /**
     * Increment the current invocation depth.
     * 
     * Returns the current invocation depth value (after increment).
     * 
     * Note: If the increment fails, the return value is -1.
     * 
     * @return int
     */
    static int incrementInvocationDepthForCurrentThread() {
        int result = -1;
        if (ognlSecurityManager != null) {
            final boolean enabled = enabledInCurrentThread.get().booleanValue();
            if (enabled) {
                result = invocationDepthWhileEnabled.get().intValue();
                if (result < Integer.MAX_VALUE) {
                    result++;
                }
                invocationDepthWhileEnabled.set(Integer.valueOf(result));
            }
        }

        return result;
    }

    /**
     * Decrement the current invocation depth.
     * 
     * Returns the current invocation depth value (after decrement).
     * 
     * Note: If the decrement fails, the return value is -1.
     * 
     * @return int
     */
    static int decrementInvocationDepthForCurrentThread() {
        int result = -1;
        if (ognlSecurityManager != null) {
            final boolean enabled = enabledInCurrentThread.get().booleanValue();
            if (enabled) {
                result = invocationDepthWhileEnabled.get().intValue();
                if (result > 0) {
                    result--;
                }
                invocationDepthWhileEnabled.set(Integer.valueOf(result));
            }
        }

        return result;
    }

    /**
     * Check if the current Thread call stack depth (as represented by the Class[] array),
     *   combined with the given Permission indicates the operation should be permitted.
     * 
     * Returns true if the Permission should be permitted, false otherwise.
     * 
     * If the Thread call stack cannot be checked, this method will throw an
     *   {@link IllegalStateException}.
     * 
     * Note: This method may be EXPENSIVE, so avoid using it in frequently executed
     *   call sequences.
     * 
     * @param clazzArray
     * @param perm
     * @param strictMode
     * 
     * @return boolean
     */
    static final boolean permitBasedOnCallStack(Class[] clazzArray, Permission perm, boolean strictMode) {
        final StackTraceElement[] stackTraceElementArray;
        if (clazzArray == null || perm == null) {
            return false;
        }
        // Attempt to detect and prevent unexpected reflection method invocation calls.
        //   Note: Considered attempting to prevent usage of AccessibleObject in 
        //     unwanted contexts but it proved too complex to distinguish from
        //     normal usage by OGNL.
        int ognlRuntimeClassFirstStackIndex = -1;
        boolean unexpectedCallSequenceFound = false;
        for (int index = 1; index < clazzArray.length; index++) {
            if (index == 2 && OgnlRuntime.class.equals(clazzArray[index])) {
                ognlRuntimeClassFirstStackIndex = index;
                break;  // Detected special case for OgnlRuntime making a method accessible
            }
            if (Method.class.equals(clazzArray[index]) && !OgnlRuntime.class.equals(clazzArray[index - 1])) {
                unexpectedCallSequenceFound = true;
                break;
            }
        }
        if (!unexpectedCallSequenceFound || ognlRuntimeClassFirstStackIndex == 2) {
            // Permit check when no unexpectedCallSequence found.
            // Permit OgnlRuntime's accessibility change calls during invokeMethod() (index 2).
            if (REFLECT_PERMISSION_SUPRESS_ACCESSCHECKS.implies(perm)) {
                return true;
            }
            // Permit certain specific permissions common to some clients, when strictMode is false.
            //   Note: "getProtectionDomain" permission may be required for certain Spring integration
            //     or other dynamic proxies, but those should be enabled by Policy if needed.
            if ( !strictMode &&
                (SECURITY_PERMISSION_GETPROPERTY_PACKAGEACCESS.implies(perm) ||
                FILE_PERMISSION_CURRENTDIRECTORY_AND_BELOW_READ.implies(perm) ||
                RUNTIME_PERMISSION_ACCESS_DECLAREDMEMBERS.implies(perm) ||
                OGNL_INVOKE_PERMISSION.implies(perm) )) {
                return true;  // Required for some clients (e.g. Struts) - may not permit in the future.
            }
        }

        return false;
    }

    // --------------------------------------------------------------------
    // SecurityManager public methods implemented by OgnlSecurityManager.
    // --------------------------------------------------------------------

    @Override
    public ThreadGroup getThreadGroup() {
        if (previousSecurityManager != null) {
            return previousSecurityManager.getThreadGroup();
        }
        return super.getThreadGroup();
    }

    @Override
    public void checkSecurityAccess(String target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkSecurityAccess(target);
        } else {
            super.checkSecurityAccess(target);
        }
    }

    // Note: This implementation does NOT define the overriden method
    //     @Override
    //     public void checkMemberAccess(Class<?> clazz, int which)
    //   This is due to the method relying on a specific stack depth so it cannot
    //     work when called by a descendant.  For that reason the default 
    //     implementation is left intact for compatibility.

    @Override
    public void checkSetFactory() {
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL SetFactory denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkSetFactory();
        }
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        if (pkg == null) {
            throw new NullPointerException("package cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPackageDefinition(pkg);
        } else if (isEnabledForCurrentThread()) {
            super.checkPackageDefinition(pkg);
        }
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (pkg == null) {
            throw new NullPointerException("package cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPackageAccess(pkg);
        } else if (isEnabledForCurrentThread()) {
            super.checkPackageAccess(pkg);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL AwtEventQueueAccess denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkAwtEventQueueAccess();
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL SystemClipboardAccess denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkSystemClipboardAccess();
        }
    }

    @Override
    public void checkPrintJobAccess() {
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL PrintJobAccess denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkPrintJobAccess();
        }
    }

    @Override
    public boolean checkTopLevelWindow(Object window) {
        if (window == null) {
            throw new NullPointerException("window cannot be null");
        }
        if (previousSecurityManager != null) {
            return previousSecurityManager.checkTopLevelWindow(window);
        } else {
            return false;  // Always indicate not allowed (even if not enabled)
        }
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPropertyAccess(key);
        } else if (isEnabledForCurrentThread()) {
            super.checkPropertyAccess(key);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (isEnabledForCurrentThread()) {
            // Note: This may cause failures if evaluated code calls libraries that require
            //   this access.  Access is rejected to prevent *writes* to system properties.
            throw new SecurityException("OGNL PropertiesAccess denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkPropertiesAccess();
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        if (maddr == null) {
            throw new NullPointerException("multicast address cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Multicast denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkMulticast(maddr, ttl);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (maddr == null) {
            throw new NullPointerException("multicast address cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Multicast denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkMulticast(maddr);
        }
    }

    @Override
    public void checkAccept(String host, int port) {
        if (host == null) {
            throw new NullPointerException("host cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Accept denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkAccept(host, port);
        }
    }

    @Override
    public void checkListen(int port) {
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Listen denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkListen(port);
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (host == null) {
            throw new NullPointerException("host cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Connect denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkConnect(host, port, context);
        }
    }

    @Override
    public void checkConnect(String host, int port) {
        if (host == null) {
            throw new NullPointerException("host cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Connect denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkConnect(host, port);
        }
    }

    @Override
    public void checkDelete(String file) {
        if (file == null) {
            throw new NullPointerException("file cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Delete denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkDelete(file);
        }
    }

    @Override
    public void checkWrite(String file) {
        if (file == null) {
            throw new NullPointerException("file cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Write denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkWrite(file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException("file descriptor cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Write denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkWrite(fd);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        if (file == null) {
            throw new NullPointerException("file cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkRead(file, context);
        } else if (isEnabledForCurrentThread()) {
            super.checkRead(file, context);
        }
    }

    @Override
    public void checkRead(String file) {
        if (file == null) {
            throw new NullPointerException("file cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkRead(file);
        } else if (isEnabledForCurrentThread()) {
            super.checkRead(file);
        }
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException("file descriptor cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkRead(fd);
        } else if (isEnabledForCurrentThread()) {
            super.checkRead(fd);
        }
    }

    @Override
    public void checkLink(String lib) {
        if (lib == null) {
            throw new NullPointerException("lib cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkLink(lib);
        } else if (isEnabledForCurrentThread()) {
            super.checkLink(lib);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (cmd == null) {
            throw new NullPointerException("cmd cannot be null");
        }
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Exec denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkExec(cmd);
        }
    }

    @Override
    public void checkExit(int status) {
        if (isEnabledForCurrentThread()) {
            throw new SecurityException("OGNL Exit denied");  // Always disallow
        } else if (previousSecurityManager != null) {
            previousSecurityManager.checkExit(status);
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (g == null) {
            throw new NullPointerException("thread group cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkAccess(g);
        } else if (isEnabledForCurrentThread()) {
            super.checkAccess(g);
        }
    }

    @Override
    public void checkAccess(Thread t) {
        if (t == null) {
            throw new NullPointerException("thread cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkAccess(t);
        } else if (isEnabledForCurrentThread()) {
            super.checkAccess(t);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (previousSecurityManager != null) {
            previousSecurityManager.checkCreateClassLoader();
        }
        // Unless the previous security manager denies it, OGNL needs to be able
        //   to create ClassLoader instances frequently in order to function properly.
        // So we MUST always permit it (so no point in checking if isEnabledForCurrentThread().
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (perm == null) {
            throw new NullPointerException("permission cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPermission(perm, context);
        } else if (isEnabledForCurrentThread()) {
            if (RUNTIME_PERMISSION_SET_SECURITYMANAGER.implies(perm)) {
                throw new SecurityException("OGNL setSecurityManager denied");  // Disallow while active
            }
            if (!permitBasedOnCallStack(getClassContext(), perm, strictMode)) {
                super.checkPermission(perm, context);
            }
        } else {
            if (RUNTIME_PERMISSION_SET_SECURITYMANAGER.implies(perm)) {
                throw new SecurityException("OGNL setSecurityManager denied");  // Disallow while not active
            }
        }
    }

    @Override
    public void checkPermission(Permission perm) {
        if (perm == null) {
            throw new NullPointerException("permission cannot be null");
        }
        if (previousSecurityManager != null) {
            previousSecurityManager.checkPermission(perm);
        } else if (isEnabledForCurrentThread()) {
            if (RUNTIME_PERMISSION_SET_SECURITYMANAGER.implies(perm)) {
                throw new SecurityException("OGNL setSecurityManager denied");  // Disallow while active
            }
            if (!permitBasedOnCallStack(getClassContext(), perm, strictMode)) {
                super.checkPermission(perm);
            }
        } else {
            if (RUNTIME_PERMISSION_SET_SECURITYMANAGER.implies(perm)) {
                throw new SecurityException("OGNL setSecurityManager denied");  // Disallow while not active
            }
        }
    }

    @Override
    public Object getSecurityContext() {
        if (previousSecurityManager != null) {
            return previousSecurityManager.getSecurityContext();
        } else {
            return super.getSecurityContext();
        }
    }

    @Override
    public boolean getInCheck() {
        if (previousSecurityManager != null) {
            return previousSecurityManager.getInCheck();
        } else if (isEnabledForCurrentThread()) {
            return super.getInCheck();
        } else {
            return false;
        }
    }

    // ----------------------------------------------------------
    // SecurityManager protected methods implemented by OgnlSecurityManager
    // ----------------------------------------------------------

    // Currently no ancestor SecurityManager protected methods are implemented.
}
