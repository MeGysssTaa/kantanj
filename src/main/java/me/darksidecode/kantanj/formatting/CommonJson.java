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

    public static String unpackJsonObject(String json) {
        json = Check.notNull(json, "json cannot be null").trim();

        if (json.isEmpty())
            throw new IllegalArgumentException("empty JSON string");

        if (json.charAt(0) == '{') {
            // A "packed" object of format { "sampleData": "sampleValue" }.
            // Here we transform it to TODO lol
        }

        return null;
    }

}
