package me.darksidecode.kantanj.db;

/**
 * Thrown to indicate that there was a failure authenticating to a Database.
 */
public class DatabaseAuthenticationException extends Exception {

    public DatabaseAuthenticationException(String message) {
        super(message);
    }

    public DatabaseAuthenticationException(Throwable cause) {
        super(cause);
    }

    public DatabaseAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
