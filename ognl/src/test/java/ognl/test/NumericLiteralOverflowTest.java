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

import ognl.ExpressionSyntaxException;
import ognl.Ognl;
import ognl.OgnlException;
import ognl.TokenMgrError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumericLiteralOverflowTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "2147483648",            // decimal above Integer.MAX_VALUE
            "-2147483648",           // lexer tokenises the leading '-' separately,
                                     // so makeInt() sees the bare 2147483648
            "9999999999",            // larger decimal
            "9223372036854775808L",  // long above Long.MAX_VALUE
            "0xFFFFFFFF",            // hex above signed 32-bit range
            "0x100000000",
            "0xFFFFFFFFFFFFFFFFL",
            "1e400",                 // double rounds to infinity
            "1e99999999999999999999",
            "1e-400",                // nonzero double rounds to zero
            "1e39f",                 // float rounds to infinity
            "1e-46f",                // nonzero float rounds to zero
            "1e99999999999999999999b",
            "1e2147483647b",         // BigDecimal scale beyond the literal limit
            "1e-2147483647b",
            "1e10001b",
            "1e-10001b"
    })
    void shouldThrowOgnlExceptionForOutOfRangeNumericLiteral(String expression) {
        assertThrows(OgnlException.class, () -> Ognl.parseExpression(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2147483648", "1e400", "1e99999999999999999999b", "1e10001b"})
    void shouldReportTokenMgrErrorAsReason(String expression) {
        ExpressionSyntaxException e = assertThrows(ExpressionSyntaxException.class, () -> Ognl.parseExpression(expression));
        assertInstanceOf(TokenMgrError.class, e.getReason());
    }

    @Test
    void shouldReportScaleAgainstAllowedRange() {
        ExpressionSyntaxException e = assertThrows(ExpressionSyntaxException.class, () -> Ognl.parseExpression("1e10001b"));
        assertTrue(e.getReason().getMessage().contains("scale -10001 is outside -10000..10000"), e.getReason().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2147483647",            // Integer.MAX_VALUE
            "9223372036854775807L",  // Long.MAX_VALUE
            "0x7FFFFFFF",            // largest in-range hex int
            "1.7976931348623157e308", // Double.MAX_VALUE
            "4.9e-324",              // Double.MIN_VALUE
            "0e400",                 // zero is exact, not an underflow
            "3.4028235e38f",         // Float.MAX_VALUE
            "1.4e-45f",              // Float.MIN_VALUE
            "1e10000b",
            "1e-10000b"
    })
    void shouldParseInRangeNumericLiteral(String expression) {
        assertDoesNotThrow(() -> Ognl.parseExpression(expression));
    }
}
