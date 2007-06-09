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

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public abstract class ExpressionNode extends SimpleNode
{
    public ExpressionNode(int i) {
        super(i);
    }

    public ExpressionNode(OgnlParser p, int i) {
        super(p, i);
    }
    /**
        Returns true iff this node is constant without respect to the children.
     */
    public boolean isNodeConstant( OgnlContext context ) throws OgnlException
    {
        return false;
    }

    public boolean isConstant( OgnlContext context ) throws OgnlException
    {
        boolean     result = isNodeConstant(context);

        if ((_children != null) && (_children.length > 0)) {
            result = true;
            for ( int i=0; result && (i < _children.length); ++i ) {
                if (_children[i] instanceof SimpleNode) {
                    result = ((SimpleNode)_children[i]).isConstant( context );
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    public String getExpressionOperator(int index)
    {
        throw new RuntimeException("unknown operator for " + OgnlParserTreeConstants.jjtNodeName[_id]);
    }

    public String toString()
    {
        String result = (_parent == null) ? "" : "(";
        
        if ((_children != null) && (_children.length > 0)) {
            for ( int i = 0; i < _children.length; ++i ) {
                if (i > 0) {
                    result += " " + getExpressionOperator(i) + " ";
                }
                result += _children[i].toString();
            }
        }
        if (_parent != null) {
            result = result + ")";
        }
        return result;
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        String result = (_parent == null || NumericExpression.class.isAssignableFrom(_parent.getClass())) ? "" : "(";

        if ((_children != null) && (_children.length > 0)) {
            for ( int i = 0; i < _children.length; ++i ) {
                if (i > 0) {
                    result += " " + getExpressionOperator(i) + " ";
                }
                
                String value = _children[i].toGetSourceString(context, target);

                if ((ASTProperty.class.isInstance(_children[i]) || ASTMethod.class.isInstance(_children[i])
                     || ASTSequence.class.isInstance(_children[i]) || ASTChain.class.isInstance(_children[i]))
                    && value != null && value.trim().length() > 0) {

                    String pre = null;
                    if (ASTMethod.class.isInstance(_children[i]))
                    {
                        pre = (String)context.get("_currentChain");
                    }

                    if (pre == null)
                        pre = "";

                    String cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                    if (cast == null)
                        cast = "";

                    value = cast + ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), context) + pre + value;
                } 

                result += value;
            }
        }

        if (_parent != null && !NumericExpression.class.isAssignableFrom(_parent.getClass())) {
            result = result + ")";
        }
        
        return result;
    }
    
    public String toSetSourceString(OgnlContext context, Object target)
    {
        String result = (_parent == null) ? "" : "(";
        
        if ((_children != null) && (_children.length > 0)) {
            for ( int i = 0; i < _children.length; ++i ) {
                if (i > 0) {
                    result += " " + getExpressionOperator(i) + " ";
                }
                
                result += _children[i].toSetSourceString(context, target);
            }
        }
        if (_parent != null) {
            result = result + ")";
        }
        
        return result;
    }
}
