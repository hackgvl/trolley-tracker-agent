package com.codeforgvl.trolleytracker;

public final class Constants {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 4;
    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 3;

    public static final long HTTP_TIMEOUT = UPDATE_INTERVAL * 3 / 4;

    public static final String LOG_TAG = "CodeForGvl";

    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }
}