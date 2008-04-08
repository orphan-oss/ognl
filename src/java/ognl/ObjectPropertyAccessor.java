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
import ognl.enhance.UnsupportedCompilationException;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that uses reflection on the target object's class to find a
 * field or a pair of set/get methods with the given property name.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ObjectPropertyAccessor implements PropertyAccessor {

    /**
     * Returns OgnlRuntime.NotFound if the property does not exist.
     */
    public Object getPossibleProperty(Map context, Object target, String name)
            throws OgnlException
    {
        Object result;
        OgnlContext ognlContext = (OgnlContext) context;

        try {
            if ((result = OgnlRuntime.getMethodValue(ognlContext, target, name, true)) == OgnlRuntime.NotFound)
            {
                result = OgnlRuntime.getFieldValue(ognlContext, target, name, true);
            }
        } catch (IntrospectionException ex) {
            throw new OgnlException(name, ex);
        } catch (OgnlException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OgnlException(name, ex);
        }

        return result;
    }

    /**
     * Returns OgnlRuntime.NotFound if the property does not exist.
     */
    public Object setPossibleProperty(Map context, Object target, String name, Object value)
            throws OgnlException
    {
        Object result = null;
        OgnlContext ognlContext = (OgnlContext) context;

        try {
            if (!OgnlRuntime.setMethodValue(ognlContext, target, name, value, true))
            {
                result = OgnlRuntime.setFieldValue(ognlContext, target, name, value) ? null : OgnlRuntime.NotFound;
            }

            if (result == OgnlRuntime.NotFound)
            {
                Method m = OgnlRuntime.getWriteMethod(target.getClass(), name);
                if (m != null)
                {
                    result = m.invoke(target, new Object[] { value});
                }
            }
        } catch (IntrospectionException ex) {
            throw new OgnlException(name, ex);
        } catch (OgnlException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OgnlException(name, ex);
        }

        return result;
    }

    public boolean hasGetProperty(OgnlContext context, Object target, Object oname)
            throws OgnlException
    {
        try {
            return OgnlRuntime.hasGetProperty(context, target, oname);
        } catch (IntrospectionException ex) {
            throw new OgnlException("checking if " + target + " has gettable property " + oname, ex);
        }
    }

    public boolean hasGetProperty(Map context, Object target, Object oname)
            throws OgnlException
    {
        return hasGetProperty((OgnlContext) context, target, oname);
    }

    public boolean hasSetProperty(OgnlContext context, Object target, Object oname)
            throws OgnlException
    {
        try {
            return OgnlRuntime.hasSetProperty(context, target, oname);
        } catch (IntrospectionException ex) {
            throw new OgnlException("checking if " + target + " has settable property " + oname, ex);
        }
    }

    public boolean hasSetProperty(Map context, Object target, Object oname)
            throws OgnlException
    {
        return hasSetProperty((OgnlContext) context, target, oname);
    }

    public Object getProperty(Map context, Object target, Object oname)
            throws OgnlException
    {
        Object result = null;
        String name = oname.toString();

        result = getPossibleProperty(context, target, name);

        if (result == OgnlRuntime.NotFound)
        {
            throw new NoSuchPropertyException(target, name);
        }

        return result;
    }

    public void setProperty(Map context, Object target, Object oname, Object value)
            throws OgnlException
    {
        String name = oname.toString();

        Object result = setPossibleProperty(context, target, name, value);

        if (result == OgnlRuntime.NotFound)
        {
            throw new NoSuchPropertyException(target, name);
        }
    }

    public Class getPropertyClass(OgnlContext context, Object target, Object index)
    {
        try {
            Method m = OgnlRuntime.getReadMethod(target.getClass(), index.toString());

            if (m == null) {

                if (String.class.isAssignableFrom(index.getClass()) && !target.getClass().isArray()) {
                    String key = ((String) index).replaceAll("\"", "");
                    try {
                        Field f = target.getClass().getField(key);
                        if (f != null) {

                            return f.getType();
                        }
                    } catch (NoSuchFieldException e) {
                        return null;
                    }
                }

                return null;
            }

            return m.getReturnType();

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object index)
    {
        try {

            String methodName = index.toString().replaceAll("\"", "");
            Method m = OgnlRuntime.getReadMethod(target.getClass(), methodName);

            // try last ditch effort of checking if they were trying to do reflection via a return method value

            if (m == null && context.getCurrentObject() != null)
                m = OgnlRuntime.getReadMethod(target.getClass(), context.getCurrentObject().toString().replaceAll("\"", ""));

            //System.out.println("tried to get read method from target: " + target.getClass() + " with methodName:" + methodName + " result: " + m);
            // try to get field if no method could be found

            if (m == null)
            {
                try
                {
                    if (String.class.isAssignableFrom(index.getClass()) && !target.getClass().isArray())
                    {
                        Field f = target.getClass().getField(methodName);

                        if (f != null)
                        {
                            context.setCurrentType(f.getType());
                            context.setCurrentAccessor(f.getDeclaringClass());

                            return "." + f.getName();
                        }
                    }
                }
                catch (NoSuchFieldException e) {
                    // ignore
                }

                return "";
            }

            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

            return "." + m.getName() + "()";

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }
    }

    public String getSourceSetter(OgnlContext context, Object target, Object index)
    {
        try {

            String methodName = index.toString().replaceAll("\"", "");
            Method m = OgnlRuntime.getWriteMethod(target.getClass(), methodName);

            if (m == null && context.getCurrentObject() != null
                && context.getCurrentObject().toString() != null)
            {
                m = OgnlRuntime.getWriteMethod(target.getClass(), context.getCurrentObject().toString().replaceAll("\"", ""));
            }

            if (m == null || m.getParameterTypes() == null || m.getParameterTypes().length <= 0)
                throw new UnsupportedCompilationException("Unable to determine setting expression on " + context.getCurrentObject()
                                                          + " with index of " + index);

            Class parm = m.getParameterTypes()[0];
            String conversion;

            if (m.getParameterTypes().length > 1)
                throw new UnsupportedCompilationException("Object property accessors can only support single parameter setters.");


            if (parm.isPrimitive())
            {
                Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass(parm);
                conversion = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                            "((" + wrapClass.getName() + ")ognl.OgnlOps#convertValue($3," + wrapClass.getName()
                                                                            + ".class, true))." + OgnlRuntime.getNumericValueGetter(wrapClass),
                                                                            parm);

            } else if (parm.isArray())
            {
                conversion = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                            "(" + ExpressionCompiler.getCastString(parm) + ")ognl.OgnlOps#toArray($3,"
                                                                            + parm.getComponentType().getName() + ".class)",
                                                                            parm);

            } else
            {
                conversion = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                            "(" + parm.getName()+ ")ognl.OgnlOps#convertValue($3,"
                                                                            + parm.getName()
                                                                            + ".class)",
                                                                            parm);
            }

            context.setCurrentType(m.getReturnType());
            context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

            return "." + m.getName() + "(" + conversion + ")";

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }
    }
}
