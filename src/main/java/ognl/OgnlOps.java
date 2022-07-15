/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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

import ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Enumeration;

/**
 * This is an abstract class with static methods that define the operations of OGNL.
 */
public abstract class OgnlOps implements NumericTypes {

    /**
     * Compares two objects for equality, even if it has to convert one of them to the other type.
     * If both objects are numeric they are converted to the widest type and compared. If one is
     * non-numeric and one is numeric the non-numeric is converted to double and compared to the
     * double numeric value. If both are non-numeric and Comparable and the types are compatible
     * (i.e. v1 is of the same or superclass of v2's type) they are compared with
     * Comparable.compareTo(). If both values are non-numeric and not Comparable or of incompatible
     * classes this will throw and IllegalArgumentException.
     *
     * @param v1 First value to compare
     * @param v2 second value to compare
     * @return integer describing the comparison between the two objects. A negative number
     * indicates that v1 &lt; v2. Positive indicates that v1 &gt; v2. Zero indicates v1 == v2.
     * @throws IllegalArgumentException if the objects are both non-numeric yet of incompatible types or do not implement
     *                                  Comparable.
     */
    public static int compareWithConversion(Object v1, Object v2) {
        int result;

        if (v1 == v2) {
            result = 0;
        } else {
            int t1 = getNumericType(v1), t2 = getNumericType(v2), type = getNumericType(t1, t2, true);

            switch (type) {
                case BIGINT:
                    result = bigIntValue(v1).compareTo(bigIntValue(v2));
                    break;

                case BIGDEC:
                    result = bigDecValue(v1).compareTo(bigDecValue(v2));
                    break;

                case NONNUMERIC:
                    if ((t1 == NONNUMERIC) && (t2 == NONNUMERIC)) {
                        if ((v1 instanceof Comparable) && v1.getClass().isAssignableFrom(v2.getClass())) {
                            result = ((Comparable) v1).compareTo(v2);
                            break;
                        } else if ((v1 instanceof Enum<?> && v2 instanceof Enum<?>) &&
                                (v1.getClass() == v2.getClass() || ((Enum) v1).getDeclaringClass() == ((Enum) v2).getDeclaringClass())) {
                            result = ((Enum) v1).compareTo((Enum) v2);
                            break;
                        } else {
                            throw new IllegalArgumentException("invalid comparison: " + v1.getClass().getName() + " and "
                                    + v2.getClass().getName());
                        }
                    }
                    // else fall through
                case FLOAT:
                case DOUBLE:
                    double dv1 = doubleValue(v1),
                            dv2 = doubleValue(v2);

                    return (dv1 == dv2) ? 0 : ((dv1 < dv2) ? -1 : 1);

                default:
                    long lv1 = longValue(v1),
                            lv2 = longValue(v2);

                    return (lv1 == lv2) ? 0 : ((lv1 < lv2) ? -1 : 1);
            }
        }
        return result;
    }

    /**
     * Returns true if object1 is equal to object2 in either the sense that they are the same object
     * or, if both are non-null if they are equal in the <CODE>equals()</CODE> sense.
     *
     * @param object1 First object to compare
     * @param object2 Second object to compare
     * @return true if v1 == v2
     */
    public static boolean isEqual(Object object1, Object object2) {
        boolean result = false;

        if (object1 == object2) {
            result = true;
        } else if (object1 != null && object2 != null) {
            if (object1.getClass().isArray()) {
                if (object2.getClass().isArray() && (object2.getClass() == object1.getClass())) {
                    result = (Array.getLength(object1) == Array.getLength(object2));
                    if (result) {
                        for (int i = 0, icount = Array.getLength(object1); result && (i < icount); i++) {
                            result = isEqual(Array.get(object1, i), Array.get(object2, i));
                        }
                    }
                }
            } else {
                int t1 = getNumericType(object1);
                int t2 = getNumericType(object2);

                // compare non-comparable non-numeric types by equals only
                if (t1 == NONNUMERIC && t2 == NONNUMERIC && (!(object1 instanceof Comparable) || !(object2 instanceof Comparable))) {
                    result = object1.equals(object2);
                } else {
                    result = compareWithConversion(object1, object2) == 0;
                }
            }
        }
        return result;
    }

