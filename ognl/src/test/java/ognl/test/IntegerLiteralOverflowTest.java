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
import ognl.OgnlException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntegerLiteralOverflowTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "2147483648",            // decimal above Integer.MAX_VALUE
            "-2147483648",           // lexer tokenises the leading '-' separately,
                                     // so makeInt() sees the bare 2147483648
            "9999999999",            // larger decimal
            "9223372036854775808L",  // long above Long.MAX_VALUE
            "0xFFFFFFFF",            // hex above signed 32-bit range
            "0x100000000",
            "0xFFFFFFFFFFFFFFFFL"
    })
    void shouldThrowOgnlExceptionForOutOfRangeIntegerLiteral(String expression) {
        assertThrows(OgnlException.class, () -> Ognl.parseExpression(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2147483647",            // Integer.MAX_VALUE
            "9223372036854775807L",  // Long.MAX_VALUE
            "0x7FFFFFFF",            // largest in-range hex int
            "1e100"                  // float path is unaffected by makeInt()
    })
    void shouldParseInRangeNumericLiteral(String expression) {
        assertDoesNotThrow(() -> Ognl.parseExpression(expression));
    }
}
