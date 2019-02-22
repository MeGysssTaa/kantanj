package me.darksidecode.kantanj.networking;

import java.util.Map;

public interface HttpRequest {

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    String getUserAgent() throws IllegalStateException;

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    RequestMethod getRequestMethod() throws IllegalStateException;

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    boolean isSecured() throws IllegalStateException;

    /**
     * @return empty Map if no special request properties should be set.
     */
    Map<String, String> getRequestProperties();

    /**
     * Default value may vary upon implementation.
     */
    int getConnectTimeout();

    /**
     * Default value may vary upon implementation.
     */
    int getReadTimeout();

    /**
     * Defaults to false.
     */
    boolean shouldFollowRedirects();

    /**
     * @throws IllegalStateException if this HttpRequest is not initialized.
     */
    String getURL() throws IllegalStateException;

}
