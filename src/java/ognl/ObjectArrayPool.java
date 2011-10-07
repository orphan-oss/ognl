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
 * This class was previously intended to produce performance improvment.<br>
 * This hand-made object pooling is now a bottleneck under high load.<br>
 * We now rely on the new jvm garbage collection improvments to handle object allocation efficiently.
 * @deprecated object-pooling now relies on the jvm garbage collection
 */
public final class ObjectArrayPool extends Object
{
    public ObjectArrayPool()
    {
        super();
    }

    public Object[] create(int arraySize)
    {
        return new Object[arraySize];
    }

    public Object[] create(Object singleton)
    {
        Object[]        result = create(1);

        result[0] = singleton;
        return result;
    }

    public Object[] create(Object object1, Object object2)
    {
        Object[]        result = create(2);

        result[0] = object1;
        result[1] = object2;
        return result;
    }

    public Object[] create(Object object1, Object object2, Object object3)
    {
        Object[]        result = create(3);

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        return result;
    }

    public Object[] create(Object object1, Object object2, Object object3, Object object4)
    {
        Object[]        result = create(4);

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        result[3] = object4;
        return result;
    }

    public Object[] create(Object object1, Object object2, Object object3, Object object4, Object object5)
    {
        Object[]        result = create(5);

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        result[3] = object4;
        result[4] = object5;
        return result;
    }

    /**
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycle(Object[] value)
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }
}