    public static boolean booleanValue(boolean value) {
        return value;
    }

    public static boolean booleanValue(int value) {
        return value > 0;
    }

    public static boolean booleanValue(float value) {
        return value > 0;
    }

    public static boolean booleanValue(long value) {
        return value > 0;
    }

    public static boolean booleanValue(double value) {
        return value > 0;
    }

    /**
     * Evaluates the given object as a boolean: if it is a Boolean object, it's easy; if it's a
     * Number or a Character, returns true for non-zero objects; and otherwise returns true for
     * non-null objects.
     *
     * @param value an object to interpret as a boolean
     * @return the boolean value implied by the given object
     */
    public static boolean booleanValue(Object value) {
        if (value == null) {
            return false;
        }

        Class<?> c = value.getClass();

        if (c == Boolean.class) {
            return (Boolean) value;
        }
        if (c == String.class) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        if (c == Character.class) {
            return (Character) value != 0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }

        return true; // non-null
    }

    /**
     * Evaluates the given object as a long integer.
     *
     * @param value an object to interpret as a long integer
     * @return the long integer value implied by the given object
     * @throws NumberFormatException if the given object can't be understood as a long integer
     */
    public static long longValue(Object value)
            throws NumberFormatException {
        if (value == null) return 0L;
        Class<?> c = value.getClass();
        if (c.getSuperclass() == Number.class) return ((Number) value).longValue();
        if (c == Boolean.class) return (Boolean) value ? 1 : 0;
        if (c == Character.class) return (Character) value;
        return Long.parseLong(stringValue(value, true));
    }

    /**
     * Evaluates the given object as a double-precision floating-point number.
     *
     * @param value an object to interpret as a double
     * @return the double value implied by the given object
     * @throws NumberFormatException if the given object can't be understood as a double
     */
    public static double doubleValue(Object value)
            throws NumberFormatException {
        if (value == null) return 0.0;
        Class<?> c = value.getClass();
        if (c.getSuperclass() == Number.class) return ((Number) value).doubleValue();
        if (c == Boolean.class) return (Boolean) value ? 1 : 0;
        if (c == Character.class) return (Character) value;
        String s = stringValue(value, true);

        return (s.length() == 0) ? 0.0 : Double.parseDouble(s);
    }

    /**
     * Evaluates the given object as a BigInteger.
     *
     * @param value an object to interpret as a BigInteger
     * @return the BigInteger value implied by the given object
     * @throws NumberFormatException if the given object can't be understood as a BigInteger
     */
    public static BigInteger bigIntValue(Object value)
            throws NumberFormatException {
        if (value == null) return BigInteger.valueOf(0L);
        Class<?> c = value.getClass();
        if (c == BigInteger.class) return (BigInteger) value;
        if (c == BigDecimal.class) return ((BigDecimal) value).toBigInteger();
        if (c.getSuperclass() == Number.class) return BigInteger.valueOf(((Number) value).longValue());
        if (c == Boolean.class) return BigInteger.valueOf((Boolean) value ? 1 : 0);
        if (c == Character.class) return BigInteger.valueOf((Character) value);
        return new BigInteger(stringValue(value, true));
    }

    /**
     * Evaluates the given object as a BigDecimal.
     *
     * @param value an object to interpret as a BigDecimal
     * @return the BigDecimal value implied by the given object
     * @throws NumberFormatException if the given object can't be understood as a BigDecimal
     */
    public static BigDecimal bigDecValue(Object value)
            throws NumberFormatException {
        if (value == null) return BigDecimal.valueOf(0L);
        Class<?> c = value.getClass();
        if (c == BigDecimal.class) return (BigDecimal) value;
        if (c == BigInteger.class) return new BigDecimal((BigInteger) value);
        if (c == Boolean.class) return BigDecimal.valueOf((Boolean) value ? 1 : 0);
        if (c == Character.class) return BigDecimal.valueOf((Character) value);
        return new BigDecimal(stringValue(value, true));
    }

