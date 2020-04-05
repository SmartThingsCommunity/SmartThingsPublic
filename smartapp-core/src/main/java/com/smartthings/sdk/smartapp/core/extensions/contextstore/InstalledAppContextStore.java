package com.smartthings.sdk.smartapp.core.extensions.contextstore;

import java.util.stream.Stream;


/**
 * Implementations of this interface handle storing and updating
 * InstalledAppContext instances, including keeping tokens up-to-date.
 *
 * IMPORTANT: Implementations of this class are responsible for keeping the
 * tokens up-to-date. Refresh tokens expire after 30 days.
 */
public interface InstalledAppContextStore<T extends InstalledAppContext> {
    /**
     * Record the given context and keep its tokens up-to-date. Call this when
     * the application is installed.
     */
    void add(T context);

    /**
     * Update the previously-recorded context and keep its tokens up-to-date.
     * Call this when the application is updated.
     */
    default void update(T context) {
        add(context);
    }

    /**
     * Remove this application from the context store. Call this when the
     * application is uninstalled.
     */
    void remove(String installedAppId);

    /**
     * Get the context for the given installedAppId. If tokens are included,
     * they should be good for at least an hour. (If there is less than that,
     * they will be refreshed before being returned.)
     */
    T get(String installedAppId);

    /**
     * Get a list of active contexts as a stream.
     */
    Stream<T> get();
}
