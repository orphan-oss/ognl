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

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.OgnlExpressionCompiler;
import ognl.internal.ClassCache;
import ognl.internal.ClassCacheImpl;
import ognl.security.OgnlSecurityManagerFactory;
import ognl.security.UserMethod;

import java.beans.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class used by internal OGNL API to do various things like:
 *
 * <ul>
 * <li>Handles majority of reflection logic / caching. </li>
 * <li>Utility methods for casting strings / various numeric types used by {@link OgnlExpressionCompiler}.</li.
 * <li>Core runtime configuration point for setting/using global {@link TypeConverter} / {@link OgnlExpressionCompiler} /
 * {@link NullHandler} instances / etc.. </li>
 *</ul>
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class OgnlRuntime {

    /**
     * Constant expression used to indicate that a given method / property couldn't be found
     * during reflection operations.
     */
    public static final Object NotFound = new Object();
    public static final List NotFoundList = new ArrayList();
    public static final Map NotFoundMap = new HashMap();
    public static final Object[] NoArguments = new Object[]{};
    public static final Class[] NoArgumentTypes = new Class[]{};

    /**
     * Token returned by TypeConverter for no conversion possible
     */
    public static final Object NoConversionPossible = "ognl.NoConversionPossible";

    /**
     * Not an indexed property
     */
    public static int INDEXED_PROPERTY_NONE = 0;
    /**
     * JavaBeans IndexedProperty
     */
    public static int INDEXED_PROPERTY_INT = 1;
    /**
     * OGNL ObjectIndexedProperty
     */
    public static int INDEXED_PROPERTY_OBJECT = 2;

    /**
     * Constant string representation of null string.
     */
    public static final String NULL_STRING = "" + null;

    /**
     * Java beans standard set method prefix.
     */
    private static final String SET_PREFIX = "set";
    /**
     * Java beans standard get method prefix.
     */
    private static final String GET_PREFIX = "get";
    /**
     * Java beans standard is<Foo> boolean getter prefix.
     */
    private static final String IS_PREFIX = "is";

    /**
     * Prefix padding for hexadecimal numbers to HEX_LENGTH.
     */
    private static final Map HEX_PADDING = new HashMap();

    private static final int HEX_LENGTH = 8;

    /**
     * Returned by <CODE>getUniqueDescriptor()</CODE> when the object is <CODE>null</CODE>.
     */
    private static final String NULL_OBJECT_STRING = "<null>";

    /**
     * Used to store the result of determining if current jvm is 1.5 language compatible.
     */
    private static boolean _jdk15 = false;
    private static boolean _jdkChecked = false;

    /**
     * Control usage of JDK9+ access handler using the JVM option:
     *   -Dognl.UseJDK9PlusAccessHandler=true
     *   -Dognl.UseJDK9PlusAccessHandler=false
     *
     * Note: Set to "true" to allow the new JDK9 and later behaviour, <b>provided a newer JDK9+
     *   is detected</b>.  By default the standard pre-JDK9 AccessHandler will be used even when
     *   running on JDK9+, so users must "opt-in" in order to enable the alternate JDK9+ AccessHandler.
     *   Using the JDK9PlusAccessHandler <b>may</b> avoid / mask JDK9+ warnings of the form:
     *     "WARNING: Illegal reflective access by ognl.OgnlRuntime"
     *   or provide an alternative  when running in environments set with "--illegal-access=deny".
     *
     * Note:  The default behaviour is to use the standard pre-JDK9 access handler.
     *   Using the "false" value has the same effect as omitting the option completely.
     *
     * Warning: Users are <b>strongly advised</b> to review their code and confirm they really
     *   need the AccessHandler modifying access levels, looking at alternatives to avoid that need.
     */
    static final String USE_JDK9PLUS_ACESS_HANDLER = "ognl.UseJDK9PlusAccessHandler";

    /**
     * Control usage of "stricter" invocation processing by invokeMethod() using the JVM options:
     *    -Dognl.UseStricterInvocation=true
     *    -Dognl.UseStricterInvocation=false
     *
     * Note: Using the "true" value has the same effect as omitting the option completely.
     *   The default behaviour is to use the "stricter" invocation processing.
     *   Using the "false" value reverts to the older "less strict" invocation processing
     *   (in the event the "stricter" processing causes issues for existing applications).
     */
    static final String USE_STRICTER_INVOCATION = "ognl.UseStricterInvocation";

    /**
     * Hold environment flag state associated with USE_JDK9PLUS_ACESS_HANDLER.
     *   Default: false (if not set)
     */
    private static final boolean _useJDK9PlusAccessHandler;
    static {
        boolean initialFlagState = false;
        try {
            final String propertyString = System.getProperty(USE_JDK9PLUS_ACESS_HANDLER);
            if (propertyString != null && propertyString.length() > 0) {
                initialFlagState = Boolean.parseBoolean(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        _useJDK9PlusAccessHandler = initialFlagState;
    }

    /**
     * Hold environment flag state associated with USE_STRICTER_INVOCATION.
     *   Default: true (if not set)
     */
    private static final boolean _useStricterInvocation;
    static {
        boolean initialFlagState = true;
        try {
            final String propertyString = System.getProperty(USE_STRICTER_INVOCATION);
            if (propertyString != null && propertyString.length() > 0) {
                initialFlagState = Boolean.parseBoolean(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        _useStricterInvocation = initialFlagState;
    }

    /*
     * Attempt to detect the system-reported Major Java Version (e.g. 5, 7, 11).
     */
    private static final int _majorJavaVersion = detectMajorJavaVersion();
    private static final boolean _jdk9Plus = _majorJavaVersion >= 9;

    /*
     * Assign an accessibility modification mechanism, based on Major Java Version and Java option flag
     *   flag {@link OgnlRuntime#USE_JDK9PLUS_ACESS_HANDLER}.
     *
     * Note: Will use the standard Pre-JDK9 accessibility modification mechanism unless OGNL is running
     *   on JDK9+ and the Java option flag has also been set true.
     */
    private static final AccessibleObjectHandler _accessibleObjectHandler;
    static {
        _accessibleObjectHandler = usingJDK9PlusAccessHandler() ? AccessibleObjectHandlerJDK9Plus.createHandler() :
            AccessibleObjectHandlerPreJDK9.createHandler();
    }

    /**
     * Private references for use in blocking direct invocation by invokeMethod().
     */
    private static final Method SYS_CONSOLE_REF;
    private static final Method SYS_EXIT_REF;
    private static final Method AO_SETACCESSIBLE_REF;
    private static final Method AO_SETACCESSIBLE_ARR_REF;

    /**
     * Initialize the Method references used for blocking usage within invokeMethod().
     */
    static {
        Method setAccessibleMethod = null;
        Method setAccessibleMethodArray = null;
        Method systemExitMethod = null;
        Method systemConsoleMethod = null;
        try {
            setAccessibleMethod = AccessibleObject.class.getMethod("setAccessible", new Class<?>[]{boolean.class});
        } catch (NoSuchMethodException nsme) {
            // Should not happen.  To debug, uncomment the next line.
            //throw new IllegalStateException("OgnlRuntime initialization missing setAccessible method", nsme);
        } catch (SecurityException se) {
            // May be blocked by existing SecurityManager.  To debug, uncomment the next line.
            //throw new SecurityException("OgnlRuntime initialization cannot access setAccessible method", se);
        } finally {
            AO_SETACCESSIBLE_REF = setAccessibleMethod;
        }

        try {
            setAccessibleMethodArray = AccessibleObject.class.getMethod("setAccessible", new Class<?>[]{AccessibleObject[].class, boolean.class});
        } catch (NoSuchMethodException nsme) {
            // Should not happen.  To debug, uncomment the next line.
            //throw new IllegalStateException("OgnlRuntime initialization missing setAccessible method", nsme);
        } catch (SecurityException se) {
            // May be blocked by existing SecurityManager.  To debug, uncomment the next line.
            //throw new SecurityException("OgnlRuntime initialization cannot access setAccessible method", se);
        } finally {
            AO_SETACCESSIBLE_ARR_REF = setAccessibleMethodArray;
        }

        try {
            systemExitMethod = System.class.getMethod("exit", new Class<?>[]{int.class});
        } catch (NoSuchMethodException nsme) {
            // Should not happen.  To debug, uncomment the next line.
            //throw new IllegalStateException("OgnlRuntime initialization missing exit method", nsme);
        } catch (SecurityException se) {
            // May be blocked by existing SecurityManager.  To debug, uncomment the next line.
            //throw new SecurityException("OgnlRuntime initialization cannot access exit method", se);
        } finally {
            SYS_EXIT_REF = systemExitMethod;
        }

        try {
            systemConsoleMethod = System.class.getMethod("console", new Class<?>[]{});  // Not available in JDK 1.5 or earlier
        } catch (NoSuchMethodException nsme) {
            // May happen for JDK 1.5 and earlier.  To debug, uncomment the next line.
            //throw new IllegalStateException("OgnlRuntime initialization missing console method", nsme);
        } catch (SecurityException se) {
            // May be blocked by existing SecurityManager.  To debug, uncomment the next line.
            //throw new SecurityException("OgnlRuntime initialization cannot access console method", se);
        } finally {
            SYS_CONSOLE_REF = systemConsoleMethod;
        }
    }

    /**
     * Control usage of the OGNL Security Manager using the JVM option:
     *   -Dognl.security.manager=true  (or any non-null value other than 'disable')
     *
     * Omit '-Dognl.security.manager=' or nullify the property to disable the feature.
     *
     * To forcibly disable the feature (only possible at OGNL Library initialization, use the option:
     *   -Dognl.security.manager=forceDisableOnInit
     *
     * Users that have their own Security Manager implementations and no intention to use the OGNL SecurityManager
     *   sandbox may choose to use the 'forceDisableOnInit' flag option for performance reasons (avoiding overhead
     *   involving the system property security checks - when that feature will not be used).
     */
    static final String OGNL_SECURITY_MANAGER = "ognl.security.manager";
    static final String OGNL_SM_FORCE_DISABLE_ON_INIT = "forceDisableOnInit";

    /**
     * Hold environment flag state associated with OGNL_SECURITY_MANAGER.  See
     * {@link OgnlRuntime#OGNL_SECURITY_MANAGER} for more details.
     *   Default: false (if not set).
     */
    private static final boolean _disableOgnlSecurityManagerOnInit;
    static {
        boolean initialFlagState = false;
        try {
            final String propertyString = System.getProperty(OGNL_SECURITY_MANAGER);
            if (propertyString != null && propertyString.length() > 0) {
                initialFlagState = OGNL_SM_FORCE_DISABLE_ON_INIT.equalsIgnoreCase(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        _disableOgnlSecurityManagerOnInit = initialFlagState;
    }

    /**
     * Allow users to revert to the old "first match" lookup for getters/setters by OGNL using the JVM options:
     *    -Dognl.UseFirstMatchGetSetLookup=true
     *    -Dognl.UseFirstMatchGetSetLookup=false
     *
     * Note: Using the "false" value has the same effect as omitting the option completely.
     *   The default behaviour is to use the "best match" lookup for getters/setters.
     *   Using the "true" value reverts to the older "first match" lookup for getters/setters
     *   (in the event the "best match" processing causes issues for existing applications).
     */
    static final String USE_FIRSTMATCH_GETSET_LOOKUP = "ognl.UseFirstMatchGetSetLookup";

    /**
     * Hold environment flag state associated with USE_FIRSTMATCH_GETSET_LOOKUP.
     *   Default: false (if not set)
     */
    private static final boolean _useFirstMatchGetSetLookup;
    static {
        boolean initialFlagState = false;
        try {
            final String propertyString = System.getProperty(USE_FIRSTMATCH_GETSET_LOOKUP);
            if (propertyString != null && propertyString.length() > 0) {
                initialFlagState = Boolean.parseBoolean(propertyString);
            }
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        _useFirstMatchGetSetLookup = initialFlagState;
    }

    static final ClassCache _methodAccessors = new ClassCacheImpl();
    static final ClassCache _propertyAccessors = new ClassCacheImpl();
    static final ClassCache _elementsAccessors = new ClassCacheImpl();
    static final ClassCache _nullHandlers = new ClassCacheImpl();

    static final ClassCache _propertyDescriptorCache = new ClassCacheImpl();
    static final ClassCache _constructorCache = new ClassCacheImpl();
    static final ClassCache _staticMethodCache = new ClassCacheImpl();
    static final ClassCache _instanceMethodCache = new ClassCacheImpl();
    static final ClassCache _invokePermissionCache = new ClassCacheImpl();
    static final ClassCache _fieldCache = new ClassCacheImpl();
    static final List _superclasses = new ArrayList(); /* Used by fieldCache lookup */
    static final ClassCache[] _declaredMethods = new ClassCache[]{new ClassCacheImpl(), new ClassCacheImpl()};

    static final Map _primitiveTypes = new HashMap(101);
    static final ClassCache _primitiveDefaults = new ClassCacheImpl();
    static final Map _methodParameterTypesCache = new HashMap(101);
    static final Map _genericMethodParameterTypesCache = new HashMap(101);
    static final Map _ctorParameterTypesCache = new HashMap(101);
    static SecurityManager _securityManager = System.getSecurityManager();
    static final EvaluationPool _evaluationPool = new EvaluationPool();
    static final ObjectArrayPool _objectArrayPool = new ObjectArrayPool();

    static final Map<Method, Boolean> _methodAccessCache = new ConcurrentHashMap<Method, Boolean>();
    static final Map<Method, Boolean> _methodPermCache = new ConcurrentHashMap<Method, Boolean>();

    static final ClassPropertyMethodCache cacheSetMethod = new ClassPropertyMethodCache();
    static final ClassPropertyMethodCache cacheGetMethod = new ClassPropertyMethodCache();

    static ClassCacheInspector _cacheInspector;

    /**
     * Expression compiler used by {@link Ognl#compileExpression(OgnlContext, Object, String)} calls.
     */
    private static OgnlExpressionCompiler _compiler;

    /**
     * Lazy loading of Javassist library
     */
    static {
        try {
            Class.forName("javassist.ClassPool");
            _compiler = new ExpressionCompiler();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Javassist library is missing in classpath! Please add missed dependency!",e);
        } catch (RuntimeException rt) {
            throw new IllegalStateException("Javassist library cannot be loaded, is it restricted by runtime environment?");
        }
    }

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private static IdentityHashMap PRIMITIVE_WRAPPER_CLASSES = new IdentityHashMap();

    /**
     * Used to provide primitive type equivalent conversions into and out of
     * native / object types.
     */
    static {
        PRIMITIVE_WRAPPER_CLASSES.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Boolean.class, Boolean.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Byte.TYPE, Byte.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Byte.class, Byte.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Character.TYPE, Character.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Character.class, Character.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Short.TYPE, Short.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Short.class, Short.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Integer.TYPE, Integer.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Integer.class, Integer.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Long.TYPE, Long.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Long.class, Long.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Float.TYPE, Float.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Float.class, Float.TYPE);
        PRIMITIVE_WRAPPER_CLASSES.put(Double.TYPE, Double.class);
        PRIMITIVE_WRAPPER_CLASSES.put(Double.class, Double.TYPE);
    }

    private static final Map NUMERIC_CASTS = new HashMap();

    /**
     * Constant strings for casting different primitive types.
     */
    static {
        NUMERIC_CASTS.put(Double.class, "(double)");
        NUMERIC_CASTS.put(Float.class, "(float)");
        NUMERIC_CASTS.put(Integer.class, "(int)");
        NUMERIC_CASTS.put(Long.class, "(long)");
        NUMERIC_CASTS.put(BigDecimal.class, "(double)");
        NUMERIC_CASTS.put(BigInteger.class, "");
    }

    private static final Map NUMERIC_VALUES = new HashMap();

    /**
     * Constant strings for getting the primitive value of different
     * native types on the generic {@link Number} object interface. (or the less
     * generic BigDecimal/BigInteger types)
     */
    static {
        NUMERIC_VALUES.put(Double.class, "doubleValue()");
        NUMERIC_VALUES.put(Float.class, "floatValue()");
        NUMERIC_VALUES.put(Integer.class, "intValue()");
        NUMERIC_VALUES.put(Long.class, "longValue()");
        NUMERIC_VALUES.put(Short.class, "shortValue()");
        NUMERIC_VALUES.put(Byte.class, "byteValue()");
        NUMERIC_VALUES.put(BigDecimal.class, "doubleValue()");
        NUMERIC_VALUES.put(BigInteger.class, "doubleValue()");
        NUMERIC_VALUES.put(Boolean.class, "booleanValue()");
    }

    private static final Map NUMERIC_LITERALS = new HashMap();

    /**
     * Numeric primitive literal string expressions.
     */
    static {
        NUMERIC_LITERALS.put(Integer.class, "");
        NUMERIC_LITERALS.put(Integer.TYPE, "");
        NUMERIC_LITERALS.put(Long.class, "l");
        NUMERIC_LITERALS.put(Long.TYPE, "l");
        NUMERIC_LITERALS.put(BigInteger.class, "d");
        NUMERIC_LITERALS.put(Float.class, "f");
        NUMERIC_LITERALS.put(Float.TYPE, "f");
        NUMERIC_LITERALS.put(Double.class, "d");
        NUMERIC_LITERALS.put(Double.TYPE, "d");
        NUMERIC_LITERALS.put(BigInteger.class, "d");
        NUMERIC_LITERALS.put(BigDecimal.class, "d");
    }

    private static final Map NUMERIC_DEFAULTS = new HashMap();

    static {
        NUMERIC_DEFAULTS.put(Boolean.class, Boolean.FALSE);
        NUMERIC_DEFAULTS.put(Byte.class, new Byte((byte) 0));
        NUMERIC_DEFAULTS.put(Short.class, new Short((short) 0));
        NUMERIC_DEFAULTS.put(Character.class, new Character((char) 0));
        NUMERIC_DEFAULTS.put(Integer.class, new Integer(0));
        NUMERIC_DEFAULTS.put(Long.class, new Long(0L));
        NUMERIC_DEFAULTS.put(Float.class, new Float(0.0f));
        NUMERIC_DEFAULTS.put(Double.class, new Double(0.0));

        NUMERIC_DEFAULTS.put(BigInteger.class, new BigInteger("0"));
        NUMERIC_DEFAULTS.put(BigDecimal.class, new BigDecimal(0.0));
    }

    static {
        PropertyAccessor p = new ArrayPropertyAccessor();

        setPropertyAccessor(Object.class, new ObjectPropertyAccessor());
        setPropertyAccessor(byte[].class, p);
        setPropertyAccessor(short[].class, p);
        setPropertyAccessor(char[].class, p);
        setPropertyAccessor(int[].class, p);
        setPropertyAccessor(long[].class, p);
        setPropertyAccessor(float[].class, p);
        setPropertyAccessor(double[].class, p);
        setPropertyAccessor(Object[].class, p);
        setPropertyAccessor(List.class, new ListPropertyAccessor());
        setPropertyAccessor(Map.class, new MapPropertyAccessor());
        setPropertyAccessor(Set.class, new SetPropertyAccessor());
        setPropertyAccessor(Iterator.class, new IteratorPropertyAccessor());
        setPropertyAccessor(Enumeration.class, new EnumerationPropertyAccessor());

        ElementsAccessor e = new ArrayElementsAccessor();

        setElementsAccessor(Object.class, new ObjectElementsAccessor());
        setElementsAccessor(byte[].class, e);
        setElementsAccessor(short[].class, e);
        setElementsAccessor(char[].class, e);
        setElementsAccessor(int[].class, e);
        setElementsAccessor(long[].class, e);
        setElementsAccessor(float[].class, e);
        setElementsAccessor(double[].class, e);
        setElementsAccessor(Object[].class, e);
        setElementsAccessor(Collection.class, new CollectionElementsAccessor());
        setElementsAccessor(Map.class, new MapElementsAccessor());
        setElementsAccessor(Iterator.class, new IteratorElementsAccessor());
        setElementsAccessor(Enumeration.class, new EnumerationElementsAccessor());
        setElementsAccessor(Number.class, new NumberElementsAccessor());

        NullHandler nh = new ObjectNullHandler();

        setNullHandler(Object.class, nh);
        setNullHandler(byte[].class, nh);
        setNullHandler(short[].class, nh);
        setNullHandler(char[].class, nh);
        setNullHandler(int[].class, nh);
        setNullHandler(long[].class, nh);
        setNullHandler(float[].class, nh);
        setNullHandler(double[].class, nh);
        setNullHandler(Object[].class, nh);

        MethodAccessor ma = new ObjectMethodAccessor();

        setMethodAccessor(Object.class, ma);
        setMethodAccessor(byte[].class, ma);
        setMethodAccessor(short[].class, ma);
        setMethodAccessor(char[].class, ma);
        setMethodAccessor(int[].class, ma);
        setMethodAccessor(long[].class, ma);
        setMethodAccessor(float[].class, ma);
        setMethodAccessor(double[].class, ma);
        setMethodAccessor(Object[].class, ma);

        _primitiveTypes.put("boolean", Boolean.TYPE);
        _primitiveTypes.put("byte", Byte.TYPE);
        _primitiveTypes.put("short", Short.TYPE);
        _primitiveTypes.put("char", Character.TYPE);
        _primitiveTypes.put("int", Integer.TYPE);
        _primitiveTypes.put("long", Long.TYPE);
        _primitiveTypes.put("float", Float.TYPE);
        _primitiveTypes.put("double", Double.TYPE);

        _primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        _primitiveDefaults.put(Boolean.class, Boolean.FALSE);
        _primitiveDefaults.put(Byte.TYPE, new Byte((byte) 0));
        _primitiveDefaults.put(Byte.class, new Byte((byte) 0));
        _primitiveDefaults.put(Short.TYPE, new Short((short) 0));
        _primitiveDefaults.put(Short.class, new Short((short) 0));
        _primitiveDefaults.put(Character.TYPE, new Character((char) 0));
        _primitiveDefaults.put(Integer.TYPE, new Integer(0));
        _primitiveDefaults.put(Long.TYPE, new Long(0L));
        _primitiveDefaults.put(Float.TYPE, new Float(0.0f));
        _primitiveDefaults.put(Double.TYPE, new Double(0.0));

        _primitiveDefaults.put(BigInteger.class, new BigInteger("0"));
        _primitiveDefaults.put(BigDecimal.class, new BigDecimal(0.0));
    }

    /**
     * Clears all of the cached reflection information normally used
     * to improve the speed of expressions that operate on the same classes
     * or are executed multiple times.
     *
     * <p>
     * <strong>Warning:</strong> Calling this too often can be a huge performance
     * drain on your expressions - use with care.
     * </p>
     */
    public static void clearCache()
    {
        synchronized(_methodParameterTypesCache) {
            _methodParameterTypesCache.clear();
        }
        synchronized(_ctorParameterTypesCache) {
            _ctorParameterTypesCache.clear();
        }
        synchronized(_propertyDescriptorCache) {
            _propertyDescriptorCache.clear();
        }
        synchronized(_constructorCache) {
            _constructorCache.clear();
        }
        synchronized(_staticMethodCache) {
            _staticMethodCache.clear();
        }
        synchronized(_instanceMethodCache) {
            _instanceMethodCache.clear();
        }
        synchronized(_invokePermissionCache) {
            _invokePermissionCache.clear();
        }
        synchronized(_fieldCache) {
            _fieldCache.clear();
            _superclasses.clear();  // Used by fieldCache lookup (synchronized on _fieldCache).
        }
        synchronized(_declaredMethods[0]) {
            _declaredMethods[0].clear();
        }
        synchronized(_declaredMethods[1]) {
            _declaredMethods[1].clear();
        }
        _methodAccessCache.clear();
        _methodPermCache.clear();
    }

    /**
     * Clears some additional caches used by OgnlRuntime.  The existing {@link OgnlRuntime#clearCache()}
     * clears the standard reflection-related caches, but some applications may have need to clear
     * the additional caches as well.
     *
     * Clearing the additional caches may have greater impact than the {@link OgnlRuntime#clearCache()}
     * method so it should only be used when the normal cache clear is insufficient.
     *
     * <p>
     * <strong>Warning:</strong> Calling this method too often can be a huge performance
     * drain on your expressions - use with care.
     * </p>
     *
     * @since 3.1.25
     */
    public static void clearAdditionalCache()
    {
        cacheSetMethod.clear();
        cacheGetMethod.clear();
        synchronized(_genericMethodParameterTypesCache) {
          _genericMethodParameterTypesCache.clear();
        }
    }

    /**
     * Checks if the current jvm is java language >= 1.5 compatible.
     *
     * @return True if jdk15 features are present.
     */
    public static boolean isJdk15()
    {
        if (_jdkChecked)
            return _jdk15;

        try
        {
            Class.forName("java.lang.annotation.Annotation");
            _jdk15 = true;
        } catch (Exception e) { /* ignore */ }

        _jdkChecked = true;

        return _jdk15;
    }

    /**
     * Get the Major Java Version detected by OGNL.
     *
     * @return Detected Major Java Version, or 5 (minimum supported version for OGNL) if unable to detect.
     */
    public static int getMajorJavaVersion() {
        return _majorJavaVersion;
    }

    /**
     * Check if the detected Major Java Version is 9 or higher (JDK 9+).
     *
     * @return Return true if the Detected Major Java version is 9 or higher, otherwise false.
     */
    public static boolean isJdk9Plus() {
        return _jdk9Plus;
    }

    public static String getNumericValueGetter(Class type)
    {
        return (String) NUMERIC_VALUES.get(type);
    }

    public static Class getPrimitiveWrapperClass(Class primitiveClass)
    {
        return (Class) PRIMITIVE_WRAPPER_CLASSES.get(primitiveClass);
    }

    public static String getNumericCast(Class type)
    {
        return (String) NUMERIC_CASTS.get(type);
    }

    public static String getNumericLiteral(Class type)
    {
        return (String) NUMERIC_LITERALS.get(type);
    }

    public static void setCompiler(OgnlExpressionCompiler compiler)
    {
        _compiler = compiler;
    }

    public static OgnlExpressionCompiler getCompiler()
    {
        return _compiler;
    }

    public static void compileExpression(OgnlContext context, Node expression, Object root)
            throws Exception
    {
        _compiler.compileExpression(context, expression, root);
    }

    /**
     * Gets the "target" class of an object for looking up accessors that are registered on the
     * target. If the object is a Class object this will return the Class itself, else it will
     * return object's getClass() result.
     */
    public static Class getTargetClass(Object o)
    {
        return (o == null) ? null : ((o instanceof Class) ? (Class) o : o.getClass());
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the object
     * given.
     */
    public static String getBaseName(Object o)
    {
        return (o == null) ? null : getClassBaseName(o.getClass());
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the class given.
     */
    public static String getClassBaseName(Class c)
    {
        String s = c.getName();

        return s.substring(s.lastIndexOf('.') + 1);
    }

    public static String getClassName(Object o, boolean fullyQualified)
    {
        if (!(o instanceof Class))
        {
            o = o.getClass();
        }

        return getClassName((Class) o, fullyQualified);
    }

    public static String getClassName(Class c, boolean fullyQualified)
    {
        return fullyQualified ? c.getName() : getClassBaseName(c);
    }

    /**
     * Returns the package name of the object's class.
     */
    public static String getPackageName(Object o)
    {
        return (o == null) ? null : getClassPackageName(o.getClass());
    }

    /**
     * Returns the package name of the class given.
     */
    public static String getClassPackageName(Class c)
    {
        String s = c.getName();
        int i = s.lastIndexOf('.');

        return (i < 0) ? null : s.substring(0, i);
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x<hex digits>.
     */
    public static String getPointerString(int num)
    {
        StringBuffer result = new StringBuffer();
        String hex = Integer.toHexString(num), pad;
        Integer l = new Integer(hex.length());

        // result.append(HEX_PREFIX);
        if ((pad = (String) HEX_PADDING.get(l)) == null) {
            StringBuffer pb = new StringBuffer();

            for (int i = hex.length(); i < HEX_LENGTH; i++) {
                pb.append('0');
            }
            pad = new String(pb);
            HEX_PADDING.put(l, pad);
        }
        result.append(pad);
        result.append(hex);
        return new String(result);
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x<hex digits> for the
     * object given. This will always return a unique value for each object.
     */
    public static String getPointerString(Object o)
    {
        return getPointerString((o == null) ? 0 : System.identityHashCode(o));
    }

    /**
     * Returns a unique descriptor string that includes the object's class and a unique integer
     * identifier. If fullyQualified is true then the class name will be fully qualified to include
     * the package name, else it will be just the class' base name.
     */
    public static String getUniqueDescriptor(Object object, boolean fullyQualified)
    {
        StringBuffer result = new StringBuffer();

        if (object != null) {
            if (object instanceof Proxy) {
                Class interfaceClass = object.getClass().getInterfaces()[0];

                result.append(getClassName(interfaceClass, fullyQualified));
                result.append('^');
                object = Proxy.getInvocationHandler(object);
            }
            result.append(getClassName(object, fullyQualified));
            result.append('@');
            result.append(getPointerString(object));
        } else {
            result.append(NULL_OBJECT_STRING);
        }
        return new String(result);
    }

    /**
     * Returns a unique descriptor string that includes the object's class' base name and a unique
     * integer identifier.
     */
    public static String getUniqueDescriptor(Object object)
    {
        return getUniqueDescriptor(object, false);
    }

    /**
     * Utility to convert a List into an Object[] array. If the list is zero elements this will
     * return a constant array; toArray() on List always returns a new object and this is wasteful
     * for our purposes.
     */
    public static Object[] toArray(List list)
    {
        Object[] result;
        int size = list.size();

        if (size == 0) {
            result = NoArguments;
        } else {
            result = getObjectArrayPool().create(list.size());
            for (int i = 0; i < size; i++) {
                result[i] = list.get(i);
            }
        }
        return result;
    }

    /**
     * Returns the parameter types of the given method.
     */
    public static Class[] getParameterTypes(Method m)
    {
        synchronized (_methodParameterTypesCache)
        {
            Class[] result;

            if ((result = (Class[]) _methodParameterTypesCache.get(m)) == null)
            {
                _methodParameterTypesCache.put(m, result = m.getParameterTypes());
            }
            return result;
        }
    }

    /**
     * Finds the appropriate parameter types for the given {@link Method} and
     * {@link Class} instance of the type the method is associated with.  Correctly
     * finds generic types if running in >= 1.5 jre as well.
     *
     * @param type The class type the method is being executed against.
     * @param m The method to find types for.
     * @return Array of parameter types for the given method.
     */
    public static Class[] findParameterTypes(Class type, Method m)
    {
        Type[] genTypes = m.getGenericParameterTypes();
        Class[] types = new Class[genTypes.length];;
        boolean noGenericParameter = true;
        for (int i=0; i < genTypes.length; i++)
        {
            if (Class.class.isInstance(genTypes[i]))
            {
                types[i] = (Class) genTypes[i];
                continue;
            }
            noGenericParameter = false;
            break;
        }
        if (noGenericParameter)
        {
            return types;
        }

        if (type == null || !isJdk15())
        {
            return getParameterTypes(m);
        }

        final Type typeGenericSuperclass = type.getGenericSuperclass();
        if (typeGenericSuperclass == null
            || !ParameterizedType.class.isInstance(typeGenericSuperclass)
            || m.getDeclaringClass().getTypeParameters() == null)
        {
            return getParameterTypes(m);
        }

        if ((types = (Class[]) _genericMethodParameterTypesCache.get(m)) != null)
        {
            ParameterizedType genericSuperclass = (ParameterizedType) typeGenericSuperclass;
            if (Arrays.equals(types, genericSuperclass.getActualTypeArguments())) {
                return types;
            }
        }

        ParameterizedType param = (ParameterizedType)typeGenericSuperclass;
        TypeVariable[] declaredTypes = m.getDeclaringClass().getTypeParameters();

        types = new Class[genTypes.length];

        for (int i=0; i < genTypes.length; i++)
        {
            TypeVariable paramType = null;

            if (TypeVariable.class.isInstance(genTypes[i]))
            {
                paramType = (TypeVariable)genTypes[i];
            } else if (GenericArrayType.class.isInstance(genTypes[i]))
            {
                paramType = (TypeVariable) ((GenericArrayType)genTypes[i]).getGenericComponentType();
            }
            else if (ParameterizedType.class.isInstance(genTypes[i]))
            {
                 types[i] = (Class) ((ParameterizedType) genTypes[i]).getRawType();
                 continue;
            } else if (Class.class.isInstance(genTypes[i]))
            {
                types[i] = (Class) genTypes[i];
                continue;
            }

            Class resolved = resolveType(param, paramType, declaredTypes);

            if (resolved != null)
            {
                if (GenericArrayType.class.isInstance(genTypes[i]))
                {
                    resolved = Array.newInstance(resolved, 0).getClass();
                }

                types[i] = resolved;
                continue;
            }

            types[i] = m.getParameterTypes()[i];
        }

        synchronized (_genericMethodParameterTypesCache)
        {
            _genericMethodParameterTypesCache.put(m, types);
        }

        return types;
    }

    static Class resolveType(ParameterizedType param, TypeVariable var, TypeVariable[] declaredTypes)
    {
        if (param.getActualTypeArguments().length < 1)
            return null;

        for (int i=0; i < declaredTypes.length; i++)
        {
            if (!TypeVariable.class.isInstance( param.getActualTypeArguments()[i])
                && declaredTypes[i].getName().equals(var.getName()))
            {
                return (Class) param.getActualTypeArguments()[i];
            }
        }

        /*
        for (int i=0; i < var.getBounds().length; i++)
        {
            Type t = var.getBounds()[i];
            Class resolvedType = null;

            if (ParameterizedType.class.isInstance(t))
            {
                ParameterizedType pparam = (ParameterizedType)t;
                for (int e=0; e < pparam.getActualTypeArguments().length; e++)
                {
                    if (!TypeVariable.class.isInstance(pparam.getActualTypeArguments()[e]))
                        continue;

                    resolvedType = resolveType(pparam, (TypeVariable)pparam.getActualTypeArguments()[e], declaredTypes);
                }
            } else
            {
                resolvedType = findType(param.getActualTypeArguments(), (Class)t);
            }

            if (resolvedType != null)
                return resolvedType;
        }
        */

        return null;
    }

    static Class findType(Type[] types, Class type)
    {
        for (int i = 0; i < types.length; i++)
        {
            if (Class.class.isInstance(types[i]) && type.isAssignableFrom((Class)types[i]))
                return (Class)types[i];
        }

        return null;
    }

    /**
     * Returns the parameter types of the given method.
     */
    public static Class[] getParameterTypes(Constructor c)
    {
        Class[] result;
        if ((result = (Class[]) _ctorParameterTypesCache.get(c)) == null) {
            synchronized (_ctorParameterTypesCache) {
                if ((result = (Class[]) _ctorParameterTypesCache.get(c)) == null) {
                    _ctorParameterTypesCache.put(c, result = c.getParameterTypes());
                }
            }
        }
        return result;
    }

    /**
     * Gets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @return SecurityManager for OGNL
     */
    public static SecurityManager getSecurityManager()
    {
        return _securityManager;
    }

    /**
     * Sets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @param value SecurityManager to set
     */
    public static void setSecurityManager(SecurityManager value)
    {
        _securityManager = value;
    }

    /**
     * Permission will be named "invoke.<declaring-class>.<method-name>".
     */
    public static Permission getPermission(Method method)
    {
        Permission result;
        Class mc = method.getDeclaringClass();

        synchronized (_invokePermissionCache) {
            Map permissions = (Map) _invokePermissionCache.get(mc);

            if (permissions == null) {
                _invokePermissionCache.put(mc, permissions = new HashMap(101));
            }
            if ((result = (Permission) permissions.get(method.getName())) == null) {
                result = new OgnlInvokePermission("invoke." + mc.getName() + "." + method.getName());
                permissions.put(method.getName(), result);
            }
        }
        return result;
    }

    public static Object invokeMethod(Object target, Method method, Object[] argsArray)
            throws InvocationTargetException, IllegalAccessException
    {
        boolean syncInvoke;
        boolean checkPermission;
        Boolean methodAccessCacheValue;
        Boolean methodPermCacheValue;

        if (_useStricterInvocation) {
            final Class methodDeclaringClass = method.getDeclaringClass();  // Note: synchronized(method) call below will already NPE, so no null check.
            if ( (AO_SETACCESSIBLE_REF != null && AO_SETACCESSIBLE_REF.equals(method)) ||
                 (AO_SETACCESSIBLE_ARR_REF != null && AO_SETACCESSIBLE_ARR_REF.equals(method)) ||
                 (SYS_EXIT_REF != null && SYS_EXIT_REF.equals(method)) ||
                 (SYS_CONSOLE_REF != null && SYS_CONSOLE_REF.equals(method)) ||
                 AccessibleObjectHandler.class.isAssignableFrom(methodDeclaringClass) ||
                 ClassResolver.class.isAssignableFrom(methodDeclaringClass) ||
                 MethodAccessor.class.isAssignableFrom(methodDeclaringClass) ||
                 MemberAccess.class.isAssignableFrom(methodDeclaringClass) ||
                 OgnlContext.class.isAssignableFrom(methodDeclaringClass) ||
                 Runtime.class.isAssignableFrom(methodDeclaringClass) ||
                 ClassLoader.class.isAssignableFrom(methodDeclaringClass) ||
                 ProcessBuilder.class.isAssignableFrom(methodDeclaringClass) ||
                 AccessibleObjectHandlerJDK9Plus.unsafeOrDescendant(methodDeclaringClass) ) {
                // Prevent calls to some specific methods, as well as all methods of certain classes/interfaces
                //   for which no (apparent) legitimate use cases exist for their usage within OGNL invokeMethod().
                throw new IllegalAccessException("Method [" + method + "] cannot be called from within OGNL invokeMethod() " +
                        "under stricter invocation mode.");
            }
        }

        // only synchronize method invocation if it actually requires it

        synchronized(method) {
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

                _accessibleObjectHandler.setAccessible(method, true);
                try {
                    result = invokeMethodInsideSandbox(target, method, argsArray);
                } finally {
                    _accessibleObjectHandler.setAccessible(method, false);
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

            result = invokeMethodInsideSandbox(target, method, argsArray);
        }

        return result;
    }

    private static Object invokeMethodInsideSandbox(Object target, Method method, Object[] argsArray)
            throws InvocationTargetException, IllegalAccessException {

        if (_disableOgnlSecurityManagerOnInit) {
            return method.invoke(target, argsArray);  // Feature was disabled at OGNL initialization.
        }

        try {
            if (System.getProperty(OGNL_SECURITY_MANAGER) == null) {
                return method.invoke(target, argsArray);
            }
        } catch (SecurityException ignored) {
            // already enabled or user has applied a policy that doesn't allow read property so we have to honor user's sandbox
        }

        if (ClassLoader.class.isAssignableFrom(method.getDeclaringClass())) {
            // to support OgnlSecurityManager.isAccessDenied
            throw new IllegalAccessException("OGNL direct access to class loader denied!");
        }

        // creating object before entering sandbox to load classes out of the sandbox
        UserMethod userMethod = new UserMethod(target, method, argsArray);
        Permissions p = new Permissions(); // not any permission
        ProtectionDomain pd = new ProtectionDomain(null, p);
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[]{pd});

        Object ognlSecurityManager = OgnlSecurityManagerFactory.getOgnlSecurityManager();

        Long token;
        try {
            token = (Long) ognlSecurityManager.getClass().getMethod("enter").invoke(ognlSecurityManager);
        } catch (NoSuchMethodException e) {
            throw new InvocationTargetException(e);
        }
        if (token == null) {
            // user has applied a policy that doesn't allow setSecurityManager so we have to honor user's sandbox
            return method.invoke(target, argsArray);
        }

        // execute user method body with all permissions denied
        try {
            return AccessController.doPrivileged(userMethod, acc);
        } catch (PrivilegedActionException e) {
            if (e.getException() instanceof InvocationTargetException) {
                throw (InvocationTargetException) e.getException();
            }
            throw new InvocationTargetException(e);
        } finally {
            try {
                ognlSecurityManager.getClass().getMethod("leave", long.class).invoke(ognlSecurityManager, token);
            } catch (NoSuchMethodException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    /**
     * Gets the class for a method argument that is appropriate for looking up methods by
     * reflection, by looking for the standard primitive wrapper classes and exchanging for them
     * their underlying primitive class objects. Other classes are passed through unchanged.
     *
     * @param arg an object that is being passed to a method
     * @return the class to use to look up the method
     */
    public static final Class getArgClass(Object arg)
    {
        if (arg == null)
            return null;
        Class c = arg.getClass();
        if (c == Boolean.class)
            return Boolean.TYPE;
        else if (c.getSuperclass() == Number.class) {
            if (c == Integer.class)
                return Integer.TYPE;
            if (c == Double.class)
                return Double.TYPE;
            if (c == Byte.class)
                return Byte.TYPE;
            if (c == Long.class)
                return Long.TYPE;
            if (c == Float.class)
                return Float.TYPE;
            if (c == Short.class)
                return Short.TYPE;
        } else if (c == Character.class)
            return Character.TYPE;
        return c;
    }

    public static Class[] getArgClasses(Object[] args) {
        if (args == null)
            return null;
        Class[] argClasses = new Class[args.length];
        for (int i=0; i<args.length; i++)
            argClasses[i] = getArgClass(args[i]);
        return argClasses;
    }

    /**
     * Tells whether the given object is compatible with the given class ---that is, whether the
     * given object can be passed as an argument to a method or constructor whose parameter type is
     * the given class. If object is null this will return true because null is compatible with any
     * type.
     */
    public static final boolean isTypeCompatible(Object object, Class c) {
        if (object == null)
            return true;
        ArgsCompatbilityReport report = new ArgsCompatbilityReport(0, new boolean[1]);
        if (!isTypeCompatible(getArgClass(object), c, 0, report))
            return false;
        if (report.conversionNeeded[0])
            return false; // we don't allow conversions during this path...
        return true;
    }

    public static final boolean isTypeCompatible(Class parameterClass, Class methodArgumentClass, int index, ArgsCompatbilityReport report)
    {
        if (parameterClass == null) {
            // happens when we cannot determine parameter...
            report.score += 500;
            return true;
        }
        if (parameterClass == methodArgumentClass)
            return true;  // exact match, no additional score
        //if (methodArgumentClass.isPrimitive())
        //    return false; // really? int can be assigned to long... *hmm*
        if (methodArgumentClass.isArray()) {
            if (parameterClass.isArray()) {
                Class pct = parameterClass.getComponentType();
                Class mct = methodArgumentClass.getComponentType();
                if (mct.isAssignableFrom(pct)) {
                    // two arrays are better then a array and a list or other conversions...
                    report.score += 25;
                    return true;
                }
                //return isTypeCompatible(pct, mct, index, report); // check inner classes
            }
            if (Collection.class.isAssignableFrom(parameterClass)) {
                // we have to assume that all Collections carry objects - generics access is of no use during runtime because of
                // Type Erasure - http://www.angelikalanger.com/GenericsFAQ/FAQSections/TechnicalDetails.html#Type%20Erasure
                Class mct = methodArgumentClass.getComponentType();
                if (mct == Object.class) {
                    report.conversionNeeded[index]=true;
                    report.score += 30;
                    return true;
                } else {
                    // Okay, the items from the list *might* not match. we better don't do that...
                    return false;
                }
            }
        } else if (Collection.class.isAssignableFrom(methodArgumentClass)) {
            if (parameterClass.isArray()) {
                // TODO get generics type here and do further evaluations...
                report.conversionNeeded[index]=true;
                report.score += 50;
                return true;
            }
            if (Collection.class.isAssignableFrom(parameterClass)) {
                if (methodArgumentClass.isAssignableFrom(parameterClass)) {
                    // direct possible List assignment - good match...
                    report.score += 2;
                    return true;
                }
                // TODO get generics type here and do further evaluations...
                report.conversionNeeded[index]=true;
                report.score += 50;
                return true;
            }
        }
        if (methodArgumentClass.isAssignableFrom(parameterClass)) {
            report.score += 40;  // works but might not the best match - weight of 50..
            return true;
        }
        if (parameterClass.isPrimitive()) {
            Class ptc = (Class)PRIMITIVE_WRAPPER_CLASSES.get(parameterClass);
            if (methodArgumentClass == ptc) {
                report.score += 2;   // quite an good match
                return true;
            }
            if (methodArgumentClass.isAssignableFrom(ptc)) {
                report.score += 10;  // works but might not the best match - weight of 10..
                return true;
            }
        }
        return false;  // dosn't match.
        /*
        boolean result = true;

        if (parameterClass != null) {
            if (methodArgumentClass.isPrimitive()) {
                if (parameterClass != methodArgumentClass) {
                    result = false;
                }
            } else if (!methodArgumentClass.isAssignableFrom(parameterClass)) {
                result = false;
            }
        }
        return result;
        */
    }

    /**
     * Tells whether the given array of objects is compatible with the given array of classes---that
     * is, whether the given array of objects can be passed as arguments to a method or constructor
     * whose parameter types are the given array of classes.
     */
    public static class ArgsCompatbilityReport {
        int score;
        boolean[] conversionNeeded;
        public ArgsCompatbilityReport(int score, boolean[] conversionNeeded) {
            this.score = score;
            this.conversionNeeded = conversionNeeded;
        }
    }

    public static final ArgsCompatbilityReport	NoArgsReport = new ArgsCompatbilityReport(0, new boolean[0]);

    public static boolean areArgsCompatible(Object[] args, Class[] classes)
    {
        ArgsCompatbilityReport report = areArgsCompatible(getArgClasses(args), classes, null);
        if (report == null)
            return false;
        for (boolean conversionNeeded : report.conversionNeeded)
            if (conversionNeeded)
                return false;
        return true;
    }

    public static ArgsCompatbilityReport areArgsCompatible(Class[] args, Class[] classes, Method m)
    {
        boolean varArgs = m != null && isJdk15() && m.isVarArgs();

        if ( args==null || args.length == 0 ) {	// handle methods without arguments
            if ( classes == null || classes.length == 0 )
                return NoArgsReport;
            else
                return null;
        }
        if (args.length != classes.length && !varArgs) {
            return null;
        } else if (varArgs) {
            /*
             *  varArg's start with a penalty of 1000.
             *  There are some java compiler rules that are hopefully reflectet by this penalty:
             *  * Legacy beats Varargs
             *  * Widening beats Varargs
             *  * Boxing beats Varargs
             */
            ArgsCompatbilityReport report = new ArgsCompatbilityReport(1000, new boolean[args.length]);
            /*
             *  varargs signature is: method(type1, type2, typeN, typeV ...)
             *  This means: All arguments up to typeN needs exact matching, all varargs need to match typeV
             */
            if (classes.length - 1 > args.length)
                // we don't have enough arguments to provide the required 'fixed' arguments
                return null;

            // type check on fixed arguments
            for (int index = 0, count = classes.length - 1; index < count; ++index)
                if (!isTypeCompatible(args[index], classes[index], index, report))
                    return null;

            // type check on varargs
            Class varArgsType = classes[classes.length - 1].getComponentType();
            for (int index = classes.length - 1, count = args.length; index < count; ++index)
                if (!isTypeCompatible(args[index], varArgsType, index, report))
                    return null;

            return report;
        } else {
            ArgsCompatbilityReport report = new ArgsCompatbilityReport(0, new boolean[args.length]);
            for (int index = 0, count = args.length; index < count; ++index)
                if (!isTypeCompatible(args[index], classes[index], index, report))
                    return null;
            return report;
        }
    }

    /**
     * Tells whether the first array of classes is more specific than the second. Assumes that the
     * two arrays are of the same length.
     */
    public static final boolean isMoreSpecific(Class[] classes1, Class[] classes2)
    {
        for (int index = 0, count = classes1.length; index < count; ++index) {
            Class c1 = classes1[index], c2 = classes2[index];
            if (c1 == c2)
                continue;
            else if (c1.isPrimitive())
                return true;
            else if (c1.isAssignableFrom(c2))
                return false;
            else if (c2.isAssignableFrom(c1))
                return true;
        }

        // They are the same! So the first is not more specific than the second.
        return false;
    }

    public static String getModifierString(int modifiers)
    {
        String result;

        if (Modifier.isPublic(modifiers))
            result = "public";
        else if (Modifier.isProtected(modifiers))
            result = "protected";
        else if (Modifier.isPrivate(modifiers))
            result = "private";
        else
            result = "";
        if (Modifier.isStatic(modifiers))
            result = "static " + result;
        if (Modifier.isFinal(modifiers))
            result = "final " + result;
        if (Modifier.isNative(modifiers))
            result = "native " + result;
        if (Modifier.isSynchronized(modifiers))
            result = "synchronized " + result;
        if (Modifier.isTransient(modifiers))
            result = "transient " + result;
        return result;
    }

    public static Class classForName(OgnlContext context, String className)
            throws ClassNotFoundException
    {
        Class result = (Class) _primitiveTypes.get(className);

        if (result == null) {
            ClassResolver resolver;

            if ((context == null) || ((resolver = context.getClassResolver()) == null)) {
                resolver = OgnlContext.DEFAULT_CLASS_RESOLVER;
            }
            result = resolver.classForName(className, context);
        }

        if (result == null)
            throw new ClassNotFoundException("Unable to resolve class: " + className);

        return result;
    }

    public static boolean isInstance(OgnlContext context, Object value, String className)
            throws OgnlException
    {
        try {
            Class c = classForName(context, className);
            return c.isInstance(value);
        } catch (ClassNotFoundException e) {
            throw new OgnlException("No such class: " + className, e);
        }
    }

    public static Object getPrimitiveDefaultValue(Class forClass)
    {
        return _primitiveDefaults.get(forClass);
    }

    public static Object getNumericDefaultValue(Class forClass)
    {
        return NUMERIC_DEFAULTS.get(forClass);
    }

    public static Object getConvertedType(OgnlContext context, Object target, Member member, String propertyName,
                                          Object value, Class type)
    {
        return context.getTypeConverter().convertValue(context, target, member, propertyName, value, type);
    }

    public static boolean getConvertedTypes(OgnlContext context, Object target, Member member, String propertyName,
                                            Class[] parameterTypes, Object[] args, Object[] newArgs)
    {
        boolean result = false;

        if (parameterTypes.length == args.length) {
            result = true;
            for (int i = 0, ilast = parameterTypes.length - 1; result && (i <= ilast); i++) {
                Object arg = args[i];
                Class type = parameterTypes[i];

                if (isTypeCompatible(arg, type)) {
                    newArgs[i] = arg;
                } else {
                    Object v = getConvertedType(context, target, member, propertyName, arg, type);

                    if (v == OgnlRuntime.NoConversionPossible) {
                        result = false;
                    } else {
                        newArgs[i] = v;
                    }
                }
            }
        }
        return result;
    }

    public static Constructor getConvertedConstructorAndArgs(OgnlContext context, Object target, List constructors,
                                                             Object[] args, Object[] newArgs)
    {
        Constructor result = null;
        TypeConverter converter = context.getTypeConverter();

        if ((converter != null) && (constructors != null)) {
            for (int i = 0, icount = constructors.size(); (result == null) && (i < icount); i++) {
                Constructor ctor = (Constructor) constructors.get(i);
                Class[] parameterTypes = getParameterTypes(ctor);

                if (getConvertedTypes(context, target, ctor, null, parameterTypes, args, newArgs)) {
                    result = ctor;
                }
            }
        }
        return result;
    }

    /**
     * Gets the appropriate method to be called for the given target, method name and arguments. If
     * successful this method will return the Method within the target that can be called and the
     * converted arguments in actualArgs. If unsuccessful this method will return null and the
     * actualArgs will be empty.
     *
     * @param context The current execution context.
     * @param source Target object to run against or method name.
     * @param target Instance of object to be run against.
     * @param propertyName Name of property to get method of.
     * @param methods List of current known methods.
     * @param args Arguments originally passed in.
     * @param actualArgs Converted arguments.
     *
     * @return Best method match or null if none could be found.
     */
    public static Method getAppropriateMethod(OgnlContext context, Object source, Object target, String propertyName,
                                              String methodName, List methods, Object[] args, Object[] actualArgs)
    {
        Method result = null;

        if (methods != null)
        {
            Class typeClass = target != null ? target.getClass() : null;
            if (typeClass == null && source != null && Class.class.isInstance(source))
            {
                typeClass = (Class)source;
            }
            Class[] argClasses = getArgClasses(args);

            MatchingMethod mm = findBestMethod(methods, typeClass, methodName, argClasses);
            if (mm != null) {
                result = mm.mMethod;
                Class[] mParameterTypes = mm.mParameterTypes;
                System.arraycopy(args, 0, actualArgs, 0, args.length);

                for (int j = 0; j < mParameterTypes.length; j++)
                {
                    Class type = mParameterTypes[j];

                    if (mm.report.conversionNeeded[j] || (type.isPrimitive() && (actualArgs[j] == null)))
                    {
                        actualArgs[j] = getConvertedType(context, source, result, propertyName, args[j], type);
                    }
                }
            }
        }

        if (result == null)
        {
            result = getConvertedMethodAndArgs(context, target, propertyName, methods, args, actualArgs);
        }

        return result;
    }

    public static Method getConvertedMethodAndArgs(OgnlContext context, Object target, String propertyName,
            List methods, Object[] args, Object[] newArgs) {
        Method result = null;
        TypeConverter converter = context.getTypeConverter();

        if ((converter != null) && (methods != null)) {
            for (int i = 0, icount = methods.size(); (result == null) && (i < icount); i++) {
                Method m = (Method) methods.get(i);
                Class[] parameterTypes = findParameterTypes(target != null ? target.getClass() : null, m);//getParameterTypes(m);

                if (getConvertedTypes(context, target, m, propertyName, parameterTypes, args, newArgs)) {
                    result = m;
                }
            }
        }
        return result;
    }

    private static class MatchingMethod {
        Method                  mMethod;
        int                     score;
        ArgsCompatbilityReport  report;
        Class[]                 mParameterTypes;

        private MatchingMethod(Method method, int score, ArgsCompatbilityReport report, Class[] mParameterTypes) {
            this.mMethod = method;
            this.score = score;
            this.report = report;
            this.mParameterTypes = mParameterTypes;
        }
    }

    private static MatchingMethod findBestMethod(List methods, Class typeClass, String name, Class[] argClasses) {
        MatchingMethod mm = null;
        IllegalArgumentException failure = null;
        for (int i = 0, icount = methods.size(); i < icount; i++) {
            Method m = (Method) methods.get(i);

            Class[] mParameterTypes = findParameterTypes(typeClass, m);
            ArgsCompatbilityReport report = areArgsCompatible(argClasses, mParameterTypes, m);
            if (report == null)
                continue;

            String methodName = m.getName();
            int score = report.score;
            if (name.equals(methodName)) {
                // exact match - no additinal score...
            } else if (name.equalsIgnoreCase(methodName)) {
                // minimal penalty..
                score += 200;
            } else if (methodName.toLowerCase().endsWith(name.toLowerCase())) {
                // has a prefix...
                score += 500;
            } else {
                // just in case...
                score += 5000;
            }
            if (mm == null || mm.score > score) {
                mm = new MatchingMethod(m, score, report, mParameterTypes);
                failure = null;
            } else if (mm.score == score) {
                // it happens that we see the same method signature multiple times - for the current class or interfaces ...
                // TODO why are all of them on the list and not only the most specific one?
                // check for same signature
                if (Arrays.equals(mm.mMethod.getParameterTypes(), m.getParameterTypes()) && mm.mMethod.getName().equals(m.getName())) {
                    boolean retsAreEqual = mm.mMethod.getReturnType().equals(m.getReturnType());
                    // it is the same method. we use the most specific one...
                    if (mm.mMethod.getDeclaringClass().isAssignableFrom(m.getDeclaringClass())) {
                        if (!retsAreEqual && !mm.mMethod.getReturnType().isAssignableFrom(m.getReturnType()))
                            System.err.println("Two methods with same method signature but return types conflict? \""+mm.mMethod+"\" and \""+m+"\" please report!");

                        mm = new MatchingMethod(m, score, report, mParameterTypes);
                        failure = null;
                    } else if (!m.getDeclaringClass().isAssignableFrom(mm.mMethod.getDeclaringClass())) {
                        // this should't happen
                        System.err.println("Two methods with same method signature but not providing classes assignable? \""+mm.mMethod+"\" and \""+m+"\" please report!");
                    } else if (!retsAreEqual && !m.getReturnType().isAssignableFrom(mm.mMethod.getReturnType()))
                        System.err.println("Two methods with same method signature but return types conflict? \""+mm.mMethod+"\" and \""+m+"\" please report!");
                } else {
                    // two methods with same score - direct compare to find the better one...
                    // legacy wins over varargs
                    if (isJdk15() && (m.isVarArgs() || mm.mMethod.isVarArgs())) {
                        if (m.isVarArgs() && !mm.mMethod.isVarArgs()) {
                            // keep with current
                        } else if (!m.isVarArgs() && mm.mMethod.isVarArgs()) {
                            // legacy wins...
                            mm = new MatchingMethod(m, score, report, mParameterTypes);
                            failure = null;
                        } else {
                            // both arguments are varargs...
                            System.err.println("Two vararg methods with same score("+score+"): \""+mm.mMethod+"\" and \""+m+"\" please report!");
                        }
                    } else {
                        int scoreCurr = 0;
                        int scoreOther = 0;
                        for (int j=0; j<argClasses.length; j++) {
                            Class argClass = argClasses[j];
                            Class mcClass = mm.mParameterTypes[j];
                            Class moClass = mParameterTypes[j];
                            if (argClass == null) {	// TODO can we avoid this case?
                                // we don't know the class. use the most generic implementation...
                                if (mcClass == moClass) {
                                    // equal args - no winner...
                                } else if (mcClass.isAssignableFrom(moClass)) {
                                    scoreOther += 1000;	// current wins...
                                } else if (moClass.isAssignableFrom(moClass)) {
                                    scoreCurr += 1000;	// other wins...
                                } else {
                                    // both items can't be assigned to each other..
                                    failure = new IllegalArgumentException("Can't decide wich method to use: \""+mm.mMethod+"\" or \""+m+"\"");
                                }
                            } else {
                                // we try to find the more specific implementation
                                if (mcClass == moClass) {
                                    // equal args - no winner...
                                } else if (mcClass == argClass) {
                                    scoreOther += 100;	// current wins...
                                } else if (moClass == argClass) {
                                    scoreCurr += 100;	// other wins...
                                } else {
                                    // both items can't be assigned to each other..
                                    // TODO: if this happens we have to put some weight on the inheritance...
                                    failure = new IllegalArgumentException("Can't decide wich method to use: \""+mm.mMethod+"\" or \""+m+"\"");
                                }
                            }
                        }
                        if (scoreCurr == scoreOther) {
                            if (failure == null) {
                                boolean currentIsAbstract = Modifier.isAbstract(mm.mMethod.getModifiers());
                                boolean otherIsAbstract = Modifier.isAbstract(m.getModifiers());
                                if (! (currentIsAbstract ^ otherIsAbstract) ) {
                                    // Only report as an error when the score is equal and BOTH methods are abstract or BOTH are concrete.
                                    // If one is abstract and the other concrete then either choice should work for OGNL,
                                    // so we just keep the current choice and continue (without error output).
                                    System.err.println("Two methods with same score("+score+"): \""+mm.mMethod+"\" and \""+m+"\" please report!");
                                }
                            }
                        } else if (scoreCurr > scoreOther) {
                            // other wins...
                            mm = new MatchingMethod(m, score, report, mParameterTypes);
                            failure = null;
                        } // else current one wins...
                    }
                }
            }
        }
        if (failure != null)
            throw failure;
        return mm;
    }

    public static Object callAppropriateMethod(OgnlContext context, Object source, Object target, String methodName,
                                               String propertyName, List methods, Object[] args)
            throws MethodFailedException
    {
        Throwable reason = null;
        Object[] actualArgs = _objectArrayPool.create(args.length);

        try {
            Method method = getAppropriateMethod(context, source, target, propertyName, methodName, methods, args, actualArgs);

            if ((method == null) || !isMethodAccessible(context, source, method, propertyName))
            {
                StringBuffer buffer = new StringBuffer();
                String className = "";

                if (target != null)
                {
                    className = target.getClass().getName() + ".";
                }

                for (int i = 0, ilast = args.length - 1; i <= ilast; i++)
                {
                    Object arg = args[i];

                    buffer.append((arg == null) ? NULL_STRING : arg.getClass().getName());
                    if (i < ilast)
                    {
                        buffer.append(", ");
                    }
                }

                throw new NoSuchMethodException(className + methodName + "(" + buffer + ")");
            }

            Object[] convertedArgs = actualArgs;

            if (isJdk15() && method.isVarArgs())
            {
                Class[] parmTypes = method.getParameterTypes();

                // split arguments in to two dimensional array for varargs reflection invocation
                // where it is expected that the parameter passed in to invoke the method
                // will look like "new Object[] { arrayOfNonVarArgsArguments, arrayOfVarArgsArguments }"

                for (int i=0; i < parmTypes.length; i++)
                {
                    if (parmTypes[i].isArray())
                    {
                        convertedArgs = new Object[i + 1];
                        System.arraycopy(actualArgs, 0, convertedArgs, 0, convertedArgs.length);

                        Object[] varArgs;

                        // if they passed in varargs arguments grab them and dump in to new varargs array

                        if (actualArgs.length > i)
                        {
                            ArrayList varArgsList = new ArrayList();
                            for (int j=i; j < actualArgs.length; j++)
                            {
                                if (actualArgs[j] != null)
                                {
                                    varArgsList.add(actualArgs[j]);
                                }
                            }

                            varArgs = varArgsList.toArray();
                        } else
                        {
                            varArgs = new Object[0];
                        }

                        convertedArgs[i] = varArgs;
                        break;
                    }
                }
            }

            return invokeMethod(target, method, convertedArgs);

        } catch (NoSuchMethodException e) {
            reason = e;
        } catch (IllegalAccessException e) {
            reason = e;
        } catch (InvocationTargetException e) {
            reason = e.getTargetException();
        } finally {
            _objectArrayPool.recycle(actualArgs);
        }

        throw new MethodFailedException(source, methodName, reason);
    }

    public static Object callStaticMethod(OgnlContext context, String className, String methodName, Object[] args)
            throws OgnlException
    {
        try {
            Class targetClass = classForName(context, className);
            if (targetClass == null)
                throw new ClassNotFoundException("Unable to resolve class with name " + className);

            MethodAccessor ma = getMethodAccessor(targetClass);

            return ma.callStaticMethod(context, targetClass, methodName, args);
        } catch (ClassNotFoundException ex) {
            throw new MethodFailedException(className, methodName, ex);
        }
    }

    /**
     * Invokes the specified method against the target object.
     *
     * @param context
     *          The current execution context.
     * @param target
     *          The object to invoke the method on.
     * @param methodName
     *          Name of the method - as in "getValue" or "add", etc..
     * @param propertyName
     *          Name of the property to call instead?
     * @param args
     *          Optional arguments needed for method.
     * @return Result of invoking method.
     *
     * @deprecated Use {@link #callMethod(OgnlContext, Object, String, Object[])} instead.
     * @throws OgnlException For lots of different reasons.
     */
    public static Object callMethod(OgnlContext context, Object target, String methodName, String propertyName, Object[] args)
            throws OgnlException
    {
        return callMethod(context, target, methodName == null ? propertyName : methodName, args);
    }

    /**
     * Invokes the specified method against the target object.
     *
     * @param context
     *          The current execution context.
     * @param target
     *          The object to invoke the method on.
     * @param methodName
     *          Name of the method - as in "getValue" or "add", etc..
     * @param args
     *          Optional arguments needed for method.
     * @return Result of invoking method.
     *
     * @throws OgnlException For lots of different reasons.
     */
    public static Object callMethod(OgnlContext context, Object target, String methodName, Object[] args)
            throws OgnlException
    {
        if (target == null)
            throw new NullPointerException("target is null for method " + methodName);

        return getMethodAccessor(target.getClass()).callMethod(context, target, methodName, args);
    }

    public static Object callConstructor(OgnlContext context, String className, Object[] args)
            throws OgnlException
    {
        Throwable reason = null;
        Object[] actualArgs = args;

        try {
            Constructor ctor = null;
            Class[] ctorParameterTypes = null;
            Class target = classForName(context, className);
            List constructors = getConstructors(target);

            for (int i = 0, icount = constructors.size(); i < icount; i++) {
                Constructor c = (Constructor) constructors.get(i);
                Class[] cParameterTypes = getParameterTypes(c);

                if (areArgsCompatible(args, cParameterTypes)
                    && (ctor == null || isMoreSpecific(cParameterTypes, ctorParameterTypes))) {
                    ctor = c;
                    ctorParameterTypes = cParameterTypes;
                }
            }
            if (ctor == null) {
                actualArgs = _objectArrayPool.create(args.length);
                if ((ctor = getConvertedConstructorAndArgs(context, target, constructors, args, actualArgs)) == null) {
                    throw new NoSuchMethodException();
                }
            }
            if (!context.getMemberAccess().isAccessible(context, target, ctor, null)) {
                throw new IllegalAccessException(
                        "access denied to " + target.getName() + "()");
            }
            return ctor.newInstance(actualArgs);
        } catch (ClassNotFoundException e) {
            reason = e;
        } catch (NoSuchMethodException e) {
            reason = e;
        } catch (IllegalAccessException e) {
            reason = e;
        } catch (InvocationTargetException e) {
            reason = e.getTargetException();
        } catch (InstantiationException e) {
            reason = e;
        } finally {
            if (actualArgs != args) {
                _objectArrayPool.recycle(actualArgs);
            }
        }

        throw new MethodFailedException(className, "new", reason);
    }

    /**
     * Don't use this method as it doesn't check member access rights via {@link MemberAccess} interface
     */
    @Deprecated
    public static final Object getMethodValue(OgnlContext context, Object target, String propertyName)
            throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        return getMethodValue(context, target, propertyName, false);
    }

    /**
     * If the checkAccessAndExistence flag is true this method will check to see if the method
     * exists and if it is accessible according to the context's MemberAccess. If neither test
     * passes this will return NotFound.
     */
    public static final Object getMethodValue(OgnlContext context, Object target, String propertyName,
                                              boolean checkAccessAndExistence)
            throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        Object result = null;
        Method m = getGetMethod(context, (target == null) ? null : target.getClass() , propertyName);
        if (m == null)
            m = getReadMethod((target == null) ? null : target.getClass(), propertyName, null);

        if (checkAccessAndExistence)
        {
            if ((m == null) || !context.getMemberAccess().isAccessible(context, target, m, propertyName))
            {
                result = NotFound;
            }
        }
        if (result == null) {
            if (m != null) {
                try {
                    result = invokeMethod(target, m, NoArguments);
                } catch (InvocationTargetException ex) {
                    throw new OgnlException(propertyName, ex.getTargetException());
                }
            } else {
                throw new NoSuchMethodException(propertyName);
            }
        }
        return result;
    }

    /**
     * Don't use this method as it doesn't check member access rights via {@link MemberAccess} interface
     */
    @Deprecated
    public static boolean setMethodValue(OgnlContext context, Object target, String propertyName, Object value)
            throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        return setMethodValue(context, target, propertyName, value, false);
    }

    public static boolean setMethodValue(OgnlContext context, Object target, String propertyName, Object value,
                                         boolean checkAccessAndExistence)
            throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        boolean result = true;
        Method m = getSetMethod(context, (target == null) ? null : target.getClass(), propertyName);

        if (checkAccessAndExistence)
        {
            if ((m == null) || !context.getMemberAccess().isAccessible(context, target, m, propertyName))
            {
                result = false;
            }
        }

        if (result)
        {
            if (m != null)
            {
                Object[] args = _objectArrayPool.create(value);

                try
                {
                    callAppropriateMethod(context, target, target, m.getName(), propertyName,
                                          Collections.nCopies(1, m), args);
                } finally
                {
                    _objectArrayPool.recycle(args);
                }
            } else
            {
                result = false;
            }
        }

        return result;
    }

    public static List getConstructors(Class targetClass)
    {
        List result;
        if ((result = (List) _constructorCache.get(targetClass)) == null) {
            synchronized (_constructorCache) {
                if ((result = (List) _constructorCache.get(targetClass)) == null) {
                    _constructorCache.put(targetClass, result = Arrays.asList(targetClass.getConstructors()));
                }
            }
        }
        return result;
    }

    public static Map getMethods(Class targetClass, boolean staticMethods)
    {
        ClassCache cache = (staticMethods ? _staticMethodCache : _instanceMethodCache);
        Map result;

        if ((result = (Map) cache.get(targetClass)) == null) {
            synchronized (cache)
            {
                if ((result = (Map) cache.get(targetClass)) == null)
                {
                    result = new HashMap(23);
                    collectMethods(targetClass, result, staticMethods);
                    cache.put(targetClass, result);
                }
            }
        }
        return result;
    }

    private static void collectMethods(Class c, Map result, boolean staticMethods) {
        Method[] ma;
        try {
            ma = c.getDeclaredMethods();
        } catch (SecurityException ignored) {
            ma = c.getMethods();
        }
        for (int i = 0, icount = ma.length; i < icount; i++)
        {
            if (c.isInterface())
            {
                if (isDefaultMethod(ma[i]))
                    addMethodToResult(result, ma[i]);
                continue;
            }

            // skip over synthetic methods
            if (!isMethodCallable(ma[i]))
                continue;

            if (Modifier.isStatic(ma[i].getModifiers()) == staticMethods)
                addMethodToResult(result, ma[i]);
        }

        Class superclass = c.getSuperclass();
        if (superclass != null)
            collectMethods(superclass, result, staticMethods);

        for (Class iface : c.getInterfaces())
            collectMethods(iface, result, staticMethods);
    }

    private static void addMethodToResult(Map result, Method method)
    {
        List ml = (List) result.get(method.getName());
        if (ml == null)
            result.put(method.getName(), ml = new ArrayList());
        ml.add(method);
    }

    /**
     * Backport of java.lang.reflect.Method#isDefault()
     *
     * JDK8+ supports Default Methods for interfaces.  Default Methods are defined as:
     *   public, non-abstract and declared within an interface (must also be non-static).
     *
     * @param method The Method to check against the requirements for a Default Method.
     *
     * @return true If the Method qualifies as a Default Method, false otherwise.
     */
    private static boolean isDefaultMethod(Method method)
    {
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }

    /**
     * Determine if the provided Method is a non-Default public Interface method.
     *
     * Public non-Default Methods are defined as:
     *   public, abstract, non-static and declared within an interface.
     *
     * @param method The Method to check against the requirements for a non-Default Method.
     *
     * @return true If method qualifies as a non-Default public Interface method, false otherwise.
     *
     * @since 3.1.25
     */
    private static boolean isNonDefaultPublicInterfaceMethod(Method method) {
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == (Modifier.ABSTRACT | Modifier.PUBLIC))
                && method.getDeclaringClass().isInterface();
    }

    public static Map getAllMethods(Class targetClass, boolean staticMethods)
    {
        ClassCache cache = (staticMethods ? _staticMethodCache : _instanceMethodCache);
        Map result;

        if ((result = (Map) cache.get(targetClass)) == null) {
            synchronized (cache)
            {
                if ((result = (Map) cache.get(targetClass)) == null)
                {
                    result = new HashMap(23);

                    for (Class c = targetClass; c != null; c = c.getSuperclass())
                    {
                        Method[] ma = c.getMethods();

                        for (int i = 0, icount = ma.length; i < icount; i++)
                        {
                            // skip over synthetic methods

                            if (!isMethodCallable(ma[i]))
                                continue;

                            if (Modifier.isStatic(ma[i].getModifiers()) == staticMethods)
                            {
                                List ml = (List) result.get(ma[i].getName());

                                if (ml == null)
                                    result.put(ma[i].getName(), ml = new ArrayList());

                                ml.add(ma[i]);
                            }
                        }
                    }
                    cache.put(targetClass, result);
                }
            }
        }
        return result;
    }

    public static List getMethods(Class targetClass, String name, boolean staticMethods)
    {
        return (List) getMethods(targetClass, staticMethods).get(name);
    }

    public static List getAllMethods(Class targetClass, String name, boolean staticMethods)
    {
        return (List) getAllMethods(targetClass, staticMethods).get(name);
    }

    public static Map getFields(Class targetClass)
    {
        Map result;

        if ((result = (Map) _fieldCache.get(targetClass)) == null) {
            synchronized (_fieldCache) {
                if ((result = (Map) _fieldCache.get(targetClass)) == null) {
                    Field fa[];

                    result = new HashMap(23);
                    try {
                        fa = targetClass.getDeclaredFields();
                    } catch (SecurityException ignored) {
                        fa = targetClass.getFields();
                    }
                    for (int i = 0; i < fa.length; i++) {
                        result.put(fa[i].getName(), fa[i]);
                    }
                    _fieldCache.put(targetClass, result);
                }
            }
        }
        return result;
    }

    public static Field getField(Class inClass, String name)
    {
        Field result = null;

        Object o = getFields(inClass).get(name);
        if(o == null) {
            synchronized (_fieldCache) {
                o = getFields(inClass).get(name);

                if (o == null) {
                    _superclasses.clear();
                    for (Class sc = inClass; (sc != null); sc = sc.getSuperclass()) {
                        if ((o = getFields(sc).get(name)) == NotFound)
                            break;

                        _superclasses.add(sc);

                        if ((result = (Field) o) != null)
                            break;
                    }
                    /*
                     * Bubble the found value (either cache miss or actual field) to all supeclasses
                     * that we saw for quicker access next time.
                     */
                    for (int i = 0, icount = _superclasses.size(); i < icount; i++) {
                        getFields((Class) _superclasses.get(i)).put(name, (result == null) ? NotFound : result);
                    }
                } else {
                    if (o instanceof Field) {
                        result = (Field) o;
                    } else {
                        if (result == NotFound)
                            result = null;
                    }
                }
            }
        }
        else {
            if (o instanceof Field) {
                result = (Field) o;
            } else {
                if (result == NotFound)
                    result = null;
            }
        }
        return result;
    }

    /**
     * Don't use this method as it doesn't check member access rights via {@link MemberAccess} interface
     */
    @Deprecated
    public static Object getFieldValue(OgnlContext context, Object target, String propertyName)
            throws NoSuchFieldException
    {
        return getFieldValue(context, target, propertyName, false);
    }

    public static Object getFieldValue(OgnlContext context, Object target, String propertyName,
                                       boolean checkAccessAndExistence)
            throws NoSuchFieldException
    {
        Object result = null;
        final Field f = getField((target == null) ? null : target.getClass(), propertyName);

        if (checkAccessAndExistence) {
            if ((f == null) || !context.getMemberAccess().isAccessible(context, target, f, propertyName)) {
                result = NotFound;
            }
        }
        if (result == null) {
            if (f == null) {
                throw new NoSuchFieldException(propertyName);
            } else {
                try {

                    if (!Modifier.isStatic(f.getModifiers())) {
                        final Object state = context.getMemberAccess().setup(context, target, f, propertyName);
                        try {
                            result = f.get(target);
                        } finally {
                            context.getMemberAccess().restore(context, target, f, propertyName, state);
                        }
                    } else {
                        throw new NoSuchFieldException(propertyName);
                    }

                } catch (IllegalAccessException ex) {
                    throw new NoSuchFieldException(propertyName);
                }
            }
        }
        return result;
    }

    public static boolean setFieldValue(OgnlContext context, Object target, String propertyName, Object value)
            throws OgnlException
    {
        boolean result = false;

        try {
            final Field f = getField((target == null) ? null : target.getClass(), propertyName);

            if (f != null) {
                final int fModifiers = f.getModifiers();
                if (!Modifier.isStatic(fModifiers) && !Modifier.isFinal(fModifiers)) {
                    final Object state = context.getMemberAccess().setup(context, target, f, propertyName);
                    try {
                        if (isTypeCompatible(value, f.getType())
                            || ((value = getConvertedType(context, target, f, propertyName, value, f.getType())) != null)) {
                            f.set(target, value);
                            result = true;
                        }
                    } finally {
                        context.getMemberAccess().restore(context, target, f, propertyName, state);
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw new NoSuchPropertyException(target, propertyName, ex);
        }
        return result;
    }

    public static boolean isFieldAccessible(OgnlContext context, Object target, Class inClass, String propertyName)
    {
        return isFieldAccessible(context, target, getField(inClass, propertyName), propertyName);
    }

    public static boolean isFieldAccessible(OgnlContext context, Object target, Field field, String propertyName)
    {
        return context.getMemberAccess().isAccessible(context, target, field, propertyName);
    }

    public static boolean hasField(OgnlContext context, Object target, Class inClass, String propertyName)
    {
        Field f = getField(inClass, propertyName);

        return (f != null) && isFieldAccessible(context, target, f, propertyName);
    }

    /**
     * Method name is getStaticField(), but actually behaves more like "getStaticFieldValue()".
     * <p/>
     * Typical usage: Returns the value (not the actual {@link Field}) for the given (static) fieldName.
     * May return the {@link Enum} constant value for the given fieldName when className is an {@link Enum}.
     * May return a {@link Class} instance when the given fieldName is "class".
     * <p/>
     * @param context    The current ognl context
     * @param className  The name of the class which contains the field
     * @param fieldName  The name of the field whose value should be returned
     * 
     * @return           The value of the (static) fieldName
     * @throws OgnlException
     */
    public static Object getStaticField(OgnlContext context, String className, String fieldName)
            throws OgnlException
    {
        Exception reason = null;
        try {
            final Class c = classForName(context, className);

            if (c == null) {
                throw new OgnlException("Unable to find class " + className + " when resolving field name of " + fieldName);
            }

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static
             * fields because it is a reserved word.
             */
            if (fieldName.equals("class"))
            {
                return c;
            } else if (OgnlRuntime.isJdk15() && c.isEnum())
            {
                try {
                    return Enum.valueOf(c, fieldName);
                } catch (IllegalArgumentException e) {
                    // ignore it, try static field
                }
            }

            Field f;
            try {
                f = c.getField(fieldName);  // Public fields checked first (direct)
            } catch (NoSuchFieldException nsfe) {
                f = OgnlRuntime.getField(c, fieldName);  // Non-public fields checked (access controlled)
                if (f == null) {
                    throw new NoSuchFieldException(fieldName);
                }
            }
            final int fModifiers = f.getModifiers();
            if (!Modifier.isStatic(fModifiers)) {
                throw new OgnlException("Field " + fieldName + " of class " + className + " is not static");
            }
            final Object result;

            if (Modifier.isPublic(fModifiers)) {
                result = f.get(null);  // Valid static public field (no access check required)
            } else {
                if (context.getMemberAccess().isAccessible(context, null, f, null)) {
                    final Object state = context.getMemberAccess().setup(context, null, f, null);
                    try {
                        result = f.get(null);
                    } finally {
                        context.getMemberAccess().restore(context, null, f, null, state);
                    }
                } else {
                    throw new IllegalAccessException("Access to " + fieldName  + " of class " + className + " is forbidden");
                }
            }

            return result;
        } catch (ClassNotFoundException e) {
            reason = e;
        } catch (NoSuchFieldException e) {
            reason = e;
        } catch (SecurityException e) {
            reason = e;
        } catch (IllegalAccessException e) {
            reason = e;
        }

        throw new OgnlException("Could not get static field " + fieldName + " from class " + className, reason);
    }

    private static String capitalizeBeanPropertyName(String propertyName) {
        if (propertyName.length() == 1) {
            return propertyName.toUpperCase();
        }
        // don't capitalize getters/setters
        if (propertyName.startsWith(GET_PREFIX) && propertyName.endsWith("()")) {
            if (Character.isUpperCase(propertyName.substring(3,4).charAt(0))) {
                return propertyName;
            }
        }
        if (propertyName.startsWith(SET_PREFIX) && propertyName.endsWith(")")) {
            if (Character.isUpperCase(propertyName.substring(3,4).charAt(0))) {
                return propertyName;
            }
        }
        if (propertyName.startsWith(IS_PREFIX) && propertyName.endsWith("()")) {
            if (Character.isUpperCase(propertyName.substring(2,3).charAt(0))) {
                return propertyName;
            }
        }
        char first = propertyName.charAt(0);
        char second = propertyName.charAt(1);
        if (Character.isLowerCase(first) && Character.isUpperCase(second)) {
            return propertyName;
        } else {
            char[] chars = propertyName.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        }
    }

    public static List getDeclaredMethods(Class targetClass, String propertyName, boolean findSets)
    {
        List result = null;
        ClassCache cache = _declaredMethods[findSets ? 0 : 1];

        Map propertyCache = (Map) cache.get(targetClass);
        if ((propertyCache == null) || ((result = (List) propertyCache.get(propertyName)) == null)) {
            synchronized (cache) {
                propertyCache = (Map) cache.get(targetClass);

                if ((propertyCache == null) || ((result = (List) propertyCache.get(propertyName)) == null)) {

                    String baseName = capitalizeBeanPropertyName(propertyName);
                    result = new ArrayList();
                    collectAccessors(targetClass, baseName, result, findSets);

                    if (propertyCache == null) {
                        cache.put(targetClass, propertyCache = new HashMap(101));
                    }
                    propertyCache.put(propertyName, result.isEmpty() ? NotFoundList : result);

                    return result.isEmpty() ? null : result;
                }
            }
        }
        return (result == NotFoundList) ? null : result;
    }

    private static void collectAccessors(Class c, String baseName, List result, boolean findSets)
    {
        Method[] methods;
        try {
            methods = c.getDeclaredMethods();
        } catch (SecurityException ignored) {
            methods = c.getMethods();
        }
        for (int i = 0; i < methods.length; i++) {
            if (c.isInterface()) {
                if (isDefaultMethod(methods[i]) || isNonDefaultPublicInterfaceMethod(methods[i])) {
                    addIfAccessor(result, methods[i], baseName, findSets);
                }
                continue;
            }

            if (!isMethodCallable(methods[i]))
                continue;

            addIfAccessor(result, methods[i], baseName, findSets);
        }

        final Class superclass = c.getSuperclass();
        if (superclass != null)
            collectAccessors(superclass, baseName, result, findSets);

        for (final Class iface : c.getInterfaces())
            collectAccessors(iface, baseName, result, findSets);
    }

    private static void addIfAccessor(List result, Method method, String baseName, boolean findSets)
    {
        final String ms = method.getName();
        if (ms.endsWith(baseName)) {
            boolean isSet = false, isIs = false;
            if ((isSet = ms.startsWith(SET_PREFIX)) || ms.startsWith(GET_PREFIX)
                    || (isIs = ms.startsWith(IS_PREFIX))) {
                int prefixLength = (isIs ? 2 : 3);
                if (isSet == findSets) {
                    if (baseName.length() == (ms.length() - prefixLength)) {
                        result.add(method);
                    }
                }
            }
        }
    }

    /**
     * Convenience used to check if a method is volatile or synthetic so as to avoid
     * calling un-callable methods.
     *
     * @param m The method to check.
     * @return True if the method should be callable, false otherwise.
     */
    static boolean isMethodCallable(Method m)
    {
        if ((isJdk15() && m.isSynthetic()) || Modifier.isVolatile(m.getModifiers()))
            return false;

        return true;
    }

    /**
     * cache get methods
     */
    public static Method getGetMethod(OgnlContext context, Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        // Cache is a map in two levels, so we provide two keys (see comments in ClassPropertyMethodCache below)
        Method method = cacheGetMethod.get(targetClass, propertyName);
        if (method != null)
            return method;

        // By checking key existence now and not before calling 'get', we will save a map resolution 90% of the times
        if (cacheGetMethod.containsKey(targetClass, propertyName))
            return null;

        method = _getGetMethod(context, targetClass, propertyName); // will be null if not found - will cache it anyway
        cacheGetMethod.put(targetClass, propertyName, method);

        return method;
    }

    /**
     * Returns a qualifying get (getter) method, if one is available for the given targetClass and propertyName.
     *
     * Note: From OGNL 3.1.25 onward, this method will attempt to find the first get getter method(s) that match:
     *         1) First get (getter) method, whether public or not.
     *         2) First public get (getter) method, provided the method's declaring class is also public.
     *            This may be the same as 1), if 1) is also public and its declaring class is also public.
     *         3) First public non-Default interface get (getter) method, provided the method's declaring class is also public.
     *       The <b>order of preference (priority)<b> for the above matches will be <b>2</b> (1st public getter),
     *       <b>3</b> (1st public non-Default interface getter), <b>1</b> (1st getter of any kind).
     *   This updated methodology should help limit the need to modify method accessibility levels in some circumstances.
     *
     * @param context The current execution context.
     * @param targetClass Class to search for a get method (getter).
     * @param propertyName Name of the property for the get method (getter).
     *
     * @return
     * @throws IntrospectionException
     * @throws OgnlException
     */
    private static Method _getGetMethod(OgnlContext context, Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        Method result = null;

        List methods = getDeclaredMethods(targetClass, propertyName, false /* find 'get' methods */);

        if (methods != null)
        {
            Method firstGetter = null;
            Method firstPublicGetter = null;
            Method firstNonDefaultPublicInterfaceGetter = null;
            for (int i = 0, icount = methods.size(); i < icount; i++)
            {
                Method m = (Method) methods.get(i);
                Class[] mParameterTypes = findParameterTypes(targetClass, m); //getParameterTypes(m);

                if (mParameterTypes.length == 0)
                {
                    boolean declaringClassIsPublic = Modifier.isPublic(m.getDeclaringClass().getModifiers());
                    if (firstGetter == null) {
                        firstGetter = m;
                        if (_useFirstMatchGetSetLookup) {
                            break;  // Stop looking (emulate original logic, return 1st match)
                        }
                    }
                    if (firstPublicGetter == null && Modifier.isPublic(m.getModifiers()) && declaringClassIsPublic) {
                        firstPublicGetter = m;
                        break;  // Stop looking (this is the best possible match)
                    }
                    if (firstNonDefaultPublicInterfaceGetter == null && isNonDefaultPublicInterfaceMethod(m) && declaringClassIsPublic) {
                        firstNonDefaultPublicInterfaceGetter = m;
                    }
                }
            }
            result = (firstPublicGetter != null) ? firstPublicGetter :
                    (firstNonDefaultPublicInterfaceGetter != null) ? firstNonDefaultPublicInterfaceGetter : firstGetter;
        }

        return result;
    }

    public static boolean isMethodAccessible(OgnlContext context, Object target, Method method, String propertyName)
    {
        return (method != null) && context.getMemberAccess().isAccessible(context, target, method, propertyName);
    }

    public static boolean hasGetMethod(OgnlContext context, Object target, Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        return isMethodAccessible(context, target, getGetMethod(context, targetClass, propertyName), propertyName);
    }

    /**
     * cache set methods method
     */
    public static Method getSetMethod(OgnlContext context, Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        // Cache is a map in two levels, so we provide two keys (see comments in ClassPropertyMethodCache below)
        Method method = cacheSetMethod.get(targetClass, propertyName);
        if (method != null)
            return method;

        // By checking key existence now and not before calling 'get', we will save a map resolution 90% of the times
        if (cacheSetMethod.containsKey(targetClass, propertyName))
            return null;

        method = _getSetMethod(context, targetClass, propertyName); // will be null if not found - will cache it anyway
        cacheSetMethod.put(targetClass, propertyName, method);

        return method;
    }

    /**
     * Returns a qualifying set (setter) method, if one is available for the given targetClass and propertyName.
     *
     * Note: From OGNL 3.1.25 onward, this method will attempt to find the first set setter method(s) that match:
     *         1) First set (setter) method, whether public or not.
     *         2) First public set (setter) method, provided the method's declaring class is also public.
     *            This may be the same as 1), if 1) is also public and its declaring class is also public.
     *         3) First public non-Default interface set (setter) method, provided the method's declaring class is also public.
     *       The <b>order of preference (priority)<b> for the above matches will be <b>2</b> (1st public setter),
     *       <b>3</b> (1st public non-Default interface setter), <b>1</b> (1st setter of any kind).
     *   This updated methodology should help limit the need to modify method accessibility levels in some circumstances.
     *
     * @param context The current execution context.
     * @param targetClass Class to search for a set method (setter).
     * @param propertyName Name of the property for the set method (setter).
     *
     * @return
     * @throws IntrospectionException
     * @throws OgnlException
     */
    private static Method _getSetMethod(OgnlContext context, Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        Method result = null;

        List methods = getDeclaredMethods(targetClass, propertyName, true /* find 'set' methods */);

        if (methods != null)
        {
            Method firstSetter = null;
            Method firstPublicSetter = null;
            Method firstNonDefaultPublicInterfaceSetter = null;
            for (int i = 0, icount = methods.size(); i < icount; i++)
            {
                Method m = (Method) methods.get(i);
                Class[] mParameterTypes = findParameterTypes(targetClass, m); //getParameterTypes(m);

                if (mParameterTypes.length == 1) {
                    boolean declaringClassIsPublic = Modifier.isPublic(m.getDeclaringClass().getModifiers());
                    if (firstSetter == null) {
                        firstSetter = m;
                        if (_useFirstMatchGetSetLookup) {
                            break;  // Stop looking (emulate original logic, return 1st match)
                        }
                    }
                    if (firstPublicSetter == null && Modifier.isPublic(m.getModifiers()) && declaringClassIsPublic) {
                        firstPublicSetter = m;
                        break;  // Stop looking (this is the best possible match)
                    }
                    if (firstNonDefaultPublicInterfaceSetter == null && isNonDefaultPublicInterfaceMethod(m) && declaringClassIsPublic) {
                        firstNonDefaultPublicInterfaceSetter = m;
                    }
                }
            }

            result = (firstPublicSetter != null) ? firstPublicSetter :
                    (firstNonDefaultPublicInterfaceSetter != null) ? firstNonDefaultPublicInterfaceSetter : firstSetter;
        }

        return result;
    }

    public static final boolean hasSetMethod(OgnlContext context, Object target, Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        return isMethodAccessible(context, target, getSetMethod(context, targetClass, propertyName), propertyName);
    }

    public static final boolean hasGetProperty(OgnlContext context, Object target, Object oname)
            throws IntrospectionException, OgnlException
    {
        Class targetClass = (target == null) ? null : target.getClass();
        String name = oname.toString();

        return hasGetMethod(context, target, targetClass, name) || hasField(context, target, targetClass, name);
    }

    public static final boolean hasSetProperty(OgnlContext context, Object target, Object oname)
            throws IntrospectionException, OgnlException
    {
        Class targetClass = (target == null) ? null : target.getClass();
        String name = oname.toString();

        return hasSetMethod(context, target, targetClass, name) || hasField(context, target, targetClass, name);
    }

    private static final boolean indexMethodCheck(List methods)
    {
        boolean result = false;

        if (methods.size() > 0) {
            Method fm = (Method) methods.get(0);
            Class[] fmpt = getParameterTypes(fm);
            int fmpc = fmpt.length;
            Class lastMethodClass = fm.getDeclaringClass();

            result = true;
            for (int i = 1; result && (i < methods.size()); i++) {
                Method m = (Method) methods.get(i);
                Class c = m.getDeclaringClass();

                // Check to see if more than one method implemented per class
                if (lastMethodClass == c) {
                    result = false;
                } else {
                    Class[] mpt = getParameterTypes(fm);
                    int mpc = fmpt.length;

                    if (fmpc != mpc) {
                        result = false;
                    }
                    for (int j = 0; j < fmpc; j++) {
                        if (fmpt[j] != mpt[j]) {
                            result = false;
                            break;
                        }
                    }
                }
                lastMethodClass = c;
            }
        }
        return result;
    }

    static void findObjectIndexedPropertyDescriptors(Class targetClass, Map intoMap)
            throws OgnlException
    {
        Map allMethods = getMethods(targetClass, false);
        Map pairs = new HashMap(101);

        for (Iterator it = allMethods.keySet().iterator(); it.hasNext();) {
            String methodName = (String) it.next();
            List methods = (List) allMethods.get(methodName);

            /*
             * Only process set/get where there is exactly one implementation of the method per
             * class and those implementations are all the same
             */
            if (indexMethodCheck(methods)) {
                boolean isGet = false, isSet = false;
                Method m = (Method) methods.get(0);

                if (((isSet = methodName.startsWith(SET_PREFIX)) || (isGet = methodName.startsWith(GET_PREFIX)))
                    && (methodName.length() > 3)) {
                    String propertyName = Introspector.decapitalize(methodName.substring(3));
                    Class[] parameterTypes = getParameterTypes(m);
                    int parameterCount = parameterTypes.length;

                    if (isGet && (parameterCount == 1) && (m.getReturnType() != Void.TYPE)) {
                        List pair = (List) pairs.get(propertyName);

                        if (pair == null) {
                            pairs.put(propertyName, pair = new ArrayList());
                        }
                        pair.add(m);
                    }
                    if (isSet && (parameterCount == 2) && (m.getReturnType() == Void.TYPE)) {
                        List pair = (List) pairs.get(propertyName);

                        if (pair == null) {
                            pairs.put(propertyName, pair = new ArrayList());
                        }
                        pair.add(m);
                    }
                }
            }
        }

        for (Iterator it = pairs.keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            List methods = (List) pairs.get(propertyName);

            if (methods.size() == 2) {
                Method method1 = (Method) methods.get(0), method2 = (Method) methods.get(1), setMethod = (method1
                        .getParameterTypes().length == 2) ? method1 : method2, getMethod = (setMethod == method1) ? method2
                        : method1;
                Class keyType = getMethod.getParameterTypes()[0], propertyType = getMethod.getReturnType();

                if (keyType == setMethod.getParameterTypes()[0]) {
                    if (propertyType == setMethod.getParameterTypes()[1]) {
                        ObjectIndexedPropertyDescriptor propertyDescriptor;

                        try {
                            propertyDescriptor = new ObjectIndexedPropertyDescriptor(propertyName, propertyType,
                                                                                     getMethod, setMethod);
                        } catch (Exception ex) {
                            throw new OgnlException("creating object indexed property descriptor for '" + propertyName
                                                    + "' in " + targetClass, ex);
                        }
                        intoMap.put(propertyName, propertyDescriptor);
                    }
                }

            }
        }
    }

    /**
     * This method returns the property descriptors for the given class as a Map.
     *
     * @param targetClass The class to get the descriptors for.
     * @return Map map of property descriptors for class.
     *
     * @throws IntrospectionException on errors using {@link Introspector}.
     * @throws OgnlException On general errors.
     */
    public static Map getPropertyDescriptors(Class targetClass)
            throws IntrospectionException, OgnlException
    {
        Map result;

        if ((result = (Map) _propertyDescriptorCache.get(targetClass)) == null)
        {
            synchronized (_propertyDescriptorCache) {
                if ((result = (Map) _propertyDescriptorCache.get(targetClass)) == null)
                {
                    PropertyDescriptor[] pda = Introspector.getBeanInfo(targetClass).getPropertyDescriptors();

                    result = new HashMap(101);
                    for (int i = 0, icount = pda.length; i < icount; i++)
                    {
                        // workaround for Introspector bug 6528714 (bugs.sun.com)
                        if (pda[i].getReadMethod() != null && !isMethodCallable(pda[i].getReadMethod()))
                        {
                            pda[i].setReadMethod(findClosestMatchingMethod(targetClass, pda[i].getReadMethod(), pda[i].getName(),
                                                                           pda[i].getPropertyType(), true));
                        }
                        if (pda[i].getWriteMethod() != null && !isMethodCallable(pda[i].getWriteMethod()))
                        {
                            pda[i].setWriteMethod(findClosestMatchingMethod(targetClass, pda[i].getWriteMethod(), pda[i].getName(),
                                                                            pda[i].getPropertyType(), false));
                        }

                        result.put(pda[i].getName(), pda[i]);
                    }

                    findObjectIndexedPropertyDescriptors(targetClass, result);
                    _propertyDescriptorCache.put(targetClass, result);
                }
            }
        }
        return result;
    }

    /**
     * This method returns a PropertyDescriptor for the given class and property name using a Map
     * lookup (using getPropertyDescriptorsMap()).
     */
    public static PropertyDescriptor getPropertyDescriptor(Class targetClass, String propertyName)
            throws IntrospectionException, OgnlException
    {
        if (targetClass == null)
            return null;

        return (PropertyDescriptor) getPropertyDescriptors(targetClass).get(propertyName);
    }

    static Method findClosestMatchingMethod(Class targetClass, Method m, String propertyName,
                                            Class propertyType, boolean isReadMethod)
    {
        List methods = getDeclaredMethods(targetClass, propertyName, !isReadMethod);

        if (methods != null)
        {
            for (Object method1 : methods)
            {
                Method method = (Method) method1;

                if (method.getName().equals(m.getName())
                        && m.getReturnType().isAssignableFrom(m.getReturnType())
                        && method.getReturnType() == propertyType
                        && method.getParameterTypes().length == m.getParameterTypes().length) {
                    return method;
                }
            }
        }
        return m;
    }

    public static PropertyDescriptor[] getPropertyDescriptorsArray(Class targetClass)
            throws IntrospectionException
    {
        PropertyDescriptor[] result = null;

        if (targetClass != null) {
            if ((result = (PropertyDescriptor[]) _propertyDescriptorCache.get(targetClass)) == null) {
                synchronized (_propertyDescriptorCache) {
                    if ((result = (PropertyDescriptor[]) _propertyDescriptorCache.get(targetClass)) == null) {
                        _propertyDescriptorCache.put(targetClass, result = Introspector.getBeanInfo(targetClass)
                                .getPropertyDescriptors());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the property descriptor with the given name for the target class given.
     *
     * @param targetClass Class for which property descriptor is desired
     * @param name        Name of property
     * @return PropertyDescriptor of the named property or null if the class has no property with
     *         the given name
     */
    public static PropertyDescriptor getPropertyDescriptorFromArray(Class targetClass, String name)
            throws IntrospectionException
    {
        PropertyDescriptor result = null;
        PropertyDescriptor[] pda = getPropertyDescriptorsArray(targetClass);

        for (int i = 0, icount = pda.length; (result == null) && (i < icount); i++) {
            if (pda[i].getName().compareTo(name) == 0) {
                result = pda[i];
            }
        }
        return result;
    }

    public static void setMethodAccessor(Class cls, MethodAccessor accessor)
    {
        synchronized (_methodAccessors) {
            _methodAccessors.put(cls, accessor);
        }
    }

    public static MethodAccessor getMethodAccessor(Class cls)
            throws OgnlException
    {
        MethodAccessor answer = (MethodAccessor) getHandler(cls, _methodAccessors);
        if (answer != null)
            return answer;
        throw new OgnlException("No method accessor for " + cls);
    }

    public static void setPropertyAccessor(Class cls, PropertyAccessor accessor)
    {
        synchronized (_propertyAccessors) {
            _propertyAccessors.put(cls, accessor);
        }
    }

    public static PropertyAccessor getPropertyAccessor(Class cls)
            throws OgnlException
    {
        PropertyAccessor answer = (PropertyAccessor) getHandler(cls, _propertyAccessors);
        if (answer != null)
            return answer;

        throw new OgnlException("No property accessor for class " + cls);
    }

    public static ElementsAccessor getElementsAccessor(Class cls)
            throws OgnlException
    {
        ElementsAccessor answer = (ElementsAccessor) getHandler(cls, _elementsAccessors);
        if (answer != null)
            return answer;
        throw new OgnlException("No elements accessor for class " + cls);
    }

    public static void setElementsAccessor(Class cls, ElementsAccessor accessor)
    {
        synchronized (_elementsAccessors) {
            _elementsAccessors.put(cls, accessor);
        }
    }

    public static NullHandler getNullHandler(Class cls)
            throws OgnlException
    {
        NullHandler answer = (NullHandler) getHandler(cls, _nullHandlers);
        if (answer != null)
            return answer;
        throw new OgnlException("No null handler for class " + cls);
    }

    public static void setNullHandler(Class cls, NullHandler handler)
    {
        synchronized (_nullHandlers) {
            _nullHandlers.put(cls, handler);
        }
    }

    private static Object getHandler(Class forClass, ClassCache handlers)
    {
        Object answer = null;

        if ((answer = handlers.get(forClass)) == null) {
            synchronized (handlers) {
                if ((answer = handlers.get(forClass)) == null) {
                    Class keyFound;

                    if (forClass.isArray()) {
                        answer = handlers.get(Object[].class);
                        keyFound = null;
                    } else {
                        keyFound = forClass;
                        outer:
                        for (Class c = forClass; c != null; c = c.getSuperclass()) {
                            answer = handlers.get(c);
                            if (answer == null) {
                                Class[] interfaces = c.getInterfaces();
                                for (int index = 0, count = interfaces.length; index < count; ++index) {
                                    Class iface = interfaces[index];

                                    answer = handlers.get(iface);
                                    if (answer == null) {
                                        /* Try super-interfaces */
                                        answer = getHandler(iface, handlers);
                                    }
                                    if (answer != null) {
                                        keyFound = iface;
                                        break outer;
                                    }
                                }
                            } else {
                                keyFound = c;
                                break;
                            }
                        }
                    }
                    if (answer != null) {
                        if (keyFound != forClass) {
                            handlers.put(forClass, answer);
                        }
                    }
                }
            }
        }
        return answer;
    }

    public static Object getProperty(OgnlContext context, Object source, Object name)
            throws OgnlException
    {
        PropertyAccessor accessor;

        if (source == null)
        {
            throw new OgnlException("source is null for getProperty(null, \"" + name + "\")");
        }
        if ((accessor = getPropertyAccessor(getTargetClass(source))) == null)
        {
            throw new OgnlException("No property accessor for " + getTargetClass(source).getName());
        }

        return accessor.getProperty(context, source, name);
    }

    public static void setProperty(OgnlContext context, Object target, Object name, Object value)
            throws OgnlException
    {
        PropertyAccessor accessor;

        if (target == null) {
            throw new OgnlException("target is null for setProperty(null, \"" + name + "\", " + value + ")");
        }
        if ((accessor = getPropertyAccessor(getTargetClass(target))) == null) {
            throw new OgnlException("No property accessor for " + getTargetClass(target).getName());
        }

        accessor.setProperty(context, target, name, value);
    }

    /**
     * Determines the index property type, if any. Returns <code>INDEXED_PROPERTY_NONE</code> if
     * the property is not index-accessible as determined by OGNL or JavaBeans. If it is indexable
     * then this will return whether it is a JavaBeans indexed property, conforming to the indexed
     * property patterns (returns <code>INDEXED_PROPERTY_INT</code>) or if it conforms to the
     * OGNL arbitrary object indexable (returns <code>INDEXED_PROPERTY_OBJECT</code>).
     */
    public static int getIndexedPropertyType(OgnlContext context, Class sourceClass, String name)
            throws OgnlException
    {
        int result = INDEXED_PROPERTY_NONE;

        try {
            PropertyDescriptor pd = getPropertyDescriptor(sourceClass, name);
            if (pd != null) {
                if (pd instanceof IndexedPropertyDescriptor) {
                    result = INDEXED_PROPERTY_INT;
                } else {
                    if (pd instanceof ObjectIndexedPropertyDescriptor) {
                        result = INDEXED_PROPERTY_OBJECT;
                    }
                }
            }
        } catch (Exception ex) {
            throw new OgnlException("problem determining if '" + name + "' is an indexed property", ex);
        }
        return result;
    }

    public static Object getIndexedProperty(OgnlContext context, Object source, String name, Object index)
            throws OgnlException
    {
        Object[] args = _objectArrayPool.create(index);

        try {
            PropertyDescriptor pd = getPropertyDescriptor((source == null) ? null : source.getClass(), name);
            Method m;

            if (pd instanceof IndexedPropertyDescriptor) {
                m = ((IndexedPropertyDescriptor) pd).getIndexedReadMethod();
            } else {
                if (pd instanceof ObjectIndexedPropertyDescriptor) {
                    m = ((ObjectIndexedPropertyDescriptor) pd).getIndexedReadMethod();
                } else {
                    throw new OgnlException("property '" + name + "' is not an indexed property");
                }
            }

            return callMethod(context, source, m.getName(), args);

        } catch (OgnlException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OgnlException("getting indexed property descriptor for '" + name + "'", ex);
        } finally {
            _objectArrayPool.recycle(args);
        }
    }

    public static void setIndexedProperty(OgnlContext context, Object source, String name, Object index,
                                          Object value)
            throws OgnlException
    {
        Object[] args = _objectArrayPool.create(index, value);

        try {
            PropertyDescriptor pd = getPropertyDescriptor((source == null) ? null : source.getClass(), name);
            Method m;

            if (pd instanceof IndexedPropertyDescriptor) {
                m = ((IndexedPropertyDescriptor) pd).getIndexedWriteMethod();
            } else {
                if (pd instanceof ObjectIndexedPropertyDescriptor) {
                    m = ((ObjectIndexedPropertyDescriptor) pd).getIndexedWriteMethod();
                } else {
                    throw new OgnlException("property '" + name + "' is not an indexed property");
                }
            }

            callMethod(context, source, m.getName(), args);

        } catch (OgnlException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OgnlException("getting indexed property descriptor for '" + name + "'", ex);
        } finally {
            _objectArrayPool.recycle(args);
        }
    }

    public static EvaluationPool getEvaluationPool()
    {
        return _evaluationPool;
    }

    public static ObjectArrayPool getObjectArrayPool()
    {
        return _objectArrayPool;
    }

    /**
     * Registers the specified {@link ClassCacheInspector} with all class reflection based internal
     * caches.  This may have a significant performance impact so be careful using this in production scenarios.
     *
     * @param inspector
     *          The inspector instance that will be registered with all internal cache instances.
     */
    public static void setClassCacheInspector(ClassCacheInspector inspector)
    {
        _cacheInspector = inspector;

        _propertyDescriptorCache.setClassInspector(_cacheInspector);
        _constructorCache.setClassInspector(_cacheInspector);
        _staticMethodCache.setClassInspector(_cacheInspector);
        _instanceMethodCache.setClassInspector(_cacheInspector);
        _invokePermissionCache.setClassInspector(_cacheInspector);
        _fieldCache.setClassInspector(_cacheInspector);
        _declaredMethods[0].setClassInspector(_cacheInspector);
        _declaredMethods[1].setClassInspector(_cacheInspector);
    }

    public static Method getMethod(OgnlContext context, Class target, String name,
                                   Node[] children, boolean includeStatic)
            throws Exception
    {
        Class[] parms;
        if (children != null && children.length > 0)
        {
            parms = new Class[children.length];

            // used to reset context after loop
            Class currType = context.getCurrentType();
            Class currAccessor = context.getCurrentAccessor();
            Object cast = context.get(ExpressionCompiler.PRE_CAST);

            context.setCurrentObject(context.getRoot());
            context.setCurrentType(context.getRoot() != null ? context.getRoot().getClass() : null);
            context.setCurrentAccessor(null);
            context.setPreviousType(null);

            for (int i=0; i < children.length; i++)
            {
                children[i].toGetSourceString(context, context.getRoot());
                parms[i] = context.getCurrentType();
            }

            context.put(ExpressionCompiler.PRE_CAST, cast);

            context.setCurrentType(currType);
            context.setCurrentAccessor(currAccessor);
            context.setCurrentObject(target);
        } else
        {
            parms = EMPTY_CLASS_ARRAY;
        }

        List methods = OgnlRuntime.getMethods(target, name, includeStatic);
        if (methods == null)
            return null;

        for (int i = 0; i < methods.size(); i++)
        {
            Method m = (Method) methods.get(i);
            boolean varArgs = isJdk15() && m.isVarArgs();

            if (parms.length != m.getParameterTypes().length && !varArgs)
                continue;

            Class[] mparms = m.getParameterTypes();
            boolean matched = true;
            for (int p = 0; p < mparms.length; p++)
            {
                if (varArgs && mparms[p].isArray()){
                    continue;
                }

                if (parms[p] == null)
                {
                    matched = false;
                    break;
                }

                if (parms[p] == mparms[p])
                    continue;

                if (mparms[p].isPrimitive()
                    && Character.TYPE != mparms[p] && Byte.TYPE != mparms[p]
                    && Number.class.isAssignableFrom(parms[p])
                    && OgnlRuntime.getPrimitiveWrapperClass(parms[p]) == mparms[p])
                {
                    continue;
                }

                matched = false;
                break;
            }

            if (matched)
                return m;
        }

        return null;
    }

    /**
     * Finds the best possible match for a method on the specified target class with a matching
     * name.
     *
     * <p>
     *  The name matched will also try different combinations like <code>is + name, has + name, get + name, etc..</code>
     * </p>
     *
     * @param target
     *          The class to find a matching method against.
     * @param name
     *          The name of the method.
     * @return The most likely matching {@link Method}, or null if none could be found.
     */
    public static Method getReadMethod(Class target, String name)
    {
        return getReadMethod(target, name, null);
    }

    public static Method getReadMethod(Class target, String name, Class[] argClasses)
    {
        try {
            if (name.indexOf('"') >= 0)
                name = name.replaceAll("\"", "");

            name = name.toLowerCase();

            Method[] methods = target.getMethods();

            // exact matches first
            ArrayList<Method> candidates = new ArrayList<Method>();

            for (int i = 0; i < methods.length; i++)
            {
                if (!isMethodCallable(methods[i]))
                    continue;

                if ((methods[i].getName().equalsIgnoreCase(name)
                     || methods[i].getName().toLowerCase().equals("get" + name)
                     || methods[i].getName().toLowerCase().equals("has" + name)
                     || methods[i].getName().toLowerCase().equals("is" + name))
                    && !methods[i].getName().startsWith("set"))
                {
                    candidates.add(methods[i]);
                }
            }
            if (!candidates.isEmpty()) {
                MatchingMethod mm = findBestMethod(candidates, target, name, argClasses);
                if (mm != null)
                    return mm.mMethod;
            }

            for (int i = 0; i < methods.length; i++)
            {
                if (!isMethodCallable(methods[i]))
                    continue;

                if (methods[i].getName().equalsIgnoreCase(name)
                    && !methods[i].getName().startsWith("set")
                    && !methods[i].getName().startsWith("get")
                    && !methods[i].getName().startsWith("is")
                    && !methods[i].getName().startsWith("has")
                    && methods[i].getReturnType() != Void.TYPE) {

                    Method m = methods[i];
                    if (!candidates.contains(m))
                        candidates.add(m);
                }
            }

            if (!candidates.isEmpty()) {
                MatchingMethod mm = findBestMethod(candidates, target, name, argClasses);
                if (mm != null)
                    return mm.mMethod;
            }

            // try one last time adding a get to beginning

            if (!name.startsWith("get")) {
                Method ret = OgnlRuntime.getReadMethod(target, "get" + name, argClasses);
                if (ret != null)
                    return ret;
            }

            if (!candidates.isEmpty()) {
                // we need to do conversions.
                // TODO we have to find out which conversions are possible!
                int reqArgCount = argClasses==null?0:argClasses.length;
                for (Method m : candidates) {
                    if (m.getParameterTypes().length == reqArgCount)
                        return m;
                }
            }

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        return null;
    }

    public static Method getWriteMethod(Class target, String name)
    {
        return getWriteMethod(target, name, null);
    }

    public static Method getWriteMethod(Class target, String name, Class[] argClasses)
    {
        try {
            if (name.indexOf('"') >= 0)
                name = name.replaceAll("\"", "");

            BeanInfo info = Introspector.getBeanInfo(target);
            MethodDescriptor[] methods = info.getMethodDescriptors();

            ArrayList<Method> candidates = new ArrayList<Method>();

            for (int i = 0; i < methods.length; i++) {
                if (!isMethodCallable(methods[i].getMethod()))
                    continue;

                if ((methods[i].getName().equalsIgnoreCase(name)
                     || methods[i].getName().toLowerCase().equals(name.toLowerCase())
                     || methods[i].getName().toLowerCase().equals("set" + name.toLowerCase()))
                    && !methods[i].getName().startsWith("get")) {

                    candidates.add(methods[i].getMethod());
                }
            }

            if (!candidates.isEmpty()) {
                MatchingMethod mm = findBestMethod(candidates, target, name, argClasses);
                if (mm != null)
                    return mm.mMethod;
            }
            // try again on pure class

            Method[] cmethods = target.getClass().getMethods();
            for (int i = 0; i < cmethods.length; i++) {
                if (!isMethodCallable(cmethods[i]))
                    continue;

                if ((cmethods[i].getName().equalsIgnoreCase(name)
                     || cmethods[i].getName().toLowerCase().equals(name.toLowerCase())
                     || cmethods[i].getName().toLowerCase().equals("set" + name.toLowerCase()))
                    && !cmethods[i].getName().startsWith("get")) {

                    Method m = methods[i].getMethod();
                    if (!candidates.contains(m))
                        candidates.add(m);
                }
            }

            if (!candidates.isEmpty()) {
                MatchingMethod mm = findBestMethod(candidates, target, name, argClasses);
                if (mm != null)
                    return mm.mMethod;
            }
            // try one last time adding a set to beginning

            if (!name.startsWith("set")) {
                Method ret = OgnlRuntime.getReadMethod(target, "set" + name, argClasses);
                if (ret != null)
                    return ret;
            }

            if (!candidates.isEmpty()) {
                // we need to do conversions.
                // TODO we have to find out which conversions are possible!
                int reqArgCount = argClasses==null?0:argClasses.length;
                for (Method m : candidates) {
                    if (m.getParameterTypes().length == reqArgCount)
                        return m;
                }

                if ( argClasses == null && candidates.size() == 1 ) {
                    // this seems to be the TestCase TestOgnlRuntime.test_Complicated_Inheritance() - is this a real world use case?
                    return candidates.get(0);
                }
            }
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        return null;
    }

    public static PropertyDescriptor getProperty(Class target, String name)
    {
        try {
            BeanInfo info = Introspector.getBeanInfo(target);

            PropertyDescriptor[] pds = info.getPropertyDescriptors();

            for (int i = 0; i < pds.length; i++) {

                if (pds[i].getName().equalsIgnoreCase(name)
                    || pds[i].getName().toLowerCase().equals(name.toLowerCase())
                    || pds[i].getName().toLowerCase().endsWith(name.toLowerCase()))
                    return pds[i];
            }

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        return null;
    }

    public static boolean isBoolean(String expression)
    {
        if (expression == null)
            return false;

        if ("true".equals(expression) || "false".equals(expression)
            || "!true".equals(expression) || "!false".equals(expression)
            || "(true)".equals(expression)
            || "!(true)".equals(expression)
            || "(false)".equals(expression)
            || "!(false)".equals(expression)
            || expression.startsWith("ognl.OgnlOps"))
            return true;

        return false;
    }

    /**
     * Compares the {@link OgnlContext#getCurrentType()} and {@link OgnlContext#getPreviousType()} class types
     * on the stack to determine if a numeric expression should force object conversion.
     * <p/>
     * <p/>
     * Normally used in conjunction with the <code>forceConversion</code> parameter of
     * {@link OgnlRuntime#getChildSource(OgnlContext,Object,Node,boolean)}.
     * </p>
     *
     * @param context The current context.
     * @return True, if the class types on the stack wouldn't be comparable in a pure numeric expression such as <code>o1 >= o2</code>.
     */
    public static boolean shouldConvertNumericTypes(OgnlContext context)
    {
        if (context.getCurrentType() == null || context.getPreviousType() == null)
            return true;

        if (context.getCurrentType() == context.getPreviousType()
            && context.getCurrentType().isPrimitive() && context.getPreviousType().isPrimitive())
            return false;

        return context.getCurrentType() != null && !context.getCurrentType().isArray()
               && context.getPreviousType() != null && !context.getPreviousType().isArray();
    }

    /**
     * Attempts to get the java source string represented by the specific child expression
     * via the {@link JavaSource#toGetSourceString(OgnlContext,Object)} interface method.
     *
     * @param context The ognl context to pass to the child.
     * @param target  The current object target to use.
     * @param child   The child expression.
     * @return The result of calling {@link JavaSource#toGetSourceString(OgnlContext,Object)} plus additional
     *         enclosures of {@link OgnlOps#convertValue(Object,Class,boolean)} for conversions.
     * @throws OgnlException Mandatory exception throwing catching.. (blehh)
     */
    public static String getChildSource(OgnlContext context, Object target, Node child)
            throws OgnlException
    {
        return getChildSource(context, target, child, false);
    }

    /**
     * Attempts to get the java source string represented by the specific child expression
     * via the {@link JavaSource#toGetSourceString(OgnlContext,Object)} interface method.
     *
     * @param context         The ognl context to pass to the child.
     * @param target          The current object target to use.
     * @param child           The child expression.
     * @param forceConversion If true, forces {@link OgnlOps#convertValue(Object,Class)} conversions on the objects.
     * @return The result of calling {@link JavaSource#toGetSourceString(OgnlContext,Object)} plus additional
     *         enclosures of {@link OgnlOps#convertValue(Object,Class,boolean)} for conversions.
     * @throws OgnlException Mandatory exception throwing catching.. (blehh)
     */
    public static String getChildSource(OgnlContext context, Object target, Node child, boolean forceConversion)
            throws OgnlException
    {
        String pre = (String) context.get("_currentChain");
        if (pre == null)
            pre = "";

        try
        {
            child.getValue(context, target);
        } catch (NullPointerException e)
        {
            // ignore
        } catch (ArithmeticException e)
        {
            context.setCurrentType(int.class);
            return "0";
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        String source = null;

        try
        {
            source = child.toGetSourceString(context, target);
        }
        catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        // handle root / method expressions that may not have proper root java source access

        if (!ASTConst.class.isInstance(child)
            && (target == null || context.getRoot() != target))
        {
            source = pre + source;
        }

        if (context.getRoot() != null)
        {
            source = ExpressionCompiler.getRootExpression(child, context.getRoot(), context) + source;
            context.setCurrentAccessor(context.getRoot().getClass());
        }

        if (ASTChain.class.isInstance(child))
        {
            String cast = (String) context.remove(ExpressionCompiler.PRE_CAST);
            if (cast == null)
                cast = "";

            source = cast + source;
        }

        if (source == null || source.trim().length() < 1)
            source = "null";

        return source;
    }



    /*
     * The idea behind this class is to provide a very fast way to cache getter/setter methods indexed by their class
     * and property name.
     *
     * Instead of creating any kind of complex key object (or a String key by appending class name and property), this
     * class directly uses the Class clazz and the String propertyName as keys of two levels of ConcurrentHashMaps,
     * so that it takes advantage of the fact that these two classes are immutable and that their respective hashCode()
     * and equals() methods are extremely fast and optimized. These two aspects should improve Map access performance.
     *
     * Also, using these structure instead of any other kind of key on a single-level map should save a lot of memory
     * given no specialized cache objects (be them of a specific CacheKey class or mere Strings) ever have to be created
     * for simply accessing the cache in search for a getter/setter method.
     *
     */
    private static final class ClassPropertyMethodCache {

        // ConcurrentHashMaps do not allow null keys or values, so we will use one of this class's own methods as
        // a replacement for signaling when the true cached value is 'null'
        private static final Method NULL_REPLACEMENT;

        private final ConcurrentHashMap<Class<?>,ConcurrentHashMap<String,Method>> cache =
                new ConcurrentHashMap<Class<?>,ConcurrentHashMap<String,Method>>();

        static
        {
            try
            {
                NULL_REPLACEMENT =
                        ClassPropertyMethodCache.class.getDeclaredMethod("get", new Class[] {Class.class,String.class});
            } catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e); // Will never happen, it's our own method, we know it exists
            }
        }

        ClassPropertyMethodCache()
        {
            super();
        }

        Method get(Class clazz, String propertyName)
        {
            ConcurrentHashMap<String,Method> methodsByPropertyName = this.cache.get(clazz);
            if (methodsByPropertyName == null)
            {
                methodsByPropertyName = new ConcurrentHashMap<String, Method>();
                this.cache.put(clazz, methodsByPropertyName);
            }
            Method method = methodsByPropertyName.get(propertyName);
            if (method == NULL_REPLACEMENT)
                return null;
            return method;
        }

        void put(Class clazz, String propertyName, Method method)
        {
            ConcurrentHashMap<String,Method> methodsByPropertyName = this.cache.get(clazz);
            if (methodsByPropertyName == null)
            {
                methodsByPropertyName = new ConcurrentHashMap<String, Method>();
                this.cache.put(clazz, methodsByPropertyName);
            }
            methodsByPropertyName.put(propertyName, (method == null? NULL_REPLACEMENT : method));
        }

        boolean containsKey(Class clazz, String propertyName)
        {
            ConcurrentHashMap<String,Method> methodsByPropertyName = this.cache.get(clazz);
            if (methodsByPropertyName == null)
                return false;

            return methodsByPropertyName.containsKey(propertyName);
        }

        /**
         * Allow clearing for the underlying cache of the ClassPropertyMethodCache.
         *
         * @since 3.1.25
         */
        void clear() {
            this.cache.clear();
        }

    }

    /**
     * Detect the (reported) Major Java version running OGNL.
     *
     * Should support naming conventions of pre-JDK9 and JDK9+.
     * See <a href="https://openjdk.java.net/jeps/223">JEP 223: New Version-String Scheme</a> for details.
     *
     * @return Detected Major Java Version, or 5 (minimum supported version for OGNL) if unable to detect.
     *
     * @since 3.1.25
     */
    static int detectMajorJavaVersion() {
        int majorVersion = -1;
        try {
            majorVersion = parseMajorJavaVersion(System.getProperty("java.version"));
        } catch (Exception ex) {
            // Unavailable (SecurityException, etc.)
        }
        if (majorVersion == -1) {
            majorVersion = 5;  // Return minimum supported Java version for OGNL
        }

        return majorVersion;
    }

    /**
     * Parse a Java version string to determine the Major Java version.
     *
     * Should support naming conventions of pre-JDK9 and JDK9+.
     * See <a href="https://openjdk.java.net/jeps/223">JEP 223: New Version-String Scheme</a> for details.
     *
     * @return Detected Major Java Version, or 5 (minimum supported version for OGNL) if unable to detect.
     *
     * @since 3.1.25
     */
    static int parseMajorJavaVersion(String versionString) {
        int majorVersion = -1;
        try {
            if (versionString != null && versionString.length() > 0) {
                final String[] sections = versionString.split("[\\.\\-\\+]");
                final int firstSection;
                final int secondSection;
                if (sections.length > 0) {  // Should not happen, guard anyway
                    if (sections[0].length() > 0) {
                        if (sections.length > 1  && sections[1].length() > 0) {
                            firstSection = Integer.parseInt(sections[0]);
                            if (sections[1].matches("\\d+")) {
                                secondSection = Integer.parseInt(sections[1]);
                            } else {
                                secondSection = -1;
                            }
                        } else  {
                            firstSection = Integer.parseInt(sections[0]);
                            secondSection = -1;
                        }
                        if (firstSection == 1 && secondSection != -1) {
                            majorVersion = secondSection;  // Pre-JDK 9 versioning
                        } else {
                            majorVersion = firstSection;   // JDK9+ versioning
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Unavailable (NumberFormatException, etc.)
        }
        if (majorVersion == -1) {
            majorVersion = 5;  // Return minimum supported Java version for OGNL
        }

        return majorVersion;
    }

    /**
     * Returns the value of the flag indicating whether the JDK9+ access handler has been
     *   been requested (it can then be used if the Major Java Version number is 9+).
     *
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#USE_JDK9PLUS_ACESS_HANDLER}.
     *
     * @return true if a request to use the JDK9+ access handler is requested, false otherwise (always use pre-JDK9 handler).
     *
     * @since 3.1.25
     */
    public static boolean getUseJDK9PlusAccessHandlerValue() {
        return _useJDK9PlusAccessHandler;
    }

    /**
     * Returns the value of the flag indicating whether "stricter" invocation is
     *   in effect or not.
     *
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#USE_STRICTER_INVOCATION}.
     *
     * @return true if stricter invocation is in effect, false otherwise.
     *
     * @since 3.1.25
     */
    public static boolean getUseStricterInvocationValue() {
        return _useStricterInvocation;
    }

    /**
     * Returns the value of the flag indicating whether the OGNL SecurityManager was disabled
     *   on initialization or not.
     *
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#OGNL_SECURITY_MANAGER} using
     *       the value {@link OgnlRuntime#OGNL_SM_FORCE_DISABLE_ON_INIT}.
     *
     * @return true if OGNL SecurityManager was disabled on initialization, false otherwise.
     *
     * @since 3.1.25
     *
     * @return
     */
    public static boolean getDisableOgnlSecurityManagerOnInitValue() {
        return _disableOgnlSecurityManagerOnInit;
    }

    /**
     * Returns an indication as to whether the current state indicates the
     *   JDK9+ (9 and later) access handler is being used / should be used.  This
     *   is based on a combination of the detected Major Java Version and the
     *   Java option flag {@link OgnlRuntime#USE_JDK9PLUS_ACESS_HANDLER}.
     *
     * @return true if the JDK9 and later access handler is being used / should be used, false otherwise.
     *
     * @since 3.1.25
     */
    public static boolean usingJDK9PlusAccessHandler() {
        return (_jdk9Plus && _useJDK9PlusAccessHandler);
    }

    /**
     * Returns the value of the flag indicating whether the old "first match" lookup for
     *   getters/setters is in effect or not.
     *
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#USE_FIRSTMATCH_GETSET_LOOKUP}.
     *
     * @return true if the old "first match" lookup is in effect, false otherwise.
     *
     * @since 3.1.25
     */
    public static boolean getUseFirstMatchGetSetLookupValue() {
        return _useFirstMatchGetSetLookup;
    }

}
