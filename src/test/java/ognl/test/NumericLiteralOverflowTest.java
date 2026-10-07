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
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests that out-of-range numeric literals surface as an {@link OgnlException} rather than a raw
 * {@link NumberFormatException}, as documented by {@link Ognl#parseExpression(String)}.
 * <p>
 * Backport of #590 (issue #583) and the {@code BigDecimal} part of #671 (issue #605).
 */
public class NumericLiteralOverflowTest {

    private static final String[] OUT_OF_RANGE = {
            "2147483648",            // decimal above Integer.MAX_VALUE
            "-2147483648",           // lexer tokenises the leading '-' separately,
                                     // so makeInt() sees the bare 2147483648
            "9999999999",            // larger decimal
            "9223372036854775808L",  // long above Long.MAX_VALUE
            "0xFFFFFFFF",            // hex above signed 32-bit range
            "0x100000000",
            "0xFFFFFFFFFFFFFFFFL",
            "1e99999999999999999999b",
            "1e2147483647b",         // BigDecimal scale beyond the literal limit
            "1e-2147483647b",
            "1e10001b",
            "1e-10001b"
    };

    private static final String[] REASON_IS_TOKEN_MGR_ERROR = {
            "2147483648",
            "1e99999999999999999999b",
            "1e10001b"
    };

    private static final String[] IN_RANGE = {
            "2147483647",            // Integer.MAX_VALUE
            "9223372036854775807L",  // Long.MAX_VALUE
            "0x7FFFFFFF",            // largest in-range hex int
            "1e100",
            "1e10000b",
            "1e-10000b"
    };

    @Test
    public void shouldThrowOgnlExceptionForOutOfRangeNumericLiteral() {
        for (final String expression : OUT_OF_RANGE) {
            assertThrows("Expected an OgnlException for out-of-range literal: " + expression,
                    OgnlException.class, () -> Ognl.parseExpression(expression));
        }
    }

    @Test
    public void shouldReportTokenMgrErrorAsReason() {
        for (final String expression : REASON_IS_TOKEN_MGR_ERROR) {
            ExpressionSyntaxException e = assertThrows(ExpressionSyntaxException.class, () -> Ognl.parseExpression(expression));
            assertTrue("Expected a TokenMgrError reason for: " + expression, e.getReason() instanceof TokenMgrError);
        }
    }

    @Test
    public void shouldReportScaleAgainstAllowedRange() {
        ExpressionSyntaxException e = assertThrows(ExpressionSyntaxException.class, () -> Ognl.parseExpression("1e10001b"));
        assertTrue(e.getReason().getMessage(), e.getReason().getMessage().contains("scale -10001 is outside -10000..10000"));
    }

    @Test
    public void shouldParseInRangeNumericLiteral() throws OgnlException {
        for (String expression : IN_RANGE) {
            assertNotNull("Expected " + expression + " to parse", Ognl.parseExpression(expression));
        }
    }
}
