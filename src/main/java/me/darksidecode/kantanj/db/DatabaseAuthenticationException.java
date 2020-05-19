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

package me.darksidecode.kantanj.db;

/**
 * Thrown to indicate that there was a failure authenticating to a Database.
 */
public class DatabaseAuthenticationException extends Exception {

    public DatabaseAuthenticationException(String message) {
        super(message);
    }

    public DatabaseAuthenticationException(Throwable cause) {
        super(cause);
    }

    public DatabaseAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
