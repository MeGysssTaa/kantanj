package me.darksidecode.kantanj.ipapi;

import lombok.Getter;

/**
 * Localized city, regionName and country can be requested
 * by setting the GET parameter lang to one of the following.
 */
public enum IPAPILanguage {

    ENGLISH    ("en",                "English (default)"),

    GERMAN     ("de",                 "Deutsch (German)"),

    SPANISH    ("es",                "Español (Spanish)"),

    PORTUGUESE ("pt-BR", "Español - Argentina (Spanish)"),

    FRENCH     ("fr",                "Français (French)"),

    JAPANESE   ("ja",                 "日本語 (Japanese)"),

    CHINESE    ("zh-CN",                 "中国 (Chinese)"),

    RUSSIAN    ("ru",                "Русский (Russian)")

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
