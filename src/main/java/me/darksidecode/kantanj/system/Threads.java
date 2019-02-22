package me.darksidecode.kantanj.system;

import java.util.concurrent.TimeUnit;

public class Threads {

    public static void sleepQuietly(long duration, TimeUnit durationUnit) {
        sleepQuietly(durationUnit.toMillis(duration));
    }

    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

}
