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

package me.darksidecode.kantanj.logging;

import java.io.PrintStream;

public interface BasicLogger {

    default boolean isEnableDebug() {
        return false;
    }

    default void setEnableDebug() {
        throw new UnsupportedOperationException(
                "setEnableDebug is not implemented in " + getClass().getName());
    }

    default void debug(String message, Object... format) {
        if (isEnableDebug())
            print(System.out, message, format);
    }

    default void info(String message, Object... format) {
        print(System.out, message, format);
    }

    default void warning(String message, Object... format) {
        print(System.err, message, format);
    }

    default void error(String message, Object... format) {
        print(System.err, message, format);
    }

    default void print(PrintStream printStream, String message, Object... format) {
        message = (((format != null) && (format.length > 0))
                ? String.format(message, format) : message);
        printStream.println(message);
    }

}
