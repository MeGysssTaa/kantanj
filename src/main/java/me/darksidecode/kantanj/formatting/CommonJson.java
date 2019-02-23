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

package me.darksidecode.kantanj.formatting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.darksidecode.kantanj.types.Check;

public final class CommonJson {

    private CommonJson() {}

    @Getter
    private static final Gson simpleGsonNoHtmlEsc = new GsonBuilder().
            disableHtmlEscaping().
            create();

    @Getter
    private static final Gson prettyGsonNoHtmlEsc = new GsonBuilder().
            setPrettyPrinting().
            disableHtmlEscaping().
            create();

    public static <T> T fromJson(String json, Class<T> typeOfT) {
        return simpleGsonNoHtmlEsc.fromJson(
                Check.notNull(json, "json cannot be null"),
                Check.notNull(typeOfT, "typeOfT cannot be null")
        );
    }

    public static String toJson(Object obj) {
        return simpleGsonNoHtmlEsc.toJson(Check.notNull(obj, "obj cannot be null"));
    }

    public static String toPrettyJson(Object obj) {
        return prettyGsonNoHtmlEsc.toJson(Check.notNull(obj, "obj cannot be null"));
    }

}
