package me.darksidecode.kantanj.db;

import lombok.Getter;
import lombok.Setter;

public class GlobalDatabasesOptions {

    @Getter @Setter
    private static volatile boolean convertLinkedTreeMapToBsonDocument = true;

}
