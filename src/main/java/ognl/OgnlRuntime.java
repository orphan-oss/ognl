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
import ognl.internal.CacheException;
import ognl.internal.entry.*;
import ognl.security.OgnlSecurityManagerFactory;
import ognl.security.UserMethod;

import java.beans.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class used by internal OGNL API to do various things like:
 *
 * <ul>
 * <li>Handles majority of reflection logic / caching. </li>
 * <li>Utility methods for casting strings / various numeric types used by {@link OgnlExpressionCompiler}.</li>
 * <li>Core runtime configuration point for setting/using global {@link TypeConverter} / {@link OgnlExpressionCompiler} /
 * {@link NullHandler} instances / etc.. </li>
 * </ul>
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
    public static final Object[] NoArguments = new Object[]{};

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
    public static final String SET_PREFIX = "set";

    /**
     * Java beans standard get method prefix.
     */
    public static final String GET_PREFIX = "get";

    /**
     * Java beans standard {@code is<Foo>} boolean getter prefix.
     */
    public static final String IS_PREFIX = "is";

    /**
     * Prefix padding for hexadecimal numbers to HEX_LENGTH.
     */
    private static final Map<Integer, String> HEX_PADDING = new HashMap<>();

    private static final int HEX_LENGTH = 8;

    /**
     * Returned by <CODE>getUniqueDescriptor()</CODE> when the object is <CODE>null</CODE>.
     */
    private static final String NULL_OBJECT_STRING = "<null>";

    /**
     * Control usage of JDK9+ access handler using the JVM option:
     * -Dognl.UseJDK9PlusAccessHandler=true
     * -Dognl.UseJDK9PlusAccessHandler=false
     * <p>
     * Note: Set to "true" to allow the new JDK9 and later behaviour, <b>provided a newer JDK9+
     * is detected</b>.  By default the standard pre-JDK9 AccessHandler will be used even when
     * running on JDK9+, so users must "opt-in" in order to enable the alternate JDK9+ AccessHandler.
     * Using the JDK9PlusAccessHandler <b>may</b> avoid / mask JDK9+ warnings of the form:
     * "WARNING: Illegal reflective access by ognl.OgnlRuntime"
     * or provide an alternative  when running in environments set with "--illegal-access=deny".
     * <p>
     * Note:  The default behaviour is to use the standard pre-JDK9 access handler.
     * Using the "false" value has the same effect as omitting the option completely.
     * <p>
     * Warning: Users are <b>strongly advised</b> to review their code and confirm they really
     * need the AccessHandler modifying access levels, looking at alternatives to avoid that need.
     */
    static final String USE_JDK9PLUS_ACCESS_HANDLER = "ognl.UseJDK9PlusAccessHandler";

    /**
     * Control usage of "stricter" invocation processing by invokeMethod() using the JVM options:
     * -Dognl.UseStricterInvocation=true
     * -Dognl.UseStricterInvocation=false
     * <p>
     * Note: Using the "true" value has the same effect as omitting the option completely.
     * The default behaviour is to use the "stricter" invocation processing.
     * Using the "false" value reverts to the older "less strict" invocation processing
     * (in the event the "stricter" processing causes issues for existing applications).
     */
    static final String USE_STRICTER_INVOCATION = "ognl.UseStricterInvocation";

    /**
     * Hold environment flag state associated with USE_JDK9PLUS_ACESS_HANDLER.
     * Default: false (if not set)
     */
    private static final boolean _useJDK9PlusAccessHandler;

    static {
        boolean initialFlagState = false;
        try {
            final String propertyString = System.getProperty(USE_JDK9PLUS_ACCESS_HANDLER);
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
     * Default: true (if not set)
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
     *   flag {@link OgnlRuntime#USE_JDK9PLUS_ACCESS_HANDLER}.
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

    /*
     * Initialize the Method references used for blocking usage within invokeMethod().
     */
    static {
        Method setAccessibleMethod = null;
        Method setAccessibleMethodArray = null;
        Method systemExitMethod = null;
        Method systemConsoleMethod = null;
        try {
            setAccessibleMethod = AccessibleObject.class.getMethod("setAccessible", boolean.class);
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
            setAccessibleMethodArray = AccessibleObject.class.getMethod("setAccessible", AccessibleObject[].class, boolean.class);
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
            systemExitMethod = System.class.getMethod("exit", int.class);
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
            systemConsoleMethod = System.class.getMethod("console");  // Not available in JDK 1.5 or earlier
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
     * -Dognl.security.manager=true  (or any non-null value other than 'disable')
     * <p>
     * Omit '-Dognl.security.manager=' or nullify the property to disable the feature.
     * <p>
     * To forcibly disable the feature (only possible at OGNL Library initialization, use the option:
     * -Dognl.security.manager=forceDisableOnInit
     * <p>
     * Users that have their own Security Manager implementations and no intention to use the OGNL SecurityManager
     * sandbox may choose to use the 'forceDisableOnInit' flag option for performance reasons (avoiding overhead
     * involving the system property security checks - when that feature will not be used).
     */
    static final String OGNL_SECURITY_MANAGER = "ognl.security.manager";
    static final String OGNL_SM_FORCE_DISABLE_ON_INIT = "forceDisableOnInit";

    /**
     * Hold environment flag state associated with OGNL_SECURITY_MANAGER.  See
     * {@link OgnlRuntime#OGNL_SECURITY_MANAGER} for more details.
     * Default: false (if not set).
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
     * -Dognl.UseFirstMatchGetSetLookup=true
     * -Dognl.UseFirstMatchGetSetLookup=false
     * <p>
     * Note: Using the "false" value has the same effect as omitting the option completely.
     * The default behaviour is to use the "best match" lookup for getters/setters.
     * Using the "true" value reverts to the older "first match" lookup for getters/setters
     * (in the event the "best match" processing causes issues for existing applications).
     */
    static final String USE_FIRSTMATCH_GETSET_LOOKUP = "ognl.UseFirstMatchGetSetLookup";

    /**
     * Hold environment flag state associated with USE_FIRSTMATCH_GETSET_LOOKUP.
     * Default: false (if not set)
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

    static final OgnlCache cache = new OgnlCache();

    private static final PrimitiveTypes primitiveTypes = new PrimitiveTypes();
    private static final PrimitiveDefaults primitiveDefaults = new PrimitiveDefaults();

    static SecurityManager securityManager = System.getSecurityManager();
    static final EvaluationPool _evaluationPool = new EvaluationPool();
    static final ObjectArrayPool _objectArrayPool = new ObjectArrayPool();

    static final Map<Method, Boolean> _methodAccessCache = new ConcurrentHashMap<>();
    static final Map<Method, Boolean> _methodPermCache = new ConcurrentHashMap<>();

    static final ClassPropertyMethodCache cacheSetMethod = new ClassPropertyMethodCache();
    static final ClassPropertyMethodCache cacheGetMethod = new ClassPropertyMethodCache();

    /**
     * Expression compiler used by {@link Ognl#compileExpression(OgnlContext, Object, String)} calls.
     */
    private static OgnlExpressionCompiler _compiler;

    /**
     * Used to provide primitive type equivalent conversions into and out of native / object types.
     */
    private static final PrimitiveWrapperClasses primitiveWrapperClasses = new PrimitiveWrapperClasses();

    /**
     * Constant strings for casting different primitive types.
     */
    private static final NumericCasts numericCasts = new NumericCasts();

    /**
     * Constant strings for getting the primitive value of different native types on the generic {@link Number} object
     * interface. (or the less generic BigDecimal/BigInteger types)
     */
    private static final NumericValues numericValues = new NumericValues();

    /**
     * Numeric primitive literal string expressions.
     */
    private static final NumericLiterals numericLiterals = new NumericLiterals();

    private static final NumericDefaults numericDefaults = new NumericDefaults();

    /*
     * Lazy loading of Javassist library
     */
    static {
        try {
            Class.forName("javassist.ClassPool");
            _compiler = new ExpressionCompiler();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Javassist library is missing in classpath! Please add missed dependency!", e);
        } catch (RuntimeException rt) {
            throw new IllegalStateException("Javassist library cannot be loaded, is it restricted by runtime environment?");
        }
    }

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

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
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Clears some additional caches used by OgnlRuntime.  The existing {@link OgnlRuntime#clearCache()}
     * clears the standard reflection-related caches, but some applications may have need to clear
     * the additional caches as well.
     * <p>
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
    public static void clearAdditionalCache() {
        cacheSetMethod.clear();
        cacheGetMethod.clear();
        cache.clear();
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

    public static String getNumericValueGetter(Class<?> type) {
        return numericValues.get(type);
    }

    public static Class<?> getPrimitiveWrapperClass(Class<?> primitiveClass) {
        return primitiveWrapperClasses.get(primitiveClass);
    }

    public static String getNumericCast(Class<? extends Number> type) {
        return numericCasts.get(type);
    }

    public static String getNumericLiteral(Class<? extends Number> type) {
        return numericLiterals.get(type);
    }

    public static void setCompiler(OgnlExpressionCompiler compiler) {
        _compiler = compiler;
    }

    public static OgnlExpressionCompiler getCompiler() {
        return _compiler;
    }

    public static void compileExpression(OgnlContext context, Node expression, Object root)
            throws Exception {
        _compiler.compileExpression(context, expression, root);
    }

    /**
     * Gets the "target" class of an object for looking up accessors that are registered on the
     * target. If the object is a Class object this will return the Class itself, else it will
     * return object's getClass() result.
     *
     * @param o the Object from which to retrieve its Class.
     * @return the Class of o.
     */
    public static Class<?> getTargetClass(Object o) {
        return (o == null) ? null : ((o instanceof Class) ? (Class<?>) o : o.getClass());
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the object
     * given.
     *
     * @param o the Object from which to retrieve its base classname.
     * @return the base classname of o's Class.
     */
    public static String getBaseName(Object o) {
        return (o == null) ? null : getClassBaseName(o.getClass());
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the class given.
     *
     * @param c the Class from which to retrieve its name.
     * @return the base classname of c.
     */
    public static String getClassBaseName(Class<?> c) {
        String s = c.getName();

        return s.substring(s.lastIndexOf('.') + 1);
    }

    public static String getClassName(Object o, boolean fullyQualified) {
        if (!(o instanceof Class)) {
            o = o.getClass();
        }

        return getClassName((Class<?>) o, fullyQualified);
    }

    public static String getClassName(Class<?> c, boolean fullyQualified) {
        return fullyQualified ? c.getName() : getClassBaseName(c);
    }

    /**
     * Returns the package name of the object's class.
     *
     * @param o the Object from which to retrieve its Class package name.
     * @return the package name of o's Class.
     */
    public static String getPackageName(Object o) {
        return (o == null) ? null : getClassPackageName(o.getClass());
    }

    /**
     * Returns the package name of the class given.
     *
     * @param c the Class from which to retrieve its package name.
     * @return the package name of c.
     */
    public static String getClassPackageName(Class<?> c) {
        String s = c.getName();
        int i = s.lastIndexOf('.');

        return (i < 0) ? null : s.substring(0, i);
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x&lt;hex digits&gt;.
     *
     * @param num the int to convert into a "pointer" string in hex format.
     * @return the String representing num as a "pointer" string in hex format.
     */
    public static String getPointerString(int num) {
        StringBuilder result = new StringBuilder();
        String hex = Integer.toHexString(num), pad;
        Integer l = hex.length();

        // result.append(HEX_PREFIX);
        if ((pad = HEX_PADDING.get(l)) == null) {
            StringBuilder pb = new StringBuilder();

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
     * Returns a "pointer" string in the usual format for these things - 0x&lt;hex digits&gt; for the
     * object given. This will always return a unique value for each object.
     *
     * @param o the Object to convert into a "pointer" string in hex format.
     * @return the String representing o as a "pointer" string in hex format.
     */
    public static String getPointerString(Object o) {
        return getPointerString((o == null) ? 0 : System.identityHashCode(o));
    }

    /**
     * Returns a unique descriptor string that includes the object's class and a unique integer
     * identifier. If fullyQualified is true then the class name will be fully qualified to include
     * the package name, else it will be just the class' base name.
     *
     * @param object         the Object for which a unique descriptor string is desired.
     * @param fullyQualified true if the descriptor string is fully-qualified (package name), false for just the Class' base name.
     * @return the unique descriptor String for the object, qualified as per fullyQualified parameter.
     */
    public static String getUniqueDescriptor(Object object, boolean fullyQualified) {
        StringBuilder result = new StringBuilder();

        if (object != null) {
            if (object instanceof Proxy) {
                Class<?> interfaceClass = object.getClass().getInterfaces()[0];

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
     *
     * @param object the Object for which a unique descriptor string is desired.
     * @return the unique descriptor String for the object, NOT fully-qualified.
     */
    public static String getUniqueDescriptor(Object object) {
        return getUniqueDescriptor(object, false);
    }

    /**
     * Utility to convert a List into an Object[] array. If the list is zero elements this will
     * return a constant array; toArray() on List always returns a new object and this is wasteful
     * for our purposes.
     *
     * @param list the List to convert into an Object array.
     * @return the array of Objects from the list.
     */
    public static Object[] toArray(List<?> list) {
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
     *
     * @param method the Method whose parameter types are being queried.
     * @return the array of Class elements representing m's parameters.  May be null if m does not utilize parameters.
     */
    public static Class<?>[] getParameterTypes(Method method) throws CacheException {
        return cache.getMethodParameterTypes(method);
    }

    /**
     * Finds the appropriate parameter types for the given {@link Method} and
     * {@link Class} instance of the type the method is associated with.  Correctly
     * finds generic types if running in &gt;= 1.5 jre as well.
     *
     * @param type   The class type the method is being executed against.
     * @param method The method to find types for.
     * @return Array of parameter types for the given method.
     */
    public static Class<?>[] findParameterTypes(Class<?> type, Method method) {
        if (type == null || type.getGenericSuperclass() == null || !(type.getGenericSuperclass() instanceof ParameterizedType)) {
            return getParameterTypes(method);
        }

        GenericMethodParameterTypeCacheEntry key = new GenericMethodParameterTypeCacheEntry(method, type);
        return cache.getGenericMethodParameterTypes(key);
    }

    /**
     * Returns the parameter types of the given method.
     *
     * @param constructor the Constructor whose parameter types are being queried.
     * @return the array of Class elements representing c's parameters.  May be null if c does not utilize parameters.
     */
    public static Class<?>[] getParameterTypes(Constructor<?> constructor) throws CacheException {
        return cache.getParameterTypes(constructor);
    }

    /**
     * Gets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @return SecurityManager for OGNL
     */
    public static SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Sets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @param value SecurityManager to set
     */
    public static void setSecurityManager(SecurityManager value) {
        securityManager = value;
    }

    /**
     * Permission will be named "invoke.&lt;declaring-class&gt;.&lt;method-name&gt;".
     *
     * @param method the Method whose Permission is being requested.
     * @return the Permission for method named "invoke.&lt;declaring-class&gt;.&lt;method-name&gt;".
     */
    public static Permission getPermission(Method method) throws CacheException {
        PermissionCacheEntry key = new PermissionCacheEntry(method);
        return cache.getInvokePermission(key);
    }

    public static Object invokeMethod(Object target, Method method, Object[] argsArray)
            throws InvocationTargetException, IllegalAccessException {
        boolean syncInvoke;
        boolean checkPermission;
        Boolean methodAccessCacheValue;
        Boolean methodPermCacheValue;

        if (_useStricterInvocation) {
            final Class<?> methodDeclaringClass = method.getDeclaringClass();  // Note: synchronized(method) call below will already NPE, so no null check.
            if ((AO_SETACCESSIBLE_REF != null && AO_SETACCESSIBLE_REF.equals(method)) ||
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
                    AccessibleObjectHandlerJDK9Plus.unsafeOrDescendant(methodDeclaringClass)) {
                // Prevent calls to some specific methods, as well as all methods of certain classes/interfaces
                //   for which no (apparent) legitimate use cases exist for their usage within OGNL invokeMethod().
                throw new IllegalAccessException("Method [" + method + "] cannot be called from within OGNL invokeMethod() " +
                        "under stricter invocation mode.");
            }
        }

        // only synchronize method invocation if it actually requires it

        synchronized (method) {
            methodAccessCacheValue = _methodAccessCache.get(method);
            if (methodAccessCacheValue == null) {
                if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                    if (!(method.isAccessible())) {
                        methodAccessCacheValue = Boolean.TRUE;
                        _methodAccessCache.put(method, methodAccessCacheValue);
                    } else {
                        methodAccessCacheValue = Boolean.FALSE;
                        _methodAccessCache.put(method, methodAccessCacheValue);
                    }
                } else {
                    methodAccessCacheValue = Boolean.FALSE;
                    _methodAccessCache.put(method, methodAccessCacheValue);
                }
            }
            syncInvoke = Boolean.TRUE.equals(methodAccessCacheValue);

            methodPermCacheValue = _methodPermCache.get(method);
            if (methodPermCacheValue == null) {
                if (securityManager != null) {
                    try {
                        securityManager.checkPermission(getPermission(method));
                        methodPermCacheValue = Boolean.TRUE;
                        _methodPermCache.put(method, methodPermCacheValue);
                    } catch (SecurityException ex) {
                        methodPermCacheValue = Boolean.FALSE;
                        _methodPermCache.put(method, methodPermCacheValue);
                        throw new IllegalAccessException("Method [" + method + "] cannot be accessed.");
                    }
                } else {
                    methodPermCacheValue = Boolean.TRUE;
                    _methodPermCache.put(method, methodPermCacheValue);
                }
            }
            checkPermission = Boolean.FALSE.equals(methodPermCacheValue);
        }

        Object result;

        if (syncInvoke) //if is not public and is not accessible
        {
            synchronized (method) {
                if (checkPermission) {
                    try {
                        securityManager.checkPermission(getPermission(method));
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
        } else {
            if (checkPermission) {
                try {
                    securityManager.checkPermission(getPermission(method));
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
    public static Class<?> getArgClass(Object arg) {
        if (arg == null)
            return null;
        Class<?> c = arg.getClass();
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

    public static Class<?>[] getArgClasses(Object[] args) {
        if (args == null) return null;

        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = getArgClass(args[i]);
        }
        return argClasses;
    }

    /**
     * Tells whether the given object is compatible with the given class ---that is, whether the
     * given object can be passed as an argument to a method or constructor whose parameter type is
     * the given class. If object is null this will return true because null is compatible with any
     * type.
     *
     * @param object the Object to check for type-compatibility with Class c.
     * @param c      the Class for which object's type-compatibility is being checked.
     * @return true if object is type-compatible with c.
     */
    public static boolean isTypeCompatible(Object object, Class<?> c) {
        if (object == null)
            return true;

        ArgsCompatbilityReport report = new ArgsCompatbilityReport(0, new boolean[1]);
        if (!isTypeCompatible(getArgClass(object), c, 0, report))
            return false;

        return !report.conversionNeeded[0]; // we don't allow conversions during this path...
    }

    public static boolean isTypeCompatible(Class<?> parameterClass, Class<?> methodArgumentClass, int index, ArgsCompatbilityReport report) {
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
                Class<?> pct = parameterClass.getComponentType();
                Class<?> mct = methodArgumentClass.getComponentType();
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
                Class<?> mct = methodArgumentClass.getComponentType();
                if (mct == Object.class) {
                    report.conversionNeeded[index] = true;
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
                report.conversionNeeded[index] = true;
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
                report.conversionNeeded[index] = true;
                report.score += 50;
                return true;
            }
        }
        if (methodArgumentClass.isAssignableFrom(parameterClass)) {
            report.score += 40;  // works but might not the best match - weight of 50..
            return true;
        }
        if (parameterClass.isPrimitive()) {
            Class<?> ptc = primitiveWrapperClasses.get(parameterClass);
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

    public static final ArgsCompatbilityReport NoArgsReport = new ArgsCompatbilityReport(0, new boolean[0]);

    public static boolean areArgsCompatible(Object[] args, Class<?>[] classes) {
        ArgsCompatbilityReport report = areArgsCompatible(getArgClasses(args), classes, null);
        if (report == null)
            return false;
        for (boolean conversionNeeded : report.conversionNeeded)
            if (conversionNeeded)
                return false;
        return true;
    }

    public static ArgsCompatbilityReport areArgsCompatible(Class<?>[] args, Class<?>[] classes, Method m) {
        boolean varArgs = m != null && m.isVarArgs();

        if (args == null || args.length == 0) {    // handle methods without arguments
            if (classes == null || classes.length == 0) {
                return NoArgsReport;
            } else {
                if (varArgs) {
                    return NoArgsReport;
                }
                return null;
            }
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
            Class<?> varArgsType = classes[classes.length - 1].getComponentType();
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
     *
     * @param classes1 the Class array being checked to see if it is "more specific" than classes2.
     * @param classes2 the Class array that classes1 is being checked against to see if classes1 is "more specific" than classes2.
     * @return true if the classes1 Class contents are "more specific" than classes2 Class contents, false otherwise.
     */
    public static boolean isMoreSpecific(Class<?>[] classes1, Class<?>[] classes2) {
        for (int index = 0, count = classes1.length; index < count; ++index) {
            Class<?> c1 = classes1[index], c2 = classes2[index];
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

    public static String getModifierString(int modifiers) {
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

    public static Class<?> classForName(OgnlContext context, String className) throws ClassNotFoundException {
        Class<?> result = primitiveTypes.get(className);

        if (result == null) {
            ClassResolver resolver;

            if ((context == null) || ((resolver = context.getClassResolver()) == null)) {
                resolver = new DefaultClassResolver();
            }
            result = resolver.classForName(className, context);
        }

        if (result == null)
            throw new ClassNotFoundException("Unable to resolve class: " + className);

        return result;
    }

    public static boolean isInstance(OgnlContext context, Object value, String className)
            throws OgnlException {
        try {
            Class<?> c = classForName(context, className);
            return c.isInstance(value);
        } catch (ClassNotFoundException e) {
            throw new OgnlException("No such class: " + className, e);
        }
    }

    public static Object getPrimitiveDefaultValue(Class<?> forClass) {
        return primitiveDefaults.get(forClass);
    }

    public static Object getNumericDefaultValue(Class<?> forClass) {
        return numericDefaults.get(forClass);
    }

    public static Object getConvertedType(OgnlContext context, Object target, Member member, String propertyName,
                                          Object value, Class<?> type) {
        return context.getTypeConverter().convertValue(context, target, member, propertyName, value, type);
    }

    public static boolean getConvertedTypes(OgnlContext context, Object target, Member member, String propertyName,
                                            Class<?>[] parameterTypes, Object[] args, Object[] newArgs) {
        boolean result = false;

        if (parameterTypes.length == args.length) {
            result = true;
            for (int i = 0, ilast = parameterTypes.length - 1; result && (i <= ilast); i++) {
                Object arg = args[i];
                Class<?> type = parameterTypes[i];

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

    public static Constructor<?> getConvertedConstructorAndArgs(OgnlContext context, Object target, List<Constructor<?>> constructors,
                                                                Object[] args, Object[] newArgs) {
        Constructor<?> result = null;
        TypeConverter converter = context.getTypeConverter();

        if ((converter != null) && (constructors != null)) {
            for (int i = 0, icount = constructors.size(); (result == null) && (i < icount); i++) {
                Constructor<?> ctor = constructors.get(i);
                Class<?>[] parameterTypes = getParameterTypes(ctor);

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
     * @param context      The current execution context.
     * @param source       Target object to run against or method name.
     * @param target       Instance of object to be run against.
     * @param propertyName Name of property to get method of.
     * @param methodName   Name of the method to get from known methods.
     * @param methods      List of current known methods.
     * @param args         Arguments originally passed in.
     * @param actualArgs   Converted arguments.
     * @return Best method match or null if none could be found.
     */
    public static Method getAppropriateMethod(OgnlContext context, Object source, Object target, String propertyName,
                                              String methodName, List<Method> methods, Object[] args, Object[] actualArgs) {
        Method result = null;

        if (methods != null) {
            Class<?> typeClass = target != null ? target.getClass() : null;
            if (typeClass == null && source instanceof Class) {
                typeClass = (Class<?>) source;
            }
            Class<?>[] argClasses = getArgClasses(args);

            MatchingMethod mm = findBestMethod(methods, typeClass, methodName, argClasses);
            if (mm != null) {
                result = mm.mMethod;
                Class<?>[] mParameterTypes = mm.mParameterTypes;
                System.arraycopy(args, 0, actualArgs, 0, args.length);

                if (actualArgs.length > 0) {
                    for (int j = 0; j < mParameterTypes.length; j++) {
                        Class<?> type = mParameterTypes[j];

                        if (mm.report.conversionNeeded[j] || (type.isPrimitive() && (actualArgs[j] == null))) {
                            actualArgs[j] = getConvertedType(context, source, result, propertyName, args[j], type);
                        }
                    }
                }
            }
        }

        if (result == null) {
            result = getConvertedMethodAndArgs(context, target, propertyName, methods, args, actualArgs);
        }

        return result;
    }

    public static Method getConvertedMethodAndArgs(OgnlContext context, Object target, String propertyName,
                                                   List<Method> methods, Object[] args, Object[] newArgs) {
        Method result = null;
        TypeConverter converter = context.getTypeConverter();

        if ((converter != null) && (methods != null)) {
            for (int i = 0, icount = methods.size(); (result == null) && (i < icount); i++) {
                Method m = methods.get(i);
                Class<?>[] parameterTypes = findParameterTypes(target != null ? target.getClass() : null, m);//getParameterTypes(m);

                if (getConvertedTypes(context, target, m, propertyName, parameterTypes, args, newArgs)) {
                    result = m;
                }
            }
        }
        return result;
    }

    private static class MatchingMethod {

        Method mMethod;
        int score;
        ArgsCompatbilityReport report;
        Class<?>[] mParameterTypes;

        private MatchingMethod(Method method, int score, ArgsCompatbilityReport report, Class<?>[] mParameterTypes) {
            this.mMethod = method;
            this.score = score;
            this.report = report;
            this.mParameterTypes = mParameterTypes;
        }
    }

    private static MatchingMethod findBestMethod(List<Method> methods, Class<?> typeClass, String name, Class<?>[] argClasses) {
        MatchingMethod mm = null;
        IllegalArgumentException failure = null;
        for (Method method : methods) {
            Class<?>[] mParameterTypes = findParameterTypes(typeClass, method);
            ArgsCompatbilityReport report = areArgsCompatible(argClasses, mParameterTypes, method);
            if (report == null)
                continue;

            String methodName = method.getName();
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
                mm = new MatchingMethod(method, score, report, mParameterTypes);
                failure = null;
            } else if (mm.score == score) {
                // it happens that we see the same method signature multiple times - for the current class or interfaces ...
                // check for same signature
                if (Arrays.equals(mm.mMethod.getParameterTypes(), method.getParameterTypes()) && mm.mMethod.getName().equals(method.getName())) {
                    // it is the same method. we use the public one...
                    if (!Modifier.isPublic(mm.mMethod.getDeclaringClass().getModifiers())
                            && Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                        mm = new MatchingMethod(method, score, report, mParameterTypes);
                        failure = null;
                    }
                } else {
                    // two methods with same score - direct compare to find the better one...
                    // legacy wins over varargs
                    if (method.isVarArgs() || mm.mMethod.isVarArgs()) {
                        if (method.isVarArgs() && !mm.mMethod.isVarArgs()) {
                            // keep with current
                        } else if (!method.isVarArgs()) {
                            // legacy wins...
                            mm = new MatchingMethod(method, score, report, mParameterTypes);
                            failure = null;
                        } else {
                            // both arguments are varargs...
                            System.err.println("Two vararg methods with same score(" + score + "): \"" + mm.mMethod + "\" and \"" + method + "\" please report!");
                        }
                    } else {
                        int scoreCurr = 0;
                        int scoreOther = 0;
                        for (int j = 0; j < argClasses.length; j++) {
                            Class<?> argClass = argClasses[j];
                            Class<?> mcClass = mm.mParameterTypes[j];
                            Class<?> moClass = mParameterTypes[j];
                            if (argClass == null) {    // TODO can we avoid this case?
                                // we don't know the class. use the most generic implementation...
                                if (mcClass == moClass) {
                                    // equal args - no winner...
                                } else if (mcClass.isAssignableFrom(moClass)) {
                                    scoreOther += 1000;    // current wins...
                                } else if (moClass.isAssignableFrom(moClass)) {
                                    scoreCurr += 1000;    // other wins...
                                } else {
                                    // both items can't be assigned to each other..
                                    failure = new IllegalArgumentException("Can't decide wich method to use: \"" + mm.mMethod + "\" or \"" + method + "\"");
                                }
                            } else {
                                // we try to find the more specific implementation
                                if (mcClass == moClass) {
                                    // equal args - no winner...
                                } else if (mcClass == argClass) {
                                    scoreOther += 100;    // current wins...
                                } else if (moClass == argClass) {
                                    scoreCurr += 100;    // other wins...
                                } else {
                                    // both items can't be assigned to each other..
                                    // TODO: if this happens we have to put some weight on the inheritance...
                                    failure = new IllegalArgumentException("Can't decide wich method to use: \"" + mm.mMethod + "\" or \"" + method + "\"");
                                }
                            }
                        }
                        if (scoreCurr == scoreOther) {
                            if (failure == null) {
                                boolean currentIsAbstract = Modifier.isAbstract(mm.mMethod.getModifiers());
                                boolean otherIsAbstract = Modifier.isAbstract(method.getModifiers());
                                if (currentIsAbstract == otherIsAbstract) {
                                    // Only report as an error when the score is equal and BOTH methods are abstract or BOTH are concrete.
                                    // If one is abstract and the other concrete then either choice should work for OGNL,
                                    // so we just keep the current choice and continue (without error output).
                                    System.err.println("Two methods with same score(" + score + "): \"" + mm.mMethod + "\" and \"" + method + "\" please report!");
                                }
                            }
                        } else if (scoreCurr > scoreOther) {
                            // other wins...
                            mm = new MatchingMethod(method, score, report, mParameterTypes);
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
                                               String propertyName, List<Method> methods, Object[] args)
            throws MethodFailedException {
        Throwable reason;
        Object[] actualArgs = _objectArrayPool.create(args.length);

        try {
            Method method = getAppropriateMethod(context, source, target, propertyName, methodName, methods, args, actualArgs);

            if (!isMethodAccessible(context, source, method, propertyName)) {
                StringBuilder buffer = new StringBuilder();
                String className = "";

                if (target != null) {
                    className = target.getClass().getName() + ".";
                }

                for (int i = 0, ilast = args.length - 1; i <= ilast; i++) {
                    Object arg = args[i];

                    buffer.append((arg == null) ? NULL_STRING : arg.getClass().getName());
                    if (i < ilast) {
                        buffer.append(", ");
                    }
                }

                throw new NoSuchMethodException(className + methodName + "(" + buffer + ")");
            }

            Object[] convertedArgs = actualArgs;

            if (method.isVarArgs()) {
                Class<?>[] parmTypes = method.getParameterTypes();

                // split arguments in to two dimensional array for varargs reflection invocation
                // where it is expected that the parameter passed in to invoke the method
                // will look like "new Object[] { arrayOfNonVarArgsArguments, arrayOfVarArgsArguments }"

                for (int i = 0; i < parmTypes.length; i++) {
                    if (parmTypes[i].isArray()) {
                        convertedArgs = new Object[i + 1];
                        if (actualArgs.length > 0) {
                            System.arraycopy(actualArgs, 0, convertedArgs, 0, convertedArgs.length);
                        }

                        Object[] varArgs;

                        // if they passed in varargs arguments grab them and dump in to new varargs array

                        if (actualArgs.length > i) {
                            List<Object> varArgsList = new ArrayList<>();
                            for (int j = i; j < actualArgs.length; j++) {
                                if (actualArgs[j] != null) {
                                    varArgsList.add(actualArgs[j]);
                                }
                            }

                            if (actualArgs.length == 1) {
                                varArgs = (Object[]) Array.newInstance(args[0].getClass(), 1);
                            } else {
                                varArgs = (Object[]) Array.newInstance(parmTypes[i].getComponentType(), varArgsList.size());
                            }
                            System.arraycopy(varArgsList.toArray(), 0, varArgs, 0, varArgs.length);
                        } else {
                            varArgs = new Object[0];
                        }
                        // If this is the only parameter, explode the array
                        if (actualArgs.length == 1 && args[0].getClass().isArray()) {
                            convertedArgs = varArgs;
                        } else { // there are more parameters, varargs is the last one
                            convertedArgs[i] = varArgs;
                        }
                        break;
                    }
                }
            }

            return invokeMethod(target, method, convertedArgs);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            reason = e;
        } catch (InvocationTargetException e) {
            reason = e.getTargetException();
        } finally {
            _objectArrayPool.recycle(actualArgs);
        }

        throw new MethodFailedException(source, methodName, reason);
    }

    public static Object callStaticMethod(OgnlContext context, String className, String methodName, Object[] args)
            throws OgnlException {
        try {
            Class<?> targetClass = classForName(context, className);

            MethodAccessor ma = getMethodAccessor(targetClass);

            return ma.callStaticMethod(context, targetClass, methodName, args);
        } catch (ClassNotFoundException ex) {
            throw new MethodFailedException(className, methodName, ex);
        }
    }

    /**
     * Invokes the specified method against the target object.
     *
     * @param context      The current execution context.
     * @param target       The object to invoke the method on.
     * @param methodName   Name of the method - as in "getValue" or "add", etc..
     * @param propertyName Name of the property to call instead?
     * @param args         Optional arguments needed for method.
     * @return Result of invoking method.
     * @throws OgnlException For lots of different reasons.
     * @deprecated Use {@link #callMethod(OgnlContext, Object, String, Object[])} instead.
     */
    public static Object callMethod(OgnlContext context, Object target, String methodName, String propertyName, Object[] args)
            throws OgnlException {
        return callMethod(context, target, methodName == null ? propertyName : methodName, args);
    }

    /**
     * Invokes the specified method against the target object.
     *
     * @param context    The current execution context.
     * @param target     The object to invoke the method on.
     * @param methodName Name of the method - as in "getValue" or "add", etc..
     * @param args       Optional arguments needed for method.
     * @return Result of invoking method.
     * @throws OgnlException For lots of different reasons.
     */
    public static Object callMethod(OgnlContext context, Object target, String methodName, Object[] args)
            throws OgnlException {
        if (target == null)
            throw new NullPointerException("target is null for method " + methodName);

        return getMethodAccessor(target.getClass()).callMethod(context, target, methodName, args);
    }

    public static Object callConstructor(OgnlContext context, String className, Object[] args)
            throws OgnlException {
        Throwable reason;
        Object[] actualArgs = args;

        try {
            Constructor<?> ctor = null;
            Class<?>[] ctorParameterTypes = null;
            Class<?> target = classForName(context, className);
            List<Constructor<?>> constructors = getConstructors(target);

            for (Constructor<?> constructor : constructors) {
                Class<?>[] cParameterTypes = getParameterTypes(constructor);

                if (areArgsCompatible(args, cParameterTypes)
                        && (ctor == null || isMoreSpecific(cParameterTypes, ctorParameterTypes))) {
                    ctor = constructor;
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
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            reason = e;
        } catch (InvocationTargetException e) {
            reason = e.getTargetException();
        } finally {
            if (actualArgs != args) {
                _objectArrayPool.recycle(actualArgs);
            }
        }

        throw new MethodFailedException(className, "new", reason);
    }

    /**
     * Don't use this method as it doesn't check member access rights via {@link MemberAccess} interface
     *
     * @param context      the current execution context.
     * @param target       the object to invoke the property name get on.
     * @param propertyName the name of the property to be retrieved from target.
     * @return the result invoking property retrieval of propertyName for target.
     * @throws OgnlException          for lots of different reasons.
     * @throws IllegalAccessException if access not permitted.
     * @throws NoSuchMethodException  if no property accessor exists.
     * @throws IntrospectionException on errors using {@link Introspector}.
     */
    @Deprecated
    public static Object getMethodValue(OgnlContext context, Object target, String propertyName)
            throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException {
        return getMethodValue(context, target, propertyName, false);
    }

    /**
     * If the checkAccessAndExistence flag is true this method will check to see if the method
     * exists and if it is accessible according to the context's MemberAccess. If neither test
     * passes this will return NotFound.
     *
     * @param context                 the current execution context.
     * @param target                  the object to invoke the property name get on.
     * @param propertyName            the name of the property to be retrieved from target.
     * @param checkAccessAndExistence true if this method should check access levels and existence for propertyName of target, false otherwise.
     * @return the result invoking property retrieval of propertyName for target.
     * @throws OgnlException          for lots of different reasons.
     * @throws IllegalAccessException if access not permitted.
     * @throws NoSuchMethodException  if no property accessor exists.
     * @throws IntrospectionException on errors using {@link Introspector}.
     */
    public static Object getMethodValue(OgnlContext context, Object target, String propertyName, boolean checkAccessAndExistence)
            throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException {
        Object result = null;
        Method m = getGetMethod((target == null) ? null : target.getClass(), propertyName);
        if (m == null)
            m = getReadMethod((target == null) ? null : target.getClass(), propertyName, null);

        if (checkAccessAndExistence) {
            if ((m == null) || !context.getMemberAccess().isAccessible(context, target, m, propertyName)) {
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
     *
     * @param context      the current execution context.
     * @param target       the object to invoke the property name get on.
     * @param propertyName the name of the property to be set for target.
     * @param value        the value to set for propertyName of target.
     * @return true if the operation succeeded, false otherwise.
     * @throws OgnlException          for lots of different reasons.
     * @throws IntrospectionException on errors using {@link Introspector}.
     */
    @Deprecated
    public static boolean setMethodValue(OgnlContext context, Object target, String propertyName, Object value) throws OgnlException, IntrospectionException {
        return setMethodValue(context, target, propertyName, value, false);
    }

    public static boolean setMethodValue(OgnlContext context, Object target, String propertyName, Object value,
                                         boolean checkAccessAndExistence)
            throws OgnlException, IntrospectionException {
        boolean result = true;
        Method m = getSetMethod(context, (target == null) ? null : target.getClass(), propertyName);

        if (checkAccessAndExistence) {
            if ((m == null) || !context.getMemberAccess().isAccessible(context, target, m, propertyName)) {
                result = false;
            }
        }

        if (result) {
            if (m != null) {
                Object[] args = _objectArrayPool.create(value);

                try {
                    callAppropriateMethod(context, target, target, m.getName(), propertyName,
                            Collections.nCopies(1, m), args);
                } finally {
                    _objectArrayPool.recycle(args);
                }
            } else {
                result = false;
            }
        }

        return result;
    }

    public static List<Constructor<?>> getConstructors(Class<?> targetClass) {
        return cache.getConstructor(targetClass);
    }

    public static Map<String, List<Method>> getMethods(Class<?> targetClass, boolean staticMethods) {
        DeclaredMethodCacheEntry.MethodType type = staticMethods ?
                DeclaredMethodCacheEntry.MethodType.STATIC :
                DeclaredMethodCacheEntry.MethodType.NON_STATIC;
        DeclaredMethodCacheEntry key = new DeclaredMethodCacheEntry(targetClass, type);
        return cache.getMethod(key);
    }

    /**
     * Backport of java.lang.reflect.Method#isDefault()
     * <p>
     * JDK8+ supports Default Methods for interfaces.  Default Methods are defined as:
     * public, non-abstract and declared within an interface (must also be non-static).
     *
     * @param method The Method to check against the requirements for a Default Method.
     * @return true If the Method qualifies as a Default Method, false otherwise.
     */
    private static boolean isDefaultMethod(Method method) {
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }

    /**
     * Determine if the provided Method is a non-Default public Interface method.
     * <p>
     * Public non-Default Methods are defined as:
     * public, abstract, non-static and declared within an interface.
     *
     * @param method The Method to check against the requirements for a non-Default Method.
     * @return true If method qualifies as a non-Default public Interface method, false otherwise.
     * @since 3.1.25
     */
    private static boolean isNonDefaultPublicInterfaceMethod(Method method) {
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == (Modifier.ABSTRACT | Modifier.PUBLIC))
                && method.getDeclaringClass().isInterface();
    }

    /*
     * @deprecated use {@link #getMethods(Class, boolean)} directly
     */
    @Deprecated
    public static Map<String, List<Method>> getAllMethods(Class<?> targetClass, boolean staticMethods) {
        return getMethods(targetClass, staticMethods);
    }

    public static List<Method> getMethods(Class<?> targetClass, String name, boolean staticMethods) {
        return getMethods(targetClass, staticMethods).get(name);
    }

    /*
     * @deprecated use {@link #getMethods(Class, String, boolean)} directly
     */
    @Deprecated
    public static List<Method> getAllMethods(Class<?> targetClass, String name, boolean staticMethods) {
        return getAllMethods(targetClass, staticMethods).get(name);
    }

    public static Map<String, Field> getFields(Class<?> targetClass) {
        return cache.getField(targetClass);
    }

    public static Field getField(Class<?> inClass, String name) {
        Field field = getFields(inClass).get(name);

        if (field == null) {
            // if field is null, it should search along the superclasses
            Class<?> superClass = inClass.getSuperclass();
            while (superClass != null) {
                field = getFields(superClass).get(name);
                if (field != null) {
                    return field;
                }
                superClass = superClass.getSuperclass();
            }
        }
        return field;
    }

    /**
     * Don't use this method as it doesn't check member access rights via {@link MemberAccess} interface
     *
     * @param context      the current execution context.
     * @param target       the object to invoke the property name get on.
     * @param propertyName the name of the property to be set for target.
     * @return the result invoking field retrieval of propertyName for target.
     * @throws NoSuchFieldException if the field does not exist.
     */
    @Deprecated
    public static Object getFieldValue(OgnlContext context, Object target, String propertyName)
            throws NoSuchFieldException {
        return getFieldValue(context, target, propertyName, false);
    }

    public static Object getFieldValue(OgnlContext context, Object target, String propertyName,
                                       boolean checkAccessAndExistence)
            throws NoSuchFieldException {
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
            throws OgnlException {
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

    public static boolean isFieldAccessible(OgnlContext context, Object target, Class<?> inClass, String propertyName) {
        return isFieldAccessible(context, target, getField(inClass, propertyName), propertyName);
    }

    public static boolean isFieldAccessible(OgnlContext context, Object target, Field field, String propertyName) {
        return context.getMemberAccess().isAccessible(context, target, field, propertyName);
    }

    public static boolean hasField(OgnlContext context, Object target, Class<?> inClass, String propertyName) {
        Field f = getField(inClass, propertyName);

        return (f != null) && isFieldAccessible(context, target, f, propertyName);
    }

    /**
     * Method name is getStaticField(), but actually behaves more like "getStaticFieldValue()".
     * <p>
     * Typical usage: Returns the value (not the actual {@link Field}) for the given (static) fieldName.
     * May return the {@link Enum} constant value for the given fieldName when className is an {@link Enum}.
     * May return a {@link Class} instance when the given fieldName is "class".
     * </p>
     *
     * @param context   The current ognl context
     * @param className The name of the class which contains the field
     * @param fieldName The name of the field whose value should be returned
     * @return The value of the (static) fieldName
     * @throws OgnlException for lots of different reasons.
     */
    public static Object getStaticField(OgnlContext context, String className, String fieldName)
            throws OgnlException {
        Exception reason;
        try {
            final Class<?> c = classForName(context, className);

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static
             * fields because it is a reserved word.
             */
            if (fieldName.equals("class")) {
                return c;
            } else if (c.isEnum()) {
                try {
                    return Enum.valueOf((Class<? extends Enum>) c, fieldName);
                } catch (IllegalArgumentException e) {
                    // ignore it, try static field
                }
            }

            final Field f = getField(c, fieldName);
            if (f == null) {
                throw new NoSuchFieldException(fieldName);
            }
            if (!Modifier.isStatic(f.getModifiers())) {
                throw new OgnlException("Field " + fieldName + " of class " + className + " is not static");
            }

            Object result;
            if (context.getMemberAccess().isAccessible(context, null, f, null)) {
                final Object state = context.getMemberAccess().setup(context, null, f, null);
                try {
                    result = f.get(null);
                } finally {
                    context.getMemberAccess().restore(context, null, f, null, state);
                }
            } else {
                throw new IllegalAccessException("Access to " + fieldName + " of class " + className + " is forbidden");
            }

            return result;
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalAccessException e) {
            reason = e;
        }

        throw new OgnlException("Could not get static field " + fieldName + " from class " + className, reason);
    }

    public static List<Method> getDeclaredMethods(Class<?> targetClass, String propertyName, boolean findSets) {
        String baseName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        List<Method> methods = new ArrayList<>();
        List<String> methodNames = new ArrayList<>(2);
        if (findSets) {
            methodNames.add(SET_PREFIX + baseName);
        } else {
            methodNames.add(IS_PREFIX + baseName);
            methodNames.add(GET_PREFIX + baseName);
        }
        for (String methodName : methodNames) {
            DeclaredMethodCacheEntry key = new DeclaredMethodCacheEntry(targetClass);
            List<Method> methodList = cache.getMethod(key).get(methodName);
            if (methodList != null) {
                methods.addAll(methodList);
            }
        }

        return methods;
    }

    /**
     * Convenience used to check if a method is a synthetic method so as to avoid
     * calling un-callable methods.  These methods are not considered callable by
     * OGNL in almost all circumstances.
     * <p>
     * This method considers any synthetic method (even bridge methods) as being un-callable.
     * Even though synthetic and bridge methods can technically be called, by default
     * OGNL excludes them from consideration.
     * <p>
     * Synthetic methods should be excluded in general, since calling such methods
     * could introduce unanticipated risks.
     *
     * @param m The method to check.
     * @return True if the method should be callable (non-synthetic), false otherwise.
     */
    public static boolean isMethodCallable(Method m) {
        return !(m.isSynthetic() || m.isBridge());
    }

    /**
     * Convenience used to check if a method is either a non-synthetic method or
     * a bridge method.
     * <p>
     * Warning:  This method should <b>NOT</b> be used as a direct replacement for
     * {@link #isMethodCallable(Method)}.  Almost all OGNL processing assumes the
     * exclusion of synthetic methods in order to process correctly.  <b>Only</b>
     * use this method to determine method callability for any OGNL processing
     * after <b>careful</b> consideration.
     * <p>
     * This method considers synthetic methods that are not also bridge methods
     * as being un-callable.
     * <p>
     * Synthetic methods should be excluded in general, since calling such methods
     * could introduce unanticipated risks.
     *
     * @param m The method to check.
     * @return True if the method should be callable (non-synthetic or bridge), false otherwise.
     * @since 3.2.16
     */
    static boolean isMethodCallable_BridgeOrNonSynthetic(Method m) {
        return !m.isSynthetic() || m.isBridge();  // Reference: See PR#104.
    }

    /**
     * cache get methods
     *
     * @param targetClass  the Class to invoke the property name "getter" retrieval on.
     * @param propertyName the name of the property for which a "getter" is sought.
     * @return the Method representing a "getter" for propertyName of targetClass.
     */
    public static Method getGetMethod(Class<?> targetClass, String propertyName) {
        // Cache is a map in two levels, so we provide two keys (see comments in ClassPropertyMethodCache below)
        Method method = cacheGetMethod.get(targetClass, propertyName);
        if (method == ClassPropertyMethodCache.NULL_REPLACEMENT) {
            return null;
        }
        if (method != null)
            return method;

        method = _getGetMethod(targetClass, propertyName); // will be null if not found - will cache it anyway
        cacheGetMethod.put(targetClass, propertyName, method);

        return method;
    }

    /**
     * Returns a qualifying get (getter) method, if one is available for the given targetClass and propertyName.
     * <p>
     * Note: From OGNL 3.1.25 onward, this method will attempt to find the first get getter method(s) that match:
     * 1) First get (getter) method, whether public or not.
     * 2) First public get (getter) method, provided the method's declaring class is also public.
     * This may be the same as 1), if 1) is also public and its declaring class is also public.
     * 3) First public non-Default interface get (getter) method, provided the method's declaring class is also public.
     * The <b>order of preference (priority)<b> for the above matches will be <b>2</b> (1st public getter),
     * <b>3</b> (1st public non-Default interface getter), <b>1</b> (1st getter of any kind).
     * This updated methodology should help limit the need to modify method accessibility levels in some circumstances.
     *
     * @param targetClass  Class to search for a get method (getter).
     * @param propertyName Name of the property for the get method (getter).
     */
    private static Method _getGetMethod(Class<?> targetClass, String propertyName) {
        Method result;

        List<Method> methods = getDeclaredMethods(targetClass, propertyName, false /* find 'get' methods */);

        Method firstGetter = null;
        Method firstPublicGetter = null;
        Method firstNonDefaultPublicInterfaceGetter = null;
        for (Method method : methods) {
            Class<?>[] mParameterTypes = findParameterTypes(targetClass, method); //getParameterTypes(m);

            if (mParameterTypes.length == 0) {
                boolean declaringClassIsPublic = Modifier.isPublic(method.getDeclaringClass().getModifiers());
                if (firstGetter == null) {
                    firstGetter = method;
                    if (_useFirstMatchGetSetLookup) {
                        break;  // Stop looking (emulate original logic, return 1st match)
                    }
                }
                if (Modifier.isPublic(method.getModifiers()) && declaringClassIsPublic) {
                    firstPublicGetter = method;
                    break;  // Stop looking (this is the best possible match)
                }
                if (firstNonDefaultPublicInterfaceGetter == null && isNonDefaultPublicInterfaceMethod(method) && declaringClassIsPublic) {
                    firstNonDefaultPublicInterfaceGetter = method;
                }
            }
        }
        result = (firstPublicGetter != null) ?
                firstPublicGetter
                : (firstNonDefaultPublicInterfaceGetter != null) ? firstNonDefaultPublicInterfaceGetter
                : firstGetter;

        return result;
    }

    public static boolean isMethodAccessible(OgnlContext context, Object target, Method method, String propertyName) {
        return (method != null) && context.getMemberAccess().isAccessible(context, target, method, propertyName);
    }

    public static boolean hasGetMethod(OgnlContext context, Object target, Class<?> targetClass, String propertyName) {
        return isMethodAccessible(context, target, getGetMethod(targetClass, propertyName), propertyName);
    }

    /**
     * cache set methods method
     *
     * @param context      the current execution context.
     * @param targetClass  the Class to invoke the property name "setter" retrieval on.
     * @param propertyName the name of the property for which a "setter" is sought.
     * @return the Method representing a "setter" for propertyName of targetClass.
     */
    public static Method getSetMethod(OgnlContext context, Class<?> targetClass, String propertyName) {
        // Cache is a map in two levels, so we provide two keys (see comments in ClassPropertyMethodCache below)
        Method method = cacheSetMethod.get(targetClass, propertyName);
        if (method == ClassPropertyMethodCache.NULL_REPLACEMENT) {
            return null;
        }
        if (method != null)
            return method;

        // By checking key existence now and not before calling 'get', we will save a map resolution 90% of the times
//        if (cacheSetMethod.containsKey(targetClass, propertyName))
//            return null;

        method = _getSetMethod(context, targetClass, propertyName); // will be null if not found - will cache it anyway
        cacheSetMethod.put(targetClass, propertyName, method);

        return method;
    }

    /**
     * Returns a qualifying set (setter) method, if one is available for the given targetClass and propertyName.
     * <p>
     * Note: From OGNL 3.1.25 onward, this method will attempt to find the first set setter method(s) that match:
     * 1) First set (setter) method, whether public or not.
     * 2) First public set (setter) method, provided the method's declaring class is also public.
     * This may be the same as 1), if 1) is also public and its declaring class is also public.
     * 3) First public non-Default interface set (setter) method, provided the method's declaring class is also public.
     * The <b>order of preference (priority)<b> for the above matches will be <b>2</b> (1st public setter),
     * <b>3</b> (1st public non-Default interface setter), <b>1</b> (1st setter of any kind).
     * This updated methodology should help limit the need to modify method accessibility levels in some circumstances.
     *
     * @param context      The current execution context.
     * @param targetClass  Class to search for a set method (setter).
     * @param propertyName Name of the property for the set method (setter).
     */
    private static Method _getSetMethod(OgnlContext context, Class<?> targetClass, String propertyName) {
        Method result;

        List<Method> methods = getDeclaredMethods(targetClass, propertyName, true /* find 'set' methods */);

        Method firstSetter = null;
        Method firstPublicSetter = null;
        Method firstNonDefaultPublicInterfaceSetter = null;
        for (Method method : methods) {
            Class<?>[] mParameterTypes = findParameterTypes(targetClass, method); //getParameterTypes(m);

            if (mParameterTypes.length == 1) {
                boolean declaringClassIsPublic = Modifier.isPublic(method.getDeclaringClass().getModifiers());
                if (firstSetter == null) {
                    firstSetter = method;
                    if (_useFirstMatchGetSetLookup) {
                        break;  // Stop looking (emulate original logic, return 1st match)
                    }
                }
                if (Modifier.isPublic(method.getModifiers()) && declaringClassIsPublic) {
                    firstPublicSetter = method;
                    break;  // Stop looking (this is the best possible match)
                }
                if (firstNonDefaultPublicInterfaceSetter == null && isNonDefaultPublicInterfaceMethod(method) && declaringClassIsPublic) {
                    firstNonDefaultPublicInterfaceSetter = method;
                }
            }
        }

        result = (firstPublicSetter != null) ? firstPublicSetter :
                (firstNonDefaultPublicInterfaceSetter != null) ? firstNonDefaultPublicInterfaceSetter : firstSetter;

        return result;
    }

    public static boolean hasSetMethod(OgnlContext context, Object target, Class<?> targetClass, String propertyName) {
        return isMethodAccessible(context, target, getSetMethod(context, targetClass, propertyName), propertyName);
    }

    public static boolean hasGetProperty(OgnlContext context, Object target, Object oname) throws IntrospectionException {
        Class<?> targetClass = (target == null) ? null : target.getClass();
        String name = oname.toString();

        return hasGetMethod(context, target, targetClass, name) || hasField(context, target, targetClass, name);
    }

    public static boolean hasSetProperty(OgnlContext context, Object target, Object oname) throws IntrospectionException {
        Class<?> targetClass = (target == null) ? null : target.getClass();
        String name = oname.toString();

        return hasSetMethod(context, target, targetClass, name) || hasField(context, target, targetClass, name);
    }

    /**
     * This method returns the property descriptors for the given class as a Map.
     *
     * @param targetClass The class to get the descriptors for.
     * @return Map of property descriptors for class.
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> targetClass) {
        return cache.getPropertyDescriptor(targetClass);
    }

    /**
     * This method returns a PropertyDescriptor for the given class and property name using a Map
     * lookup (using getPropertyDescriptorsMap()).
     *
     * @param targetClass  the class to get the descriptors for.
     * @param propertyName the property name of targetClass for which a Descriptor is requested.
     * @return the PropertyDescriptor for propertyName of targetClass.
     * @throws OgnlException On general errors.
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> targetClass, String propertyName) throws OgnlException {
        if (targetClass == null)
            return null;

        return getPropertyDescriptors(targetClass).get(propertyName);
    }

    public static PropertyDescriptor[] getPropertyDescriptorsArray(Class<?> targetClass) {
        Collection<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(targetClass).values();
        return propertyDescriptors.toArray(new PropertyDescriptor[0]);
    }

    /**
     * Gets the property descriptor with the given name for the target class given.
     *
     * @param targetClass Class for which property descriptor is desired
     * @param name        Name of property
     * @return PropertyDescriptor of the named property or null if the class has no property with
     * the given name
     */
    public static PropertyDescriptor getPropertyDescriptorFromArray(Class<?> targetClass, String name) {
        PropertyDescriptor result = null;
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptorsArray(targetClass);

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (result != null) {
                break;
            }
            if (propertyDescriptor.getName().compareTo(name) == 0) {
                result = propertyDescriptor;
            }
        }
        return result;
    }

    public static void setMethodAccessor(Class<?> clazz, MethodAccessor accessor) {
        cache.setMethodAccessor(clazz, accessor);
    }

    public static MethodAccessor getMethodAccessor(Class<?> clazz)
            throws OgnlException {
        return cache.getMethodAccessor(clazz);
    }

    public static void setPropertyAccessor(Class<?> clazz, PropertyAccessor accessor) {
        cache.setPropertyAccessor(clazz, accessor);
    }

    public static PropertyAccessor getPropertyAccessor(Class<?> clazz)
            throws OgnlException {
        return cache.getPropertyAccessor(clazz);
    }

    public static ElementsAccessor getElementsAccessor(Class<?> clazz)
            throws OgnlException {
        return cache.getElementsAccessor(clazz);
    }

    public static void setElementsAccessor(Class<?> clazz, ElementsAccessor accessor) {
        cache.setElementsAccessor(clazz, accessor);
    }

    public static NullHandler getNullHandler(Class<?> clazz)
            throws OgnlException {
        return cache.getNullHandler(clazz);
    }

    public static void setNullHandler(Class<?> clazz, NullHandler handler) {
        cache.setNullHandler(clazz, handler);
    }

    public static Object getProperty(OgnlContext context, Object source, Object name)
            throws OgnlException {
        PropertyAccessor accessor;

        if (source == null) {
            throw new OgnlException("source is null for getProperty(null, \"" + name + "\")");
        }
        if ((accessor = getPropertyAccessor(getTargetClass(source))) == null) {
            throw new OgnlException("No property accessor for " + getTargetClass(source).getName());
        }

        return accessor.getProperty(context, source, name);
    }

    public static void setProperty(OgnlContext context, Object target, Object name, Object value)
            throws OgnlException {
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
     *
     * @param sourceClass the Class to invoke indexed property type retrieval on.
     * @param name        the name of the property for which an indexed property type is sought.
     * @return the indexed property type (int) for the property name of sourceClass. Returns <code>INDEXED_PROPERTY_NONE</code> if name is not an indexed property.
     * @throws OgnlException for lots of different reasons.
     */
    public static int getIndexedPropertyType(Class<?> sourceClass, String name) throws OgnlException {
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
            throws OgnlException {
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
            throws OgnlException {
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

    public static EvaluationPool getEvaluationPool() {
        return _evaluationPool;
    }

    public static ObjectArrayPool getObjectArrayPool() {
        return _objectArrayPool;
    }

    /**
     * Registers the specified {@link ClassCacheInspector} with all class reflection based internal
     * caches.  This may have a significant performance impact so be careful using this in production scenarios.
     *
     * @param inspector The inspector instance that will be registered with all internal cache instances.
     */
    public static void setClassCacheInspector(ClassCacheInspector inspector) {
        cache.setClassCacheInspector(inspector);
    }

    public static Method getMethod(OgnlContext context, Class<?> target, String name, Node[] children, boolean includeStatic)
            throws Exception {
        Class<?>[] parms;
        if (children != null && children.length > 0) {
            parms = new Class[children.length];

            // used to reset context after loop
            Class<?> currType = context.getCurrentType();
            Class<?> currAccessor = context.getCurrentAccessor();
            Object cast = context.get(ExpressionCompiler.PRE_CAST);

            context.setCurrentObject(context.getRoot());
            context.setCurrentType(context.getRoot() != null ? context.getRoot().getClass() : null);
            context.setCurrentAccessor(null);
            context.setPreviousType(null);

            for (int i = 0; i < children.length; i++) {
                children[i].toGetSourceString(context, context.getRoot());
                parms[i] = context.getCurrentType();
            }

            context.put(ExpressionCompiler.PRE_CAST, cast);

            context.setCurrentType(currType);
            context.setCurrentAccessor(currAccessor);
            context.setCurrentObject(target);
        } else {
            parms = EMPTY_CLASS_ARRAY;
        }

        List<Method> methods = OgnlRuntime.getMethods(target, name, includeStatic);
        if (methods == null)
            return null;

        for (Method method : methods) {
            boolean varArgs = method.isVarArgs();

            if (parms.length != method.getParameterTypes().length && !varArgs)
                continue;

            Class<?>[] mparms = method.getParameterTypes();
            boolean matched = true;
            for (int p = 0; p < mparms.length; p++) {
                if (varArgs && mparms[p].isArray()) {
                    continue;
                }

                if (parms[p] == null) {
                    matched = false;
                    break;
                }

                if (parms[p] == mparms[p])
                    continue;

                if (mparms[p].isPrimitive()
                        && Character.TYPE != mparms[p] && Byte.TYPE != mparms[p]
                        && Number.class.isAssignableFrom(parms[p])
                        && OgnlRuntime.getPrimitiveWrapperClass(parms[p]) == mparms[p]) {
                    continue;
                }

                matched = false;
                break;
            }

            if (matched)
                return method;
        }

        return null;
    }

    /**
     * Finds the best possible match for a method on the specified target class with a matching
     * name.
     *
     * <p>
     * The name matched will also try different combinations like <code>is + name, has + name, get + name, etc..</code>
     * </p>
     *
     * @param target The class to find a matching method against.
     * @param name   The name of the method.
     * @return The most likely matching {@link Method}, or null if none could be found.
     */
    public static Method getReadMethod(Class<?> target, String name) {
        return getReadMethod(target, name, null);
    }

    public static Method getReadMethod(Class<?> target, String name, Class<?>[] argClasses) {
        try {
            if (name.indexOf('"') >= 0)
                name = name.replaceAll("\"", "");

            name = name.toLowerCase();

            Method[] methods = target.getMethods();

            // exact matches first
            ArrayList<Method> candidates = new ArrayList<>();

            for (Method method : methods) {
                // Consider bridge methods as callable (also) for Read methods.
                if (!isMethodCallable_BridgeOrNonSynthetic(method)) {
                    continue;
                }

                if ((method.getName().equalsIgnoreCase(name)
                        || method.getName().toLowerCase().equals("get" + name)
                        || method.getName().toLowerCase().equals("has" + name)
                        || method.getName().toLowerCase().equals("is" + name))
                        && !method.getName().startsWith("set")) {
                    candidates.add(method);
                }
            }
            if (!candidates.isEmpty()) {
                MatchingMethod mm = findBestMethod(candidates, target, name, argClasses);
                if (mm != null)
                    return mm.mMethod;
            }

            for (Method method : methods) {
                // Consider bridge methods as callable (also) for Read methods.
                if (!isMethodCallable_BridgeOrNonSynthetic(method)) {
                    continue;
                }

                if (method.getName().equalsIgnoreCase(name)
                        && !method.getName().startsWith("set")
                        && !method.getName().startsWith("get")
                        && !method.getName().startsWith("is")
                        && !method.getName().startsWith("has")
                        && method.getReturnType() != Void.TYPE) {

                    if (!candidates.contains(method)) {
                        candidates.add(method);
                    }
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
                int reqArgCount = argClasses == null ? 0 : argClasses.length;
                for (Method m : candidates) {
                    if (m.getParameterTypes().length == reqArgCount)
                        return m;
                }
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return null;
    }

    public static Method getWriteMethod(Class<?> target, String name) {
        return getWriteMethod(target, name, null);
    }

    public static Method getWriteMethod(Class<?> target, String name, Class<?>[] argClasses) {
        try {
            if (name.indexOf('"') >= 0) {
                name = name.replaceAll("\"", "");
            }

            BeanInfo info = Introspector.getBeanInfo(target);
            MethodDescriptor[] methods = info.getMethodDescriptors();

            ArrayList<Method> candidates = new ArrayList<>();

            for (MethodDescriptor method : methods) {
                // Consider bridge methods as callable (also) for Write methods.
                if (!isMethodCallable_BridgeOrNonSynthetic(method.getMethod())) {
                    continue;
                }

                if ((method.getName().equalsIgnoreCase(name)
                        || method.getName().equalsIgnoreCase(name)
                        || method.getName().toLowerCase().equals("set" + name.toLowerCase()))
                        && !method.getName().startsWith("get")) {

                    candidates.add(method.getMethod());
                }
            }

            if (!candidates.isEmpty()) {
                MatchingMethod mm = findBestMethod(candidates, target, name, argClasses);
                if (mm != null)
                    return mm.mMethod;
            }

            // try again on pure class
            Method[] cmethods = target.getMethods();
            for (Method cmethod : cmethods) {
                // Consider bridge methods as callable (also) for Write methods.
                if (!isMethodCallable_BridgeOrNonSynthetic(cmethod)) {
                    continue;
                }

                if ((cmethod.getName().equalsIgnoreCase(name)
                        || cmethod.getName().toLowerCase().equals("set" + name.toLowerCase()))
                        && !cmethod.getName().startsWith("get")) {

                    if (!candidates.contains(cmethod))
                        candidates.add(cmethod);
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
                int reqArgCount = argClasses == null ? 0 : argClasses.length;
                for (Method m : candidates) {
                    if (m.getParameterTypes().length == reqArgCount)
                        return m;
                }

                if (argClasses == null && candidates.size() == 1) {
                    // this seems to be the TestCase TestOgnlRuntime.test_Complicated_Inheritance() - is this a real world use case?
                    return candidates.get(0);
                }
            }
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return null;
    }

    public static PropertyDescriptor getProperty(Class<?> target, String name) {
        try {
            BeanInfo info = Introspector.getBeanInfo(target);

            PropertyDescriptor[] pds = info.getPropertyDescriptors();

            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equalsIgnoreCase(name) || pd.getName().toLowerCase().endsWith(name.toLowerCase()))
                    return pd;
            }

        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        return null;
    }

    public static boolean isBoolean(String expression) {
        if (expression == null)
            return false;

        return "true".equals(expression)
                || "false".equals(expression)
                || "!true".equals(expression)
                || "!false".equals(expression)
                || "(true)".equals(expression)
                || "!(true)".equals(expression)
                || "(false)".equals(expression)
                || "!(false)".equals(expression)
                || expression.startsWith("ognl.OgnlOps");
    }

    /**
     * Compares the {@link OgnlContext#getCurrentType()} and {@link OgnlContext#getPreviousType()} class types
     * on the stack to determine if a numeric expression should force object conversion.
     * <p>
     * Normally used in conjunction with the <code>forceConversion</code> parameter of
     * {@link OgnlRuntime#getChildSource(OgnlContext, Object, Node)}.
     * </p>
     *
     * @param context The current context.
     * @return True, if the class types on the stack wouldn't be comparable in a pure numeric expression such as <code>o1 &gt;= o2</code>.
     */
    public static boolean shouldConvertNumericTypes(OgnlContext context) {
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
     * via the {@link JavaSource#toGetSourceString(OgnlContext, Object)} interface method.
     *
     * @param context         The ognl context to pass to the child.
     * @param target          The current object target to use.
     * @param child           The child expression.
     * @return The result of calling {@link JavaSource#toGetSourceString(OgnlContext, Object)} plus additional
     * enclosures of {@link OgnlOps#convertValue(Object, Class, boolean)} for conversions.
     */
    public static String getChildSource(OgnlContext context, Object target, Node child) {
        String pre = (String) context.get("_currentChain");
        if (pre == null)
            pre = "";

        try {
            child.getValue(context, target);
        } catch (NullPointerException e) {
            // ignore
        } catch (ArithmeticException e) {
            context.setCurrentType(int.class);
            return "0";
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        String source;
        try {
            source = child.toGetSourceString(context, target);
        } catch (Throwable t) {
            throw OgnlOps.castToRuntime(t);
        }

        // handle root / method expressions that may not have proper root java source access

        if (!(child instanceof ASTConst)
                && (target == null || context.getRoot() != target)) {
            source = pre + source;
        }

        if (context.getRoot() != null) {
            source = ExpressionCompiler.getRootExpression(child, context.getRoot(), context) + source;
            context.setCurrentAccessor(context.getRoot().getClass());
        }

        if (child instanceof ASTChain) {
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

        private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>> cache =
                new ConcurrentHashMap<>();

        static {
            try {
                NULL_REPLACEMENT =
                        ClassPropertyMethodCache.class.getDeclaredMethod("get", Class.class, String.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e); // Will never happen, it's our own method, we know it exists
            }
        }

        ClassPropertyMethodCache() {
            super();
        }

        Method get(Class<?> clazz, String propertyName) {
            ConcurrentHashMap<String, Method> methodsByPropertyName = this.cache.get(clazz);
            if (methodsByPropertyName == null) {
                return null;
            }
            return methodsByPropertyName.get(propertyName);
        }

        void put(Class<?> clazz, String propertyName, Method method) {
            ConcurrentHashMap<String, Method> methodsByPropertyName = this.cache.get(clazz);
            if (methodsByPropertyName == null) {
                methodsByPropertyName = new ConcurrentHashMap<>();
                ConcurrentHashMap<String, Method> old = this.cache.putIfAbsent(clazz, methodsByPropertyName);
                if (null != old) {
                    methodsByPropertyName = old;
                }
            }
            methodsByPropertyName.putIfAbsent(propertyName, (method == null ? NULL_REPLACEMENT : method));
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
     * <p>
     * Should support naming conventions of pre-JDK9 and JDK9+.
     * See <a href="https://openjdk.java.net/jeps/223">JEP 223: New Version-String Scheme</a> for details.
     *
     * @return Detected Major Java Version, or 5 (minimum supported version for OGNL) if unable to detect.
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
     * <p>
     * Should support naming conventions of pre-JDK9 and JDK9+.
     * See <a href="https://openjdk.java.net/jeps/223">JEP 223: New Version-String Scheme</a> for details.
     *
     * @return Detected Major Java Version, or 5 (minimum supported version for OGNL) if unable to detect.
     * @since 3.1.25
     */
    static int parseMajorJavaVersion(String versionString) {
        int majorVersion = -1;
        try {
            if (versionString != null && versionString.length() > 0) {
                final String[] sections = versionString.split("[.\\-+]");
                final int firstSection;
                final int secondSection;
                if (sections.length > 0) {  // Should not happen, guard anyway
                    if (sections[0].length() > 0) {
                        if (sections.length > 1 && sections[1].length() > 0) {
                            firstSection = Integer.parseInt(sections[0]);
                            if (sections[1].matches("\\d+")) {
                                secondSection = Integer.parseInt(sections[1]);
                            } else {
                                secondSection = -1;
                            }
                        } else {
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
     * been requested (it can then be used if the Major Java Version number is 9+).
     * <p>
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#USE_JDK9PLUS_ACCESS_HANDLER}.
     *
     * @return true if a request to use the JDK9+ access handler is requested, false otherwise (always use pre-JDK9 handler).
     * @since 3.1.25
     */
    public static boolean getUseJDK9PlusAccessHandlerValue() {
        return _useJDK9PlusAccessHandler;
    }

    /**
     * Returns the value of the flag indicating whether "stricter" invocation is
     * in effect or not.
     * <p>
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#USE_STRICTER_INVOCATION}.
     *
     * @return true if stricter invocation is in effect, false otherwise.
     * @since 3.1.25
     */
    public static boolean getUseStricterInvocationValue() {
        return _useStricterInvocation;
    }

    /**
     * Returns the value of the flag indicating whether the OGNL SecurityManager was disabled
     * on initialization or not.
     * <p>
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#OGNL_SECURITY_MANAGER} using
     * the value {@link OgnlRuntime#OGNL_SM_FORCE_DISABLE_ON_INIT}.
     *
     * @return true if OGNL SecurityManager was disabled on initialization, false otherwise.
     * @since 3.1.25
     */
    public static boolean getDisableOgnlSecurityManagerOnInitValue() {
        return _disableOgnlSecurityManagerOnInit;
    }

    /**
     * Returns an indication as to whether the current state indicates the
     * JDK9+ (9 and later) access handler is being used / should be used.  This
     * is based on a combination of the detected Major Java Version and the
     * Java option flag {@link OgnlRuntime#USE_JDK9PLUS_ACCESS_HANDLER}.
     *
     * @return true if the JDK9 and later access handler is being used / should be used, false otherwise.
     * @since 3.1.25
     */
    public static boolean usingJDK9PlusAccessHandler() {
        return (_jdk9Plus && _useJDK9PlusAccessHandler);
    }

    /**
     * Returns the value of the flag indicating whether the old "first match" lookup for
     * getters/setters is in effect or not.
     * <p>
     * Note: Value is controlled by a Java option flag {@link OgnlRuntime#USE_FIRSTMATCH_GETSET_LOOKUP}.
     *
     * @return true if the old "first match" lookup is in effect, false otherwise.
     * @since 3.1.25
     */
    public static boolean getUseFirstMatchGetSetLookupValue() {
        return _useFirstMatchGetSetLookup;
    }

}
