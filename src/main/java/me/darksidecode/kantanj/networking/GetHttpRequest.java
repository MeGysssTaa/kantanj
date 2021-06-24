/*
 * Copyright 2021 German Vekhorev (DarksideCode)
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

public class GetHttpRequest extends SimpleHttpRequest {

    @Override
    public GetHttpRequest done() {
        return this;
    }

    @Override
    public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
        throw new IllegalStateException("cannot change " +
                "request method for " + getClass().getName());
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }

    @Override
    public SimpleHttpRequest doInput(boolean doInput) {
        throw new IllegalStateException("cannot change doInput for " + getClass().getName());
    }

    @Override
    public boolean shouldDoInput() {
        return true;
    }

    @Override
    public SimpleHttpRequest doOutput(boolean doOutput) {
        throw new IllegalStateException("cannot change doOutput for " + getClass().getName());
    }

    @Override
    public boolean shouldDoOutput() {
        return false;
    }

}
