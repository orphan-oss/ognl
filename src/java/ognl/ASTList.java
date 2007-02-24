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

    private Class _getterClass;
    
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
        return _getterClass;
    }
    
    public Class getSetterClass()
    {
        return _getterClass;
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
        Class mainClass = null;
        
        if (_parent == null || !ASTCtor.class.isInstance(_parent)) {
            result += "java.util.Arrays.asList( new Object[] ";
            _getterClass = List.class;
        } else {
            
            mainClass = (Class)context.get("_ctorClass");
            _getterClass = mainClass;
        }
        
        result += "{ ";
        
        for(int i = 0; i < jjtGetNumChildren(); ++i) {
            if (i > 0) {
                result = result + ", ";
            }
            
            String value = _children[i].toGetSourceString(context, target);
            
            //System.out.println("astlist child class " + _children[i].getClass().getName() + " and source: " + value + " mainClass: " + mainClass);
            
            if (mainClass != null && String.class.isAssignableFrom(mainClass) && !value.startsWith("\"")) {
                value = "\"" + value + "\"";
            } else if (_getterClass == List.class && value.startsWith("'")) {
                value = value.replaceAll("'", "\"");
            } else if ((ASTProperty.class.isInstance(_children[i]) || ASTMethod.class.isInstance(_children[i])
                    || ASTSequence.class.isInstance(_children[i]) || ASTChain.class.isInstance(_children[i]))
                    && value != null && value.trim().length() > 0) {
                    
                    String pre = (String)context.get("_currentChain");
                    if (pre == null)
                        pre = "";
                    
                    String cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                    if (cast == null)
                        cast = "";
                    
                    value = cast + ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), false) + pre + value;
                    
            } else if (!ASTVarRef.class.isInstance(_children[i]) 
                    && NodeType.class.isInstance(_children[i])) {
                
                NodeType ctype = (NodeType)_children[i];
                
                if (mainClass != null && !mainClass.isPrimitive()) {
                    
                    value = "new " + mainClass.getName() + "(" + value + ")";
                } else if (ctype.getGetterClass() != null 
                        && Number.class.isAssignableFrom(ctype.getGetterClass())
                        && (mainClass == null || !mainClass.isPrimitive())) {
                    
                    value = "new " + ctype.getGetterClass().getName() + "(" + value + ")";
                } else if (ctype.getGetterClass() != null && String.class == ctype.getGetterClass()
                        && value != null && !value.startsWith("\"")) {
                    
                    value = "\"" + value + "\"";
                } else if (ctype.getGetterClass() != null 
                        && (Boolean.class == ctype.getGetterClass() || Boolean.TYPE == ctype.getGetterClass())
                        && (mainClass == null || !mainClass.isPrimitive())) {
                    
                    value = "Boolean.valueOf(" + value + ")";
                } else if (value == null || value.length() <= 0)
                    value = "null";
            }
            
            result += value;
        }
        
        context.setCurrentType(_getterClass);
        
        if (_parent == null || !ASTCtor.class.isInstance(_parent))
            return result + " })";
        else
            return result + " }";
    }
    
    public String toSetSourceString(OgnlContext context, Object target)
    {
        throw new UnsupportedCompilationException("Can't generate setter for ASTList.");
    }
}
