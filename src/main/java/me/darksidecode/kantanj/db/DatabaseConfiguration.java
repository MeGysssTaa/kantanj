package me.darksidecode.kantanj.db;

import me.darksidecode.kantanj.crypto.Encryptor;

public interface DatabaseConfiguration {

    /**
     * @return The authentication and encryption mechanism to be used for this database.
     *         May be null - in that case, the database will be treated as
     *         unsecured and no sort of encryption will be performed for read/write
     *         operations.
     */
    Encryptor getEncryptor();

}