    /**
     * Evaluates the given object as a String and trims it if the trim flag is true.
     *
     * @param value an object to interpret as a String
     * @param trim  true if result should be whitespace-trimmed, false otherwise.
     * @return the String value implied by the given object as returned by the toString() method, or
     * "null" if the object is null.
     */
    public static String stringValue(Object value, boolean trim) {
        String result;

        if (value == null) {
            result = OgnlRuntime.NULL_STRING;
        } else {
            result = value.toString();
            if (trim) {
                result = result.trim();
            }
        }
        return result;
    }

    /**
     * Evaluates the given object as a String.
     *
     * @param value an object to interpret as a String
     * @return the String value implied by the given object as returned by the toString() method, or
     * "null" if the object is null.
     */
    public static String stringValue(Object value) {
        return stringValue(value, false);
    }

    /**
     * Returns a constant from the NumericTypes interface that represents the numeric type of the
     * given object.
     *
     * @param value an object that needs to be interpreted as a number
     * @return the appropriate constant from the NumericTypes interface
     */
    public static int getNumericType(Object value) {
        if (value != null) {
            Class<?> c = value.getClass();
            if (c == Integer.class) return INT;
            if (c == Double.class) return DOUBLE;
            if (c == Boolean.class) return BOOL;
            if (c == Byte.class) return BYTE;
            if (c == Character.class) return CHAR;
            if (c == Short.class) return SHORT;
            if (c == Long.class) return LONG;
            if (c == Float.class) return FLOAT;
            if (c == BigInteger.class) return BIGINT;
            if (c == BigDecimal.class) return BIGDEC;
        }
        return NONNUMERIC;
    }

    public static Object toArray(char value, Class<?> toType) {
        return toArray(new Character(value), toType);
    }

    public static Object toArray(byte value, Class<?> toType) {
        return toArray(new Byte(value), toType);
    }

    public static Object toArray(int value, Class<?> toType) {
        return toArray(new Integer(value), toType);
    }

    public static Object toArray(long value, Class<?> toType) {
        return toArray(new Long(value), toType);
    }

    public static Object toArray(float value, Class<?> toType) {
        return toArray(new Float(value), toType);
    }

    public static Object toArray(double value, Class<?> toType) {
        return toArray(new Double(value), toType);
    }

    public static Object toArray(boolean value, Class<?> toType) {
        return toArray(Boolean.valueOf(value), toType);
    }

    public static Object convertValue(char value, Class<?> toType) {
        return convertValue(new Character(value), toType);
    }

    public static Object convertValue(byte value, Class<?> toType) {
        return convertValue(new Byte(value), toType);
    }

    public static Object convertValue(int value, Class<?> toType) {
        return convertValue(new Integer(value), toType);
    }

    public static Object convertValue(long value, Class<?> toType) {
        return convertValue(new Long(value), toType);
    }

    public static Object convertValue(float value, Class<?> toType) {
        return convertValue(new Float(value), toType);
    }

    public static Object convertValue(double value, Class<?> toType) {
        return convertValue(new Double(value), toType);
    }

    public static Object convertValue(boolean value, Class<?> toType) {
        return convertValue(Boolean.valueOf(value), toType);
    }

    public static Object convertValue(char value, Class<?> toType, boolean preventNull) {
        return convertValue(new Character(value), toType, preventNull);
    }

    public static Object convertValue(byte value, Class<?> toType, boolean preventNull) {
        return convertValue(new Byte(value), toType, preventNull);
    }

    public static Object convertValue(int value, Class<?> toType, boolean preventNull) {
        return convertValue(new Integer(value), toType, preventNull);
    }

    public static Object convertValue(long value, Class<?> toType, boolean preventNull) {
        return convertValue(new Long(value), toType, preventNull);
    }

    public static Object convertValue(float value, Class<?> toType, boolean preventNull) {
        return convertValue(new Float(value), toType, preventNull);
    }

    public static Object convertValue(double value, Class<?> toType, boolean preventNull) {
        return convertValue(new Double(value), toType, preventNull);
    }

    public static Object convertValue(boolean value, Class<?> toType, boolean preventNull) {
        return convertValue(Boolean.valueOf(value), toType, preventNull);
    }

    public static Object toArray(char value, Class<?> toType, boolean preventNull) {
        return toArray(new Character(value), toType, preventNull);
    }

    public static Object toArray(byte value, Class<?> toType, boolean preventNull) {
        return toArray(new Byte(value), toType, preventNull);
    }

