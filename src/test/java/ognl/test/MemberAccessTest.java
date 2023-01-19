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
package ognl.test;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Simple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberAccessTest {

    private Simple root;
    private OgnlContext context;

    @ParameterizedTest()
    @ValueSource(
            strings = {"@Runtime@getRuntime()", "bigIntValue", "getBigIntValue()"}
    )
    void shouldBlockAccessReadToSpecificProperties(String expression) {
        assertThrows(OgnlException.class, () -> Ognl.getValue(expression, context, root), "");
    }

    @Test
    void shouldBlockAccessOnWriteToSpecificProperties() {
        assertThrows(OgnlException.class, () -> Ognl.setValue("bigIntValue", context, root, 25), "");
    }

    @Test
    void shouldAllowAccessToOtherProperties() throws Exception {
        assertThat(Ognl.getValue("@System@getProperty('java.specification.version')", context, root))
                .isEqualTo(System.getProperty("java.specification.version"));
        assertThat(Ognl.getValue("stringValue", context, root))
                .isEqualTo(root.getStringValue());
    }

    @BeforeEach
    public void setUp() {
        /* Should allow access at all to the Simple class except for the bigIntValue property */
        DefaultMemberAccess ma = new DefaultMemberAccess(false) {

            public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
                if (target == Runtime.class) {
                    return false;
                }
                if (target instanceof Simple) {
                    if (propertyName != null) {
                        return !propertyName.equals("bigIntValue")
                                && super.isAccessible(context, target, member, propertyName);
                    } else {
                        if (member instanceof Method) {
                            return !member.getName().equals("getBigIntValue")
                                    && !member.getName().equals("setBigIntValue")
                                    && super.isAccessible(context, target, member, null);
                        }
                    }
                }
                return super.isAccessible(context, target, member, propertyName);
            }
        };

        root = new Simple();
        context = Ognl.createDefaultContext(root, ma);
    }
}
