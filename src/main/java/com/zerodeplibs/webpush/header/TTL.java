package com.zerodeplibs.webpush.header;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.util.concurrent.TimeUnit;

/**
 * The utility class for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">TTL</a> header field.
 *
 * <p>
 * <b>Example:</b>
 * </p>
 * <pre class="code">
 * // Suggests that the push message is retained for 2 days.
 * myHeader.addHeader("TTL", TTL.days(2));
 * </pre>
 *
 * @author Tomoki Sato
 */
public abstract class TTL {

    /**
     * Creates a value for the TTL header field by specifying the number of days.
     *
     * @param days how many days a push message is retained by the push service.
     * @return the value converted from days to seconds.
     * @throws IllegalArgumentException if the number of days is negative.
     */
    public static Long days(long days) {
        return seconds(TimeUnit.DAYS.toSeconds(days));
    }

    /**
     * Creates a value for the TTL header field by specifying the number of hours.
     *
     * @param hours how many hours a push message is retained by the push service.
     * @return the value converted from hours to seconds.
     * @throws IllegalArgumentException if the number of seconds is negative.
     */
    public static Long hours(long hours) {
        return seconds(TimeUnit.HOURS.toSeconds(hours));
    }


    /**
     * Creates a value for the TTL header field by specifying the number of minutes.
     *
     * @param minutes how many minutes a push message is retained by the push service.
     * @return the value converted from minutes to seconds.
     * @throws IllegalArgumentException if the number of minutes is negative.
     */
    public static Long minutes(long minutes) {
        return seconds(TimeUnit.MINUTES.toSeconds(minutes));
    }

    /**
     * Creates a value for the TTL header field by specifying the number of seconds.
     *
     * @param seconds how many seconds a push message is retained by the push service.
     * @return the given seconds.
     * @throws IllegalArgumentException if the number of seconds is negative.
     */
    public static Long seconds(long seconds) {
        WebPushPreConditions.checkArgument(seconds >= 0,
            "TTL should be a non-negative number.");
        return seconds;
    }

}