    public static Object toArray(int value, Class<?> toType, boolean preventNull) {
        return toArray(new Integer(value), toType, preventNull);
    }

    public static Object toArray(long value, Class<?> toType, boolean preventNull) {
        return toArray(new Long(value), toType, preventNull);
    }

    public static Object toArray(float value, Class<?> toType, boolean preventNull) {
        return toArray(new Float(value), toType, preventNull);
    }

    public static Object toArray(double value, Class<?> toType, boolean preventNull) {
        return toArray(new Double(value), toType, preventNull);
    }

    public static Object toArray(boolean value, Class<?> toType, boolean preventNull) {
        return toArray(Boolean.valueOf(value), toType, preventNull);
    }

    /**
     * Returns the value converted numerically to the given class type This method also detects when
     * arrays are being converted and converts the components of one array to the type of the other.
     *
     * @param value  an object to be converted to the given type
     * @param toType class type to be converted to
     * @return converted value of the type given, or value if the value cannot be converted to the
     * given type.
     */
    public static Object convertValue(Object value, Class<?> toType) {
        return convertValue(value, toType, false);
    }

    public static Object toArray(Object value, Class<?> toType) {
        return toArray(value, toType, false);
    }

    public static Object toArray(Object value, Class<?> toType, boolean preventNulls) {
        if (value == null)
            return null;

        Object result;

        if (value.getClass().isArray() && toType.isAssignableFrom(value.getClass().getComponentType()))
            return value;

        if (!value.getClass().isArray()) {

            if (toType == Character.TYPE)
                return stringValue(value).toCharArray();

            if (value instanceof Collection)
                return ((Collection<?>) value).toArray((Object[]) Array.newInstance(toType, 0));

            Object arr = Array.newInstance(toType, 1);
            Array.set(arr, 0, convertValue(value, toType, preventNulls));

            return arr;
        }

        result = Array.newInstance(toType, Array.getLength(value));
        for (int i = 0, icount = Array.getLength(value); i < icount; i++) {
            Array.set(result, i, convertValue(Array.get(value, i), toType));
        }

        return result;
    }

    public static Object convertValue(Object value, Class<?> toType, boolean preventNulls) {
        Object result = null;

        if (value != null && toType.isAssignableFrom(value.getClass()))
            return value;

        if (value != null) {
            /* If array -> array then convert components of array individually */
            if (value.getClass().isArray() && toType.isArray()) {
                Class<?> componentType = toType.getComponentType();

                result = Array.newInstance(componentType, Array.getLength(value));
                for (int i = 0, icount = Array.getLength(value); i < icount; i++) {
                    Array.set(result, i, convertValue(Array.get(value, i), componentType));
                }
            } else if (value.getClass().isArray()) {

                return convertValue(Array.get(value, 0), toType);
            } else if (toType.isArray()) {

                if (toType.getComponentType() == Character.TYPE) {

                    result = stringValue(value).toCharArray();
                } else if (toType.getComponentType() == Object.class) {
                    if (value instanceof Collection) {
                        Collection<?> vc = (Collection<?>) value;
                        return vc.toArray(new Object[0]);
                    } else
                        return new Object[]{value};
                }
            } else {
                if ((toType == Integer.class) || (toType == Integer.TYPE)) {
                    result = (int) longValue(value);
                }
                if ((toType == Double.class) || (toType == Double.TYPE)) result = doubleValue(value);
                if ((toType == Boolean.class) || (toType == Boolean.TYPE))
                    result = booleanValue(value) ? Boolean.TRUE : Boolean.FALSE;
                if ((toType == Byte.class) || (toType == Byte.TYPE)) result = (byte) longValue(value);
                if ((toType == Character.class) || (toType == Character.TYPE))
                    result = (char) longValue(value);
                if ((toType == Short.class) || (toType == Short.TYPE)) result = (short) longValue(value);
                if ((toType == Long.class) || (toType == Long.TYPE)) result = longValue(value);
                if ((toType == Float.class) || (toType == Float.TYPE)) result = (float) doubleValue(value);
                if (toType == BigInteger.class) result = bigIntValue(value);
                if (toType == BigDecimal.class) result = bigDecValue(value);
                if (toType == String.class) result = stringValue(value);
            }
        } else {
            if (toType.isPrimitive()) {
                result = OgnlRuntime.getPrimitiveDefaultValue(toType);
            } else if (preventNulls && toType == Boolean.class) {
                result = Boolean.FALSE;
            } else if (preventNulls && Number.class.isAssignableFrom(toType)) {
                result = OgnlRuntime.getNumericDefaultValue(toType);
            }
        }

        if (result == null && preventNulls)
            return value;

        if (value != null && result == null) {

            throw new IllegalArgumentException("Unable to convert type " + value.getClass().getName() + " of " + value + " to type of " + toType.getName());
        }

        return result;
    }

