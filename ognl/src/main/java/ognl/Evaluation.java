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

/**
 * An <code>Evaluation</code> is and object that holds a node being evaluated
 * and the source from which that node will take extract its
 * value.  It refers to child evaluations that occur as
 * a result of the nodes' evaluation.
 */
public class Evaluation<C extends OgnlContext<C>> {

    private SimpleNode<C> node;
    private Object source;
    private boolean setOperation;
    private Object result;
    private Throwable exception;
    private Evaluation<C> parent;
    private Evaluation<C> next;
    private Evaluation<C> previous;
    private Evaluation<C> firstChild;
    private Evaluation<C> lastChild;

    /**
     * Constructs a new "get" <code>Evaluation</code> from the node and source given.
     *
     * @param node   a SimpleNode for this Evaluation.
     * @param source a source Object for this Evaluation.
     */
    public Evaluation(SimpleNode<C> node, Object source) {
        super();
        this.node = node;
        this.source = source;
    }

    /**
     * Constructs a new <code>Evaluation</code> from the node and source given.
     * If <code>setOperation</code> is true this <code>Evaluation</code> represents
     * a "set" as opposed to a "get".
     *
     * @param node         a SimpleNode for this Evaluation.
     * @param source       a source Object for this Evaluation.
     * @param setOperation true to identify this Evaluation as a set operation, false to identify it as a get operation.
     */
    public Evaluation(SimpleNode<C> node, Object source, boolean setOperation) {
        this(node, source);
        this.setOperation = setOperation;
    }

    /**
     * Returns the <code>SimpleNode</code> for this <code>Evaluation</code>
     *
     * @return the SimpleNode for this Evaluation.
     */
    public SimpleNode<C> getNode() {
        return node;
    }

    /**
     * Sets the node of the evaluation.  Normally applications do not need to
     * set this.  Notable exceptions to this rule are custom evaluators that
     * choose between navigable objects (as in a multi-root evaluator where
     * the navigable node is chosen at runtime).
     *
     * @param value the SimpleNode to set for this Evaluation.
     */
    public void setNode(SimpleNode<C> value) {
        node = value;
    }

    /**
     * Returns the source object on which this Evaluation operated.
     *
     * @return the source Object operated upon by this Evaluation.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Sets the source of the evaluation.  Normally applications do not need to
     * set this.  Notable exceptions to this rule are custom evaluators that
     * choose between navigable objects (as in a multi-root evaluator where
     * the navigable node is chosen at runtime).
     *
     * @param value the source Object to be set for this Evaluation.
     */
    public void setSource(Object value) {
        source = value;
    }

    /**
     * Returns true if this Evaluation represents a set operation.
     *
     * @return true if this Evaluation represents a set operation, false otherwise.
     */
    public boolean isSetOperation() {
        return setOperation;
    }

    /**
     * Marks the Evaluation as a set operation if the value is true, else
     * marks it as a get operation.
     *
     * @param value true to identify this Evaluation as a set operation, false to identify it as a get operation.
     */
    public void setSetOperation(boolean value) {
        setOperation = value;
    }

    /**
     * Returns the result of the Evaluation, or null if it was a set operation.
     *
     * @return the result of the Evaluation (for a get operation), or null (for a set operation).
     */
    public Object getResult() {
        return result;
    }

    /**
     * Sets the result of the Evaluation.  This method is normally only used
     * interally and should not be set without knowledge of what you are doing.
     *
     * @param value the result Object for this Evaluation.
     */
    public void setResult(Object value) {
        result = value;
    }

    /**
     * Returns the exception that occurred as a result of evaluating the
     * Evaluation, or null if no exception occurred.
     *
     * @return an exception if one occurred during evaluation, or null (no exception) otherwise.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Sets the exception that occurred as a result of evaluating the
     * Evaluation.  This method is normally only used interally and
     * should not be set without knowledge of what you are doing.
     *
     * @param value the Throwable exception that occurred during the evaluation of this Evaluation.
     */
    public void setException(Throwable value) {
        exception = value;
    }

    /**
     * Returns the parent evaluation of this evaluation.  If this returns
     * null then it is is the root evaluation of a tree.
     *
     * @return the parent Evaluation of the current Evaluation, or null if no parent exists.
     */
    public Evaluation<C> getParent() {
        return parent;
    }

    /**
     * Returns the next sibling of this evaluation.  Returns null if
     * this is the last in a chain of evaluations.
     *
     * @return the next sibling Evaluation of the current Evaluation, or null if this is the last Evaluation in a chain.
     */
    public Evaluation<C> getNext() {
        return next;
    }

    /**
     * Returns the previous sibling of this evaluation.  Returns null if
     * this is the first in a chain of evaluations.
     *
     * @return the previous sibling Evaluation of the current Evaluation, or null if this is the first Evaluation in a chain.
     */
    public Evaluation<C> getPrevious() {
        return previous;
    }

