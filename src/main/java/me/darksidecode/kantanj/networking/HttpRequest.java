/*
 * Copyright 2020 DarksideCode
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

import java.util.Map;

public interface HttpRequest {

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    String getUserAgent() throws IllegalStateException;

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    RequestMethod getRequestMethod() throws IllegalStateException;

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    boolean isSecured() throws IllegalStateException;

    /**
     * @return empty Map if no special request properties should be set.
     */
    Map<String, String> getRequestProperties();

    /**
     * Default value may vary upon implementation.
     */
    int getConnectTimeout();

    /**
     * Default value may vary upon implementation.
     */
    int getReadTimeout();

    /**
     * Defaults to false.
     */
    boolean shouldFollowRedirects();

    /**
     * Defaults to true.
     */
    boolean shouldDoInput();

    /**
     * Default may vary upon implementation, e.g. false
     * for GetHttpRequest and true for PostHttpRequest.
     */
    boolean shouldDoOutput();

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    String getURL() throws IllegalStateException;

}