    /**
     * Converts the specified value to a primitive integer value.
     *
     * <ul>
     *  <li>Null values will cause a -1 to be returned.</li>
     *  <li>{@link Number} instances have their intValue() methods invoked.</li>
     *  <li>All other types result in calling Integer.parseInt(value.toString());</li>
     * </ul>
     *
     * @param value The object to get the value of.
     * @return A valid integer.
     */
    public static int getIntValue(Object value) {
        try {
            if (value == null)
                return -1;

            if (value instanceof Number) {

                return ((Number) value).intValue();
            }

            String str = value instanceof String ? (String) value : value.toString();

            return Integer.parseInt(str);
        } catch (Throwable t) {
            throw new RuntimeException("Error converting " + value + " to integer:", t);
        }
    }

    /**
     * Returns the constant from the NumericTypes interface that best expresses the type of a
     * numeric operation on the two given objects.
     *
     * @param v1 one argument to a numeric operator
     * @param v2 the other argument
     * @return the appropriate constant from the NumericTypes interface
     */
    public static int getNumericType(Object v1, Object v2) {
        return getNumericType(v1, v2, false);
    }

    /**
     * Returns the constant from the NumericTypes interface that best expresses the type of an
     * operation, which can be either numeric or not, on the two given types.
     *
     * @param t1              type of one argument to an operator
     * @param t2              type of the other argument
     * @param canBeNonNumeric whether the operator can be interpreted as non-numeric
     * @return the appropriate constant from the NumericTypes interface
     */
    public static int getNumericType(int t1, int t2, boolean canBeNonNumeric) {
        if (t1 == t2) return t1;

        if (canBeNonNumeric && (t1 == NONNUMERIC || t2 == NONNUMERIC || t1 == CHAR || t2 == CHAR)) return NONNUMERIC;

        if (t1 == NONNUMERIC) t1 = DOUBLE; // Try to interpret strings as doubles...
        if (t2 == NONNUMERIC) t2 = DOUBLE; // Try to interpret strings as doubles...

        if (t1 >= MIN_REAL_TYPE) {
            if (t2 >= MIN_REAL_TYPE) return Math.max(t1, t2);
            if (t2 < INT) return t1;
            if (t2 == BIGINT) return BIGDEC;
            return Math.max(DOUBLE, t1);
        } else if (t2 >= MIN_REAL_TYPE) {
            if (t1 < INT) return t2;
            if (t1 == BIGINT) return BIGDEC;
            return Math.max(DOUBLE, t2);
        } else return Math.max(t1, t2);
    }

    /**
     * Returns the constant from the NumericTypes interface that best expresses the type of an
     * operation, which can be either numeric or not, on the two given objects.
     *
     * @param v1              one argument to an operator
     * @param v2              the other argument
     * @param canBeNonNumeric whether the operator can be interpreted as non-numeric
     * @return the appropriate constant from the NumericTypes interface
     */
    public static int getNumericType(Object v1, Object v2, boolean canBeNonNumeric) {
        return getNumericType(getNumericType(v1), getNumericType(v2), canBeNonNumeric);
    }

