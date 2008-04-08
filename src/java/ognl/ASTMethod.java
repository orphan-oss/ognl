//--------------------------------------------------------------------------
//Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//All rights reserved.

//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are
//met:

//Redistributions of source code must retain the above copyright notice,
//this list of conditions and the following disclaimer.
//Redistributions in binary form must reproduce the above copyright
//notice, this list of conditions and the following disclaimer in the
//documentation and/or other materials provided with the distribution.
//Neither the name of the Drew Davidson nor the names of its contributors
//may be used to endorse or promote products derived from this software
//without specific prior written permission.

//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//DAMAGE.
//--------------------------------------------------------------------------
package ognl;

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Method;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTMethod extends SimpleNode implements OrderedReturn, NodeType
{

    private String _methodName;

    private String _lastExpression;

    private String _coreExpression;

    private Class _getterClass;

    public ASTMethod(int id)
    {
        super(id);
    }

    public ASTMethod(OgnlParser p, int id)
    {
        super(p, id);
    }

    /** Called from parser action. */
    public void setMethodName(String methodName)
    {
        _methodName = methodName;
    }

    /**
     * Returns the method name that this node will call.
     */
    public String getMethodName()
    {
        return _methodName;
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException
    {
        Object[] args = OgnlRuntime.getObjectArrayPool().create(jjtGetNumChildren());

        try {
            Object result, root = context.getRoot();

            for(int i = 0, icount = args.length; i < icount; ++i) {
                args[i] = _children[i].getValue(context, root);
            }

            result = OgnlRuntime.callMethod(context, source, _methodName, args);

            if (result == null)
            {
                NullHandler nh = OgnlRuntime.getNullHandler(OgnlRuntime.getTargetClass(source));
                result = nh.nullMethodResult(context, source, _methodName, args);
            }

            return result;

        } finally {
            OgnlRuntime.getObjectArrayPool().recycle(args);
        }
    }

    public String getLastExpression()
    {
        return _lastExpression;
    }

    public String getCoreExpression()
    {
        return _coreExpression;
    }

    public Class getGetterClass()
    {
        return _getterClass;
    }

    public Class getSetterClass()
    {
        return _getterClass;
    }

    public String toString()
    {
        String result = _methodName;

        result = result + "(";
        if ((_children != null) && (_children.length > 0)) {

            for(int i = 0; i < _children.length; i++) {
                if (i > 0) {
                    result = result + ", ";
                }

                result = result + _children[i];
            }
        }

        result = result + ")";
        return result;
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        /*  System.out.println("methodName is " + _methodName + " for target " + target + " target class: " + (target != null ? target.getClass() : null)
  + " current type: " + context.getCurrentType());*/
        if (target == null)
            throw new UnsupportedCompilationException("Target object is null.");

        String post = "";
        String result = null;
        Method m = null;

        try {

            m = OgnlRuntime.getMethod(context, context.getCurrentType() != null ? context.getCurrentType() : target.getClass(), _methodName, _children, false);
            if (m == null)
                m = OgnlRuntime.getReadMethod(target.getClass(), _methodName, _children != null ? _children.length : -1);

            if (m == null) {
                m = OgnlRuntime.getWriteMethod(target.getClass(), _methodName, _children != null ? _children.length : -1);

                if (m != null) {

                    context.setCurrentType(m.getReturnType());
                    context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

                    _coreExpression = toSetSourceString(context, target);
                    if (_coreExpression == null || _coreExpression.length() < 1)
                        throw new UnsupportedCompilationException("can't find suitable getter method");

                    _coreExpression +=  ";";
                    _lastExpression = "null";

                    return _coreExpression;
                }

                return "";
            } else {

                _getterClass = m.getReturnType();
            }

            // TODO:  This is a hacky workaround until javassist supports varargs method invocations

            boolean varArgs = OgnlRuntime.isJdk15() && m.isVarArgs();

            if (varArgs)
            {
                throw new UnsupportedCompilationException("Javassist does not currently support varargs method calls");
            }

            result = "." + m.getName() + "(";

            if ((_children != null) && (_children.length > 0))
            {
                Class[] parms = m.getParameterTypes();
                String prevCast = (String)context.remove(ExpressionCompiler.PRE_CAST);
/*
                System.out.println("before children methodName is " + _methodName + " for target " + target + " target class: " + (target != null ? target.getClass() : null)
                           + " current type: " + context.getCurrentType() + " and previous type: " + context.getPreviousType());*/

                for(int i = 0; i < _children.length; i++)
                {
                    if (i > 0)
                    {
                        result = result + ", ";
                    }

                    Class prevType = context.getCurrentType();

                    context.setCurrentObject(context.getRoot());
                    context.setCurrentType(context.getRoot() != null ? context.getRoot().getClass() : null);
                    context.setCurrentAccessor(null);
                    context.setPreviousType(null);

                    Object value = _children[i].getValue(context, context.getRoot());
                    String parmString = _children[i].toGetSourceString(context, context.getRoot());

                    if (parmString == null || parmString.trim().length() < 1)
                        parmString = "null";

                    // to undo type setting of constants when used as method parameters
                    if (ASTConst.class.isInstance(_children[i]))
                    {
                        context.setCurrentType(prevType);
                    }

                    parmString = ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), context) + parmString;

                    String cast = "";
                    if (ExpressionCompiler.shouldCast(_children[i]))
                    {
                        cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                    }
                    if (cast == null)
                        cast = "";

                    if (!ASTConst.class.isInstance(_children[i]))
                        parmString = cast + parmString;

                    Class valueClass = value != null ? value.getClass() : null;
                    if (NodeType.class.isAssignableFrom(_children[i].getClass()))
                        valueClass = ((NodeType)_children[i]).getGetterClass();

                    if ((!varArgs || varArgs && (i + 1) < parms.length) && valueClass != parms[i])
                    {
                        if (parms[i].isArray()) {

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                                        "(" + ExpressionCompiler.getCastString(parms[i])
                                                                                        + ")ognl.OgnlOps#toArray(" + parmString + ", " + parms[i].getComponentType().getName()
                                                                                        + ".class, true)",
                                                                                        parms[i]
                            );

                        } else  if (parms[i].isPrimitive()) {

                            Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass(parms[i]);

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                                        "((" + wrapClass.getName()
                                                                                        + ")ognl.OgnlOps#convertValue(" + parmString + ","
                                                                                        + wrapClass.getName() + ".class, true))."
                                                                                        + OgnlRuntime.getNumericValueGetter(wrapClass),
                                                                                        parms[i]
                            );

                        } else if (parms[i] != Object.class) {
                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                                        "(" + parms[i].getName() + ")ognl.OgnlOps#convertValue(" + parmString + "," + parms[i].getName() + ".class)",
                                                                                        parms[i]
                            );
                        } else if ((NodeType.class.isInstance(_children[i])
                                    && ((NodeType)_children[i]).getGetterClass() != null
                                    && Number.class.isAssignableFrom(((NodeType)_children[i]).getGetterClass()))
                                   || (valueClass != null && valueClass.isPrimitive()))
                        {
                            parmString = " ($w) " + parmString;
                        } else if (valueClass != null && valueClass.isPrimitive())
                        {
                            parmString = "($w) " + parmString;
                        }
                    }

                    result += parmString;
                }

                if (prevCast != null)
                {
                    context.put(ExpressionCompiler.PRE_CAST, prevCast);
                }
            }

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        try
        {
            Object contextObj = getValueBody(context, target);
            context.setCurrentObject(contextObj);
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        result += ")" + post;

        if (m.getReturnType() == void.class) {
            _coreExpression = result + ";";
            _lastExpression = "null";
        }

        context.setCurrentType(m.getReturnType());
        context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        /*System.out.println("current type: " + context.getCurrentType() + " target:" + target + " " + context.getCurrentObject()
                           + " last child? " + lastChild(context));*/
        Method m = OgnlRuntime.getWriteMethod(context.getCurrentType() != null ?
                context.getCurrentType() : target.getClass(),
                                              _methodName, _children != null ? _children.length : -1);
        if (m == null)
        {
            throw new UnsupportedCompilationException("Unable to determine setter method generation for " + _methodName);
        }

        String post = "";
        String result = "." + m.getName() + "(";

        if (m.getReturnType() != void.class && m.getReturnType().isPrimitive()
            && (_parent == null || !ASTTest.class.isInstance(_parent)))
        {
            Class wrapper = OgnlRuntime.getPrimitiveWrapperClass(m.getReturnType());

            ExpressionCompiler.addCastString(context, "new " + wrapper.getName() + "(");
            post = ")";
            _getterClass = wrapper;
        }

        boolean varArgs = OgnlRuntime.isJdk15() && m.isVarArgs();

        if (varArgs)
        {
            throw new UnsupportedCompilationException("Javassist does not currently support varargs method calls");
        }

        try {
            /* if (lastChild(context) && m.getParameterTypes().length > 0 && _children.length <= 0)
                throw new UnsupportedCompilationException("Unable to determine setter method generation for " + m); */

            if ((_children != null) && (_children.length > 0))
            {
                Class[] parms = m.getParameterTypes();
                String prevCast = (String)context.remove(ExpressionCompiler.PRE_CAST);

                for(int i = 0; i < _children.length; i++)
                {
                    if (i > 0)
                    {
                        result += ", ";
                    }

                    Class prevType = context.getCurrentType();

                    context.setCurrentObject(context.getRoot());
                    context.setCurrentType(context.getRoot() != null ? context.getRoot().getClass() : null);
                    context.setCurrentAccessor(null);
                    context.setPreviousType(null);

                    Object value = _children[i].getValue(context, context.getRoot());
                    String parmString = _children[i].toSetSourceString(context, context.getRoot());

                    if (context.getCurrentType() == Void.TYPE || context.getCurrentType() == void.class)
                        throw new UnsupportedCompilationException("Method argument can't be a void type.");

                    if (parmString == null || parmString.trim().length() < 1)
                    {
                        if (ASTProperty.class.isInstance(_children[i]) || ASTMethod.class.isInstance(_children[i])
                            || ASTStaticMethod.class.isInstance(_children[i]) || ASTChain.class.isInstance(_children[i]))
                            throw new UnsupportedCompilationException("ASTMethod setter child returned null from a sub property expression.");

                        parmString = "null";
                    }

                    // to undo type setting of constants when used as method parameters
                    if (ASTConst.class.isInstance(_children[i]))
                    {
                        context.setCurrentType(prevType);
                    }

                    parmString = ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), context) + parmString;

                    String cast = "";
                    if (ExpressionCompiler.shouldCast(_children[i]))
                    {
                        cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                    }

                    if (cast == null)
                        cast = "";

                    parmString = cast + parmString;

                    Class valueClass = value != null ? value.getClass() : null;
                    if (NodeType.class.isAssignableFrom(_children[i].getClass()))
                        valueClass = ((NodeType)_children[i]).getGetterClass();

                    if (valueClass != parms[i])
                    {
                        if (parms[i].isArray())
                        {
                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                                        "(" + ExpressionCompiler.getCastString(parms[i])
                                                                                        + ")ognl.OgnlOps#toArray(" + parmString + ", "
                                                                                        + parms[i].getComponentType().getName()
                                                                                        + ".class)",
                                                                                        parms[i]
                            );

                        } else  if (parms[i].isPrimitive())
                        {
                            Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass(parms[i]);

                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                                        "((" + wrapClass.getName()
                                                                                        + ")ognl.OgnlOps#convertValue(" + parmString + ","
                                                                                        + wrapClass.getName() + ".class, true))."
                                                                                        + OgnlRuntime.getNumericValueGetter(wrapClass),
                                                                                        parms[i]
                            );

                        } else if (parms[i] != Object.class)
                        {
                            parmString = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                                        "(" + parms[i].getName() + ")ognl.OgnlOps#convertValue("
                                                                                        + parmString + "," + parms[i].getName() + ".class)",
                                                                                        parms[i]
                            );

                        } else if ((NodeType.class.isInstance(_children[i])
                                    && ((NodeType)_children[i]).getGetterClass() != null
                                    && Number.class.isAssignableFrom(((NodeType)_children[i]).getGetterClass()))
                                   || (valueClass != null && valueClass.isPrimitive()))
                        {
                            parmString = " ($w) " + parmString;
                        } else if (valueClass != null && valueClass.isPrimitive())
                        {
                            parmString = "($w) " + parmString;
                        }
                    }

                    result += parmString;
                }

                if (prevCast != null)
                {
                    context.put(ExpressionCompiler.PRE_CAST, prevCast);
                }
            }

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        try
        {
            Object contextObj = getValueBody(context, target);
            context.setCurrentObject(contextObj);
        } catch (Throwable t)
        {
            // ignore
        }

        context.setCurrentType(m.getReturnType());
        context.setCurrentAccessor(OgnlRuntime.getCompiler().getSuperOrInterfaceClass(m, m.getDeclaringClass()));

        return result + ")" + post;
    }
}
