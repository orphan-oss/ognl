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

/**
    An <code>Evaluation</code> is and object that holds a node being evaluated
    and the source from which that node will take extract its
    value.  It refers to child evaluations that occur as
    a result of the nodes' evaluation.
 */
public class Evaluation extends Object
{
    private SimpleNode          node;
    private Object              source;
    private boolean             setOperation;
    private Object              result;
    private Throwable           exception;
    private Evaluation          parent;
    private Evaluation          next;
    private Evaluation          previous;
    private Evaluation          firstChild;
    private Evaluation          lastChild;

    /**
        Constructs a new "get" <code>Evaluation</code> from the node and source given.
     *
     * @param node a SimpleNode for this Evaluation.
     * @param source a source Object for this Evaluation.
     */
    public Evaluation(SimpleNode node, Object source)
    {
        super();
        this.node = node;
        this.source = source;
    }

    /**
        Constructs a new <code>Evaluation</code> from the node and source given.
        If <code>setOperation</code> is true this <code>Evaluation</code> represents
        a "set" as opposed to a "get".
     *
     * @param node a SimpleNode for this Evaluation.
     * @param source a source Object for this Evaluation.
     * @param setOperation true to identify this Evaluation as a set operation, false to identify it as a get operation.
     */
    public Evaluation(SimpleNode node, Object source, boolean setOperation)
    {
        this(node, source);
        this.setOperation = setOperation;
    }

    /**
        Returns the <code>SimpleNode</code> for this <code>Evaluation</code>
     *
     * @return the SimpleNode for this Evaluation.
     */
    public SimpleNode getNode()
    {
        return node;
    }

    /**
        Sets the node of the evaluation.  Normally applications do not need to
        set this.  Notable exceptions to this rule are custom evaluators that
        choose between navigable objects (as in a multi-root evaluator where
        the navigable node is chosen at runtime).
     *
     * @param value the SimpleNode to set for this Evaluation.
     */
    public void setNode(SimpleNode value)
    {
        node = value;
    }

    /**
        Returns the source object on which this Evaluation operated.
     *
     * @return the source Object operated upon by this Evaluation.
     */
    public Object getSource()
    {
        return source;
    }

    /**
        Sets the source of the evaluation.  Normally applications do not need to
        set this.  Notable exceptions to this rule are custom evaluators that
        choose between navigable objects (as in a multi-root evaluator where
        the navigable node is chosen at runtime).
     *
     * @param value the source Object to be set for this Evaluation.
     */
    public void setSource(Object value)
    {
        source = value;
    }

    /**
        Returns true if this Evaluation represents a set operation.
     *
     * @return true if this Evaluation represents a set operation, false otherwise.
     */
    public boolean isSetOperation()
    {
        return setOperation;
    }

    /**
        Marks the Evaluation as a set operation if the value is true, else
        marks it as a get operation.
     *
     * @param value true to identify this Evaluation as a set operation, false to identify it as a get operation.
     */
    public void setSetOperation(boolean value)
    {
        setOperation = value;
    }

    /**
        Returns the result of the Evaluation, or null if it was a set operation.
     *
     * @return the result of the Evaluation (for a get operation), or null (for a set operation).
     */
    public Object getResult()
    {
        return result;
    }

    /**
        Sets the result of the Evaluation.  This method is normally only used
        interally and should not be set without knowledge of what you are doing.
     *
     * @param value the result Object for this Evaluation.
     */
    public void setResult(Object value)
    {
        result = value;
    }

    /**
        Returns the exception that occurred as a result of evaluating the
        Evaluation, or null if no exception occurred.
     * 
     * @return an exception if one occurred during evaluation, or null (no exception) otherwise.
     */
    public Throwable getException()
    {
        return exception;
    }

    /**
        Sets the exception that occurred as a result of evaluating the
        Evaluation.  This method is normally only used interally and
        should not be set without knowledge of what you are doing.
     *
     * @param value the Throwable exception that occurred during the evaluation of this Evaluation.
     */
    public void setException(Throwable value)
    {
        exception = value;
    }

    /**
        Returns the parent evaluation of this evaluation.  If this returns
        null then it is is the root evaluation of a tree.
     *
     * @return the parent Evaluation of the current Evaluation, or null if no parent exists.
     */
    public Evaluation getParent()
    {
        return parent;
    }

    /**
        Returns the next sibling of this evaluation.  Returns null if
        this is the last in a chain of evaluations.
     *
     * @return the next sibling Evaluation of the current Evaluation, or null if this is the last Evaluation in a chain.
     */
    public Evaluation getNext()
    {
        return next;
    }

