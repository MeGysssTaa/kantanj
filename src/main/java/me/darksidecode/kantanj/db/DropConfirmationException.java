package me.darksidecode.kantanj.db;

import me.darksidecode.kantanj.types.Check;

public class DropConfirmationException extends Exception {

    private final Database database;

    public DropConfirmationException(Database database) {
        this.database = Check.notNull(database, "database cannot be null");
    }

    /**
     * Confirm the drop action.
     * @return the database whose contents were erased.
     */
    public Database confirmAndDrop() {
        database.__dropConfirm00__();
        return database;
    }

}
