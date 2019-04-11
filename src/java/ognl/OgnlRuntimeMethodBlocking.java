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

import ognl.enhance.OgnlExpressionCompiler;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class used by internal OGNL API to do delegate (alternate) OgnlRuntime processing.
 * 
 * Delegate convention used by internal OGNL API to do allow for delegate
 * (alternate) OgnlRuntime processing.
 * 
 * Convention:
 *   The convention for delegate methods is:
 *   1) For a method named "methodName", the delegate method name should be "delegateMethodName".
 *   2) Descendant classes should implement ALL delegate methods as "protected".
 *   3) Descendant classes should utilize the Singleton pattern (at most one instance exists).
 *   4) Descendant classes class should NOT call any ancestor OgnlRuntime methods that
 *      support delegate processing.
 *      Note:  Violating this convention may result in unwanted recursion failures.
 * 
 * @since 3.1.24
 */
public class OgnlRuntimeMethodBlocking extends OgnlRuntime {

    private static final Map<Method, Boolean> _methodDenyList = new ConcurrentHashMap<Method, Boolean>(101);

    private static final Method ADDMETHODTODENYLIST_REF;

    private static volatile OgnlRuntimeMethodBlocking ognlRuntimeMethodBlockingSingleton = null;

    /**
     * Initialize the Method references used in invokeMethod() checks
     */
    static {
        try {
            ADDMETHODTODENYLIST_REF = OgnlRuntimeMethodBlocking.class.getMethod("addMethodToDenyList", new Class<?>[]{Method.class});
        } catch (NoSuchMethodException nsme) {
            throw new IllegalStateException("OgnlRuntimeMethodBlocking initialization missing required method", nsme);
        }
    }


    /**
     * Generate an instance (for Singleton use)
     */
    private OgnlRuntimeMethodBlocking() {
        // For Singleton
    }

    /**
     * Get instance of OgnlRuntimeMethodBlocking.
     * 
     * Intended (only) for use by OgnlRuntime.
     * 
     * @return 
     */
    public synchronized static OgnlRuntimeMethodBlocking getInstance() {
        if (ognlRuntimeMethodBlockingSingleton == null) {
            ognlRuntimeMethodBlockingSingleton = new OgnlRuntimeMethodBlocking();
        }

        return ognlRuntimeMethodBlockingSingleton;
    }

    /**
     * Add a method to the OgnlRuntimeMethodBlocking method deny list.
     * 
     * The OgnlRuntimeMethodBlocking method deny list is only additive (only provide a method to
     * add deny list elements, no methods to clear or remove entries).
     * 
     * @param method (a non-null Method parameter)
     */
    public static void addMethodToDenyList(Method method)
    {
        if (method == null) {
            throw new IllegalArgumentException("Cannot add a null Method to the deny list");
        }

        _methodDenyList.put(method, Boolean.TRUE);
    }

