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
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class Networking {

    private Networking() {}

    /**
     * By default, any user agents in Java are silently updated with a String
     * of format "Java/1.8.0". This is often not desired.
     */
    public static void removeJavaUserAgentSuffixGlobally() {
        System.setProperty("http.agent", "");
    }

    public static String resolveIPv4(String host) {
        try {
            Check.notNull(host, "host cannot be null");

            for (InetAddress inetAddress : InetAddress.getAllByName(host))
                if (inetAddress instanceof Inet4Address)
                    return inetAddress.getHostAddress();

            throw new UnknownHostException("unknown host " + host);
        } catch (Exception ex) {
            throw new RuntimeException("failed to resolve IPv4 of " + host, ex);
        }
    }

    public static final class Http {
        private Http() {}

        public static String post(PostHttpRequest request) {
            try {
                HttpURLConnection con = openConnection(request);
                StringBuilder response = new StringBuilder();

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(request.getPostData());
                outputStream.close();

                if (request.shouldDoInput())
                    return readResponseAndDisconnect(con, response);
                else {
                    con.disconnect();
                    return null;
                }
            } catch (Exception ex) {
                throw new RuntimeException("http POST request failed", ex);
            }
        }

        public static String get(GetHttpRequest request) {
            try {
                HttpURLConnection con = openConnection(request);
                StringBuilder response = new StringBuilder();

                return readResponseAndDisconnect(con, response);
            } catch (Exception ex) {
                throw new RuntimeException("http GET request failed", ex);
            }
        }

        public static HttpURLConnection openConnection(HttpRequest request) {
            Check.notNull(request, "request cannot be null");

            try {
                URL url = new URL(request.getURL());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod(request.getRequestMethod().name());
                Map<String, String> requestProps = request.getRequestProperties();

                for (String prop : requestProps.keySet())
                    con.setRequestProperty(prop, requestProps.get(prop));

                if (request instanceof PostHttpRequest) {
                    if (!(requestProps.containsKey("Content-Type")))
                        con.setRequestProperty("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());

                    con.setRequestProperty("Content-Length",
                            String.valueOf(((PostHttpRequest) request).getPostData().length));
                }

                con.setRequestProperty("User-Agent", request.getUserAgent());
                con.setInstanceFollowRedirects(request.shouldFollowRedirects());
                con.setConnectTimeout(request.getConnectTimeout());
                con.setReadTimeout(request.getReadTimeout());

                con.setDoInput(request.shouldDoInput());
                con.setDoOutput(request.shouldDoOutput());

                return con;
            } catch (Exception ex) {
                throw new RuntimeException("failed to establish a new connection", ex);
            }
        }

        private static String readResponseAndDisconnect(HttpURLConnection con,
                                                        StringBuilder response) throws IOException {
            InputStream inputStream = con.getInputStream();

            for (String line : IOUtils.readLines(inputStream, StandardCharsets.UTF_8))
                response.append(line).append('\n');

            inputStream.close();
            con.disconnect();

            if ((response.length() > 0) && (response.charAt(response.length() - 1) == '\n'))
                // Delete trailing new-line.
                response.deleteCharAt(response.length() - 1);

            return response.toString();
        }
    }

}
