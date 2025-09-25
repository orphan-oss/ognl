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

public final class EvaluationPool<C extends OgnlContext<C>> {

    /**
     * Returns an Evaluation that contains the node, source and whether it
     * is a set operation.  If there are no Evaluation objects in the
     * pool one is created and returned.
     *
     * @param node   a SimpleNode for an Evaluation to be created.
     * @param source a source Object for an Evaluation to be created.
     * @return an Evaluation based on the parameters.
     */
    public Evaluation<C> create(SimpleNode<C> node, Object source) {
        return create(node, source, false);
    }

    /**
     * Returns an Evaluation that contains the node, source and whether it
     * is a set operation.
     *
     * @param node         a SimpleNode for an Evaluation to be created.
     * @param source       a source Object for an Evaluation to be created.
     * @param setOperation true to identify the Evaluation to be created as a set operation, false to identify it as a get operation.
     * @return an Evaluation based on the parameters.
     */
    public Evaluation<C> create(SimpleNode<C> node, Object source, boolean setOperation) {
        // synchronization is removed as we do not rely anymore on the in-house object pooling
        return new Evaluation<>(node, source, setOperation);
    }
}
