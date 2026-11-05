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
 * @since 3.4.18
 */
public final class PropertyCandidate {

    public enum Step {
        /** A JavaBeans getter or setter. */
        ACCESSOR,
        /** A field, found by exact name on the class or a superclass. */
        FIELD,
        /** A method matched by name without the JavaBeans prefix rules. */
        LENIENT,
        /** An indexed getter or setter. */
        INDEXED
    }

    private final Member member;
    private final Step step;

    public PropertyCandidate(Member member, Step step) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(step, "step");
        if (step == Step.FIELD ? !(member instanceof Field) : !(member instanceof Method)) {
            throw new IllegalArgumentException("Step " + step + " does not fit member " + member);
        }
        this.member = member;
        this.step = step;
    }

    public Member member() {
        return member;
    }

    public Step step() {
        return step;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyCandidate)) {
            return false;
        }
        PropertyCandidate other = (PropertyCandidate) o;
        return member.equals(other.member) && step == other.step;
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, step);
    }

    @Override
    public String toString() {
        return "PropertyCandidate[member=" + member + ", step=" + step + "]";
    }
}
