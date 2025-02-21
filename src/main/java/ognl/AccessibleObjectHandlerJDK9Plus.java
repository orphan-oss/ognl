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
 * AccessibleObject.
 * <p>
 * If the JDK 9+ mechanism fails, this class will fall back to a standard pre-JDK 9 reflection mechanism.
 * Note:  That may cause "WARNING: Illegal reflective access" output to be generated to stdout/stderr.
 * <p>
 * For reference, this class draws on information from the following locations:
 * - Post about Illegal Reflective Access <a href="https://stackoverflow.com/questions/50251798/what-is-an-illegal-reflective-access">what is an illegal reflective access</a>
 * - Blog on Unsafe <a href="http://mishadoff.com/blog/java-magic-part-4-sun-dot-misc-dot-unsafe/">Java Magic. Part 4: sun.misc.Unsafe</a>
 * - Blog on Unsafe <a href="https://www.baeldung.com/java-unsafe">Guide to sun.misc.Unsafe</a>
 * - JEP about access to Unsafe being retained in JDK 9 <a href="https://openjdk.java.net/jeps/260">JEP 260: Encapsulate Most Internal APIs</a>
 * <p>
 * In addition to the above, inspiration was drawn from Gson: <a href="https://github.com/google/gson/pull/1218">PR 1218</a>,
 * <a href="https://github.com/google/gson/pull/1306">PR 1306</a>.
 * <p>
 * Appreciation and credit to the authors, contributors and commenters for the information contained in the preceding links.
 *
 * @since 3.1.24
 */
class AccessibleObjectHandlerJDK9Plus implements AccessibleObjectHandler {
    private static final Class<?> CLAZZ_UNSAFE = instantiateClazzUnsafe();
    private static final Object UNSAFE_INSTANCE = instantiateUnsafeInstance();
    private static final Method UNSAFE_OBJECT_FIELD_OFFSET_METHOD = instantiateUnsafeObjectFieldOffsetMethod();
    private static final Method UNSAFE_PUT_BOOLEAN_METHOD = instantiateUnsafePutBooleanMethod();
    private static final Field ACCESSIBLE_OBJECT_OVERRIDE_FIELD = instantiateAccessibleObjectOverrideField();
    private static final long ACCESSIBLE_OBJECT_OVERRIDE_FIELD_OFFSET = determineAccessibleObjectOverrideFieldOffset();

    /**
     * Private constructor
     */
    private AccessibleObjectHandlerJDK9Plus() {
    }

    /**
     * Package-accessible method to determine if a given class is Unsafe or a descendant
     * of Unsafe.
     *
     * @param clazz the Class upon which to perform the unsafe check.
     * @return true if parameter is Unsafe or a descendant, false otherwise
     */
    static boolean unsafeOrDescendant(final Class<?> clazz) {
        return (CLAZZ_UNSAFE != null && CLAZZ_UNSAFE.isAssignableFrom(clazz));
    }

    /**
     * Instantiate an instance of the Unsafe class.
     *
     * @return class if available, null otherwise
     */
    private static Class<?> instantiateClazzUnsafe() {
        return null;
    }

    /**
     * Instantiate an instance of Unsafe object.
     *
     * @return instance if available, null otherwise
     */
    private static Object instantiateUnsafeInstance() {
        Object unsafe;

        if (AccessibleObjectHandlerJDK9Plus.CLAZZ_UNSAFE != null) {
            Field field = null;
            try {
                field = AccessibleObjectHandlerJDK9Plus.CLAZZ_UNSAFE.getDeclaredField("theUnsafe");
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
     * @return method if available, null otherwise
     */
    private static Method instantiateUnsafeObjectFieldOffsetMethod() {
        Method method;

        if (AccessibleObjectHandlerJDK9Plus.CLAZZ_UNSAFE != null) {
            try {
                method = AccessibleObjectHandlerJDK9Plus.CLAZZ_UNSAFE.getMethod("objectFieldOffset", Field.class);
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
     * @return method if available, null otherwise
     */
    private static Method instantiateUnsafePutBooleanMethod() {
        Method method;

        if (AccessibleObjectHandlerJDK9Plus.CLAZZ_UNSAFE != null) {
            try {
                method = AccessibleObjectHandlerJDK9Plus.CLAZZ_UNSAFE.getMethod("putBoolean", Object.class, long.class, boolean.class);
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
     * Attempt to determine the AccessibleObject override field offset.
     *
     * @return field offset if available, -1 otherwise
     */
    private static long determineAccessibleObjectOverrideFieldOffset() {
        long offset = -1;

        if (ACCESSIBLE_OBJECT_OVERRIDE_FIELD != null && UNSAFE_OBJECT_FIELD_OFFSET_METHOD != null && UNSAFE_INSTANCE != null) {
            try {
                offset = (Long) UNSAFE_OBJECT_FIELD_OFFSET_METHOD.invoke(UNSAFE_INSTANCE, ACCESSIBLE_OBJECT_OVERRIDE_FIELD);
            } catch (Throwable t) {
                // Don't care (offset already -1)
            }
        }

        return offset;
    }

    /**
     * Package-level generator of an AccessibleObjectHandlerJDK9Plus instance.
     * <p>
     * Not intended for use outside of the package.
     * <p>
     * Note: An AccessibleObjectHandlerJDK9Plus will only be created if running on a
     * JDK9+ and the environment flag is set.  Otherwise this method will return
     * an AccessibleHandlerPreJDK9 instance instead,
     *
     * @return an AccessibleObjectHandler instance
     * @since 3.1.24
     */
    static AccessibleObjectHandler createHandler() {
        return new AccessibleObjectHandlerJDK9Plus();
    }

    /**
     * Utilize accessibility modification mechanism for JDK 9 (Java Major Version 9) and later.
     * Should that mechanism fail, attempt a standard pre-JDK9 accessibility modification.
     *
     * @param accessibleObject the AccessibleObject upon which to apply the flag.
     * @param flag             the new accessible flag value.
     */
    public void setAccessible(AccessibleObject accessibleObject, boolean flag) {
        boolean operationComplete = false;

        if (UNSAFE_INSTANCE != null && UNSAFE_PUT_BOOLEAN_METHOD != null && ACCESSIBLE_OBJECT_OVERRIDE_FIELD_OFFSET != -1) {
            try {
                UNSAFE_PUT_BOOLEAN_METHOD.invoke(UNSAFE_INSTANCE, accessibleObject, ACCESSIBLE_OBJECT_OVERRIDE_FIELD_OFFSET, flag);
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
