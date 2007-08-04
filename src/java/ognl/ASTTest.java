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

import ognl.enhance.UnsupportedCompilationException;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTTest extends ExpressionNode
{
    public ASTTest(int id) {
        super(id);
    }

    public ASTTest(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody( OgnlContext context, Object source ) throws OgnlException
    {
        Object test = _children[0].getValue( context, source );
        int branch = OgnlOps.booleanValue(test)? 1 : 2;
        return _children[branch].getValue( context, source );
    }

    protected void setValueBody( OgnlContext context, Object target, Object value ) throws OgnlException
    {
        Object test = _children[0].getValue( context, target );
        int branch = OgnlOps.booleanValue(test)? 1 : 2;
        _children[branch].setValue( context, target, value );
    }

    public String getExpressionOperator(int index)
    {
        return (index == 1) ? "?" : ":";
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        if (target == null)
            throw new UnsupportedCompilationException("evaluation resulted in null expression.");

        if (_children.length != 3)
            throw new UnsupportedCompilationException("Can only compile test expressions with two children." + _children.length);

        String result = "";
        
        try {

            String first = OgnlRuntime.getChildSource(context, target, _children[0]);            
            if (!OgnlRuntime.isBoolean(first) && !context.getCurrentType().isPrimitive())
                first = OgnlRuntime.getCompiler().createLocalReference(context, first, context.getCurrentType());
            
            if (ExpressionNode.class.isInstance(_children[0]))
            {
                first = "(" + first + ")";
            }
            
            String second = OgnlRuntime.getChildSource(context, target, _children[1]);
            Class secondType = context.getCurrentType();

            if (!OgnlRuntime.isBoolean(second) && !context.getCurrentType().isPrimitive())
                second = OgnlRuntime.getCompiler().createLocalReference(context, second, context.getCurrentType());

            if (ExpressionNode.class.isInstance(_children[1])) 
            {
                second = "(" + second + ")";
            }

            String third = OgnlRuntime.getChildSource(context, target, _children[2]);
            Class thirdType = context.getCurrentType();
            
            if (!OgnlRuntime.isBoolean(third) && !context.getCurrentType().isPrimitive())
                third = OgnlRuntime.getCompiler().createLocalReference(context, third, context.getCurrentType());
            if (ExpressionNode.class.isInstance(_children[2]))
            {
                third = "(" + third + ")";
            }

            boolean mismatched = (secondType.isPrimitive() && !thirdType.isPrimitive())
                                || (!secondType.isPrimitive() && thirdType.isPrimitive()) ? true : false;

            result += "ognl.OgnlOps.booleanValue(" + first + ")";

            result += " ? ";

            result += (mismatched ? " ($w) " : "") + second;
            result += " : ";

            result += (mismatched ? " ($w) " : "") + third;

            context.setCurrentObject(target);
            context.setCurrentType(mismatched ? Object.class : secondType);

            return result;
        
        } catch (NullPointerException e) {
            
            // expected to happen in some instances
            throw new UnsupportedCompilationException("evaluation resulted in null expression.");
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }
    }
}
