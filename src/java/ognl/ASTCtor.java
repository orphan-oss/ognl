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

import java.lang.reflect.Array;
import java.util.List;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTCtor extends SimpleNode
{

    private String className;
    private boolean isArray;

    public ASTCtor(int id)
    {
        super(id);
    }

    public ASTCtor(OgnlParser p, int id)
    {
        super(p, id);
    }

    /** Called from parser action. */
    void setClassName(String className)
    {
        this.className = className;
    }

    void setArray(boolean value)
    {
        isArray = value;
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException
    {
        Object result, root = context.getRoot();
        int count = jjtGetNumChildren();
        Object[] args = OgnlRuntime.getObjectArrayPool().create(count);

        try {
            for(int i = 0; i < count; ++i) {
                args[i] = _children[i].getValue(context, root);
            }
            if (isArray) {
                if (args.length == 1) {
                    try {
                        Class componentClass = OgnlRuntime.classForName(context, className);
                        List sourceList = null;
                        int size;

                        if (args[0] instanceof List) {
                            sourceList = (List) args[0];
                            size = sourceList.size();
                        } else {
                            size = (int) OgnlOps.longValue(args[0]);
                        }
                        result = Array.newInstance(componentClass, size);
                        if (sourceList != null) {
                            TypeConverter converter = context.getTypeConverter();

                            for(int i = 0, icount = sourceList.size(); i < icount; i++) {
                                Object o = sourceList.get(i);

                                if ((o == null) || componentClass.isInstance(o)) {
                                    Array.set(result, i, o);
                                } else {
                                    Array.set(result, i, converter.convertValue(context, null, null, null, o,
                                            componentClass));
                                }
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                        throw new OgnlException("array component class '" + className + "' not found", ex);
                    }
                } else {
                    throw new OgnlException("only expect array size or fixed initializer list");
                }
            } else {
                result = OgnlRuntime.callConstructor(context, className, args);
            }

            return result;
        } finally {
            OgnlRuntime.getObjectArrayPool().recycle(args);
        }
    }

    public String toString()
    {
        String result = "new " + className;

        if (isArray) {
            if (_children[0] instanceof ASTConst) {
                result = result + "[" + _children[0] + "]";
            } else {
                result = result + "[] " + _children[0];
            }
        } else {
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
        }
        return result;
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        String result = "new " + className;

        Class clazz = null;
        try {

            clazz = OgnlRuntime.classForName(context, className);

            context.put("_ctorClass", clazz);

            Object value = this.getValueBody(context, target);
            context.setCurrentObject(value);

            if (clazz != null) {

                context.setCurrentType(clazz);
                context.setCurrentAccessor(clazz);
            }

        } catch (Throwable t) {
            if (UnsupportedCompilationException.class.isInstance(t))
                throw (UnsupportedCompilationException)t;
            else
                throw new RuntimeException(t);
        }

        try {

            if (isArray) {
                if (_children[0] instanceof ASTConst) {

                    result = result + "[" + _children[0].toGetSourceString(context, target) + "]";
                } else if (ASTProperty.class.isInstance(_children[0])) {

                    result = result + "["
                            + ExpressionCompiler.getRootExpression(_children[0], target, false)
                            + _children[0].toGetSourceString(context, target)
                            + "]";
                } else if (ASTChain.class.isInstance(_children[0])) {

                    result = result + "[" + _children[0].toGetSourceString(context, target) + "]";
                } else {

                    result = result + "[] "+ _children[0].toGetSourceString(context, target);
                }

            } else {
                result = result + "(";

                if ((_children != null) && (_children.length > 0)) {
                    for(int i = 0; i < _children.length; i++) {
                        if (i > 0) {
                            result = result + ", ";
                        }

                        Object objValue = _children[i].getValue(context, context.getRoot());
                        String value = _children[i].toGetSourceString(context, target);

                        if (className.equals("String") && !value.startsWith("\""))
                            value = "\"" + value + "\"";
                        else if (NodeType.class.isInstance(_children[i])) {
                            NodeType ctype = (NodeType)_children[i];

                            if (ctype.getGetterClass() != null && String.class == ctype.getGetterClass() && !value.startsWith("\""))
                                value = "\"" + value + "\"";
                        }

                        value = ExpressionCompiler.getRootExpression(_children[0], target, false) + value;

                        if (context.get(ExpressionCompiler.PRE_CAST) != null) {
                            value = context.remove(ExpressionCompiler.PRE_CAST) + value;
                        }
                        
                        if (objValue != null && !objValue.getClass().isPrimitive() && !ASTConst.class.isInstance(_children[i])) {

                            value = "(" + ExpressionCompiler.getCastString(objValue.getClass()) + ")" + value;
                        }

                        result += value;
                    }
                }
                result = result + ")";
            }

            context.remove("_ctorClass");

            context.setCurrentType(clazz);

            context.setCurrentObject(getValue(context, target));
        }catch (Throwable t) {
            if (UnsupportedCompilationException.class.isInstance(t))
                throw (UnsupportedCompilationException)t;
            else
                throw new RuntimeException(t);
        }

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        return "";
    }
}