    /**
     * Returns a new Number object of an appropriate type to hold the given integer value. The type
     * of the returned object is consistent with the given type argument, which is a constant from
     * the NumericTypes interface.
     *
     * @param type  the nominal numeric type of the result, a constant from the NumericTypes interface
     * @param value the integer value to convert to a Number object
     * @return a Number object with the given value, of type implied by the type argument
     */
    public static Number newInteger(int type, long value) {
        switch (type) {
            case BOOL:
            case CHAR:
            case INT:
                return (int) value;
            case FLOAT:
                if ((long) (float) value == value) {
                    return (float) value;
                }
                // else fall through:
            case DOUBLE:
                if ((long) (double) value == value) {
                    return (double) value;
                }
                // else fall through:
            case LONG:
                return value;
            case BYTE:
                return (byte) value;

            case SHORT:
                return (short) value;

            default:
                return BigInteger.valueOf(value);
        }
    }

    /**
     * Returns a new Number object of an appropriate type to hold the given real value. The type of
     * the returned object is always either Float or Double, and is only Float if the given type tag
     * (a constant from the NumericTypes interface) is FLOAT.
     *
     * @param type  the nominal numeric type of the result, a constant from the NumericTypes interface
     * @param value the real value to convert to a Number object
     * @return a Number object with the given value, of type implied by the type argument
     */
    public static Number newReal(int type, double value) {
        if (type == FLOAT) return (float) value;
        return value;
    }

    public static Object binaryOr(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        if (type == BIGINT || type == BIGDEC) return bigIntValue(v1).or(bigIntValue(v2));
        return newInteger(type, longValue(v1) | longValue(v2));
    }

    public static Object binaryXor(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        if (type == BIGINT || type == BIGDEC) return bigIntValue(v1).xor(bigIntValue(v2));
        return newInteger(type, longValue(v1) ^ longValue(v2));
    }

    public static Object binaryAnd(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        if (type == BIGINT || type == BIGDEC) return bigIntValue(v1).and(bigIntValue(v2));
        return newInteger(type, longValue(v1) & longValue(v2));
    }

    public static boolean equal(Object v1, Object v2) {
        if (v1 == null) return v2 == null;
        return v1 == v2 || isEqual(v1, v2);
    }

    public static boolean less(Object v1, Object v2) {
        return compareWithConversion(v1, v2) < 0;
    }

    public static boolean greater(Object v1, Object v2) {
        return compareWithConversion(v1, v2) > 0;
    }

    public static boolean in(Object v1, Object v2)
            throws OgnlException {
        if (v2 == null) // A null collection is always treated as empty
            return false;

        ElementsAccessor elementsAccessor = OgnlRuntime.getElementsAccessor(OgnlRuntime.getTargetClass(v2));

        for (Enumeration<?> e = elementsAccessor.getElements(v2); e.hasMoreElements(); ) {
            Object o = e.nextElement();

            if (equal(v1, o))
                return true;
        }

        return false;
    }

    public static Object shiftLeft(Object v1, Object v2) {
        int type = getNumericType(v1);
        if (type == BIGINT || type == BIGDEC) return bigIntValue(v1).shiftLeft((int) longValue(v2));
        return newInteger(type, longValue(v1) << (int) longValue(v2));
    }

    public static Object shiftRight(Object v1, Object v2) {
        int type = getNumericType(v1);
        if (type == BIGINT || type == BIGDEC) return bigIntValue(v1).shiftRight((int) longValue(v2));
        return newInteger(type, longValue(v1) >> (int) longValue(v2));
    }

    public static Object unsignedShiftRight(Object v1, Object v2) {
        int type = getNumericType(v1);
        if (type == BIGINT || type == BIGDEC) return bigIntValue(v1).shiftRight((int) longValue(v2));
        if (type <= INT) return newInteger(INT, ((int) longValue(v1)) >>> (int) longValue(v2));
        return newInteger(type, longValue(v1) >>> (int) longValue(v2));
    }

    public static Object add(Object v1, Object v2) {
        int type = getNumericType(v1, v2, true);
        switch (type) {
            case BIGINT:
                return bigIntValue(v1).add(bigIntValue(v2));
            case BIGDEC:
                return bigDecValue(v1).add(bigDecValue(v2));
            case FLOAT:
            case DOUBLE:
                return newReal(type, doubleValue(v1) + doubleValue(v2));
            case NONNUMERIC:
                int t1 = getNumericType(v1),
                        t2 = getNumericType(v2);

                if (((t1 != NONNUMERIC) && (v2 == null)) || ((t2 != NONNUMERIC) && (v1 == null))) {
                    throw new NullPointerException("Can't add values " + v1 + " , " + v2);
                }

                return stringValue(v1) + stringValue(v2);
            default:
                return newInteger(type, longValue(v1) + longValue(v2));
        }
    }

