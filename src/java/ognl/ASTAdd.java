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
import ognl.enhance.UnsupportedCompilationException;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTAdd extends NumericExpression
{
    public ASTAdd(int id) {
        super(id);
    }

    public ASTAdd(OgnlParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        flattenTree();
    }

    protected Object getValueBody( OgnlContext context, Object source ) throws OgnlException
    {
        Object result = _children[0].getValue( context, source );
        for ( int i=1; i < _children.length; ++i )
            result = OgnlOps.add( result, _children[i].getValue(context, source) );
        return result;
    }
    
    public String getExpressionOperator(int index)
    {
        return "+";
    }
    
    boolean isWider(NodeType type, NodeType lastType)
    {
        if (lastType == null)
            return true;
        
        //System.out.println("checking isWider(" + type.getGetterClass() + " , " + lastType.getGetterClass() + ")");
        
        if (String.class.isAssignableFrom(lastType.getGetterClass()))
            return false;
        
        if (String.class.isAssignableFrom(type.getGetterClass()))
            return true;
        
        if (_parent != null && String.class.isAssignableFrom(type.getGetterClass()))
            return true;
        
        if (String.class.isAssignableFrom(lastType.getGetterClass()) && Object.class == type.getGetterClass())
            return false;
        
        if (_parent != null && String.class.isAssignableFrom(lastType.getGetterClass()))
            return false;
        else if (_parent == null && String.class.isAssignableFrom(lastType.getGetterClass()))
            return true;
        else if (_parent == null && String.class.isAssignableFrom(type.getGetterClass()))
            return false;
        
        if (BigDecimal.class.isAssignableFrom(type.getGetterClass())
                || BigInteger.class.isAssignableFrom(type.getGetterClass()))
            return true;
        
        if (BigDecimal.class.isAssignableFrom(lastType.getGetterClass())
                || BigInteger.class.isAssignableFrom(lastType.getGetterClass()))
            return false;
        
        if (Double.class.isAssignableFrom(type.getGetterClass()))
            return true;
        
        if (Integer.class.isAssignableFrom(type.getGetterClass())
                && Double.class.isAssignableFrom(lastType.getGetterClass()))
                return false;
        
        if (Float.class.isAssignableFrom(type.getGetterClass())
                && Integer.class.isAssignableFrom(lastType.getGetterClass()))
                return true;
        
        return true;
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        try {
            String result = "";

            NodeType lastType = null;

            // go through once to determine the ultimate type

            if ((_children != null) && (_children.length > 0)) {

                Class currType = context.getCurrentType();
                Class currAccessor = context.getCurrentAccessor();
                
                Object cast = context.get(ExpressionCompiler.PRE_CAST);
                
                for ( int i = 0; i < _children.length; ++i ) {
                    
                    _children[i].toGetSourceString(context, target);
                    
                    if (NodeType.class.isInstance(_children[i]) && ((NodeType)_children[i]).getGetterClass() != null
                            &&  isWider((NodeType)_children[i], lastType)) {
                        
                        lastType = (NodeType)_children[i];
                    }
                }
                
                context.put(ExpressionCompiler.PRE_CAST, cast);
                
                context.setCurrentType(currType);
                context.setCurrentAccessor(currAccessor);
            }

            // reset context since previous children loop would have changed it
            
            context.setCurrentObject(target);
            
            if ((_children != null) && (_children.length > 0)) {
                for ( int i = 0; i < _children.length; ++i ) {
                    
                    if (i > 0) {
                        result += " " + getExpressionOperator(i) + " ";
                    }

                    String expr = _children[i].toGetSourceString(context, target);

                    if ((expr != null && "null".equals(expr))
                           || (!ASTConst.class.isInstance(_children[i]) && (expr == null || expr.trim().length() <= 0))) {

                        expr = "null";
                    }

                    //System.out.println("astadd child class: " + _children[i].getClass().getName() + " and return expr: " + expr);
                    
                    if (ASTProperty.class.isInstance(_children[i])) {
                        
                        expr = ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), false) + expr;
                        context.setCurrentAccessor(context.getRoot().getClass());
                    } else if (ASTMethod.class.isInstance(_children[i])) {
                        
                        String chain = (String)context.get("_currentChain");
                        
                        String rootExpr = ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), false);

                        //System.out.println("astadd chains is >>" + chain + "<< and rootExpr is >>" + rootExpr + "<<");
                        
                        // dirty fix for overly aggressive casting dot operations
                        if (rootExpr.endsWith(".") && chain != null && chain.startsWith(").")) {
                            chain = chain.substring(1, chain.length());
                        }

                        expr = rootExpr + (chain != null ? chain + "." : "") + expr;
                        
                        context.setCurrentAccessor(context.getRoot().getClass());
                        
                    } else if (_parent == null && ASTChain.class.isInstance(_children[i])) {
                        
                        String rootExpr = ExpressionCompiler.getRootExpression(_children[i], context.getRoot(), false);
                        //System.out.println("astadd rootExpr " + rootExpr + " and expr: " + expr);
                        
                        if (!ASTProperty.class.isInstance(_children[i].jjtGetChild(0)) && rootExpr.endsWith(")") && expr.startsWith(")"))
                            expr = expr.substring(1, expr.length());
                        
                        expr = rootExpr + expr;
                        context.setCurrentAccessor(context.getRoot().getClass());
                        
                        String cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                        if (cast == null)
                            cast = "";
                        
                        expr = cast + expr;
                    }

                    // turn quoted characters into quoted strings
                    
                    if (context.getCurrentType() != null && context.getCurrentType() == Character.class
                        && ASTConst.class.isInstance(_children[i])) {
                        
                        expr = expr.replaceAll("'", "\"");
                    } else {
                        if (!ASTVarRef.class.isAssignableFrom(_children[i].getClass())
                            && !ASTProperty.class.isInstance(_children[i])
                            && !ASTMethod.class.isInstance(_children[i])
                            && !ASTSequence.class.isInstance(_children[i])
                            && !ASTChain.class.isInstance(_children[i])
                            && !NumericExpression.class.isAssignableFrom(_children[i].getClass())) {

                            if (lastType != null && String.class.isAssignableFrom(lastType.getGetterClass()))  {
                                //System.out.println("Input expr >>" + expr + "<<");
                                expr = expr.replaceAll("&quot;", "\"");
                                expr = expr.replaceAll("\"", "'");
                                expr = "\"" + expr + "\"";
                                //System.out.println("Expr now >>" + expr + "<<");
                            }
                        }
                    }
                    
                    if (!OrderedReturn.class.isInstance(_parent)) {
                        result += "(";
                    }
                    
                    result += expr;
                    
                    if ((lastType == null || !String.class.isAssignableFrom(lastType.getGetterClass()))
                            && NodeType.class.isInstance(_children[i]) 
                            && !ASTConst.class.isAssignableFrom(_children[i].getClass())
                            && !NumericExpression.class.isAssignableFrom(_children[i].getClass())) {
                        
                        NodeType ctype = (NodeType)_children[i];
                        if (ctype.getGetterClass() != null 
                                && Number.class.isAssignableFrom(ctype.getGetterClass())
                                && !ASTMethod.class.isAssignableFrom(_children[i].getClass())) {
                            
                            if (ASTVarRef.class.isAssignableFrom(_children[i].getClass()))
                                result += ".";
                            
                            result += OgnlRuntime.getNumericValueGetter(ctype.getGetterClass());
                        }
                    }

                    if (!OrderedReturn.class.isInstance(_parent)) {
                        result += ")";
                    }
                }
            }
            
            if (lastType != null) {
                
                _getterClass = lastType.getGetterClass();
                context.setCurrentType(_getterClass);
                context.setCurrentAccessor(null);
            }
            
            if (_parent == null || ASTSequence.class.isAssignableFrom(_parent.getClass())) {
                
                //if (_getterClass != null && Number.class.isAssignableFrom(_getterClass))
                  //  result = OgnlRuntime.getNumericCast(_getterClass) + "(" + result + ")";
                if (_getterClass != null && String.class.isAssignableFrom(_getterClass))
                    _getterClass = Object.class;
            }

            return result;

        } catch (Throwable t) {
            if (UnsupportedCompilationException.class.isInstance(t))
                throw (UnsupportedCompilationException)t;
            else
                throw new RuntimeException(t);
        }
    }
}
