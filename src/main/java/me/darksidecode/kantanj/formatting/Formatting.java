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

package me.darksidecode.kantanj.formatting;

import me.darksidecode.kantanj.types.Check;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class Formatting {

    private Formatting() {}

    public static String urlEncodeUtf8(String s) {
        try {
            return URLEncoder.encode(Check.notNull(
                    s, "cannot encode null string"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("error in utf8 url encoding for string: " + s);
        }
    }

    public static String unixLines(String s) {
        return s.
                replace("\r\n", "\n").
                replace("\r",   "\n");
    }

}
