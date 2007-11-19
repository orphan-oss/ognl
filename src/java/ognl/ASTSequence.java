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
import ognl.enhance.OrderedReturn;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTSequence extends SimpleNode implements NodeType, OrderedReturn
{
    private Class _getterClass;
    private String _lastExpression;
    private String _coreExpression;

    public ASTSequence(int id) {
        super(id);
    }

    public ASTSequence(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody( OgnlContext context, Object source ) throws OgnlException
    {
        Object result = null;
        for ( int i=0; i < _children.length; ++i )
        {
            result = _children[i].getValue( context, source );
        }
        
        return result; // The result is just the last one we saw.
    }

    protected void setValueBody( OgnlContext context, Object target, Object value ) throws OgnlException {
        int last = _children.length - 1;
        for ( int i=0; i < last; ++i ) {
            _children[i].getValue( context, target );
        }
        _children[last].setValue( context, target, value );
    }

    public Class getGetterClass()
    {
        return _getterClass;
    }

    public Class getSetterClass()
    {
        return null;
    }

    public String getLastExpression()
    {
        return _lastExpression;
    }

    public String getCoreExpression()
    {
        return _coreExpression;
    }

    public String toString()
    {
        String      result = "";

        for ( int i=0; i < _children.length; ++i ) {
            if (i > 0) {
                result = result + ", ";
            }
            result = result + _children[i];
        }
        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        return "";
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        String result = "";

        NodeType _lastType = null;

        for (int i = 0; i < _children.length; ++i)
        {
            //System.out.println("astsequence child : " + _children[i].getClass().getName());
            String seqValue = _children[i].toGetSourceString(context, target);

            if ((i + 1) < _children.length
                && ASTOr.class.isInstance(_children[i])) {
                seqValue = "(" + seqValue + ")";
            }

            if (i > 0 && ASTProperty.class.isInstance(_children[i])
                && seqValue != null && seqValue.trim().length() > 0)
            {
                String pre = (String)context.get("_currentChain");
                if (pre == null)
                    pre = "";

                seqValue = ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), context) + pre + seqValue;
                context.setCurrentAccessor(context.getRoot().getClass());
            }

            if ((i + 1) >= _children.length)
            {
                _coreExpression = result;
                _lastExpression = seqValue;
            }

            if (seqValue != null && seqValue.trim().length() > 0 && (i + 1) < _children.length)
                result += seqValue + ";";
            else if (seqValue != null && seqValue.trim().length() > 0)
                result += seqValue;

            // set last known type from last child with a type

            if (NodeType.class.isInstance(_children[i]) && ((NodeType)_children[i]).getGetterClass() != null)
                _lastType = (NodeType)_children[i];
        }

        if (_lastType != null)
        {
            _getterClass = _lastType.getGetterClass();
        }

        return result;
    }
}
