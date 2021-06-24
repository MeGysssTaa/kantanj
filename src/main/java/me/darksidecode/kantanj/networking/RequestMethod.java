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

/**
 * The set of common methods for HTTP/1.1. These method names are
 * case sensitive and they must be used in uppercase.
 */
public enum RequestMethod {

    /**
     * The GET method is used to retrieve information from the given server using a given URI.
     * Requests using GET should only retrieve data and should have no types effect on the data.
     */
    GET,

    /**
     * Same as GET, but transfers the status line and header section only.
     * @see RequestMethod#GET
     */
    HEAD,

    /**
     * A POST request is used to send data to the server, for example,
     * customer information, file upload, etc. using HTML forms.
     */
    POST,

    /**
     * Replaces all current representations of the target resource with the uploaded content.
     */
    PUT,

    /**
     * Removes all current representations of the target resource given by a URI.
     */
    DELETE,

    /**
     * Establishes a tunnel to the server identified by a given URI.
     */
    CONNECT,

    /**
     * Describes the communication options for the target resource.
     */
    OPTIONS,

    /**
     * Performs a message loop-back test along the path to the target resource.
     */
    TRACE,

    ;

}
