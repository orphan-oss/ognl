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

import ognl.enhance.ExpressionCompiler;
import ognl.enhance.UnsupportedCompilationException;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTOr extends BooleanExpression {
    public ASTOr(int id)
    {
        super(id);
    }

    public ASTOr(OgnlParser p, int id)
    {
        super(p, id);
    }

    public void jjtClose()
    {
        flattenTree();
    }

    protected Object getValueBody(OgnlContext context, Object source) throws OgnlException
    {
        Object result = null;
        int last = _children.length - 1;
        for (int i = 0; i <= last; ++i) {
            result = _children[i].getValue(context, source);
            if (i != last && OgnlOps.booleanValue(result))
                break;
        }
        return result;
    }

    protected void setValueBody(OgnlContext context, Object target, Object value) throws OgnlException
    {
        int last = _children.length - 1;
        for (int i = 0; i < last; ++i) {
            Object v = _children[i].getValue(context, target);
            if (OgnlOps.booleanValue(v))
                return;
        }
        _children[last].setValue(context, target, value);
    }

    public String getExpressionOperator(int index)
    {
        return "||";
    }

    public Class getGetterClass()
    {
        return null;
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        if (_children.length != 2)
            throw new UnsupportedCompilationException("Can only compile boolean expressions with two children.");

        String result = "(";

        try {

            String first = OgnlRuntime.getChildSource(context, target, _children[0]);
            if (!OgnlRuntime.isBoolean(first))
                first = OgnlRuntime.getCompiler().createLocalReference(context, first, context.getCurrentType());
            
            Class firstType = context.getCurrentType();

            String second = OgnlRuntime.getChildSource(context, target, _children[1]);
            if (!OgnlRuntime.isBoolean(second))
                second = OgnlRuntime.getCompiler().createLocalReference(context, second, context.getCurrentType());

            Class secondType = context.getCurrentType();

            boolean mismatched = (firstType.isPrimitive() && !secondType.isPrimitive())
                                || (!firstType.isPrimitive() && secondType.isPrimitive()) ? true : false;

            result += "ognl.OgnlOps.booleanValue(" + first + ")";

            result += " ? ";

            result += (mismatched ? " ($w) " : "") + first;

            result += " : ";

            result += (mismatched ? " ($w) " : "") + second;

            result += ")";

            context.setCurrentObject(target);
            context.setCurrentType(Boolean.TYPE);

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        if (_children.length != 2)
            throw new UnsupportedCompilationException("Can only compile boolean expressions with two children.");

        String pre = (String) context.get("_currentChain");
        if (pre == null)
            pre = "";

        String result = "";

        try {

            _children[0].getValue(context, target);

            String first = ExpressionCompiler.getRootExpression(_children[0], context.getRoot(), context)
                           + pre + _children[0].toGetSourceString(context, target);
            if (!OgnlRuntime.isBoolean(first))
                first = OgnlRuntime.getCompiler().createLocalReference(context, first, Object.class);

            _children[1].getValue(context, target);
            
            String second = ExpressionCompiler.getRootExpression(_children[1], context.getRoot(), context)
                            + pre + _children[1].toSetSourceString(context, target);
            if (!OgnlRuntime.isBoolean(second))
                second = OgnlRuntime.getCompiler().createLocalReference(context, second, context.getCurrentType());

            result += "ognl.OgnlOps.booleanValue(" + first + ")";

            result += " ? ";

            result += first;
            result += " : ";

            result += second;

            context.setCurrentObject(target);

            context.setCurrentType(Boolean.TYPE);

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        return result;
    }
}