    /**
     * Add a predefined "minimal" list of methods to the OgnlRuntimeMethodBlocking method deny list.
     * 
     * It uses a small list of methods that OGNL expressions should not normally need to call.
     * 
     * @throws NoSuchMethodException
     */
    public static void prepareMinimalMethodDenyList() throws NoSuchMethodException
    {
        final Class<?>[] noClassArgument = new Class<?>[0];
        final Class<?>[] singleClassArgument = new Class<?>[1];
        final Class<?>[] twoClassArgument = new Class<?>[2];
        final Class<?>[] threeClassArgument = new Class<?>[3];

        // Deny some OgnlRuntime methods (which seem reasonable to restrict)
        addMethodToDenyList(OgnlRuntime.class.getMethod("getSecurityManager", noClassArgument));
        addMethodToDenyList(OgnlRuntime.class.getMethod("getCompiler", noClassArgument));
        singleClassArgument[0] = SecurityManager.class;
        addMethodToDenyList(OgnlRuntime.class.getMethod("setSecurityManager", singleClassArgument));
        singleClassArgument[0] = OgnlExpressionCompiler.class;
        addMethodToDenyList(OgnlRuntime.class.getMethod("setCompiler", singleClassArgument));
        threeClassArgument[0] = OgnlContext.class;
        threeClassArgument[1] = Node.class;
        threeClassArgument[2] = Object.class;
        addMethodToDenyList(OgnlRuntime.class.getMethod("compileExpression", threeClassArgument));

        // Deny some System methods
        addMethodToDenyList(System.class.getMethod("getSecurityManager", noClassArgument));
        singleClassArgument[0] = SecurityManager.class;
        addMethodToDenyList(System.class.getMethod("setSecurityManager", singleClassArgument));
        singleClassArgument[0] = Properties.class;
        addMethodToDenyList(System.class.getMethod("setProperties", singleClassArgument));
        singleClassArgument[0] = String.class;
        addMethodToDenyList(System.class.getMethod("clearProperty", singleClassArgument));
        addMethodToDenyList(System.class.getMethod("load", singleClassArgument));
        addMethodToDenyList(System.class.getMethod("loadLibrary", singleClassArgument));
        addMethodToDenyList(System.class.getMethod("mapLibraryName", singleClassArgument));
        singleClassArgument[0] = InputStream.class;
        addMethodToDenyList(System.class.getMethod("setIn", singleClassArgument));
        singleClassArgument[0] = PrintStream.class;
        addMethodToDenyList(System.class.getMethod("setOut", singleClassArgument));
        addMethodToDenyList(System.class.getMethod("setErr", singleClassArgument));
        singleClassArgument[0] = int.class;
        addMethodToDenyList(System.class.getMethod("exit", singleClassArgument));
        twoClassArgument[0] = String.class;
        twoClassArgument[1] = String.class;
        addMethodToDenyList(System.class.getMethod("setProperty", twoClassArgument));

        // Deny ProcessBuilder start method
        addMethodToDenyList(ProcessBuilder.class.getMethod("start", noClassArgument));

        // Deny some Runtime methods
        addMethodToDenyList(Runtime.class.getMethod("getRuntime", noClassArgument));
        singleClassArgument[0] = Thread.class;
        addMethodToDenyList(Runtime.class.getMethod("addShutdownHook", singleClassArgument));
        singleClassArgument[0] = int.class;
        addMethodToDenyList(Runtime.class.getMethod("exit", singleClassArgument));
        addMethodToDenyList(Runtime.class.getMethod("halt", singleClassArgument));
        singleClassArgument[0] = InputStream.class;
        try {
            addMethodToDenyList(Runtime.class.getMethod("getLocalizedInputStream", singleClassArgument));
        } catch (NoSuchMethodException nsme) {
            // Deprecated method.  Avoid exception if it disappears in later JDK versions
        }
        singleClassArgument[0] = OutputStream.class;
        try {
            addMethodToDenyList(Runtime.class.getMethod("getLocalizedOutputStream", singleClassArgument));
        } catch (NoSuchMethodException nsme) {
            // Deprecated method.  Avoid exception if it disappears in later JDK versions
        }
        singleClassArgument[0] = String.class;
        addMethodToDenyList(Runtime.class.getMethod("exec", singleClassArgument));
        addMethodToDenyList(Runtime.class.getMethod("load", singleClassArgument));
        addMethodToDenyList(Runtime.class.getMethod("loadLibrary", singleClassArgument));
        singleClassArgument[0] = String[].class;
        addMethodToDenyList(Runtime.class.getMethod("exec", singleClassArgument));
        twoClassArgument[0] = String[].class;
        twoClassArgument[1] = String[].class;
        addMethodToDenyList(Runtime.class.getMethod("exec", twoClassArgument));
        twoClassArgument[0] = String.class;
        twoClassArgument[1] = String[].class;
        addMethodToDenyList(Runtime.class.getMethod("exec", twoClassArgument));
        threeClassArgument[0] = String[].class;
        threeClassArgument[1] = String[].class;
        threeClassArgument[2] = File.class;
        addMethodToDenyList(Runtime.class.getMethod("exec", threeClassArgument));
        threeClassArgument[0] = String.class;
        threeClassArgument[1] = String[].class;
        threeClassArgument[2] = File.class;
        addMethodToDenyList(Runtime.class.getMethod("exec", threeClassArgument));

        // Deny some Thread methods
        addMethodToDenyList(Thread.class.getMethod("currentThread", noClassArgument));
        addMethodToDenyList(Thread.class.getMethod("dumpStack", noClassArgument));
        addMethodToDenyList(Thread.class.getMethod("getAllStackTraces", noClassArgument));
        addMethodToDenyList(Thread.class.getMethod("getContextClassLoader", noClassArgument));
        addMethodToDenyList(Thread.class.getMethod("getDefaultUncaughtExceptionHandler", noClassArgument));
        addMethodToDenyList(Thread.class.getMethod("yield", noClassArgument));
        singleClassArgument[0] = Thread[].class;
        addMethodToDenyList(Thread.class.getMethod("enumerate", singleClassArgument));
        singleClassArgument[0] = Thread.UncaughtExceptionHandler.class;
        addMethodToDenyList(Thread.class.getMethod("setDefaultUncaughtExceptionHandler", singleClassArgument));
        singleClassArgument[0] = long.class;
        addMethodToDenyList(Thread.class.getMethod("sleep", singleClassArgument));
        twoClassArgument[0] = long.class;
        twoClassArgument[1] = int.class;
        addMethodToDenyList(Thread.class.getMethod("sleep", twoClassArgument));
    }

