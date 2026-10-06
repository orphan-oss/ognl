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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The deprecated {@code addDefaultContext(..., context)} overloads must keep whatever policy the given context
 * already carries and was not passed explicitly, so a caller's restrictions survive the call (#668).
 */
@SuppressWarnings({"rawtypes", "unchecked", "removal"})
class OgnlAddDefaultContextPolicyTest {

    public static class Bean {
        public String getName() {
            return "name";
        }
    }

    private static class DenyAllMemberAccess extends AbstractMemberAccess {
        @Override
        public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
            return false;
        }
    }

    private Bean bean;
    private MemberAccess memberAccess;
    private ClassResolver classResolver;
    private TypeConverter typeConverter;
    private OgnlContext source;

    @BeforeEach
    void setUp() {
        bean = new Bean();
        memberAccess = new DenyAllMemberAccess();
        classResolver = new DefaultClassResolver();
        typeConverter = new DefaultTypeConverter();
        source = Ognl.createDefaultContext(bean, memberAccess, classResolver, typeConverter);
    }

    @Test
    void rootAndContextKeepsMemberAccessClassResolverAndTypeConverter() {
        OgnlContext context = Ognl.addDefaultContext(bean, source);

        assertSame(memberAccess, context.getMemberAccess());
        assertSame(classResolver, context.getClassResolver());
        assertSame(typeConverter, context.getTypeConverter());
    }

    @Test
    void explicitClassResolverKeepsMemberAccessAndTypeConverter() {
        ClassResolver otherResolver = new DefaultClassResolver();

        OgnlContext context = Ognl.addDefaultContext(bean, otherResolver, source);

        assertSame(memberAccess, context.getMemberAccess());
        assertSame(otherResolver, context.getClassResolver());
        assertSame(typeConverter, context.getTypeConverter());
    }

    @Test
    void explicitClassResolverAndTypeConverterKeepsMemberAccess() {
        ClassResolver otherResolver = new DefaultClassResolver();
        TypeConverter otherConverter = new DefaultTypeConverter();

        OgnlContext context = Ognl.addDefaultContext(bean, otherResolver, otherConverter, source);

        assertSame(memberAccess, context.getMemberAccess());
        assertSame(otherResolver, context.getClassResolver());
        assertSame(otherConverter, context.getTypeConverter());
    }

    @Test
    void restrictiveMemberAccessStillAppliesToTheReturnedContext() {
        OgnlContext context = Ognl.addDefaultContext(bean, source);

        assertThrows(OgnlException.class, () -> Ognl.getValue("name", context, bean));
    }

    @Test
    void contextVariablesAreCarriedOver() {
        source.put("answer", 42);

        OgnlContext context = Ognl.addDefaultContext(bean, source);

        assertEquals(42, context.get("answer"));
    }

    @Test
    void nullContextStillGetsAPublicOnlyDefault() throws OgnlException {
        OgnlContext context = Ognl.addDefaultContext(bean, (OgnlContext) null);

        assertNotNull(context.getMemberAccess());
        assertEquals("name", Ognl.getValue("name", context, bean));
    }

    @Test
    void nullContextWithExplicitClassResolverKeepsThatResolver() throws OgnlException {
        ClassResolver otherResolver = new DefaultClassResolver();

        OgnlContext context = Ognl.addDefaultContext(bean, otherResolver, (OgnlContext) null);

        assertSame(otherResolver, context.getClassResolver());
        assertEquals("name", Ognl.getValue("name", context, bean));
    }

    @Test
    void nullContextWithExplicitClassResolverAndTypeConverterKeepsBoth() throws OgnlException {
        ClassResolver otherResolver = new DefaultClassResolver();
        TypeConverter otherConverter = new DefaultTypeConverter();

        OgnlContext context = Ognl.addDefaultContext(bean, otherResolver, otherConverter, (OgnlContext) null);

        assertSame(otherResolver, context.getClassResolver());
        assertSame(otherConverter, context.getTypeConverter());
        assertEquals("name", Ognl.getValue("name", context, bean));
    }
}
