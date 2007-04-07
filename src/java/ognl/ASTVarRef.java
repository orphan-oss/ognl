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

import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;


/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTVarRef extends SimpleNode implements NodeType, OrderedReturn {
    
    private String _name;
    
    protected Class _getterClass;

    protected String _core;
    protected String _last;

    public ASTVarRef(int id)
    {
        super(id);
    }
    
    public ASTVarRef(OgnlParser p, int id)
    {
        super(p, id);
    }

    void setName(String name)
    {
        this._name = name;
    }
    
    protected Object getValueBody(OgnlContext context, Object source)
        throws OgnlException
    {
        return context.get(_name);
    }

    protected void setValueBody(OgnlContext context, Object target, Object value)
        throws OgnlException
    {
        context.put(_name, value);
    }
    
    public Class getGetterClass()
    {
        return _getterClass;
    }
    
    public Class getSetterClass()
    {
        return null;
    }

    public String getCoreExpression()
    {
        return _core;
    }

    public String getLastExpression()
    {
        return _last;
    }

    public String toString()
    {
        return "#" + _name;
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        Object value = context.get(_name);

        if (value != null) {
            
            _getterClass = value.getClass();
        }

        context.setCurrentType(_getterClass);
        context.setCurrentAccessor(context.getClass());
        
        context.setCurrentObject(value);
        //context.setRoot(context.get(_name));
        
        if (context.getCurrentObject() == null)
            throw new UnsupportedCompilationException("Current context object is null, can't compile var reference.");

        String pre = "";
        String post = "";
        if (context.getCurrentType() != null) {
            pre = "((" + OgnlRuntime.getCompiler().getInterfaceClass(context.getCurrentType()).getName() + ")";
            post = ")";
        }
        
        if (_parent != null && ASTAssign.class.isInstance(_parent)) {
            _core = "$1.put(\"" + _name + "\",";
            _last = pre + "$1.get(\"" + _name + "\")" + post;
            
            return _core;
        }

        return pre + "$1.get(\"" + _name + "\")" + post;
    }
    
    public String toSetSourceString(OgnlContext context, Object target)
    {
        return toGetSourceString(context, target);
    }
}
