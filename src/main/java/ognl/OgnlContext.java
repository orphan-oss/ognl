/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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

import ognl.enhance.LocalReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class defines the execution context for an OGNL expression
 */
public class OgnlContext {

    private static final String ROOT_CONTEXT_KEY = "root";
    private static final String THIS_CONTEXT_KEY = "this";
    private static final String TRACE_EVALUATIONS_CONTEXT_KEY = "_traceEvaluations";
    private static final String LAST_EVALUATION_CONTEXT_KEY = "_lastEvaluation";
    private static final String KEEP_LAST_EVALUATION_CONTEXT_KEY = "_keepLastEvaluation";
    private static final String PROPERTY_KEY_PREFIX = "ognl";
    private static boolean DEFAULT_TRACE_EVALUATIONS = false;
    private static boolean DEFAULT_KEEP_LAST_EVALUATION = false;

    private static final Map<String, Object> RESERVED_KEYS = new HashMap<>(6);

    private Object root;
    private Object currentObject;
    private Node currentNode;
    private boolean traceEvaluations = DEFAULT_TRACE_EVALUATIONS;
    private Evaluation rootEvaluation;
    private Evaluation currentEvaluation;
    private Evaluation lastEvaluation;
    private boolean keepLastEvaluation = DEFAULT_KEEP_LAST_EVALUATION;

    private final Map<String, Object> internalContext;

    private final ClassResolver classResolver;
    private final TypeConverter typeConverter;
    private final MemberAccess memberAccess;

    static {

        RESERVED_KEYS.put(ROOT_CONTEXT_KEY, null);
        RESERVED_KEYS.put(THIS_CONTEXT_KEY, null);
        RESERVED_KEYS.put(TRACE_EVALUATIONS_CONTEXT_KEY, null);
        RESERVED_KEYS.put(LAST_EVALUATION_CONTEXT_KEY, null);
        RESERVED_KEYS.put(KEEP_LAST_EVALUATION_CONTEXT_KEY, null);

        try {
            String property;
            if ((property = System.getProperty(PROPERTY_KEY_PREFIX + ".traceEvaluations")) != null) {
                DEFAULT_TRACE_EVALUATIONS = Boolean.parseBoolean(property.trim());
            }
            if ((property = System.getProperty(PROPERTY_KEY_PREFIX + ".keepLastEvaluation")) != null) {
                DEFAULT_KEEP_LAST_EVALUATION = Boolean.parseBoolean(property.trim());
            }
        } catch (SecurityException ex) {
            // restricted access environment, just keep defaults
        }
    }

    private final List<Class<?>> typeStack = new ArrayList<>(3);     // size 3 should be enough stack for most expressions
    private final List<Class<?>> accessorStack = new ArrayList<>(3); // size 3 should be enough stack for most expressions

    private int localReferenceCounter = 0;
    private Map<String, LocalReference> localReferenceMap = null;

    /**
     * Constructs a new OgnlContext with the given class resolver, type converter and member access.
     * If any of these parameters is null the default will be used, except <span class="strong">memberAccess which must be non-null</span>.
     *
     * @param classResolver the ClassResolver for a new OgnlContext.
     * @param typeConverter the TypeConverter for a new OgnlContext.
     * @param memberAccess  the MemberAccess for a new OgnlContext.  <span class="strong">Must be non-null</span>.
     */
    public OgnlContext(ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess) {
        // No 'values' map has been specified, so we create one of the default size: 23 entries
        this(memberAccess, classResolver, typeConverter, null);
    }

