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

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PropertyResolutionFixtures {

    private PropertyResolutionFixtures() {
    }

    public static class LombokBean {
        private String uRange;

        public String getURange() {
            return uRange;
        }

        public void setURange(String uRange) {
            this.uRange = uRange;
        }
    }

    public static class SpecBean {
        private String uRange;

        public String getuRange() {
            return uRange;
        }

        public void setuRange(String uRange) {
            this.uRange = uRange;
        }
    }

    public static class LenientBean {
        public boolean hasChildren() {
            return false;
        }

        public String label() {
            return "lbl";
        }
    }

    public static class FieldBean {
        public String name;
        public final String fixed = "f";
        public static String shared = "s";
    }

    public static class SubFieldBean extends FieldBean {
    }

    public static class GetterOnlyBean {
        public String getTitle() {
            return "t";
        }
    }

    public static class BothBean {
        public String value;
        public int setValueCalls;

        public String getValue() {
            return "g:" + value;
        }

        public void setValue(String v) {
            setValueCalls++;
            this.value = "m:" + v;
        }
    }

    public static class ConvBean {
        public int count;
        public Object lastLenient;

        public void count(Object o) {
            lastLenient = o;
        }
    }

    public static class IndexedBean {
        String[] items = {"a", "b", "c"};
        Map<String, String> attrs = new HashMap<>();
        public int getItemsIntCalls;

        public String[] getItems() {
            return items;
        }

        public String getItems(int i) {
            getItemsIntCalls++;
            return items[i];
        }

        public void setItems(int i, String v) {
            items[i] = v;
        }

        public String getItems(String key) {
            return "key:" + key;
        }

        public String getAttr(String key) {
            return attrs.get(key);
        }

        public void setAttr(String key, String value) {
            attrs.put(key, value);
        }
    }

    public record Point(int x, int y) {
    }

    public static class DenyingMemberAccess<C extends OgnlContext<C>> extends DefaultMemberAccess<C> {
        private final Set<String> denied;

        public DenyingMemberAccess(String... deniedMemberNames) {
            super(false);
            this.denied = Set.of(deniedMemberNames);
        }

        @Override
        public boolean isAccessible(C context, Object target, Member member, String propertyName) {
            if (denied.contains(member.getName())) {
                return false;
            }
            return super.isAccessible(context, target, member, propertyName);
        }
    }

    public static <C extends OgnlContext<C>> C context(Object root, String... deniedMemberNames) {
        return Ognl.createDefaultContext(root, new DenyingMemberAccess<C>(deniedMemberNames));
    }
}
