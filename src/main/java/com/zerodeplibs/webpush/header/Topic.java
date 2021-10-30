package com.zerodeplibs.webpush.header;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The utility class used for setting the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.4">Topic</a> header field.
 *
 * <h3>Example:</h3>
 * <pre class="code">
 * // Makes sure the "Topic" header field doesn't contain illegal characters
 * // and the length doesn't exceed the limit.
 * // (no more than 32 characters from the URL and a filename-safe Base 64 alphabet).
 * myHeader.addHeader("Topic", Topic.ensure("SomeTopic"));
 * </pre>
 *
 * @author Tomoki Sato
 */
public class Topic {

    /**
     * The name of the Topic header field.
     */
    public static String HEADER_NAME = "Topic";

    private Topic() {
    }

    private static final Pattern BASE64_URL_PATTERN =
        Pattern.compile("^[A-Za-z0-9\\-_]{1,32}$");

    private static final String MSG = "The Topic header field must be no more than 32 characters "
        + "from the URL and a filename-safe Base 64 alphabet";

    /**
     * Makes sure that the given topic is no more than 32 characters
     * from the URL and a filename-safe Base64 alphabet.
     * If the given topic doesn't meet these constraints,
     * {@link IllegalArgumentException} is thrown.
     *
     * @param topic a topic.
     * @return the same value as the given topic.
     * @throws IllegalArgumentException if the given topic doesn't meet the constraints.
     */
    public static String ensure(String topic) {
        WebPushPreConditions.checkNotNull(topic, "topic");

        Matcher matcher = BASE64_URL_PATTERN.matcher(topic);
        WebPushPreConditions.checkArgument(matcher.matches(), MSG);
        return topic;
    }
}