    /**
     * Constructs a new OgnlContext with the given member access, class resolver, type converter and values.
     * If any of these parameters is null the default will be used, except <span class="strong">memberAccess which must be non-null</span>.
     *
     * @param memberAccess   the MemberAccess for a new OgnlContext.  <span class="strong">Must be non-null</span>.
     * @param classResolver  the ClassResolver for a new OgnlContext.
     * @param typeConverter  the TypeConverter for a new OgnlContext.
     * @param initialContext the initial context of values to provide for a new OgnlContext.
     */
    public OgnlContext(MemberAccess memberAccess, ClassResolver classResolver, TypeConverter typeConverter, OgnlContext initialContext) {
        if (classResolver != null) {
            this.classResolver = classResolver;
        } else {
            this.classResolver = new DefaultClassResolver();
        }
        if (typeConverter != null) {
            this.typeConverter = typeConverter;
        } else {
            this.typeConverter = new DefaultTypeConverter();
        }
        if (memberAccess != null) {
            this.memberAccess = memberAccess;
        } else {
            throw new IllegalArgumentException("MemberAccess implementation must be provided - null not permitted!");
        }

        this.internalContext = new HashMap<>(23);  // No 'values' map has been specified, so we create one of the default size: 23 entries

        if (initialContext != null) {
            this.internalContext.putAll(initialContext.internalContext);
        }
    }

    /**
     * Set (put) the provided value map content into the existing values Map for this OgnlContext.
     *
     * @param values a Map of additional values to put into this OgnlContext.
     */
    public void setValues(Map<Object, Object> values) {
        for (Object k : values.keySet()) {
            internalContext.put(k.toString(), values.get(k));
        }
    }

    /**
     * Get the values Map for this OgnlContext.
     *
     * @return Map of values for this OgnlContext.
     */
    public Map<?, Object> getValues() {
        return internalContext;
    }

    @Deprecated
    public void setClassResolver(ClassResolver ignore) {
        // no-op
    }

    public ClassResolver getClassResolver() {
        return classResolver;
    }

    @Deprecated
    public void setTypeConverter(TypeConverter ignore) {
        // no-op
    }

    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    @Deprecated
    public void setMemberAccess(MemberAccess ignore) {
        // no-op
    }

    public MemberAccess getMemberAccess() {
        return memberAccess;
    }

    public void setRoot(Object value) {
        root = value;
        accessorStack.clear();
        typeStack.clear();
        currentObject = value;

        if (currentObject != null) {
            setCurrentType(currentObject.getClass());
        }
    }

    public Object getRoot() {
        return root;
    }

    public boolean getTraceEvaluations() {
        return traceEvaluations;
    }

    public void setTraceEvaluations(boolean value) {
        traceEvaluations = value;
    }

    public Evaluation getLastEvaluation() {
        return lastEvaluation;
    }

    public void setLastEvaluation(Evaluation value) {
        lastEvaluation = value;
    }

    /**
     * This method can be called when the last evaluation has been used and can be returned for
     * reuse in the free pool maintained by the runtime. This is not a necessary step, but is useful
     * for keeping memory usage down. This will recycle the last evaluation and then set the last
     * evaluation to null.
     *
     * @deprecated since 3.2
     */
    @Deprecated
    public void recycleLastEvaluation() {
        lastEvaluation = null;
    }

    /**
     * Returns true if the last evaluation that was done on this context is retained and available
     * through <code>getLastEvaluation()</code>. The default is true.
     *
     * @return true if the last evaluation for this context is retained and available through <code>getLastEvaluation()</code>, false otherwise.
     */
    public boolean getKeepLastEvaluation() {
        return keepLastEvaluation;
    }

    /**
     * Sets whether the last evaluation that was done on this context is retained and available
     * through <code>getLastEvaluation()</code>. The default is true.
     *
     * @param value true if the last evaluation for this context should be retained and available through <code>getLastEvaluation()</code>, false otherwise.
     */
    public void setKeepLastEvaluation(boolean value) {
        keepLastEvaluation = value;
    }

    public void setCurrentObject(Object value) {
        currentObject = value;
    }

    public Object getCurrentObject() {
        return currentObject;
    }

    public void setCurrentAccessor(Class<?> type) {
        accessorStack.add(type);
    }

    public Class<?> getCurrentAccessor() {
        if (accessorStack.isEmpty())
            return null;

        return accessorStack.get(accessorStack.size() - 1);
    }

    public Class<?> getPreviousAccessor() {
        if (accessorStack.isEmpty())
            return null;

        if (accessorStack.size() > 1)
            return accessorStack.get(accessorStack.size() - 2);
        else
            return null;
    }

    public Class<?> getFirstAccessor() {
        if (accessorStack.isEmpty())
            return null;

        return accessorStack.get(0);
    }

