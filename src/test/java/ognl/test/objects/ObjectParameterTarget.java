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
package ognl.test.objects;

/**
 * Every entry point takes a bare {@code Object}, so the compiler has to box the argument rather than convert it.
 */
public class ObjectParameterTarget {

    private final Object value;
    private String wrapped;
    private String extra;

    public ObjectParameterTarget() {
        this(null);
    }

    public ObjectParameterTarget(Object value) {
        this.value = value;
    }

    public String getDescription() {
        return describe(value);
    }

    public String take(Object v) {
        return describe(v);
    }

    public ObjectParameterTarget wrap(Object v) {
        wrapped = describe(v);
        return this;
    }

    public String getWrapped() {
        return wrapped;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    private static String describe(Object v) {
        return v.getClass().getSimpleName() + "=" + v;
    }
}
