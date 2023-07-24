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
package ognl.enhance;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import ognl.ASTAnd;
import ognl.ASTChain;
import ognl.ASTConst;
import ognl.ASTCtor;
import ognl.ASTList;
import ognl.ASTMethod;
import ognl.ASTOr;
import ognl.ASTProperty;
import ognl.ASTRootVarRef;
import ognl.ASTStaticField;
import ognl.ASTStaticMethod;
import ognl.ASTVarRef;
import ognl.ClassResolver;
import ognl.ExpressionNode;
import ognl.Node;
import ognl.OgnlContext;
import ognl.OgnlRuntime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for managing/providing functionality related to compiling generated java source
 * expressions via bytecode enhancements for a given ognl expression.
 */
public class ExpressionCompiler implements OgnlExpressionCompiler {

    /**
     * Key used to store any java source string casting statements in the {@link OgnlContext} during
     * class compilation.
     */
    public static final String PRE_CAST = "_preCast";

    /**
     * {@link ClassLoader} instances.
     */
    protected Map<ClassResolver, EnhancedClassLoader> loaders = new HashMap<>();

    /**
     * Javassist class definition pool.
     */
    protected ClassPool classPool;

    protected int classCounter = 0;

    /**
     * Default constructor, does nothing.
     */
    public ExpressionCompiler() {
    }

    /**
     * Used by {@link #castExpression(OgnlContext, Node, String)} to store the cast java
     * source string in to the current {@link OgnlContext}. This will either add to the existing
     * string present if it already exists or create a new instance and store it using the static key
     * of {@link #PRE_CAST}.
     *
     * @param context The current execution context.
     * @param cast    The java source string to store in to the context.
     */
    public static void addCastString(OgnlContext context, String cast) {
        String value = (String) context.get(PRE_CAST);

        if (value != null)
            value = cast + value;
        else
            value = cast;

        context.put(PRE_CAST, value);
    }


    /**
     * Returns the appropriate casting expression (minus parens) for the specified class type.
     *
     * <p>
     * For instance, if given an {@link Integer} object the string <code>"java.lang.Integer"</code>
     * would be returned. For an array of primitive ints <code>"int[]"</code> and so on..
     * </p>
     *
     * @param type The class to cast a string expression for.
     * @return The converted raw string version of the class name.
     */
    public static String getCastString(Class<?> type) {
        if (type == null)
            return null;

        return type.isArray() ? type.getComponentType().getName() + "[]" : type.getName();
    }

    /**
     * Convenience method called by many different property/method resolving AST types to get a root expression
     * resolving string for the given node.  The callers are mostly ignorant and rely on this method to properly
     * determine if the expression should be cast at all and take the appropriate actions if it should.
     *
     * @param expression The node to check and generate a root expression to if necessary.
     * @param root       The root object for this execution.
     * @param context    The current execution context.
     * @return Either an empty string or a root path java source string compatible with javassist compilations
     * from the root object up to the specified {@link Node}.
     */
    public static String getRootExpression(Node expression, Object root, OgnlContext context) {
        String rootExpr = "";

        if (!shouldCast(expression))
            return rootExpr;

        if ((!(expression instanceof ASTList)
                && !(expression instanceof ASTVarRef)
                && !(expression instanceof ASTStaticMethod)
                && !(expression instanceof ASTStaticField)
                && !(expression instanceof ASTConst)
                && !(expression instanceof ExpressionNode)
                && !(expression instanceof ASTCtor)
                && root != null)
                ||
                (root != null && expression instanceof ASTRootVarRef)) {

            Class<?> castClass = OgnlRuntime.getCompiler().getRootExpressionClass(expression, context);

            if (castClass.isArray() || expression instanceof ASTRootVarRef) {
                rootExpr = "((" + getCastString(castClass) + ")$2)";

                if (expression instanceof ASTProperty && !((ASTProperty) expression).isIndexedAccess()) {
                    rootExpr += ".";
                }
            } else if ((expression instanceof ASTProperty && ((ASTProperty) expression).isIndexedAccess()) || expression instanceof ASTChain) {
                rootExpr = "((" + getCastString(castClass) + ")$2)";
            } else {
                rootExpr = "((" + getCastString(castClass) + ")$2).";
            }
        }

        return rootExpr;
    }