    /**
     * Add a predefined "standard" list of methods to the OgnlRuntimeMethodBlocking method deny list.
     * 
     * It uses a larger list of methods that OGNL expressions should not normally need to call.
     * The generated method deny list includes everything provided by prepareMinimalMethodDenyList()
     * and more.
     * 
     * @throws NoSuchMethodException
     */
    public static void prepareStandardMethodDenyList() throws NoSuchMethodException
    {
        final Class<?>[] noClassArgument = new Class<?>[0];
        final Class<?>[] singleClassArgument = new Class<?>[1];
        final Class<?>[] twoClassArgument = new Class<?>[2];

        // Deny the minimal list first
        prepareMinimalMethodDenyList();

        // Deny more System methods
        try {
            addMethodToDenyList(System.class.getMethod("console", noClassArgument));
        } catch (NoSuchMethodException nsme) {
            // JDK 1.6+ method.  Avoid exception if running under JDK 1.5
        }
        addMethodToDenyList(System.class.getMethod("inheritedChannel", noClassArgument));
        addMethodToDenyList(System.class.getMethod("getProperties", noClassArgument));
        addMethodToDenyList(System.class.getMethod("getenv", noClassArgument));
        addMethodToDenyList(System.class.getMethod("gc", noClassArgument));
        addMethodToDenyList(System.class.getMethod("runFinalization", noClassArgument));
        singleClassArgument[0] = String.class;
        addMethodToDenyList(System.class.getMethod("getProperty", singleClassArgument));
        addMethodToDenyList(System.class.getMethod("getenv", singleClassArgument));
        try {
            addMethodToDenyList(System.class.getMethod("runFinalizersOnExit", singleClassArgument));
        } catch (NoSuchMethodException nsme) {
            // Deprecated method.  Avoid exception if it disappears in later JDK versions
        }
        twoClassArgument[0] = String.class;
        twoClassArgument[1] = String.class;
        addMethodToDenyList(System.class.getMethod("getProperty", twoClassArgument));

        // Deny more Runtime methods
        addMethodToDenyList(Runtime.class.getMethod("gc", noClassArgument));
        addMethodToDenyList(Runtime.class.getMethod("runFinalization", noClassArgument));

        singleClassArgument[0] = boolean.class;
        try {
            addMethodToDenyList(Runtime.class.getMethod("runFinalizersOnExit", singleClassArgument));
        } catch (NoSuchMethodException nsme) {
            // Deprecated method.  Avoid exception if it disappears in later JDK versions
        }
        addMethodToDenyList(Runtime.class.getMethod("traceInstructions", singleClassArgument));
        addMethodToDenyList(Runtime.class.getMethod("traceMethodCalls", singleClassArgument));
    }