    /**
     * Gets the current class type being evaluated on the stack, as set by {@link #setCurrentType(Class)}.
     *
     * @return The current object type, may be null.
     */
    public Class<?> getCurrentType() {
        if (typeStack.isEmpty())
            return null;

        return typeStack.get(typeStack.size() - 1);
    }

    public void setCurrentType(Class<?> type) {
        typeStack.add(type);
    }

    /**
     * Represents the last known object type on the evaluation stack, will be the value of
     * the last known {@link #getCurrentType()}.
     *
     * @return The previous type of object on the stack, may be null.
     */
    public Class<?> getPreviousType() {
        if (typeStack.isEmpty())
            return null;

        if (typeStack.size() > 1)
            return typeStack.get(typeStack.size() - 2);
        else
            return null;
    }

    public void setPreviousType(Class<?> type) {
        if (typeStack.isEmpty() || typeStack.size() < 2)
            return;

        typeStack.set(typeStack.size() - 2, type);
    }

    public Class<?> getFirstType() {
        if (typeStack.isEmpty())
            return null;

        return typeStack.get(0);
    }

    public void setCurrentNode(Node value) {
        currentNode = value;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Gets the current Evaluation from the top of the stack. This is the Evaluation that is in
     * process of evaluating.
     *
     * @return the current Evaluation from the top of the stack (being evaluated).
     */
    public Evaluation getCurrentEvaluation() {
        return currentEvaluation;
    }

    public void setCurrentEvaluation(Evaluation value) {
        currentEvaluation = value;
    }

    /**
     * Gets the root of the evaluation stack. This Evaluation contains the node representing the
     * root expression and the source is the root source object.
     *
     * @return the root Evaluation from the stack (the root expression node).
     */
    public Evaluation getRootEvaluation() {
        return rootEvaluation;
    }

    public void setRootEvaluation(Evaluation value) {
        rootEvaluation = value;
    }

    /**
     * Returns the Evaluation at the relative index given. This should be zero or a negative number
     * as a relative reference back up the evaluation stack. Therefore getEvaluation(0) returns the
     * current Evaluation.
     *
     * @param relativeIndex the relative index for the Evaluation to retrieve from the stack (with 0 being the current Evaluation).  relativeIndex should be &lt;= 0.
     * @return the Evaluation at relativeIndex, or null if relativeIndex is &gt; 0.
     */
    public Evaluation getEvaluation(int relativeIndex) {
        Evaluation result = null;

        if (relativeIndex <= 0) {
            result = currentEvaluation;
            while ((++relativeIndex < 0) && (result != null)) {
                result = result.getParent();
            }
        }
        return result;
    }

    /**
     * Pushes a new Evaluation onto the stack. This is done before a node evaluates. When evaluation
     * is complete it should be popped from the stack via <code>popEvaluation()</code>.
     *
     * @param value the Evaluation to push onto the stack.
     */
    public void pushEvaluation(Evaluation value) {
        if (currentEvaluation != null) {
            currentEvaluation.addChild(value);
        } else {
            setRootEvaluation(value);
        }
        setCurrentEvaluation(value);
    }

    /**
     * Pops the current Evaluation off of the top of the stack. This is done after a node has
     * completed its evaluation.
     *
     * @return the Evaluation popped from the top of the stack.
     */
    public Evaluation popEvaluation() {
        Evaluation result;

        result = currentEvaluation;
        setCurrentEvaluation(result.getParent());
        if (currentEvaluation == null) {
            setLastEvaluation(getKeepLastEvaluation() ? result : null);
            setRootEvaluation(null);
            setCurrentNode(null);
        }
        return result;
    }

    public int incrementLocalReferenceCounter() {
        return ++localReferenceCounter;
    }

    public void addLocalReference(String key, LocalReference reference) {
        if (localReferenceMap == null) {
            localReferenceMap = new LinkedHashMap<>();
        }

        localReferenceMap.put(key, reference);
    }

    public Map<String, LocalReference> getLocalReferences() {
        return Collections.unmodifiableMap(localReferenceMap);
    }

    /* ================= Map interface ================= */
    public int size() {
        return internalContext.size();
    }

    public boolean isEmpty() {
        return internalContext.isEmpty();
    }

    public boolean containsKey(String key) {
        return internalContext.containsKey(key);
    }

    public boolean containsValue(String value) {
        return internalContext.containsValue(value);
    }

    public Object get(String key) {
        Object result;

        if (RESERVED_KEYS.containsKey(key)) {
            switch (key) {
                case OgnlContext.THIS_CONTEXT_KEY:
                    result = getCurrentObject();
                    break;
                case OgnlContext.ROOT_CONTEXT_KEY:
                    result = getRoot();
                    break;
                case OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY:
                    result = getTraceEvaluations() ? Boolean.TRUE : Boolean.FALSE;
                    break;
                case OgnlContext.LAST_EVALUATION_CONTEXT_KEY:
                    result = getLastEvaluation();
                    break;
                case OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY:
                    result = getKeepLastEvaluation() ? Boolean.TRUE : Boolean.FALSE;
                    break;
                default:
                    throw new IllegalArgumentException("unknown reserved key '" + key + "'");
            }
        } else {
            result = internalContext.get(key);
        }
        return result;
    }

    public Object put(String key, Object value) {
        Object result;

        if (RESERVED_KEYS.containsKey(key)) {
            switch (key) {
                case OgnlContext.THIS_CONTEXT_KEY:
                    result = getCurrentObject();
                    setCurrentObject(value);
                    break;
                case OgnlContext.ROOT_CONTEXT_KEY:
                    result = getRoot();
                    setRoot(value);
                    break;
                case OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY:
                    result = getTraceEvaluations() ? Boolean.TRUE : Boolean.FALSE;
                    setTraceEvaluations(OgnlOps.booleanValue(value));
                    break;
                case OgnlContext.LAST_EVALUATION_CONTEXT_KEY:
                    result = getLastEvaluation();
                    lastEvaluation = (Evaluation) value;
                    break;
                case OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY:
                    result = getKeepLastEvaluation() ? Boolean.TRUE : Boolean.FALSE;
                    setKeepLastEvaluation(OgnlOps.booleanValue(value));
                    break;
                default:
                    throw new IllegalArgumentException("unknown reserved key '" + key + "'");
            }
        } else {
            result = internalContext.put(key, value);
        }

        return result;
    }

    public Object remove(String key) {
        Object result;

        if (RESERVED_KEYS.containsKey(key)) {
            switch (key) {
                case OgnlContext.THIS_CONTEXT_KEY:
                    result = getCurrentObject();
                    setCurrentObject(null);
                    break;
                case OgnlContext.ROOT_CONTEXT_KEY:
                    result = getRoot();
                    setRoot(null);
                    break;
                case OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY:
                    throw new IllegalArgumentException("Can't remove "
                            + OgnlContext.TRACE_EVALUATIONS_CONTEXT_KEY + " from context");
                case OgnlContext.LAST_EVALUATION_CONTEXT_KEY:
                    result = lastEvaluation;
                    setLastEvaluation(null);
                    break;
                case OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY:
                    throw new IllegalArgumentException("Can't remove "
                            + OgnlContext.KEEP_LAST_EVALUATION_CONTEXT_KEY + " from context");
                default:
                    throw new IllegalArgumentException("Unknown reserved key '" + key + "'");
            }
        } else {
            result = internalContext.remove(key);
        }
        return result;
    }

    public void putAll(Map<String, ?> t) {
        for (Map.Entry<String, ?> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        internalContext.clear();
        typeStack.clear();
        accessorStack.clear();

        localReferenceCounter = 0;
        if (localReferenceMap != null) {
            localReferenceMap.clear();
        }

        setRoot(null);
        setCurrentObject(null);
        setRootEvaluation(null);
        setCurrentEvaluation(null);
        setLastEvaluation(null);
        setCurrentNode(null);
    }

    public Collection<Object> values() {
        return Collections.unmodifiableCollection(internalContext.values());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OgnlContext)) {
            return false;
        }
        OgnlContext otherContext = (OgnlContext) other;
        return internalContext.equals(otherContext.internalContext);
    }

    @Override
    public int hashCode() {
        return internalContext.hashCode();
    }

    public void addAll(OgnlContext context) {
        this.internalContext.putAll(context.internalContext);
    }
}