    /**
     * Used by {@link #getRootExpression(Node, Object, OgnlContext)} to determine if the expression
     * needs to be cast at all.
     *
     * @param expression The node to check against.
     * @return Yes if the node type should be cast - false otherwise.
     */
    public static boolean shouldCast(Node expression) {
        if (expression instanceof ASTChain) {
            Node child = expression.jjtGetChild(0);
            if (child instanceof ASTConst
                    || child instanceof ASTStaticMethod
                    || child instanceof ASTStaticField
                    || (child instanceof ASTVarRef && !(child instanceof ASTRootVarRef)))
                return false;
        }

        return !(expression instanceof ASTConst);
    }

    public String castExpression(OgnlContext context, Node expression, String body) {
        // ok - so this looks really f-ed up ...and it is ..eh if you can do it better I'm all for it :)

        if (context.getCurrentAccessor() == null
                || context.getPreviousType() == null
                || context.getCurrentAccessor().isAssignableFrom(context.getPreviousType())
                || (context.getCurrentType() != null
                && context.getCurrentObject() != null
                && context.getCurrentType().isAssignableFrom(context.getCurrentObject().getClass())
                && context.getCurrentAccessor().isAssignableFrom(context.getPreviousType()))
                || body == null || body.trim().length() < 1
                || (context.getCurrentType() != null && context.getCurrentType().isArray()
                && (context.getPreviousType() == null || context.getPreviousType() != Object.class))
                || expression instanceof ASTOr
                || expression instanceof ASTAnd
                || expression instanceof ASTRootVarRef
                || context.getCurrentAccessor() == Class.class
                || (context.get(ExpressionCompiler.PRE_CAST) != null && ((String) context.get(ExpressionCompiler.PRE_CAST)).startsWith("new"))
                || expression instanceof ASTStaticField
                || expression instanceof ASTStaticMethod
                || (expression instanceof OrderedReturn && ((OrderedReturn) expression).getLastExpression() != null))
            return body;

        ExpressionCompiler.addCastString(context, "((" + ExpressionCompiler.getCastString(context.getCurrentAccessor()) + ")");

        return ")" + body;
    }

    public String getClassName(Class<?> clazz) {
        if (clazz.getName().equals("java.util.AbstractList$Itr"))
            return Iterator.class.getName();

        if (Modifier.isPublic(clazz.getModifiers()) && clazz.isInterface())
            return clazz.getName();

        return getClassName(clazz, clazz.getInterfaces());
    }

    private String getClassName(Class<?> clazz, Class<?>[] interfaces) {
        for (Class<?> anInterface : interfaces) {
            if (anInterface.getName().indexOf("util.List") > 0) {
                return anInterface.getName();
            } else if (anInterface.getName().indexOf("Iterator") > 0) {
                return anInterface.getName();
            }
        }

        final Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            final Class<?>[] superClazzInterfaces = superClazz.getInterfaces();
            if (superClazzInterfaces.length > 0)
                return getClassName(superClazz, superClazzInterfaces);
        }