    /**
     * Returns the first child of this evaluation.  Returns null if
     * there are no children.
     *
     * @return the first child Evaluation of the current Evaluation, or null if no children exist.
     */
    public Evaluation<C> getFirstChild() {
        return firstChild;
    }

    /**
     * Returns the last child of this evaluation.  Returns null if
     * there are no children.
     *
     * @return the last child Evaluation of the current Evaluation, or null if no children exist.
     */
    public Evaluation<C> getLastChild() {
        return lastChild;
    }

    /**
     * Gets the first descendent.  In any Evaluation tree this will the
     * Evaluation that was first executed.
     *
     * @return the first descendant Evaluation (first Evaluation executed in the tree).
     */
    public Evaluation<C> getFirstDescendant() {
        if (firstChild != null) {
            return firstChild.getFirstDescendant();
        }
        return this;
    }

    /**
     * Gets the last descendent.  In any Evaluation tree this will the
     * Evaluation that was most recently executing.
     *
     * @return the last descendant Evaluation (most recent Evaluation executed in the tree).
     */
    public Evaluation<C> getLastDescendant() {
        if (lastChild != null) {
            return lastChild.getLastDescendant();
        }
        return this;
    }

    /**
     * Adds a child to the list of children of this evaluation.  The
     * parent of the child is set to the receiver and the children
     * references are modified in the receiver to reflect the new child.
     * The lastChild of the receiver is set to the child, and the
     * firstChild is set also if child is the first (or only) child.
     *
     * @param child an Evaluation to add as a child to the current Evaluation.
     */
    public void addChild(Evaluation<C> child) {
        if (firstChild == null) {
            firstChild = lastChild = child;
        } else {
            if (firstChild == lastChild) {
                firstChild.next = child;
                lastChild = child;
                lastChild.previous = firstChild;
            } else {
                child.previous = lastChild;
                lastChild.next = child;
                lastChild = child;
            }
        }
        child.parent = this;
    }

    /**
     * Reinitializes this Evaluation to the parameters specified.
     *
     * @param node         a SimpleNode for this Evaluation.
     * @param source       a source Object for this Evaluation.
     * @param setOperation true to identify this Evaluation as a set operation, false to identify it as a get operation.
     */
    public void init(SimpleNode<C> node, Object source, boolean setOperation) {
        this.node = node;
        this.source = source;
        this.setOperation = setOperation;
        result = null;
        exception = null;
        parent = null;
        next = null;
        previous = null;
        firstChild = null;
        lastChild = null;
    }

    /**
     * Resets this Evaluation to the initial state.
     */
    public void reset() {
        init(null, null, false);
    }

    /**
     * Produces a String value for the Evaluation.  If compact is
     * true then a more compact form of the description only including
     * the node type and unique identifier is shown, else a full
     * description including source and result are shown.  If showChildren
     * is true the child evaluations are printed using the depth string
     * given as a prefix.
     *
     * @param compact      true to generate a compact form of the description for this Evaluation, false for a full form.
     * @param showChildren true to generate descriptions for child Evaluation elements of this Evaluation.
     * @param depth        prefix String to use in front of child Evaluation description output - used when showChildren is true.
     * @return the description of this Evaluation as a String.
     */
    public String toString(boolean compact, boolean showChildren, String depth) {
        StringBuilder stringResult;

        if (compact) {
            stringResult = new StringBuilder(depth + "<" + node.getClass().getName() + " " + System.identityHashCode(this) + ">");
        } else {
            String ss = (source != null) ? source.getClass().getName() : "null",
                    rs = (result != null) ? result.getClass().getName() : "null";

            stringResult = new StringBuilder(depth + "<" + node.getClass().getName() + ": [" + (setOperation ? "set" : "get") + "] source = " + ss + ", result = " + result + " [" + rs + "]>");
        }
        if (showChildren) {
            Evaluation<C> child = firstChild;

            stringResult.append("\n");
            while (child != null) {
                stringResult.append(child.toString(compact, depth + "  "));
                child = child.next;
            }
        }
        return stringResult.toString();
    }

    /**
     * Produces a String value for the Evaluation.  If compact is
     * true then a more compact form of the description only including
     * the node type and unique identifier is shown, else a full
     * description including source and result are shown.  Child
     * evaluations are printed using the depth string given as a prefix.
     *
     * @param compact true to generate a compact form of the description for this Evaluation, false for a full form.
     * @param depth   prefix String to use in front of child Evaluation description output - used when showChildren is true.
     * @return the description of this Evaluation as a String.
     */
    public String toString(boolean compact, String depth) {
        return toString(compact, true, depth);
    }

    /**
     * Returns a String description of the Evaluation.
     *
     * @return the description of this Evaluation as a String.
     */
    public String toString() {
        return toString(false, "");
    }
}
