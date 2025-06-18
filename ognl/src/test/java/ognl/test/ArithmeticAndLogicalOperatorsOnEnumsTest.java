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

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArithmeticAndLogicalOperatorsOnEnumsTest {

    private static final String FULLY_QUALIFIED_CLASSNAME = ArithmeticAndLogicalOperatorsOnEnumsTest.class.getName();

    private OgnlContext context;
    private Root root;

    public enum EnumNoBody {ENUM1, ENUM2;}

    public enum EnumEmptyBody {ENUM1 {}, ENUM2 {};}

    public enum EnumBasicBody {
        ENUM1 {
            public final Integer value() {
                return 10;
            }
        },
        ENUM2 {
            public final Integer value() {
                return 20;
            }
        };
    }

    @BeforeEach
    void setUp() {
        context = Ognl.createDefaultContext(null);
        context.put("x", "1");
        context.put("y", new BigDecimal(1));

        root = new Root();
    }

    @Test
    void enumNoBodyEquality() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", context, root));
    }

    @Test
    void enumNoBodyInequality() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1", context, root));
    }

    @Test
    void enumNoBodyEqualityEnum2() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", context, root));
    }

    @Test
    void enumNoBodyInequalityEnum2() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", context, root));
    }

    @Test
    void enumNoBodyDifferentEnums() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", context, root));
    }

    @Test
    void enumNoBodyDifferentEnumsEquality() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM2", context, root));
    }

    @Test
    void enumEmptyBodyEquality() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", context, root));
    }

    @Test
    void enumEmptyBodyInequality() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", context, root));
    }

    @Test
    void enumEmptyBodyEqualityEnum2() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", context, root));
    }

    @Test
    void enumEmptyBodyInequalityEnum2() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", context, root));
    }

    @Test
    void enumEmptyBodyDifferentEnums() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", context, root));
    }

    @Test
    void enumEmptyBodyDifferentEnumsEquality() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM2", context, root));
    }

    @Test
    void enumBasicBodyEquality() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", context, root));
    }

    @Test
    void enumBasicBodyInequality() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", context, root));
    }

    @Test
    void enumBasicBodyEqualityEnum2() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", context, root));
    }

    @Test
    void enumBasicBodyInequalityEnum2() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", context, root));
    }

    @Test
    void enumBasicBodyDifferentEnums() throws OgnlException {
        assertEquals(Boolean.TRUE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", context, root));
    }

    @Test
    void enumBasicBodyDifferentEnumsEquality() throws OgnlException {
        assertEquals(Boolean.FALSE, Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM2", context, root));
    }

    @Test
    void enumNoBodyAndEnumEmptyBodyEquality() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", context, root);
        });
    }

    @Test
    void enumNoBodyAndEnumEmptyBodyInequality() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1", context, root);
        });
    }

    @Test
    void enumNoBodyAndEnumBasicBodyEquality() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", context, root);
        });
    }

    @Test
    void enumNoBodyAndEnumBasicBodyInequality() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumNoBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", context, root);
        });
    }

    @Test
    void enumEmptyBodyAndEnumBasicBodyEquality() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 == @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", context, root);
        });
    }

    @Test
    void enumEmptyBodyAndEnumBasicBodyInequality() {
        assertThrows(IllegalArgumentException.class, () -> {
            Ognl.getValue("@" + FULLY_QUALIFIED_CLASSNAME + "$EnumEmptyBody@ENUM1 != @" + FULLY_QUALIFIED_CLASSNAME + "$EnumBasicBody@ENUM1", context, root);
        });
    }
}
