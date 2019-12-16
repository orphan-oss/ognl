/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * and/or LICENSE file distributed with this work for additional
 * information regarding copyright ownership.  The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ognl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utilizes a JDK 9 and later mechanism for changing the accessibility level of a given
 *   AccessibleObject.
 *
 * If the JDK 9+ mechanism fails, this class will fall back to a standard pre-JDK 9 reflection mechanism.
 *   Note:  That may cause "WARNING: Illegal reflective access" output to be generated to stdout/stderr.
 *
 * For reference, this class draws on information from the following locations:
 *   - Post about Illegal Reflective Access <a href="https://stackoverflow.com/questions/50251798/what-is-an-illegal-reflective-access">what is an illegal reflective access</a>
 *   - Blog on Unsafe <a href="http://mishadoff.com/blog/java-magic-part-4-sun-dot-misc-dot-unsafe/">Java Magic. Part 4: sun.misc.Unsafe</a>
 *   - Blog on Unsafe <a href="https://www.baeldung.com/java-unsafe">Guide to sun.misc.Unsafe</a>
 *   - JEP about access to Unsafe being retained in JDK 9 <a href="https://openjdk.java.net/jeps/260">JEP 260: Encapsulate Most Internal APIs</a>
 *
 * In addition to the above, inspiration was drawn from Gson: <a href="https://github.com/google/gson/pull/1218">PR 1218</a>,
 *   <a href="https://github.com/google/gson/pull/1306">PR 1306</a>.
 *
 * Appreciation and credit to the authors, contributors and commenters for the information contained in the preceding links.
 *
 * @since 3.1.24
 */
class AccessibleObjectHandlerJDK9Plus implements AccessibleObjectHandler
{
    private static final Class _clazzUnsafe = instantiateClazzUnsafe();
    private static final Object _unsafeInstance = instantiateUnsafeInstance(_clazzUnsafe);
    private static final Method _unsafeObjectFieldOffsetMethod = instantiateUnsafeObjectFieldOffsetMethod(_clazzUnsafe);
    private static final Method _unsafePutBooleanMethod = instantiateUnsafePutBooleanMethod(_clazzUnsafe);
    private static final Field _accessibleObjectOverrideField = instantiateAccessibleObjectOverrideField();
    private static final long _accessibleObjectOverrideFieldOffset = determineAccessibleObjectOverrideFieldOffset();

    /**
     * Private constructor
     */
    private AccessibleObjectHandlerJDK9Plus() {}

    /**
     * Package-accessible method to determine if a given class is Unsafe or a descendant
     *   of Unsafe.
     *
     * @param clazz the Class upon which to perform the unsafe check.
     * @return true if parameter is Unsafe or a descendant, false otherwise
     */
    static boolean unsafeOrDescendant(final Class clazz) {
        return (_clazzUnsafe != null ? _clazzUnsafe.isAssignableFrom(clazz) : false);
    }

    /**
     * Instantiate an instance of the Unsafe class.
     *
     * @return class if available, null otherwise
     */
    private static Class instantiateClazzUnsafe() {
        Class clazz;

        try {
            clazz = Class.forName("sun.misc.Unsafe");
        } catch (Throwable t) {
            clazz = null;
        }

        return clazz;
    }

    /**
     * Instantiate an instance of Unsafe object.
     *
     * @param clazz (expected to be an Unsafe instance)
     * @return instance if available, null otherwise
     */
    private static Object instantiateUnsafeInstance(Class clazz) {
        Object unsafe;

        if (clazz != null) {
            Field field = null;
            try {
                field = clazz.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                unsafe = field.get(null);
            } catch (Throwable t) {
                unsafe = null;
            } finally {
                if (field != null) {
                    try {
                        field.setAccessible(false);
                    } catch (Throwable t) {
                        // Don't care
                    }
                }
            }
        } else {
            unsafe = null;
        }

        return unsafe;
    }

    /**
     * Instantiate an Unsafe.objectFieldOffset() method instance.
     *
     * @param clazz (expected to be an Unsafe instance)
     * @return method if available, null otherwise
     */
    private static Method instantiateUnsafeObjectFieldOffsetMethod(Class clazz) {
        Method method;

        if (clazz != null) {
            try {
                method = clazz.getMethod("objectFieldOffset", Field.class);
            } catch (Throwable t) {
                method = null;
            }
        } else {
            method = null;
        }

        return method;
    }

    /**
     * Instantiate an Unsafe.putBoolean() method instance.
     *
     * @param clazz (expected to be an Unsafe instance)
     * @return method if available, null otherwise
     */
    private static Method instantiateUnsafePutBooleanMethod(Class clazz) {
        Method method;

        if (clazz != null) {
            try {
                method = clazz.getMethod("putBoolean", Object.class, long.class, boolean.class);
            } catch (Throwable t) {
                method = null;
            }
        } else {
            method = null;
        }

        return method;
    }

    /**
     * Instantiate an AccessibleObject override field instance.
     *
     * @return field if available, null otherwise
     */
    private static Field instantiateAccessibleObjectOverrideField() {
        Field field;

        try {
            field = AccessibleObject.class.getDeclaredField("override");
        } catch (Throwable t) {
            field = null;
        }

        return field;
    }

    /**
     * Attempt to determined the AccessibleObject override field offset.
     *
     * @return field offset if available, -1 otherwise
     */
    private static long determineAccessibleObjectOverrideFieldOffset() {
        long offset = -1;

        if (_accessibleObjectOverrideField != null && _unsafeObjectFieldOffsetMethod != null && _unsafeInstance != null) {
            try {
                offset = (Long) _unsafeObjectFieldOffsetMethod.invoke(_unsafeInstance, _accessibleObjectOverrideField);
            } catch (Throwable t) {
                // Don't care (offset already -1)
            }
        }

        return offset;
    }

    /**
     * Package-level generator of an AccessibleObjectHandlerJDK9Plus instance.
     *
     * Not intended for use outside of the package.
     *
     * Note: An AccessibleObjectHandlerJDK9Plus will only be created if running on a
     *   JDK9+ and the environment flag is set.  Otherwise this method will return
     *   an AccessibleHandlerPreJDK9 instance instead,
     *
     * @return an AccessibleObjectHandler instance
     *
     * @since 3.1.24
     */
    static AccessibleObjectHandler createHandler() {
        if (OgnlRuntime.usingJDK9PlusAccessHandler()){
            return new AccessibleObjectHandlerJDK9Plus();
        } else {
            return AccessibleObjectHandlerPreJDK9.createHandler();
        }
    }

    /**
     * Utilize accessibility modification mechanism for JDK 9 (Java Major Version 9) and later.
     *   Should that mechanism fail, attempt a standard pre-JDK9 accessibility modification.
     *
     * @param accessibleObject the AccessibleObject upon which to apply the flag.
     * @param flag the new accessible flag value.
     */
    public void setAccessible(AccessibleObject accessibleObject, boolean flag) {
        boolean operationComplete = false;

        if (_unsafeInstance != null && _unsafePutBooleanMethod != null && _accessibleObjectOverrideFieldOffset != -1) {
            try {
                _unsafePutBooleanMethod.invoke(_unsafeInstance, accessibleObject, _accessibleObjectOverrideFieldOffset, flag);
                operationComplete = true;
            } catch (Throwable t) {
                // Don't care (operationComplete already false)
            }
        }
        if (!operationComplete) {
            // Fallback to standard reflection if Unsafe processing fails
            accessibleObject.setAccessible(flag);
        }
    }

}
