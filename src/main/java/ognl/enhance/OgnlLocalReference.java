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

import java.util.Objects;

/**
 * Implementation of {@link LocalReference}.
 */
public class OgnlLocalReference implements LocalReference {

    private final String name;
    private final Class<?> clazzType;
    private final String expression;

    public OgnlLocalReference(String name, String expression, Class<?> clazzType) {
        this.name = name;
        this.clazzType = clazzType;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public Class<?> getType() {
        return clazzType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OgnlLocalReference that = (OgnlLocalReference) o;

        if (!Objects.equals(expression, that.expression)) {
            return false;
        }
        if (!Objects.equals(name, that.name)) {
            return false;
        }
        return Objects.equals(clazzType, that.clazzType);
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (clazzType != null ? clazzType.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "LocalReferenceImpl[" +
                "_name='" + name + '\'' +
                '\n' +
                ", _type=" + clazzType +
                '\n' +
                ", _expression='" + expression + '\'' +
                '\n' +
                ']';
    }
}
