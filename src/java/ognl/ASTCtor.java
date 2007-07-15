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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
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

    public boolean isArray()
    {
        return isArray;
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
        Object ctorValue = null;
        try {

            clazz = OgnlRuntime.classForName(context, className);
            
            ctorValue = this.getValueBody(context, target);
            context.setCurrentObject(ctorValue);
            
            if (clazz != null && ctorValue != null) {
                
                context.setCurrentType(ctorValue.getClass());
                context.setCurrentAccessor(ctorValue.getClass());
            }

            if (isArray)
                context.put("_ctorClass", clazz);

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        try {

            if (isArray) {
                if (_children[0] instanceof ASTConst) {
                    
                    result = result + "[" + _children[0].toGetSourceString(context, target) + "]";
                } else if (ASTProperty.class.isInstance(_children[0])) {

                    result = result + "["
                            + ExpressionCompiler.getRootExpression(_children[0], target, context)
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

                    Object[] values = new Object[_children.length];
                    String[] expressions = new String[_children.length];
                    Class[] types = new Class[_children.length];

                    // first populate arrays with child values
                    
                    for(int i = 0; i < _children.length; i++) {
                        
                        Object objValue = _children[i].getValue(context, context.getRoot());
                        String value = _children[i].toGetSourceString(context, target);

                        if (!ASTRootVarRef.class.isInstance(_children[i]))
                        {
                            value = ExpressionCompiler.getRootExpression(_children[i], target, context) + value;
                        }

                        String cast = "";
                        if (ExpressionCompiler.shouldCast(_children[i])) {

                            cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                        }
                        if (cast == null)
                            cast = "";

                        if (!ASTConst.class.isInstance(_children[i]))
                            value = cast + value;

                        values[i] = objValue;
                        expressions[i] = value;
                        types[i] = context.getCurrentType();
                    }

                    // now try and find a matching constructor
                    
                    Constructor[] cons = clazz.getConstructors();
                    Constructor ctor = null;
                    Class[] ctorParamTypes = null;
                    
                    for (int i=0; i < cons.length; i++)
                    {
                        Class[] ctorTypes = cons[i].getParameterTypes();

                        if (OgnlRuntime.areArgsCompatible(values, ctorTypes)
                            && (ctor == null || OgnlRuntime.isMoreSpecific(ctorTypes, ctorParamTypes))) {
                            ctor = cons[i];
                            ctorParamTypes = ctorTypes;
                        }
                    }

                    if (ctor == null)
                        ctor = OgnlRuntime.getConvertedConstructorAndArgs(context, clazz, OgnlRuntime.getConstructors(clazz), values, new Object[values.length]);

                    if (ctor == null)
                        throw new NoSuchMethodException("Unable to find constructor appropriate for arguments in class: " + clazz);

                    ctorParamTypes = ctor.getParameterTypes();

                    // now loop over child values again and build up the actual source string

                    for(int i = 0; i < _children.length; i++) {
                        if (i > 0) {
                            result = result + ", ";
                        }

                        String value = expressions[i];

                        if (types[i].isPrimitive()) {

                            String literal = OgnlRuntime.getNumericLiteral(types[i]);
                            if (literal != null)
                                value += literal;
                        }

                        if (ctorParamTypes[i] != types[i]) {

                            if (values[i] != null && !types[i].isPrimitive()
                                && !values[i].getClass().isArray() && !ASTConst.class.isInstance(_children[i])) {
                                
                                value = "(" + OgnlRuntime.getCompiler().getInterfaceClass(values[i].getClass()).getName() + ")" + value;
                            } else if (!ASTConst.class.isInstance(_children[i])
                                       || (ASTConst.class.isInstance(_children[i]) && !types[i].isPrimitive())) {
                                
                                if (!types[i].isArray()
                                    && types[i].isPrimitive() && !ctorParamTypes[i].isPrimitive())
                                    value = "new " + ExpressionCompiler.getCastString(OgnlRuntime.getPrimitiveWrapperClass(types[i])) + "(" + value + ")";
                                else
                                    value = " ($w) " + value;
                            }
                        }

                        result += value;
                    }

                }
                result = result + ")";
            }

            context.setCurrentType(ctorValue != null ? ctorValue.getClass() : clazz);
            context.setCurrentAccessor(clazz);
            context.setCurrentObject(ctorValue);

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        context.remove("_ctorClass");

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        return "";
    }
}
