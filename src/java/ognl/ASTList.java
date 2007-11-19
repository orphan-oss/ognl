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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTList extends SimpleNode implements NodeType
{
    public ASTList(int id)
    {
        super(id);
    }

    public ASTList(OgnlParser p, int id)
    {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException
    {
        List answer = new ArrayList(jjtGetNumChildren());
        for(int i = 0; i < jjtGetNumChildren(); ++i)
            answer.add(_children[i].getValue(context, source));
        return answer;
    }

    public Class getGetterClass()
    {
        return null;
    }

    public Class getSetterClass()
    {
        return null;
    }

    public String toString()
    {
        String result = "{ ";

        for(int i = 0; i < jjtGetNumChildren(); ++i) {
            if (i > 0) {
                result = result + ", ";
            }
            result = result + _children[i].toString();
        }
        return result + " }";
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        String result = "";
        boolean array = false;

        if (_parent != null && ASTCtor.class.isInstance(_parent)
            && ((ASTCtor)_parent).isArray()) {

            array = true;
        }

        context.setCurrentType(List.class);
        context.setCurrentAccessor(List.class);

        if (!array)
        {
            if (jjtGetNumChildren() < 1)
                return "java.util.Arrays.asList( new Object[0])";

            result += "java.util.Arrays.asList( new Object[] ";
        }

        result += "{ ";

        try {

            for(int i = 0; i < jjtGetNumChildren(); ++i) {
                if (i > 0) {
                    result = result + ", ";
                }

                Class prevType = context.getCurrentType();

                Object objValue = _children[i].getValue(context, context.getRoot());
                String value = _children[i].toGetSourceString(context, target);

                // to undo type setting of constants when used as method parameters
                if (ASTConst.class.isInstance(_children[i])) {

                    context.setCurrentType(prevType);
                }

                value = ExpressionCompiler.getRootExpression(_children[i], target, context) + value;

                String cast = "";
                if (ExpressionCompiler.shouldCast(_children[i])) {

                    cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                }
                if (cast == null)
                    cast = "";

                if (!ASTConst.class.isInstance(_children[i]))
                    value = cast + value;

                Class ctorClass = (Class)context.get("_ctorClass");
                if (array && ctorClass != null && !ctorClass.isPrimitive()) {

                    Class valueClass = value != null ? value.getClass() : null;
                    if (NodeType.class.isAssignableFrom(_children[i].getClass()))
                        valueClass = ((NodeType)_children[i]).getGetterClass();

                    if (valueClass != null && ctorClass.isArray()) {

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                               "(" + ExpressionCompiler.getCastString(ctorClass)
                                                                               + ")ognl.OgnlOps.toArray(" + value + ", " + ctorClass.getComponentType().getName()
                                                                               + ".class, true)",
                                                                               ctorClass
                        );

                    } else  if (ctorClass.isPrimitive()) {

                        Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass(ctorClass);

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                               "((" + wrapClass.getName()
                                                                               + ")ognl.OgnlOps.convertValue(" + value + ","
                                                                               + wrapClass.getName() + ".class, true))."
                                                                               + OgnlRuntime.getNumericValueGetter(wrapClass),
                                                                               ctorClass
                        );

                    } else if (ctorClass != Object.class) {

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                               "(" + ctorClass.getName() + ")ognl.OgnlOps.convertValue(" + value + "," + ctorClass.getName() + ".class)",
                                                                               ctorClass
                        );

                    } else if ((NodeType.class.isInstance(_children[i])
                                && ((NodeType)_children[i]).getGetterClass() != null
                                && Number.class.isAssignableFrom(((NodeType)_children[i]).getGetterClass()))
                               || valueClass.isPrimitive()) {

                        value = " ($w) (" + value + ")";
                    } else if (valueClass.isPrimitive()) {
                        value = "($w) (" + value + ")";
                    }

                } else if (ctorClass == null || !ctorClass.isPrimitive()) {

                    value = " ($w) (" + value + ")";
                }

                if (objValue == null || value.length() <= 0)
                    value = "null";

                result += value;
            }

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        context.setCurrentType(List.class);
        context.setCurrentAccessor(List.class);

        result += "}";

        if (!array)
            result += ")";

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        throw new UnsupportedCompilationException("Can't generate setter for ASTList.");
    }
}
