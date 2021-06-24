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

package me.darksidecode.kantanj.types;

import java.io.File;
import java.util.NoSuchElementException;

public final class Check {

    private static final String USERNAME_CHARSET = "qwertyuiopasdfghjklzxcvbnm1234567890_";

    private Check() {}

    public static <T> T notNull(T obj, String message, Object... format) {
        if (obj == null)
            throw new NullPointerException(makeMessage(message, format));

        return obj;
    }

    public static <T> T[] notEmpty(T[] array, String message, Object... format) {
        if (notNull(array, "array cannot be null").length == 0)
            throw new IllegalArgumentException(makeMessage(message, format));
        return array;
    }

    public static <T> T instanceOf(Object obj, Class<T> type, String message, Object... format) {
        notNull(obj, "object cannot be null");
        notNull(type, "type cannot be null");

        if (!(type.isInstance(notNull(obj, message)))) // also checks obj!=null
            throw new ClassCastException(makeMessage(message, format));
        return (T) obj;
    }

    public static String username(String username) {
        notNull(username, "username cannot be null");

        for (char c : username.toCharArray())
            if (USERNAME_CHARSET.indexOf(c) == -1)
                throw new IllegalArgumentException("illegal character in username: " + c);

        return username;
    }

    public static void state(boolean b, String message, Object... format) {
        if (b) throw new IllegalStateException(makeMessage(message, format));
    }

    public static File fileExists(File file, String message, Object... format) {
        if (!(Check.notNull(file, "file cannot be null").exists()))
            throw new NoSuchElementException(makeMessage(message, format));
        return file;
    }

    private static String makeMessage(String message, Object... format) {
        return ((format != null) && (format.length > 0))
                ? String.format(message, format) : message;
    }

}
