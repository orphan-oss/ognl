//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
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
     * Package-accessible constructor
     */
    AccessibleObjectHandlerJDK9Plus() {}

    /**
     * Package-accessible method to determine if a given class is Unsafe or a descendant
     *   of Unsafe.
     *
     * @param clazz
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
     * Utilize accessibility modification mechanism for JDK 9 (Java Major Version 9) and later.
     *   Should that mechanism fail, attempt a standard pre-JDK9 accessibility modification.
     *
     * @param accessibleObject
     * @param flag
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
