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

/**
 * Superclass for OGNL exceptions, incorporating an optional encapsulated exception.
 */
public class OgnlException extends Exception {

    private static final long serialVersionUID = 1225801032966287635L;

    /**
     * The root evaluation of the expression when the exception was thrown
     */
    private Evaluation _evaluation;

    /**
     * Constructs an OgnlException with no message or encapsulated exception.
     */
    public OgnlException() {
        this(null, null);
    }

    /**
     * Constructs an OgnlException with the given message but no encapsulated exception.
     *
     * @param msg the exception's detail message
     */
    public OgnlException(String msg) {
        this(msg, null);
    }

    /**
     * Constructs an OgnlException with the given message and encapsulated exception.
     *
     * @param msg    the exception's detail message
     * @param reason the encapsulated exception
     */
    public OgnlException(String msg, Throwable reason) {
        super(msg, reason, true, false);
    }

    /**
     * Constructs an OgnlException with the given message and encapsulated exception,
     * with control on exception suppression and stack trace collection.
     *
     * @param message            the exception's detail message
     * @param reason             the encapsulated exception
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     *                           See {@link java.lang.Throwable#Throwable(String, Throwable, boolean, boolean)} for more info.
     */
    protected OgnlException(String message, Throwable reason, boolean enableSuppression, boolean writableStackTrace) {
        super(message, reason, enableSuppression, writableStackTrace);
    }

    /**
     * Returns the encapsulated exception, or null if there is none.
     *
     * @return the encapsulated exception
     */
    public Throwable getReason() {
        return getCause();
    }

    /**
     * Returns the Evaluation that was the root evaluation when the exception was
     * thrown.
     *
     * @return The {@link Evaluation}.
     */
    public Evaluation getEvaluation() {
        return _evaluation;
    }

    /**
     * Sets the Evaluation that was current when this exception was thrown.
     *
     * @param value The {@link Evaluation}.
     */
    public void setEvaluation(Evaluation value) {
        _evaluation = value;
    }

    /**
     * Returns a string representation of this exception.
     *
     * @return a string representation of this exception
     */
    public String toString() {
        if (getCause() == null) {
            return super.toString();
        }

        return super.toString() + " [" + getCause() + "]";
    }

}
