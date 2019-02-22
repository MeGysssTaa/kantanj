package me.darksidecode.kantanj.logging;

import java.io.PrintStream;

public interface BasicLogger {

    default boolean isEnableDebug() {
        return false;
    }

    default void setEnableDebug() {
        throw new UnsupportedOperationException(
                "setEnableDebug is not implemented in " + getClass().getName());
    }

    default void debug(String message, Object... format) {
        if (isEnableDebug())
            print(System.out, message, format);
    }

    default void info(String message, Object... format) {
        print(System.out, message, format);
    }

    default void warning(String message, Object... format) {
        print(System.err, message, format);
    }

    default void error(String message, Object... format) {
        print(System.err, message, format);
    }

    default void print(PrintStream printStream, String message, Object... format) {
        message = (((format != null) && (format.length > 0))
                ? String.format(message, format) : message);
        printStream.println(message);
    }

}
