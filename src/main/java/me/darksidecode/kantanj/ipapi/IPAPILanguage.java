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

package me.darksidecode.kantanj.ipapi;

import lombok.Getter;

/**
 * Localized city, regionName and country can be requested
 * by setting the GET parameter lang to one of the following.
 */
public enum IPAPILanguage {

    ENGLISH    ("en",                  "English (default)"),

    GERMAN     ("de",                   "Deutsch (German)"),

    SPANISH    ("es",                  "Español (Spanish)"),

    PORTUGUESE ("pt-BR", "Português - Brasil (Portuguese)"),

    FRENCH     ("fr",                  "Français (French)"),

    JAPANESE   ("ja",                   "日本語 (Japanese)"),

    CHINESE    ("zh-CN",                   "中国 (Chinese)"),

    RUSSIAN    ("ru",                  "Русский (Russian)")

    ;

    @Getter
    private final String iso639Name;

    @Getter
    private final String description;

    IPAPILanguage(String iso639Name, String description) {
        this.iso639Name = iso639Name;
        this.description = description;
    }

}
