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

import ognl.PropertyResolutionFixtures.BothBean;
import ognl.PropertyResolutionFixtures.IndexedBean;
import ognl.PropertyResolutionFixtures.LenientBean;
import ognl.PropertyResolutionFixtures.LombokBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPropertyResolverTest {

    private final PropertyResolver resolver = DefaultPropertyResolver.INSTANCE;

    @Test
    void readPrefersAccessorAndOmitsLenient() throws Exception {
        assertEquals(List.of(
                        PropertyCandidate.accessor(BothBean.class.getMethod("getValue")),
                        PropertyCandidate.field(BothBean.class.getField("value"))),
                resolver.candidates(BothBean.class, "value", PropertyAccessKind.READ));
    }

    @Test
    void readFallsBackToLenientWithoutGetter() throws Exception {
        assertEquals(List.of(PropertyCandidate.lenient(LenientBean.class.getMethod("label"))),
                resolver.candidates(LenientBean.class, "label", PropertyAccessKind.READ));
    }

    @Test
    void writeListsAccessorFieldLenient() throws Exception {
        assertEquals(List.of(
                        PropertyCandidate.accessor(BothBean.class.getMethod("setValue", String.class)),
                        PropertyCandidate.field(BothBean.class.getField("value")),
                        PropertyCandidate.lenient(OgnlRuntime.getWriteMethod(BothBean.class, "value"))),
                resolver.candidates(BothBean.class, "value", PropertyAccessKind.WRITE));
    }

    @Test
    void lombokBaseNames() throws Exception {
        assertEquals(LombokBean.class.getMethod("getURange"),
                resolver.candidates(LombokBean.class, "uRange", PropertyAccessKind.READ).get(0).member());
    }

    @Test
    void indexedReadListsAllOverloads() {
        List<Member> members = resolver.candidates(IndexedBean.class, "items", PropertyAccessKind.INDEXED_READ)
                .stream().map(PropertyCandidate::member).toList();
        assertEquals(OgnlRuntime.getMethods(IndexedBean.class, "getItems", false), members);
        assertEquals(3, members.size());
    }

    @Test
    void objectIndexedWrite() throws Exception {
        List<PropertyCandidate> candidates = resolver.candidates(IndexedBean.class, "attr", PropertyAccessKind.INDEXED_WRITE);
        assertEquals(List.of(PropertyCandidate.indexed(IndexedBean.class.getMethod("setAttr", String.class, String.class))),
                candidates);
    }

    @Test
    void unknownPropertyIsEmpty() {
        for (PropertyAccessKind kind : PropertyAccessKind.values()) {
            assertTrue(resolver.candidates(BothBean.class, "nope", kind).isEmpty(), kind.name());
        }
    }

    @Test
    void candidateRejectsMismatchedStep() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> new PropertyCandidate(BothBean.class.getField("value"), PropertyCandidate.Step.ACCESSOR));
        assertThrows(IllegalArgumentException.class,
                () -> new PropertyCandidate(BothBean.class.getMethod("getValue"), PropertyCandidate.Step.FIELD));
        assertThrows(IllegalArgumentException.class,
                () -> new PropertyCandidate(BothBean.class.getConstructor(), PropertyCandidate.Step.ACCESSOR));
        assertThrows(NullPointerException.class, () -> new PropertyCandidate(null, PropertyCandidate.Step.ACCESSOR));
        assertThrows(NullPointerException.class, () -> new PropertyCandidate(BothBean.class.getMethod("getValue"), null));
    }
}
