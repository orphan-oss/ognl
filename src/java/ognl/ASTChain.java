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
import ognl.enhance.OrderedReturn;
import ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Array;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTChain extends SimpleNode implements NodeType, OrderedReturn
{

    private Class _getterClass;
    private Class _setterClass;

    private String _lastExpression;

    private String _coreExpression;

    public ASTChain(int id)
    {
        super(id);
    }

    public ASTChain(OgnlParser p, int id)
    {
        super(p, id);
    }

    public String getLastExpression()
    {
        return _lastExpression;
    }

    public String getCoreExpression()
    {
        return _coreExpression;
    }

    public void jjtClose()
    {
        flattenTree();
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException
    {
        Object result = source;

        for(int i = 0, ilast = _children.length - 1; i <= ilast; ++i)
        {
            boolean handled = false;

            if (i < ilast) {
                if (_children[i] instanceof ASTProperty) {
                    ASTProperty propertyNode = (ASTProperty) _children[i];
                    int indexType = propertyNode.getIndexedPropertyType(context, result);

                    if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (_children[i + 1] instanceof ASTProperty)) {
                        ASTProperty indexNode = (ASTProperty) _children[i + 1];

                        if (indexNode.isIndexedAccess()) {
                            Object index = indexNode.getProperty(context, result);

                            if (index instanceof DynamicSubscript) {
                                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT) {
                                    Object array = propertyNode.getValue(context, result);
                                    int len = Array.getLength(array);

                                    switch(((DynamicSubscript) index).getFlag()) {
                                        case DynamicSubscript.ALL:
                                            result = Array.newInstance(array.getClass().getComponentType(), len);
                                            System.arraycopy(array, 0, result, 0, len);
                                            handled = true;
                                            i++;
                                            break;
                                        case DynamicSubscript.FIRST:
                                            index = new Integer((len > 0) ? 0 : -1);
                                            break;
                                        case DynamicSubscript.MID:
                                            index = new Integer((len > 0) ? (len / 2) : -1);
                                            break;
                                        case DynamicSubscript.LAST:
                                            index = new Integer((len > 0) ? (len - 1) : -1);
                                            break;
                                    }
                                } else {
                                    if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT) { throw new OgnlException(
                                            "DynamicSubscript '" + indexNode
                                            + "' not allowed for object indexed property '" + propertyNode
                                            + "'"); }
                                }
                            }
                            if (!handled) 
                            {
                                result = OgnlRuntime.getIndexedProperty(context, result,
                                                                        propertyNode.getProperty(context, result).toString(),
                                                                        index);
                                handled = true;
                                i++;
                            }
                        }
                    }
                }
            }
            if (!handled)
            {
                result = _children[i].getValue(context, result);
            }
        }
        return result;
    }

    protected void setValueBody(OgnlContext context, Object target, Object value)
            throws OgnlException
    {
        boolean handled = false;

        for(int i = 0, ilast = _children.length - 2; i <= ilast; ++i)
        {
            if (i <= ilast) {
                if (_children[i] instanceof ASTProperty)
                {
                    ASTProperty propertyNode = (ASTProperty) _children[i];
                    int indexType = propertyNode.getIndexedPropertyType(context, target);

                    if ((indexType != OgnlRuntime.INDEXED_PROPERTY_NONE) && (_children[i + 1] instanceof ASTProperty))
                    {
                        ASTProperty indexNode = (ASTProperty) _children[i + 1];

                        if (indexNode.isIndexedAccess())
                        {
                            Object index = indexNode.getProperty(context, target);

                            if (index instanceof DynamicSubscript)
                            {
                                if (indexType == OgnlRuntime.INDEXED_PROPERTY_INT)
                                {
                                    Object array = propertyNode.getValue(context, target);
                                    int len = Array.getLength(array);

                                    switch(((DynamicSubscript) index).getFlag())
                                    {
                                        case DynamicSubscript.ALL:
                                            System.arraycopy(target, 0, value, 0, len);
                                            handled = true;
                                            i++;
                                            break;
                                        case DynamicSubscript.FIRST:
                                            index = new Integer((len > 0) ? 0 : -1);
                                            break;
                                        case DynamicSubscript.MID:
                                            index = new Integer((len > 0) ? (len / 2) : -1);
                                            break;
                                        case DynamicSubscript.LAST:
                                            index = new Integer((len > 0) ? (len - 1) : -1);
                                            break;
                                    }
                                } else
                                {
                                    if (indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT)
                                    {
                                        throw new OgnlException("DynamicSubscript '" + indexNode
                                                                + "' not allowed for object indexed property '" + propertyNode
                                                                + "'");
                                    }
                                }
                            }
                            if (!handled && i == ilast)
                            {
                                OgnlRuntime.setIndexedProperty(context, target,
                                                               propertyNode.getProperty(context, target).toString(),
                                                               index, value);
                                handled = true;
                                i++;
                            } else if (!handled) {
                                target = OgnlRuntime.getIndexedProperty(context, target,
                                                                        propertyNode.getProperty(context, target).toString(),
                                                                        index);
                                i++;
                                continue;
                            }
                        }
                    }
                }
            }
            if (!handled)
            {
                target = _children[i].getValue(context, target);
            }
        }
        if (!handled)
        {
            _children[_children.length - 1].setValue(context, target, value);
        }
    }

    public boolean isSimpleNavigationChain(OgnlContext context)
            throws OgnlException
    {
        boolean result = false;

        if ((_children != null) && (_children.length > 0)) {
            result = true;
            for(int i = 0; result && (i < _children.length); i++) {
                if (_children[i] instanceof SimpleNode) {
                    result = ((SimpleNode) _children[i]).isSimpleProperty(context);
                } else {
                    result = false;
                }
            }
        }
        return result;
    }

    public Class getGetterClass()
    {
        return _getterClass;
    }

    public Class getSetterClass()
    {
        return _setterClass;
    }

    public String toString()
    {
        String result = "";

        if ((_children != null) && (_children.length > 0)) {
            for(int i = 0; i < _children.length; i++) {
                if (i > 0) {
                    if (!(_children[i] instanceof ASTProperty) || !((ASTProperty) _children[i]).isIndexedAccess()) {
                        result = result + ".";
                    }
                }
                result += _children[i].toString();
            }
        }
        return result;
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        String prevChain = (String)context.get("_currentChain");

        if (target != null)
        {
            context.setCurrentObject(target);
            context.setCurrentType(target.getClass());
        }

        String result = "";
        NodeType _lastType = null;
        boolean ordered = false;
        boolean constructor = false;
        try {
            if ((_children != null) && (_children.length > 0))
            {
                for(int i = 0; i < _children.length; i++)
                {
              /*      System.out.println("astchain child: " + _children[i].getClass().getName()
              + " with current object target " + context.getCurrentObject()
              + " current type: " + context.getCurrentType());*/

                    String value = _children[i].toGetSourceString(context, context.getCurrentObject());

//                    System.out.println("astchain child returned >>  " + value + "  <<");

                    if (ASTCtor.class.isInstance(_children[i]))
                        constructor = true;

                    if (NodeType.class.isInstance(_children[i])
                        && ((NodeType)_children[i]).getGetterClass() != null)
                    {
                        _lastType = (NodeType)_children[i];
                    }

//                    System.out.println("Astchain i: " + i + " currentobj : " + context.getCurrentObject() + " and root: " + context.getRoot());
                    if (!ASTVarRef.class.isInstance(_children[i]) && !constructor
                        && !(OrderedReturn.class.isInstance(_children[i]) && ((OrderedReturn)_children[i]).getLastExpression() != null)
                        && (_parent == null || !ASTSequence.class.isInstance(_parent)))
                    {
                        value = OgnlRuntime.getCompiler().castExpression(context, _children[i], value);
                    }

                    /*System.out.println("astchain value now : " + value + " with index " + i
                                       + " current type " + context.getCurrentType() + " current accessor " + context.getCurrentAccessor()
                                       + " prev type " + context.getPreviousType() + " prev accessor " + context.getPreviousAccessor());*/

                    if (OrderedReturn.class.isInstance(_children[i]) && ((OrderedReturn)_children[i]).getLastExpression() != null)
                    {
                        ordered = true;
                        OrderedReturn or = (OrderedReturn)_children[i];

                        if (or.getCoreExpression() == null || or.getCoreExpression().trim().length() <= 0)
                            result = "";
                        else
                            result += or.getCoreExpression();

                        _lastExpression = or.getLastExpression();

                        if (context.get(ExpressionCompiler.PRE_CAST) != null)
                        {
                            _lastExpression = context.remove(ExpressionCompiler.PRE_CAST) + _lastExpression;
                        }
                    } else if (ASTOr.class.isInstance(_children[i])
                               || ASTAnd.class.isInstance(_children[i])
                               || ASTCtor.class.isInstance(_children[i])
                               || (ASTStaticField.class.isInstance(_children[i]) && _parent == null))
                    {
                        context.put("_noRoot", "true");
                        result = value;
                    } else
                    {
                        result += value;
                    }

                    context.put("_currentChain", result);
                }
            }
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        if (_lastType != null)
        {
            _getterClass = _lastType.getGetterClass();
            _setterClass = _lastType.getSetterClass();
        }

        if (ordered)
        {
            _coreExpression = result;
        }

        context.put("_currentChain", prevChain);

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        String prevChain = (String)context.get("_currentChain");
        String prevChild = (String)context.get("_lastChild");

        if (prevChain != null)
            throw new UnsupportedCompilationException("Can't compile nested chain expressions.");

        if (target != null)
        {
            context.setCurrentObject(target);
            context.setCurrentType(target.getClass());
        }

        String result = "";
        NodeType _lastType = null;
        boolean constructor = false;
        try {
            if ((_children != null) && (_children.length > 0))
            {
                if (ASTConst.class.isInstance(_children[0]))
                {
                    throw new UnsupportedCompilationException("Can't modify constant values.");
                }

                for(int i = 0; i < _children.length; i++)
                {
//                    System.out.println("astchain setsource child[" + i + "] : " + _children[i].getClass().getName());

                    if (i == (_children.length -1))
                    {
                        context.put("_lastChild", "true");
                    }

                    String value = _children[i].toSetSourceString(context, context.getCurrentObject());
                    //if (value == null || value.trim().length() <= 0)
                      //  return "";

//                    System.out.println("astchain setter child returned >>  " + value + "  <<");

                    if (ASTCtor.class.isInstance(_children[i]))
                        constructor = true;

                    if (NodeType.class.isInstance(_children[i])
                        && ((NodeType)_children[i]).getGetterClass() != null)
                    {
                        _lastType = (NodeType)_children[i];
                    }

                    if (!ASTVarRef.class.isInstance(_children[i]) && !constructor
                        && !(OrderedReturn.class.isInstance(_children[i]) && ((OrderedReturn)_children[i]).getLastExpression() != null)
                        && (_parent == null || !ASTSequence.class.isInstance(_parent)))
                    {
                        value = OgnlRuntime.getCompiler().castExpression(context, _children[i], value);
                    }

//                    System.out.println("astchain setter after cast value is: " + value);

                    /*if (!constructor && !OrderedReturn.class.isInstance(_children[i])
                        && (_parent == null || !ASTSequence.class.isInstance(_parent)))
                    {
                        value = OgnlRuntime.getCompiler().castExpression(context, _children[i], value);
                    }*/

                    if (ASTOr.class.isInstance(_children[i])
                        || ASTAnd.class.isInstance(_children[i])
                        || ASTCtor.class.isInstance(_children[i])
                        || ASTStaticField.class.isInstance(_children[i])) {
                        context.put("_noRoot", "true");
                        result = value;
                    } else
                        result += value;

                    context.put("_currentChain", result);
                }
            }
        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        context.put("_lastChild", prevChild);
        context.put("_currentChain", prevChain);

        if (_lastType != null)
            _setterClass = _lastType.getSetterClass();

        return result;
    }
}
