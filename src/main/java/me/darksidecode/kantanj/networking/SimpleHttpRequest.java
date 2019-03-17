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

import me.darksidecode.kantanj.formatting.Formatting;
import me.darksidecode.kantanj.types.Check;

import java.util.HashMap;
import java.util.Map;

public class SimpleHttpRequest implements HttpRequest {

    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    public static final int DEFAULT_READ_TIMEOUT    = 5000;

    private String userAgent;

    private RequestMethod requestMethod;

    private String baseUrl;

    private String path;

    private final Map<String, String> queryParams = new HashMap<>();

    private final Map<String, String> requestProperties = new HashMap<>();

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout    = DEFAULT_READ_TIMEOUT;

    private boolean followRedirects;
    private boolean doInput = true, doOutput;

    public SimpleHttpRequest done() {
        return this;
    }

    public SimpleHttpRequest userAgent(String userAgent) {
        return userAgent(userAgent, false);
    }

    public SimpleHttpRequest userAgent(String userAgent, boolean removeJavaSuffix) {
        this.userAgent = Check.notNull(userAgent, "userAgent cannot be null");

        if (removeJavaSuffix)
            Networking.removeJavaUserAgentSuffixGlobally();
        return this;
    }

    public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
        Check.state(this.requestMethod != null, "requestMethod already set");

        this.requestMethod = Check.notNull(requestMethod, "requestMethod cannot be null");
        return this;
    }

    public SimpleHttpRequest baseUrl(String baseUrl) {
        Check.state(this.baseUrl != null, "baseUrl already set");
        Check.notNull(baseUrl, "baseUrl cannot be null");

        if ((!(baseUrl.startsWith("http://"))) && (!(baseUrl.startsWith("https://"))))
            throw new IllegalArgumentException("non-http protocol in baseUrl: " + baseUrl);

        this.baseUrl = (baseUrl.endsWith("/"))
                ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return this;
    }

    public SimpleHttpRequest path(String path) {
        Check.state(this.path != null, "path already set");
        Check.notNull(path, "path cannot be null");

        if (path.isEmpty())
            throw new IllegalArgumentException("path cannot be empty");

        if (path.charAt(0) == '/') {
            if (path.length() == 1)
                throw new IllegalArgumentException("path cannot consist of a single slash");
            else
                // Remove the leading slash to avoid requests like "example.com//path".
                path = path.substring(1);
        }

        this.path = path;
        return this;
    }

    public SimpleHttpRequest queryParam(String queryParam, Object value) {
        if (queryParams.containsKey(Check.notNull(queryParam, "queryParam key cannot be null")))
            throw new IllegalStateException("duplicate query param \"" + queryParam + "\"");

        queryParams.put(queryParam, Check.notNull(
                value, "queryParam value cannot be null").toString());
        return this;
    }

    public SimpleHttpRequest encodedQueryParam(String queryParam, String valueToEncode) {
        return queryParam(queryParam, Formatting.urlEncodeUtf8(valueToEncode));
    }

    public SimpleHttpRequest queryParams(Object... queryParams) {
        if ((Check.notEmpty(queryParams,
                "queryParams cannot be empty").length % 2) != 0)
            throw new IllegalArgumentException("queryParams length must be " +
                    "a multple of two, with query params keys (String) at even " +
                    "indexes and their values (Object) at the adjacent odd ones");

        for (int i = 0; i < queryParams.length - 1; i += 2) {
            Object objAtEven = Check.notNull(queryParams[i],
                    "query param key cannot be null (index %s)", i);
            String qpKey = Check.instanceOf(objAtEven, String.class,
                    "even indexes must contain query params keys; " +
                            "expected String at index %s, but got %s",
                            i, objAtEven.getClass().getName());

            queryParam(qpKey, queryParams[i+1]); // value notNull check is done inside queryParam(...)
        }

        return this;
    }

    public SimpleHttpRequest requestProperty(String propertyName, String value) {
        if (Check.notNull(propertyName,
                "propertyName cannot be null").equals("User-Agent"))
            throw new IllegalArgumentException("User-Agent must be set using method userAgent(...)");

        requestProperties.put(propertyName,
                Check.notNull(value, "value cannot be null"));
        return this;
    }

    public SimpleHttpRequest connectTimeout(int timeoutMillis) {
        if (timeoutMillis < 0)
            throw new IllegalArgumentException("timeoutMillis must be a positive integer");

        connectTimeout = timeoutMillis;
        return this;
    }

    public SimpleHttpRequest readTimeout(int timeoutMillis) {
        if (timeoutMillis < 0)
            throw new IllegalArgumentException("timeoutMillis must be a positive integer");

        readTimeout = timeoutMillis;
        return this;
    }

    public SimpleHttpRequest followRedirects(boolean follow) {
        followRedirects = follow;
        return this;
    }

    public SimpleHttpRequest doInput(boolean doInput) {
        this.doInput = doInput;
        return this;
    }

    public SimpleHttpRequest doOutput(boolean doOutput) {
        this.doOutput = doOutput;
        return this;
    }

    @Override
    public String getUserAgent() {
        Check.state(userAgent == null, "userAgent not set");
        return userAgent;
    }

    @Override
    public RequestMethod getRequestMethod() {
        Check.state(requestMethod == null, "requestMethod not set");
        return requestMethod;
    }

    @Override
    public boolean isSecured() {
        Check.state(baseUrl == null, "baseUrl not set");
        return baseUrl.startsWith("https://");
    }

    @Override
    public Map<String, String> getRequestProperties() {
        return requestProperties;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public boolean shouldFollowRedirects() {
        return followRedirects;
    }

    @Override
    public boolean shouldDoInput() {
        return doInput;
    }

    @Override
    public boolean shouldDoOutput() {
        return doOutput;
    }

    @Override
    public String getURL() throws IllegalStateException {
        Check.state(baseUrl == null, "baseUrl not set");
        Check.state(path == null, "path not set");

        StringBuilder url = new StringBuilder(baseUrl + '/' + path);
        boolean firstQParam = true;

        for (String queryParam : queryParams.keySet()) {
            char appendChar = '&';

            if (firstQParam) {
                appendChar = '?';
                firstQParam = false;
            }

            url.append(appendChar).
                append(queryParam).
                append('=').
                append(queryParams.get(queryParam));
        }

        return url.toString();
    }

    public GetHttpRequest asGetRequest() {
        if (this instanceof GetHttpRequest)
            return (GetHttpRequest) this;
        else
            throw new ClassCastException("not a GET request: " + getClass().getName());
    }

    public PostHttpRequest asPostRequest() {
        if (this instanceof PostHttpRequest)
            return (PostHttpRequest) this;
        else
            throw new ClassCastException("not a POST request: " + getClass().getName());
    }

}