    public static Object subtract(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        switch (type) {
            case BIGINT:
                return bigIntValue(v1).subtract(bigIntValue(v2));
            case BIGDEC:
                return bigDecValue(v1).subtract(bigDecValue(v2));
            case FLOAT:
            case DOUBLE:
                return newReal(type, doubleValue(v1) - doubleValue(v2));
            default:
                return newInteger(type, longValue(v1) - longValue(v2));
        }
    }

    public static Object multiply(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        switch (type) {
            case BIGINT:
                return bigIntValue(v1).multiply(bigIntValue(v2));
            case BIGDEC:
                return bigDecValue(v1).multiply(bigDecValue(v2));
            case FLOAT:
            case DOUBLE:
                return newReal(type, doubleValue(v1) * doubleValue(v2));
            default:
                return newInteger(type, longValue(v1) * longValue(v2));
        }
    }

    public static Object divide(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        switch (type) {
            case BIGINT:
                return bigIntValue(v1).divide(bigIntValue(v2));
            case BIGDEC:
                return bigDecValue(v1).divide(bigDecValue(v2), RoundingMode.HALF_EVEN);
            case FLOAT:
            case DOUBLE:
                return newReal(type, doubleValue(v1) / doubleValue(v2));
            default:
                return newInteger(type, longValue(v1) / longValue(v2));
        }
    }

    public static Object remainder(Object v1, Object v2) {
        int type = getNumericType(v1, v2);
        switch (type) {
            case BIGDEC:
            case BIGINT:
                return bigIntValue(v1).remainder(bigIntValue(v2));
            default:
                return newInteger(type, longValue(v1) % longValue(v2));
        }
    }

    public static Object negate(Object value) {
        int type = getNumericType(value);
        switch (type) {
            case BIGINT:
                return bigIntValue(value).negate();
            case BIGDEC:
                return bigDecValue(value).negate();
            case FLOAT:
            case DOUBLE:
                return newReal(type, -doubleValue(value));
            default:
                return newInteger(type, -longValue(value));
        }
    }

    public static Object bitNegate(Object value) {
        int type = getNumericType(value);
        switch (type) {
            case BIGDEC:
            case BIGINT:
                return bigIntValue(value).not();
            default:
                return newInteger(type, ~longValue(value));
        }
    }

    public static String getEscapeString(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0, icount = value.length(); i < icount; i++) {
            result.append(getEscapedChar(value.charAt(i)));
        }
        return new String(result);
    }

    public static String getEscapedChar(char ch) {
        String result;

        switch (ch) {
            case '\b':
                result = "\b";
                break;
            case '\t':
                result = "\\t";
                break;
            case '\n':
                result = "\\n";
                break;
            case '\f':
                result = "\\f";
                break;
            case '\r':
                result = "\\r";
                break;
            case '\"':
                result = "\\\"";
                break;
            case '\'':
                result = "\\\'";
                break;
            case '\\':
                result = "\\\\";
                break;
            default:
                if (Character.isISOControl(ch)) {

                    String hc = Integer.toString(ch, 16);
                    int hcl = hc.length();

                    result = "\\u";
                    if (hcl < 4) {
                        if (hcl == 3) {
                            result = result + "0";
                        } else {
                            if (hcl == 2) {
                                result = result + "00";
                            } else {
                                result = result + "000";
                            }
                        }
                    }

                    result = result + hc;
                } else {
                    result = ch + "";
                }
                break;
        }
        return result;
    }

    public static Object returnValue(Object ignore, Object returnValue) {
        return returnValue;
    }

    /**
     * Utility method that converts incoming exceptions to {@link RuntimeException}
     * instances - or casts them if they already are.
     *
     * @param t The exception to cast.
     * @return The exception cast to a {@link RuntimeException}.
     */
    public static RuntimeException castToRuntime(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;

        if (t instanceof OgnlException)
            throw new UnsupportedCompilationException("Error evluating expression: " + t.getMessage(), t);

        return new RuntimeException(t);
    }
}
