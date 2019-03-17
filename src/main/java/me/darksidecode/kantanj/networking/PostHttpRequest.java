/*
 * Copyright 2019 DarksideCode
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

package me.darksidecode.kantanj.networking;

import me.darksidecode.kantanj.types.Check;

import java.nio.charset.StandardCharsets;

public class PostHttpRequest extends SimpleHttpRequest {

    private byte[] postData;

    @Override
    public PostHttpRequest done() {
        return this;
    }

    @Override
    public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
        throw new IllegalStateException("cannot change " +
                "request method for " + getClass().getName());
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.POST;
    }

    @Override
    public SimpleHttpRequest doOutput(boolean doOutput) {
        throw new IllegalStateException("cannot change doOutput for " + getClass().getName());
    }

    @Override
    public boolean shouldDoOutput() {
        return true;
    }

    public PostHttpRequest postData(byte[] postData) {
        this.postData = Check.notNull(postData, "post data cannot be null");
        return this;
    }

    public PostHttpRequest postUtf8Data(String postDataUtf8) {
        postData = Check.notNull(postDataUtf8,
                "post data cannot be null").getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public byte[] getPostData() {
        if (postData == null)
            throw new IllegalArgumentException("post data is not set");
        return postData;
    }

}
