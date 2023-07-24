/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * and/or LICENSE file distributed with this work for additional
 * information regarding copyright ownership.  The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ognl;

import ognl.enhance.ExpressionAccessor;
import ognl.enhance.OgnlExpressionCompiler;
import ognl.security.OgnlSecurityManager;

import java.io.StringReader;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * This class provides static methods for parsing and interpreting OGNL expressions.
 * The simplest use of the Ognl class is to get the value of an expression from an object, without
 * extra context or pre-parsing.
 * <pre>
 * import ognl.Ognl; import ognl.OgnlException; try { result = Ognl.getValue(expression, root); }
 * catch (OgnlException ex) { // Report error or recover }
 * </pre>
 * <p>
 * This will parse the expression given and evaluate it against the root object given, returning the
 * result. If there is an error in the expression, such as the property is not found, the exception
 * is encapsulated into an {@link OgnlException OgnlException}.
 * <p>
 * Other more sophisticated uses of Ognl can pre-parse expressions. This provides two advantages: in
 * the case of user-supplied expressions it allows you to catch parse errors before evaluation and
 * it allows you to cache parsed expressions into an AST for better speed during repeated use. The
 * pre-parsed expression is always returned as an <code>Object</code> to simplify use for programs
 * that just wish to store the value for repeated use and do not care that it is an AST. If it does
 * care it can always safely cast the value to an <code>AST</code> type.
 * <p>
 * The Ognl class also takes a <i>context map</i> as one of the parameters to the set and get
 * methods. This allows you to put your own variables into the available namespace for OGNL
 * expressions. The default context contains only the <code>#root</code> and <code>#context</code>
 * keys, which are required to be present. The <code>addDefaultContext(Object, Map)</code> method
 * will alter an existing <code>Map</code> to put the defaults in. Here is an example that shows
 * how to extract the <code>documentName</code> property out of the root object and append a
 * string with the current user name in parens:
 * <pre>
 * private Map context = new HashMap(); public void setUserName(String value) {
 * context.put("userName", value); } try { // get value using our own custom context map result =
 * Ognl.getValue("documentName + \" (\" + ((#userName == null) ? \"&lt;nobody&gt;\" : #userName) +
 * \")\"", context, root); } catch (OgnlException ex) { // Report error or recover }
 * </pre>
 */
public abstract class Ognl {

    private static volatile Integer expressionMaxLength = null;
    private static volatile Boolean expressionMaxLengthFrozen = Boolean.FALSE;

    /**
     * Applies a maximum allowed length on OGNL expressions for security reasons.
     *
     * @param expressionMaxLength the OGNL expressions maximum allowed length. Use null (default) to disable this functionality.
     * @throws SecurityException        if the caller is inside OGNL expression itself.
     * @throws IllegalStateException    if the expression maximum allowed length is frozen.
     * @throws IllegalArgumentException if the provided expressionMaxLength is &lt; 0.
     * @since 3.1.26
     */
    public static synchronized void applyExpressionMaxLength(Integer expressionMaxLength) {
        if (System.getSecurityManager() instanceof OgnlSecurityManager) {
            throw new SecurityException("the OGNL expressions maximum allowed length is not accessible inside expression itself!");
        }
        if (expressionMaxLengthFrozen) {
            throw new IllegalStateException("The OGNL expression maximum allowed length has been frozen and cannot be changed.");
        }
        if (expressionMaxLength != null && expressionMaxLength < 0) {
            throw new IllegalArgumentException("The provided OGNL expression maximum allowed length, " + expressionMaxLength + ", is illegal.");
        } else {
            Ognl.expressionMaxLength = expressionMaxLength;
        }
    }

    /**
     * Freezes (prevents updates to) the maximum allowed length on OGNL expressions at the current value.
     * This makes it clear to other OGNL callers that the value should not be changed.
     *
     * @throws SecurityException if the caller is inside OGNL expression itself.
     * @since 3.1.26
     */
    public static synchronized void freezeExpressionMaxLength() {
        if (System.getSecurityManager() instanceof OgnlSecurityManager) {
            throw new SecurityException("Freezing the OGNL expressions maximum allowed length is not accessible inside expression itself!");
        }
        Ognl.expressionMaxLengthFrozen = Boolean.TRUE;
    }

    /**
     * Thaws (allows updates to) the maximum allowed length on OGNL expressions.
     * This makes it clear to other OGNL callers that the value can (again) be changed.
     *
     * @throws SecurityException if the caller is inside OGNL expression itself.
     * @since 3.1.26
     */
    public static synchronized void thawExpressionMaxLength() {
        if (System.getSecurityManager() instanceof OgnlSecurityManager) {
            throw new SecurityException("Thawing the OGNL expressions maximum allowed length is not accessible inside expression itself!");
        }
        Ognl.expressionMaxLengthFrozen = Boolean.FALSE;
    }

    /**
     * Parses the given OGNL expression and returns a tree representation of the expression that can
     * be used by <CODE>Ognl</CODE> static methods.
     *
     * @param expression the OGNL expression to be parsed
     * @return a tree representation of the expression
     * @throws ExpressionSyntaxException if the expression is malformed
     * @throws OgnlException             if there is a pathological environmental problem
     */
    public static Object parseExpression(String expression) throws OgnlException {
        final Integer currentExpressionMaxLength = Ognl.expressionMaxLength;  // Limit access to the volatile variable to a single operation
        if (currentExpressionMaxLength != null && expression != null && expression.length() > currentExpressionMaxLength) {
            throw new OgnlException("Parsing blocked due to security reasons!",
                    new SecurityException("This expression exceeded maximum allowed length: " + expression));
        }
        try {
            assert expression != null;
            OgnlParser parser = new OgnlParser(new StringReader(expression));
            return parser.topLevelExpression();
        } catch (ParseException | TokenMgrError e) {
            throw new ExpressionSyntaxException(expression, e);
        }
    }

    /**
     * Parses and compiles the given expression using the {@link OgnlExpressionCompiler} returned
     * from {@link OgnlRuntime#getCompiler()}.
     *
     * @param context    The context to use.
     * @param root       The root object for the given expression.
     * @param expression The expression to compile.
     * @return The node with a compiled accessor set on {@link Node#getAccessor()} if compilation
     * was successfull. In instances where compilation wasn't possible because of a partially null
     * expression the {@link ExpressionAccessor} instance may be null and the compilation of this expression
     * still possible at some as yet indertermined point in the future.
     * @throws Exception If a compilation error occurs.
     */
    public static Node compileExpression(OgnlContext context, Object root, String expression) throws Exception {
        Node expr = (Node) Ognl.parseExpression(expression);

        OgnlRuntime.compileExpression(context, expr, root);

        return expr;
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     *
     * @param root the root of the object graph
     * @return a new {@link OgnlContext} with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext createDefaultContext(Object root) {
        MemberAccess memberAccess = new AbstractMemberAccess() {
            @Override
            public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
                int modifiers = member.getModifiers();
                return Modifier.isPublic(modifiers);
            }
        };
        return addDefaultContext(root, memberAccess, null, null, null);
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     *
     * @param root the root of the object graph
     * @return a new {@link OgnlContext} with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext createDefaultContext(Object root, Map<Object, Object> values) {
        return createDefaultContext(root).withValues(values);
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     *
     * @param root          The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @return a new OgnlContext with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext createDefaultContext(Object root, ClassResolver classResolver) {
        MemberAccess memberAccess = new AbstractMemberAccess() {
            @Override
            public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
                int modifiers = member.getModifiers();
                return Modifier.isPublic(modifiers);
            }
        };
        return addDefaultContext(root, memberAccess, classResolver, null, null);
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     *
     * @param root          The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param converter     Converter used to convert return types of an expression in to their desired types.
     * @return a new {@link OgnlContext} with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext createDefaultContext(Object root, ClassResolver classResolver, TypeConverter converter) {
        MemberAccess memberAccess = new AbstractMemberAccess() {
            @Override
            public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
                int modifiers = member.getModifiers();
                return Modifier.isPublic(modifiers);
            }
        };
        return addDefaultContext(root, memberAccess, classResolver, converter, null);
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     *
     * @param root          The root of the object graph.
     * @param memberAccess  Java security handling object to determine semantics for accessing normally private/protected
     *                      methods / fields.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param converter     Converter used to convert return types of an expression in to their desired types.
     * @return a new {@link OgnlContext} with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext createDefaultContext(Object root, MemberAccess memberAccess, ClassResolver classResolver, TypeConverter converter) {
        return addDefaultContext(root, memberAccess, classResolver, converter, null);
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     *
     * @param root         The root of the object graph.
     * @param memberAccess Java security handling object to determine semantics for accessing normally private/protected
     *                     methods / fields.
     * @return a new {@link OgnlContext} with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext createDefaultContext(Object root, MemberAccess memberAccess) {
        return addDefaultContext(root, memberAccess, null, null, null);
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given
     * so that cached maps can be used as a context.
     *
     * @param root    the root of the object graph
     * @param context the context to which OGNL context will be added.
     * @return {@link OgnlContext} with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext addDefaultContext(Object root, OgnlContext context) {
        return addDefaultContext(root, context.getMemberAccess(), null, null, context);
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given
     * so that cached maps can be used as a context.
     *
     * @param root          The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param context       The context to which OGNL context will be added.
     * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext addDefaultContext(Object root, ClassResolver classResolver, OgnlContext context) {
        return addDefaultContext(root, context.getMemberAccess(), classResolver, null, context);
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given
     * so that cached maps can be used as a context.
     *
     * @param root          The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param converter     Converter used to convert return types of an expression in to their desired types.
     * @param context       The context to which OGNL context will be added.
     * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext addDefaultContext(Object root, ClassResolver classResolver, TypeConverter converter, OgnlContext context) {
        return addDefaultContext(root, context.getMemberAccess(), classResolver, converter, context);
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given
     * so that cached maps can be used as a context.
     *
     * @param root          the root of the object graph
     * @param memberAccess  Definition for handling private/protected access.
     * @param classResolver The class loading resolver that should be used to resolve class references.
     * @param converter     The type converter to be used by default.
     * @param initialContext       Default context to use, if not an {@link OgnlContext} will be dumped into
     *                      a new {@link OgnlContext} object.
     * @return Context Map with the keys <CODE>root</CODE> and <CODE>context</CODE> set
     * appropriately
     */
    public static OgnlContext addDefaultContext(Object root, MemberAccess memberAccess, ClassResolver classResolver, TypeConverter converter, OgnlContext initialContext) {
        OgnlContext result = new OgnlContext(memberAccess, classResolver, converter, initialContext);
        result.setRoot(root);
        if (initialContext != null) {
            result.addAll(initialContext);
        }
        return result;
    }

    /**
     * Gets the currently configured {@link TypeConverter} for the given context - if any.
     *
     * @param context The context to get the converter from.
     * @return The converter - or null if none found.
     */
    public static TypeConverter getTypeConverter(OgnlContext context) {
        if (context != null) {
            return context.getTypeConverter();
        }
        return null;
    }

    /**
     * Sets the root object to use for all expressions in the given context - doesn't necessarily replace
     * root object instances explicitly passed in to other expression resolving methods on this class.
     *
     * @param context The context to store the root object in.
     * @param root    The root object.
     */
    public static void setRoot(OgnlContext context, Object root) {
        context.setRoot(root);
    }

    /**
     * Gets the stored root object for the given context - if any.
     *
     * @param context The context to get the root object from.
     * @return The root object - or null if none found.
     */
    public static Object getRoot(OgnlContext context) {
        return context.getRoot();
    }

    /**
     * Gets the last {@link Evaluation} executed on the given context.
     *
     * @param context The context to get the evaluation from.
     * @return The {@link Evaluation} - or null if none was found.
     */
    public static Evaluation getLastEvaluation(OgnlContext context) {
        return context.getLastEvaluation();
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object. The
     * default context is set for the given context and root via <CODE>addDefaultContext()</CODE>.
     *
     * @param tree    the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param context the naming context for the evaluation
     * @param root    the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static Object getValue(Object tree, OgnlContext context, Object root) throws OgnlException {
        return getValue(tree, context, root, null);
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object. The
     * default context is set for the given context and root via <CODE>addDefaultContext()</CODE>.
     *
     * @param tree       the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param context    the naming context for the evaluation
     * @param root       the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static Object getValue(Object tree, OgnlContext context, Object root, Class<?> resultType) throws OgnlException {
        Object result;

        Node node = (Node) tree;

        if (node.getAccessor() != null) {
            result = node.getAccessor().get(context, root);
        } else {
            result = node.getValue(context, root);
        }

        if (resultType != null) {
            result = getTypeConverter(context).convertValue(context, root, null, null, result, resultType);
        }
        return result;
    }

    /**
     * Gets the value represented by the given pre-compiled expression on the specified root
     * object.
     *
     * @param expression The pre-compiled expression, as found in {@link Node#getAccessor()}.
     * @param context    The ognl context.
     * @param root       The object to retrieve the expression value from.
     * @return The value.
     */
    public static Object getValue(ExpressionAccessor expression, OgnlContext context, Object root) {
        return expression.get(context, root);
    }

    /**
     * Gets the value represented by the given pre-compiled expression on the specified root
     * object.
     *
     * @param expression The pre-compiled expression, as found in {@link Node#getAccessor()}.
     * @param context    The ognl context.
     * @param root       The object to retrieve the expression value from.
     * @param resultType The desired object type that the return value should be converted to using the {@link #getTypeConverter(OgnlContext)} }.
     * @return The value.
     */
    public static Object getValue(ExpressionAccessor expression, OgnlContext context, Object root, Class<?> resultType) {
        return getTypeConverter(context).convertValue(context, root, null, null, expression.get(context, root), resultType);
    }

    /**
     * Evaluates the given OGNL expression to extract a value from the given root object in a given
     * context
     *
     * @param expression the OGNL expression to be parsed
     * @param context    the naming context for the evaluation
     * @param root       the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     * @see #parseExpression(String)
     * @see #getValue(Object, Object)
     */
    public static Object getValue(String expression, OgnlContext context, Object root) throws OgnlException {
        return getValue(expression, context, root, null);
    }

    /**
     * Evaluates the given OGNL expression to extract a value from the given root object in a given
     * context
     *
     * @param expression the OGNL expression to be parsed
     * @param context    the naming context for the evaluation
     * @param root       the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     * @see #parseExpression(String)
     * @see #getValue(Object, Object)
     */
    public static Object getValue(String expression, OgnlContext context, Object root, Class<?> resultType) throws OgnlException {
        return getValue(parseExpression(expression), context, root, resultType);
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object.
     *
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param root the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static Object getValue(Object tree, Object root)
            throws OgnlException {
        return getValue(tree, root, null);
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object.
     *
     * @param tree       the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param root       the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static Object getValue(Object tree, Object root, Class<?> resultType) throws OgnlException {
        return getValue(tree, createDefaultContext(root), root, resultType);
    }

    /**
     * Convenience method that combines calls to <code> parseExpression </code> and
     * <code> getValue</code>.
     *
     * @param expression the OGNL expression to be parsed
     * @param root       the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws ExpressionSyntaxException        if the expression is malformed
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     * @see #parseExpression(String)
     * @see #getValue(Object, Object)
     */
    public static Object getValue(String expression, Object root) throws OgnlException {
        return getValue(expression, root, null);
    }

    /**
     * Convenience method that combines calls to <code> parseExpression </code> and
     * <code> getValue</code>.
     *
     * @param expression the OGNL expression to be parsed
     * @param root       the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws ExpressionSyntaxException        if the expression is malformed
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     * @see #parseExpression(String)
     * @see #getValue(Object, Object)
     */
    public static Object getValue(String expression, Object root, Class<?> resultType) throws OgnlException {
        return getValue(parseExpression(expression), root, resultType);
    }

    /**
     * Evaluates the given OGNL expression tree to insert a value into the object graph rooted at
     * the given root object. The default context is set for the given context and root via <CODE>addDefaultContext()</CODE>.
     *
     * @param tree    the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param context the naming context for the evaluation
     * @param root    the root object for the OGNL expression
     * @param value   the value to insert into the object graph
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static void setValue(Object tree, OgnlContext context, Object root, Object value) throws OgnlException {
        Node n = (Node) tree;

        if (n.getAccessor() != null) {
            n.getAccessor().set(context, root, value);
            return;
        }

        n.setValue(context, root, value);
    }

    /**
     * Sets the value given using the pre-compiled expression on the specified root
     * object.
     *
     * @param expression The pre-compiled expression, as found in {@link Node#getAccessor()}.
     * @param context    The ognl context.
     * @param root       The object to set the expression value on.
     * @param value      The value to set.
     */
    public static void setValue(ExpressionAccessor expression, OgnlContext context,
                                Object root, Object value) {
        expression.set(context, root, value);
    }

    /**
     * Evaluates the given OGNL expression to insert a value into the object graph rooted at the
     * given root object given the context.
     *
     * @param expression the OGNL expression to be parsed
     * @param root       the root object for the OGNL expression
     * @param context    the naming context for the evaluation
     * @param value      the value to insert into the object graph
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static void setValue(String expression, OgnlContext context, Object root, Object value) throws OgnlException {
        setValue(parseExpression(expression), context, root, value);
    }

    /**
     * Evaluates the given OGNL expression tree to insert a value into the object graph rooted at
     * the given root object.
     *
     * @param tree  the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param root  the root object for the OGNL expression
     * @param value the value to insert into the object graph
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     */
    public static void setValue(Object tree, Object root, Object value)
            throws OgnlException {
        setValue(tree, createDefaultContext(root), root, value);
    }

    /**
     * Convenience method that combines calls to <code> parseExpression </code> and
     * <code> setValue</code>.
     *
     * @param expression the OGNL expression to be parsed
     * @param root       the root object for the OGNL expression
     * @param value      the value to insert into the object graph
     * @throws ExpressionSyntaxException        if the expression is malformed
     * @throws MethodFailedException            if the expression called a method which failed
     * @throws NoSuchPropertyException          if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException                    if there is a pathological environmental problem
     * @see #parseExpression(String)
     * @see #setValue(Object, Object, Object)
     */
    public static void setValue(String expression, Object root, Object value)
            throws OgnlException {
        setValue(parseExpression(expression), root, value);
    }

    /**
     * Checks if the specified {@link Node} instance represents a constant
     * expression.
     *
     * @param tree    The {@link Node} to check.
     * @param context The context to use.
     * @return True if the node is a constant - false otherwise.
     * @throws OgnlException If an error occurs checking the expression.
     */
    public static boolean isConstant(Object tree, OgnlContext context) throws OgnlException {
        return ((SimpleNode) tree).isConstant(addDefaultContext(null, context));
    }

    /**
     * Checks if the specified expression represents a constant expression.
     *
     * @param expression The expression to check.
     * @param context    The context to use.
     * @return True if the node is a constant - false otherwise.
     * @throws OgnlException If an error occurs checking the expression.
     */
    public static boolean isConstant(String expression, OgnlContext context) throws OgnlException {
        return isConstant(parseExpression(expression), context);
    }

    /**
     * Same as {@link #isConstant(String, OgnlContext)} - only the {@link Map} context
     * is created for you.
     *
     * @param tree The {@link Node} to check.
     * @return True if the node represents a constant expression - false otherwise.
     * @throws OgnlException If an exception occurs.
     */
    public static boolean isConstant(Object tree) throws OgnlException {
        return isConstant(tree, createDefaultContext(null));
    }

    /**
     * Same as {@link #isConstant(String, OgnlContext)} - only the {@link Map}
     * instance is created for you.
     *
     * @param expression The expression to check.
     * @return True if the expression represents a constant - false otherwise.
     * @throws OgnlException If an exception occurs.
     */
    public static boolean isConstant(String expression) throws OgnlException {
        return isConstant(parseExpression(expression), createDefaultContext(null));
    }

    public static boolean isSimpleProperty(Object tree, OgnlContext context) throws OgnlException {
        return ((SimpleNode) tree).isSimpleProperty(addDefaultContext(null, context));
    }

    public static boolean isSimpleProperty(String expression, OgnlContext context) throws OgnlException {
        return isSimpleProperty(parseExpression(expression), context);
    }

    public static boolean isSimpleProperty(Object tree) throws OgnlException {
        return isSimpleProperty(tree, createDefaultContext(null));
    }

    public static boolean isSimpleProperty(String expression) throws OgnlException {
        return isSimpleProperty(parseExpression(expression), createDefaultContext(null));
    }

    public static boolean isSimpleNavigationChain(Object tree, OgnlContext context) throws OgnlException {
        return ((SimpleNode) tree).isSimpleNavigationChain(context);
    }

    public static boolean isSimpleNavigationChain(String expression, OgnlContext context) throws OgnlException {
        return isSimpleNavigationChain(parseExpression(expression), context);
    }

    public static boolean isSimpleNavigationChain(Object tree) throws OgnlException {
        return isSimpleNavigationChain(tree, createDefaultContext(null));
    }

    public static boolean isSimpleNavigationChain(String expression) throws OgnlException {
        return isSimpleNavigationChain(parseExpression(expression), createDefaultContext(null));
    }

    /**
     * You can't make one of these.
     */
    private Ognl() {
    }
}
