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
package ognl.test.objects;

public class Component extends Object {
    private URLStorage toDisplay = new URLStorage();
    private Page page = new Page();

    public static class URLStorage extends Object {
        private String pictureUrl = "http://www.picturespace.com/pictures/100";

        public String getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(String value) {
            pictureUrl = value;
        }
    }

    public static class Page extends Object {
        public Object createRelativeAsset(String value) {
            return "/toplevel/" + value;
        }
    }

    public Component() {
        super();
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page value) {
        page = value;
    }

    public URLStorage getToDisplay() {
        return toDisplay;
    }

    public void setToDisplay(URLStorage value) {
        toDisplay = value;
    }
}
