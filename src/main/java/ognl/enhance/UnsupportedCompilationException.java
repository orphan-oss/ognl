/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * and/or LICENSE file distributed with this work for additional
 * information regarding copyright ownership.  The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
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
package ognl.enhance;

/**
 * Thrown during bytecode enhancement conversions of ognl expressions to indicate
 * that a certain expression isn't currently supported as a pure java bytecode enhanced
 * version.
 *
 * <p>
 * If this exception is thrown it is expected that ognl will fall back to default ognl
 * evaluation of the expression.
 * </p>
 */
public class UnsupportedCompilationException extends RuntimeException {

    private static final long serialVersionUID = 37018630558258414L;

    public UnsupportedCompilationException(String message) {
        super(message);
    }

    public UnsupportedCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