    /**
        Returns the previous sibling of this evaluation.  Returns null if
        this is the first in a chain of evaluations.
     *
     * @return the previous sibling Evaluation of the current Evaluation, or null if this is the first Evaluation in a chain.
     */
    public Evaluation getPrevious()
    {
        return previous;
    }

    /**
        Returns the first child of this evaluation.  Returns null if
        there are no children.
     *
     * @return the first child Evaluation of the current Evaluation, or null if no children exist.
     */
    public Evaluation getFirstChild()
    {
        return firstChild;
    }

    /**
        Returns the last child of this evaluation.  Returns null if
        there are no children.
     *
     * @return the last child Evaluation of the current Evaluation, or null if no children exist.
     */
    public Evaluation getLastChild()
    {
        return lastChild;
    }

    /**
        Gets the first descendent.  In any Evaluation tree this will the
        Evaluation that was first executed.
     *
     * @return the first descendant Evaluation (first Evaluation executed in the tree).
     */
    public Evaluation getFirstDescendant()
    {
        if (firstChild != null) {
            return firstChild.getFirstDescendant();
        }
        return this;
    }

    /**
        Gets the last descendent.  In any Evaluation tree this will the
        Evaluation that was most recently executing.
     *
    * @return the last descendant Evaluation (most recent Evaluation executed in the tree).
     */
    public Evaluation getLastDescendant()
    {
        if (lastChild != null) {
            return lastChild.getLastDescendant();
        }
        return this;
    }

    /**
        Adds a child to the list of children of this evaluation.  The
        parent of the child is set to the receiver and the children
        references are modified in the receiver to reflect the new child.
        The lastChild of the receiver is set to the child, and the
        firstChild is set also if child is the first (or only) child.
     *
     * @param child an Evaluation to add as a child to the current Evaluation.
     */
    public void addChild(Evaluation child)
    {
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
        Reinitializes this Evaluation to the parameters specified.
     *
     * @param node a SimpleNode for this Evaluation.
     * @param source a source Object for this Evaluation.
     * @param setOperation true to identify this Evaluation as a set operation, false to identify it as a get operation.
     */
    public void init(SimpleNode node, Object source, boolean setOperation)
    {
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
        Resets this Evaluation to the initial state.
     */
    public void reset()
    {
        init(null, null, false);
    }

    /**
        Produces a String value for the Evaluation.  If compact is
        true then a more compact form of the description only including
        the node type and unique identifier is shown, else a full
        description including source and result are shown.  If showChildren
        is true the child evaluations are printed using the depth string
        given as a prefix.
     *
     * @param compact true to generate a compact form of the description for this Evaluation, false for a full form.
     * @param showChildren true to generate descriptions for child Evaluation elements of this Evaluation.
     * @param depth prefix String to use in front of child Evaluation description output - used when showChildren is true.
     * @return the description of this Evaluation as a String.
     */
    public String toString(boolean compact, boolean showChildren, String depth)
    {
        String      stringResult;

        if (compact) {
            stringResult = depth + "<" + node.getClass().getName() + " " + System.identityHashCode(this) + ">";
        } else {
            String      ss = (source != null) ? source.getClass().getName() : "null",
                        rs = (result != null) ? result.getClass().getName() : "null";

            stringResult = depth + "<" + node.getClass().getName() + ": [" + (setOperation ? "set" : "get") + "] source = " + ss + ", result = " + result + " [" + rs + "]>";
        }
        if (showChildren) {
            Evaluation  child = firstChild;

            stringResult += "\n";
            while (child != null) {
                stringResult += child.toString(compact, depth + "  ");
                child = child.next;
            }
        }
        return stringResult;
    }

    /**
        Produces a String value for the Evaluation.  If compact is
        true then a more compact form of the description only including
        the node type and unique identifier is shown, else a full
        description including source and result are shown.  Child
        evaluations are printed using the depth string given as a prefix.
     *
     * @param compact true to generate a compact form of the description for this Evaluation, false for a full form.
     * @param depth prefix String to use in front of child Evaluation description output - used when showChildren is true.
     * @return the description of this Evaluation as a String.
     */
    public String toString(boolean compact, String depth)
    {
        return toString(compact, true, depth);
    }

    /**
        Returns a String description of the Evaluation.
     *
     * @return the description of this Evaluation as a String.
     */
    public String toString()
    {
        return toString(false, "");
    }
}
