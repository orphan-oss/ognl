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

import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTAssign extends SimpleNode
{
    public ASTAssign(int id) {
        super(id);
    }

    public ASTAssign(OgnlParser p, int id) {
        super(p, id);
    }

    protected Object getValueBody( OgnlContext context, Object source ) throws OgnlException
    {
        Object result = _children[1].getValue( context, source );
        _children[0].setValue( context, source, result );
        return result;
    }

    public String toString()
    {
        return _children[0] + " = " + _children[1];
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        String result = "";

        String first = _children[0].toGetSourceString(context, target);
        String second = "";
        
        if (ASTProperty.class.isInstance(_children[1])) {
            second += "((" + OgnlRuntime.getCompiler().getClassName(target.getClass()) + ")$2).";
        }
        
        second += _children[1].toGetSourceString(context, target);
        
        if (ASTSequence.class.isAssignableFrom(_children[1].getClass())) {
            ASTSequence seq = (ASTSequence)_children[1];

            context.setCurrentType(Object.class);

            String core = seq.getCoreExpression();
            if (core.endsWith(";"))
                core = core.substring(0, core.lastIndexOf(";"));

            second = OgnlRuntime.getCompiler().createLocalReference(context,
                    "ognl.OgnlOps.returnValue(($w)" + core  + ", ($w) " + seq.getLastExpression() + ")",
                    Object.class);
        }

        if (NodeType.class.isInstance(_children[1])
                && !ASTProperty.class.isInstance(_children[1])
                && ((NodeType)_children[1]).getGetterClass() != null && !OrderedReturn.class.isInstance(_children[1])) {
            
            second = "new " + ((NodeType)_children[1]).getGetterClass().getName() + "(" + second + ")";
        }
        
        if (OrderedReturn.class.isAssignableFrom(_children[0].getClass())
            && ((OrderedReturn)_children[0]).getCoreExpression() != null) {
            context.setCurrentType(Object.class);

            result = first + second + ")";

            // System.out.println("building ordered ret from child[0] with result of:" + result);

            result = OgnlRuntime.getCompiler().createLocalReference(context,
                    "ognl.OgnlOps.returnValue(($w)" + result + ", ($w)" + ((OrderedReturn)_children[0]).getLastExpression() + ")",
                    Object.class);
        }

        return result;
    }
    
    public String toSetSourceString(OgnlContext context, Object target)
    {
        String result = "";
        
        result += _children[0].toSetSourceString(context, target);
        
        if (ASTProperty.class.isInstance(_children[1])) {
            result += "((" + OgnlRuntime.getCompiler().getClassName(target.getClass()) + ")$2).";
        }
        
        String value =_children[1].toSetSourceString(context, target);
        
        if (value == null)
            throw new UnsupportedCompilationException("Value for assignment is null, can't enhance statement to bytecode.");
        
        if (ASTSequence.class.isAssignableFrom(_children[1].getClass())) {
            ASTSequence seq = (ASTSequence)_children[1];
            result = seq.getCoreExpression() + result;
            value = seq.getLastExpression();
        }
        
        if (NodeType.class.isInstance(_children[1]) 
                && !ASTProperty.class.isInstance(_children[1])
                && ((NodeType)_children[1]).getGetterClass() != null) {
            
            value = "new " + ((NodeType)_children[1]).getGetterClass().getName() + "(" + value + ")";
        }
        
        return result + value + ")";
    }
}
