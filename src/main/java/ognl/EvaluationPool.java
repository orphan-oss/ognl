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

import java.util.*;

public final class EvaluationPool extends Object
{
    public EvaluationPool()
    {
        this(0);
    }

    public EvaluationPool(int initialSize)
    {
        super();
        // do not init object pooling
    }

    /**
        Returns an Evaluation that contains the node, source and whether it
        is a set operation.  If there are no Evaluation objects in the
        pool one is created and returned.
     */
    public Evaluation create(SimpleNode node, Object source)
    {
        return create(node, source, false);
    }

    /**
        Returns an Evaluation that contains the node, source and whether it
        is a set operation. 
     */
    public Evaluation create(SimpleNode node, Object source, boolean setOperation)
    {
        // synchronization is removed as we do not rely anymore on the in-house object pooling
        return new Evaluation(node, source, setOperation);
    }

    /**
        Recycles an Evaluation
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycle(Evaluation value)
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }

    /**
        Recycles an of Evaluation and all of it's siblings
        and children.
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycleAll(Evaluation value)
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }

    /**
        Recycles a List of Evaluation objects
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycleAll(List value)
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }

    /**
        Returns the number of items in the pool
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getSize()
    {
        return 0;
    }

    /**
        Returns the number of items this pool has created since
        it's construction.
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getCreatedCount()
    {
        return 0;
    }

    /**
        Returns the number of items this pool has recovered from
        the pool since its construction.
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getRecoveredCount()
    {
        return 0;
    }

    /**
        Returns the number of items this pool has recycled since
        it's construction.
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getRecycledCount()
    {
        return 0;
    }
}
