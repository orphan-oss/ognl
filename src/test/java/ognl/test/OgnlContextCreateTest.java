/*
 * Copyright 2020 OGNL Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ognl.test;

import junit.framework.TestCase;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.test.objects.Simple;

import java.util.HashMap;
import java.util.Map;

public class OgnlContextCreateTest extends TestCase {

    public void testCreateContext() throws OgnlException {
        OgnlContext context = Ognl.createDefaultContext(null).withValues(prepareValues());
        assertEquals("test100", Ognl.getValue("#test", context, new Simple()));
    }

    public void testCreateContextWithRoot() throws OgnlException {
        Simple root = new Simple();
        OgnlContext context = Ognl.createDefaultContext(root, prepareValues());
        assertEquals("test100", Ognl.getValue("#test", context, root));
    }

    private Map<String, Object> prepareValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("test", "test100");
        return values;
    }

}
