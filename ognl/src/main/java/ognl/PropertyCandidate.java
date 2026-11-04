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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A member OGNL may use for a property access, with the resolution step that found it.
 *
 * @since 3.5.0
 */
public record PropertyCandidate(Member member, Step step) {

    public enum Step {
        /** A JavaBeans getter or setter. */
        ACCESSOR,
        FIELD,
        /** A method matched by name without the JavaBeans prefix rules. */
        LENIENT,
        /** An indexed getter or setter. */
        INDEXED
    }

    public PropertyCandidate {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(step, "step");
        if (step == Step.FIELD ? !(member instanceof Field) : !(member instanceof Method)) {
            throw new IllegalArgumentException("Step " + step + " does not fit member " + member);
        }
    }

    public static PropertyCandidate accessor(Method method) {
        return new PropertyCandidate(method, Step.ACCESSOR);
    }

    public static PropertyCandidate field(Field field) {
        return new PropertyCandidate(field, Step.FIELD);
    }

    public static PropertyCandidate lenient(Method method) {
        return new PropertyCandidate(method, Step.LENIENT);
    }

    public static PropertyCandidate indexed(Method method) {
        return new PropertyCandidate(method, Step.INDEXED);
    }
}