        return clazz.getName();
    }

    public Class<?> getSuperOrInterfaceClass(Method method, Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            Class<?> intClass;

            for (Class<?> anInterface : interfaces) {
                intClass = getSuperOrInterfaceClass(method, anInterface);

                if (intClass != null) {
                    return intClass;
                }

                if (Modifier.isPublic(anInterface.getModifiers()) && containsMethod(method, anInterface)) {
                    return anInterface;
                }
            }
        }

        if (clazz.getSuperclass() != null) {
            Class<?> superClass = getSuperOrInterfaceClass(method, clazz.getSuperclass());

            if (superClass != null)
                return superClass;
        }

        if (Modifier.isPublic(clazz.getModifiers()) && containsMethod(method, clazz))
            return clazz;

        return null;
    }

    /**
     * Helper utility method used by compiler to help resolve class-&gt;method mappings
     * during method calls to {@link OgnlExpressionCompiler#getSuperOrInterfaceClass(java.lang.reflect.Method, Class)}.
     *
     * @param method The method to check for existance of.
     * @param clazz  The class to check for the existance of a matching method definition to the method passed in.
     * @return True if the class contains the specified method, false otherwise.
     */
    public boolean containsMethod(Method method, Class<?> clazz) {
        Method[] methods = clazz.getMethods();

        for (Method value : methods) {
            if (value.getName().equals(method.getName()) && value.getReturnType() == method.getReturnType()) {
                Class<?>[] parms = method.getParameterTypes();

                Class<?>[] methodParams = value.getParameterTypes();
                if (methodParams.length != parms.length)
                    continue;

                boolean parmsMatch = true;
                for (int p = 0; p < parms.length; p++) {
                    if (parms[p] != methodParams[p]) {
                        parmsMatch = false;
                        break;
                    }
                }

                if (!parmsMatch)
                    continue;

                Class<?>[] exceptions = method.getExceptionTypes();

                Class<?>[] methodExceptions = value.getExceptionTypes();
                if (methodExceptions.length != exceptions.length) {
                    continue;
                }

                boolean exceptionsMatch = true;
                for (int e = 0; e < exceptions.length; e++) {
                    if (exceptions[e] != methodExceptions[e]) {
                        exceptionsMatch = false;
                        break;
                    }
                }

                if (!exceptionsMatch)
                    continue;

                return true;
            }
        }

        return false;
    }

    public Class<?> getInterfaceClass(Class<?> clazz) {
        if (clazz.getName().equals("java.util.AbstractList$Itr"))
            return Iterator.class;

        if (Modifier.isPublic(clazz.getModifiers()) && clazz.isInterface() || clazz.isPrimitive()) {
            return clazz;
        }

        return getInterfaceClass(clazz, clazz.getInterfaces());
    }

    private Class<?> getInterfaceClass(Class<?> clazz, Class<?>[] interfaces) {
        for (Class<?> anInterface : interfaces) {
            if (List.class.isAssignableFrom(anInterface))
                return List.class;
            else if (Iterator.class.isAssignableFrom(anInterface))
                return Iterator.class;
            else if (Map.class.isAssignableFrom(anInterface))
                return Map.class;
            else if (Set.class.isAssignableFrom(anInterface))
                return Set.class;
            else if (Collection.class.isAssignableFrom(anInterface))
                return Collection.class;
        }

        final Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            final Class<?>[] superClazzInterfaces = superClazz.getInterfaces();
            if (superClazzInterfaces.length > 0)
                return getInterfaceClass(superClazz, superClazzInterfaces);
        }

        return clazz;
    }

    public Class<?> getRootExpressionClass(Node rootNode, OgnlContext context) {
        if (context.getRoot() == null) {
            return null;
        }

        Class<?> ret = context.getRoot().getClass();

        if (context.getFirstAccessor() != null && context.getFirstAccessor().isInstance(context.getRoot())) {
            ret = context.getFirstAccessor();
        }

        return ret;
    }

    public void compileExpression(OgnlContext context, Node expression, Object root) throws Exception {
        if (expression.getAccessor() != null) {
            return;
        }

        String getBody, setBody;

        EnhancedClassLoader loader = getClassLoader(context);
        ClassPool pool = getClassPool(context, loader);

        CtClass newClass = pool.makeClass(expression.getClass().getName() + expression.hashCode() + classCounter++ + "Accessor");
        newClass.addInterface(getCtClass(ExpressionAccessor.class));

        CtClass ognlClass = getCtClass(OgnlContext.class);
        CtClass objClass = getCtClass(Object.class);

        CtMethod valueGetter = new CtMethod(objClass, "get", new CtClass[]{ognlClass, objClass}, newClass);
        CtMethod valueSetter = new CtMethod(CtClass.voidType, "set", new CtClass[]{ognlClass, objClass, objClass}, newClass);

        CtField nodeMember = null; // will only be set if uncompilable exception is thrown

        CtClass nodeClass = getCtClass(Node.class);
        CtMethod setExpression = null;

        try {
            getBody = generateGetter(context, newClass, pool, valueGetter, expression, root);
        } catch (UnsupportedCompilationException uc) {
            nodeMember = new CtField(nodeClass, "_node", newClass);
            newClass.addField(nodeMember);

            getBody = generateOgnlGetter(newClass, valueGetter, nodeMember);

            setExpression = CtNewMethod.setter("setExpression", nodeMember);
            newClass.addMethod(setExpression);
        }

        try {
            setBody = generateSetter(context, newClass, pool, valueSetter, expression, root);
        } catch (UnsupportedCompilationException uc) {
            if (nodeMember == null) {
                nodeMember = new CtField(nodeClass, "_node", newClass);
                newClass.addField(nodeMember);
            }

            setBody = generateOgnlSetter(newClass, valueSetter, nodeMember);

            if (setExpression == null) {
                setExpression = CtNewMethod.setter("setExpression", nodeMember);
                newClass.addMethod(setExpression);
            }
        }

        try {
            newClass.addConstructor(CtNewConstructor.defaultConstructor(newClass));

            Class<?> clazz = instantiateClass(pool, newClass);
            newClass.detach();

            expression.setAccessor((ExpressionAccessor) clazz.newInstance());

            // need to set expression on node if the field was just defined.
            if (nodeMember != null) {
                expression.getAccessor().setExpression(expression);
            }

        } catch (Throwable t) {
            throw new RuntimeException("Error compiling expression on object " + root
                    + " with expression node " + expression + " getter body: " + getBody
                    + " setter body: " + setBody, t);
        }

    }


    /**
     * Called when <code>newClass</code> has been fully populated and is ready to be instantiated.
     *
     * @param pool     the javassist ClassPool context
     * @param newClass the definition of the new class
     * @return The compiled class
     * @throws CannotCompileException if thrown by javassist
     */
    protected Class<?> instantiateClass(final ClassPool pool, final CtClass newClass) throws CannotCompileException
    {
        return pool.toClass(newClass, OgnlContext.class);
    }


    protected String generateGetter(OgnlContext context, CtClass newClass, ClassPool pool, CtMethod valueGetter, Node expression, Object root) throws Exception {
        String pre = "";
        String post = "";
        String body;

        context.setRoot(root);

        // the ExpressionAccessor API has to reference the generic Object class for get/set operations, so this sets up that known
        // type beforehand

        context.remove(PRE_CAST);

        // Recursively generate the java source code representation of the top level expression

        String getterCode = expression.toGetSourceString(context, root);

        if (getterCode == null || getterCode.trim().length() <= 0
                && !ASTVarRef.class.isAssignableFrom(expression.getClass()))
            getterCode = "null";

        String castExpression = (String) context.get(PRE_CAST);

        if (context.getCurrentType() == null
                || context.getCurrentType().isPrimitive()
                || Character.class.isAssignableFrom(context.getCurrentType())
                || Object.class == context.getCurrentType()) {
            pre = pre + " ($w) (";
            post = post + ")";
        }

        String rootExpr = !getterCode.equals("null") ? getRootExpression(expression, root, context) : "";

        String noRoot = (String) context.remove("_noRoot");
        if (noRoot != null) {
            rootExpr = "";
        }

        createLocalReferences(context, pool, newClass, valueGetter.getParameterTypes());

        if (expression instanceof OrderedReturn && ((OrderedReturn) expression).getLastExpression() != null) {
            body = "{ "
                    + (expression instanceof ASTMethod || expression instanceof ASTChain ? rootExpr : "")
                    + (castExpression != null ? castExpression : "")
                    + ((OrderedReturn) expression).getCoreExpression()
                    + " return " + pre + ((OrderedReturn) expression).getLastExpression()
                    + post
                    + ";}";

        } else {

            body = "{  return "
                    + pre
                    + (castExpression != null ? castExpression : "")
                    + rootExpr
                    + getterCode
                    + post
                    + ";}";
        }

        if (body.contains("..")) {
            body = body.replaceAll("\\.\\.", ".");
        }

        valueGetter.setBody(body);
        newClass.addMethod(valueGetter);

        return body;
    }

    public String createLocalReference(OgnlContext context, String expression, Class<?> type) {
        String referenceName = "ref" + context.incrementLocalReferenceCounter();
        context.addLocalReference(referenceName, new OgnlLocalReference(referenceName, expression, type));

        String castString = "";
        if (!type.isPrimitive())
            castString = "(" + ExpressionCompiler.getCastString(type) + ") ";

        return castString + referenceName + "($$)";
    }

    private void createLocalReferences(OgnlContext context, ClassPool pool, CtClass clazz, CtClass[] params) throws CannotCompileException, NotFoundException {
        Map<String, LocalReference> referenceMap = context.getLocalReferences();
        if (referenceMap == null || referenceMap.size() < 1) {
            return;
        }

        Iterator<LocalReference> it = referenceMap.values().iterator();

        while (it.hasNext()) {
            LocalReference ref = it.next();

            String widener = ref.getType().isPrimitive() ? " " : " ($w) ";

            String body = "{";
            body += " return  " + widener + ref.getExpression() + ";";
            body += "}";

            if (body.contains("..")) {
                body = body.replaceAll("\\.\\.", ".");
            }

            CtMethod method = new CtMethod(pool.get(getCastString(ref.getType())), ref.getName(), params, clazz);
            method.setBody(body);

            clazz.addMethod(method);

            it.remove();
        }
    }

    protected String generateSetter(OgnlContext context, CtClass newClass, ClassPool pool, CtMethod valueSetter, Node expression, Object root) throws Exception {
        if (expression instanceof ExpressionNode || expression instanceof ASTConst) {
            throw new UnsupportedCompilationException("Can't compile expression/constant setters.");
        }

        context.setRoot(root);
        context.remove(PRE_CAST);

        String body;

        String setterCode = expression.toSetSourceString(context, root);
        String castExpression = (String) context.get(PRE_CAST);

        if (setterCode == null || setterCode.trim().length() < 1)
            throw new UnsupportedCompilationException("Can't compile null setter body.");

        if (root == null)
            throw new UnsupportedCompilationException("Can't compile setters with a null root object.");

        String pre = getRootExpression(expression, root, context);

        String noRoot = (String) context.remove("_noRoot");
        if (noRoot != null)
            pre = "";

        createLocalReferences(context, pool, newClass, valueSetter.getParameterTypes());

        body = "{"
                + (castExpression != null ? castExpression : "")
                + pre
                + setterCode + ";}";

        if (body.contains("..")) {
            body = body.replaceAll("\\.\\.", ".");
        }

        valueSetter.setBody(body);
        newClass.addMethod(valueSetter);

        return body;
    }

    /**
     * Fail safe getter creation when normal compilation fails.
     *
     * @param clazz       The javassist class the new method should be attached to.
     * @param valueGetter The method definition the generated code will be contained within.
     * @param node        The root expression node.
     * @return The generated source string for this method, the method will still be
     * added via the javassist API either way so this is really a convenience
     * for exception reporting / debugging.
     * @throws Exception If a javassist error occurs.
     */
    protected String generateOgnlGetter(CtClass clazz, CtMethod valueGetter, CtField node) throws Exception {
        String body = "return " + node.getName() + ".getValue($1, $2);";

        valueGetter.setBody(body);
        clazz.addMethod(valueGetter);

        return body;
    }

    /**
     * Fail safe setter creation when normal compilation fails.
     *
     * @param clazz       The javassist class the new method should be attached to.
     * @param valueSetter The method definition the generated code will be contained within.
     * @param node        The root expression node.
     * @return The generated source string for this method, the method will still be
     * added via the javassist API either way so this is really a convenience
     * for exception reporting / debugging.
     * @throws Exception If a javassist error occurs.
     */
    protected String generateOgnlSetter(CtClass clazz, CtMethod valueSetter, CtField node)
            throws Exception {
        String body = node.getName() + ".setValue($1, $2, $3);";

        valueSetter.setBody(body);
        clazz.addMethod(valueSetter);

        return body;
    }

    /**
     * Creates a {@link ClassLoader} instance compatible with the javassist classloader and normal
     * OGNL class resolving semantics.
     *
     * @param context The current execution context.
     * @return The created {@link ClassLoader} instance.
     */
    protected EnhancedClassLoader getClassLoader(OgnlContext context) {
        EnhancedClassLoader ret = loaders.get(context.getClassResolver());

        if (ret != null) {
            return ret;
        }

        ClassLoader classLoader = new ContextClassLoader(OgnlContext.class.getClassLoader(), context);

        ret = new EnhancedClassLoader(classLoader);
        loaders.put(context.getClassResolver(), ret);

        return ret;
    }

    /**
     * Loads a new class definition via javassist for the specified class.
     *
     * @param searchClass The class to load.
     * @return The javassist class equivalent.
     * @throws NotFoundException When the class definition can't be found.
     */
    protected CtClass getCtClass(Class<?> searchClass) throws NotFoundException {
        return classPool.get(searchClass.getName());
    }

    /**
     * Gets either a new or existing {@link ClassPool} for use in compiling javassist
     * classes.  A new class path object is inserted in to the returned {@link ClassPool} using
     * the passed in <code>loader</code> instance if a new pool needs to be created.
     *
     * @param context The current execution context.
     * @param loader  The {@link ClassLoader} instance to use - as returned by {@link #getClassLoader(OgnlContext)}.
     * @return The existing or new {@link ClassPool} instance.
     */
    protected ClassPool getClassPool(OgnlContext context, EnhancedClassLoader loader) {
        if (classPool != null) {
            return classPool;
        }

        classPool = ClassPool.getDefault();
        classPool.insertClassPath(new LoaderClassPath(loader.getParent()));

        return classPool;
    }
}