    /**
     * Perform delegate (alternate) invokeMethod() processing.
     * 
     * Checks for denied methods, then performs standard processing
     * (same logic as ancestor invokeMethod()) if checks pass.
     * 
     * Note: It is recommended that when OgnlRuntime invokeMethod() code is changed that
     *   this method be reviewed to determine if the changes are applicable here.
     * 
     * @param target
     * @param method
     * @param argsArray
     * 
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException 
     */
    @Override
    protected Object delegateInvokeMethod(Object target, Method method, Object[] argsArray)
            throws InvocationTargetException, IllegalAccessException
    {
        boolean syncInvoke;
        boolean checkPermission;
        Boolean methodAccessCacheValue;
        Boolean methodPermCacheValue;

        // only synchronize method invocation if it actually requires it

        synchronized(method) {
            // Always disallow invokeMethod() calling addMethodToDenyList()
            if (ADDMETHODTODENYLIST_REF.equals(method)) {
                throw new IllegalAccessException("Method [" + method + "] is not permitted.");
            }

            // Disallow any methods in the deny list (only check if deny list nonempty, for JIT performance)
            if (_methodDenyList.isEmpty() == false) {
                if (_methodDenyList.get(method) != null) {
                    throw new IllegalAccessException("Method [" + method + "] is deny listed.");
                }
            }

            // Passed deny checks, perform standard OgnlRuntime invokeMethod() call
            methodAccessCacheValue = _methodAccessCache.get(method);
            if (methodAccessCacheValue == null) {
                if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                {
                    if (!(((AccessibleObject) method).isAccessible()))
                    {
                        methodAccessCacheValue = Boolean.TRUE;
                        _methodAccessCache.put(method, methodAccessCacheValue);
                    } else
                    {
                        methodAccessCacheValue = Boolean.FALSE;
                        _methodAccessCache.put(method, methodAccessCacheValue);
                    }
                } else
                {
                    methodAccessCacheValue = Boolean.FALSE;
                    _methodAccessCache.put(method, methodAccessCacheValue);
                }
            }
            syncInvoke = Boolean.TRUE.equals(methodAccessCacheValue);

            methodPermCacheValue = _methodPermCache.get(method);
            if (methodPermCacheValue == null) {
                if (_securityManager != null) {
                    try
                    {
                        _securityManager.checkPermission(getPermission(method));
                        methodPermCacheValue = Boolean.TRUE;
                        _methodPermCache.put(method, methodPermCacheValue);
                    } catch (SecurityException ex) {
                        methodPermCacheValue = Boolean.FALSE;
                        _methodPermCache.put(method, methodPermCacheValue);
                        throw new IllegalAccessException("Method [" + method + "] cannot be accessed.");
                    }
                }
                else {
                    methodPermCacheValue = Boolean.TRUE;
                    _methodPermCache.put(method, methodPermCacheValue);
                }
            }
            checkPermission = Boolean.FALSE.equals(methodPermCacheValue);
        }

        Object result;

        if (syncInvoke) //if is not public and is not accessible
        {
            synchronized(method)
            {
                if (checkPermission)
                {
                    try
                    {
                        _securityManager.checkPermission(getPermission(method));
                    } catch (SecurityException ex) {
                        throw new IllegalAccessException("Method [" + method + "] cannot be accessed.");
                    }
                }

                ((AccessibleObject) method).setAccessible(true);
                try {
                    result = method.invoke(target, argsArray);
                } finally {
                    ((AccessibleObject) method).setAccessible(false);
                }
            }
        } else
        {
            if (checkPermission)
            {
                try
                {
                    _securityManager.checkPermission(getPermission(method));
                } catch (SecurityException ex) {
                    throw new IllegalAccessException("Method [" + method + "] cannot be accessed.");
                }
            }

            result = method.invoke(target, argsArray);
        }

        return result;
    }

}
