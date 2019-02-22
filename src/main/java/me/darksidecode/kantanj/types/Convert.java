package me.darksidecode.kantanj.types;

import com.google.gson.internal.LinkedTreeMap;
import org.bson.Document;

public final class Convert {

    private Convert() {}

    public static Document treeMapToBsonDoc(LinkedTreeMap map) {
        return new Document(map);
    }

}
