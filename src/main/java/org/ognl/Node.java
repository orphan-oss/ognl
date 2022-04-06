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
package org.ognl;

import org.ognl.enhance.ExpressionAccessor;

/**
 * JJTree interface for AST nodes, as modified to handle the OGNL operations getValue and
 * setValue.  JJTree's original comment:
 * <p>
 * All AST nodes must implement this interface.  It provides basic
 * machinery for constructing the parent and child relationships
 * between nodes.
 */
public interface Node extends JavaSource {

    /**
     * This method is called after the node has been made the current
     * node.  It indicates that child nodes can now be added to it.
     */
    void jjtOpen();

    /**
     * This method is called after all the child nodes have been
     * added.
     */
    void jjtClose();

    /**
     * This pair of methods are used to inform the node of its
     * parent.
     *
     * @param n the Node to make the parent of this node.
     */
    void jjtSetParent(Node n);

    Node jjtGetParent();

    /**
     * This method tells the node to add its argument to the node's
     * list of children.
     *
     * @param n the Node to add as a child of this node.
     * @param i the position at which to add the child node.
     */
    void jjtAddChild(Node n, int i);

    /**
     * This method returns a child node.  The children are numbered
     * from zero, left to right.
     *
     * @param i the position from which to get the child node.
     * @return the child Node at position i.
     */
    Node jjtGetChild(int i);

    /**
     * Return the number of children the node has.
     *
     * @return the number of children for this node.
     */
    int jjtGetNumChildren();

    // OGNL additions to Node:

    /**
     * Extracts the value from the given source object that is appropriate for this node
     * within the given context.
     *
     * @param context the OgnlContext within which to perform the operation.
     * @param source  the Object from which to get the value.
     * @return the value from the source (as appropriate within the provided context).
     * @throws OgnlException if the value get fails.
     */
    Object getValue(OgnlContext context, Object source) throws OgnlException;

    /**
     * Sets the given value in the given target as appropriate for this node within the
     * given context.
     *
     * @param context the OgnlContext within which to perform the operation.
     * @param target  the Object upon which to set the value.
     * @param value   the Object representing the value to apply to the target.
     * @throws OgnlException if the value set fails.
     */
    void setValue(OgnlContext context, Object target, Object value) throws OgnlException;

    /**
     * Gets the compiled bytecode enhanced expression accessor for getting/setting values.
     *
     * @return The accessor for this node, or null if none has been compiled for it.
     */
    ExpressionAccessor getAccessor();

    /**
     * Sets a new compiled accessor for this node expression.
     *
     * @param accessor The compiled representation of this node.
     */
    void setAccessor(ExpressionAccessor accessor);
}
