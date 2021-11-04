package com.zerodeplibs.webpush.header;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * The utility class used for setting the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">TTL</a> header field.
 * </p>
 *
 * <div><b>Example:</b></div>
 * <pre class="code">
 * // Suggests that the push message is retained for 2 days.
 * myHeader.addHeader("TTL", TTL.days(2));
 * </pre>
 *
 * @author Tomoki Sato
 */
public class TTL {

    /**
     * The name of the TTL header field.
     */
    public static String HEADER_NAME = "TTL";

    private TTL() {
    }

    /**
     * Convert the given TTL in days to seconds.
     *
     * @param days the TTL in days(how many days a push message is retained by the push service).
     * @return the TTL in seconds.
     * @throws IllegalArgumentException if the given days is negative.
     */
    public static long days(long days) {
        return seconds(TimeUnit.DAYS.toSeconds(days));
    }

    /**
     * Convert the given TTL in hours to seconds.
     *
     * @param hours the TTL in hours(how many hours a push message is retained by the push service).
     * @return the TTL in seconds.
     * @throws IllegalArgumentException if the given hours is negative.
     */
    public static long hours(long hours) {
        return seconds(TimeUnit.HOURS.toSeconds(hours));
    }


    /**
     * Convert the given TTL in minutes to seconds.
     *
     * @param minutes the TTL in minutes
     *                (how many minutes a push message is retained by the push service).
     * @return the TTL in seconds.
     * @throws IllegalArgumentException if the given minutes is negative.
     */
    public static long minutes(long minutes) {
        return seconds(TimeUnit.MINUTES.toSeconds(minutes));
    }

    /**
     * Check if the given TTL in seconds isn't negative.
     *
     * @param seconds the TTL in seconds
     *                (how many seconds a push message is retained by the push service).
     * @return the given seconds.
     * @throws IllegalArgumentException if the given seconds is negative.
     */
    public static long seconds(long seconds) {
        WebPushPreConditions.checkArgument(seconds >= 0,
            "TTL should be a non-negative number.");
        return seconds;
    }

}
