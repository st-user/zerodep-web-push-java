package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This class represents a push message to be encrypted.
 *
 * @author Tomoki Sato
 * @see MessageEncryption
 */
public class PushMessage {

    private final byte[] messageBytes;

    private PushMessage(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }

    /**
     * Creates a PushMessage with the given byte array.
     *
     * @param messageBytes the byte array representing a push message.
     * @return a new PushMessage
     * @throws IllegalArgumentException if the array is null or empty.
     */
    public static PushMessage of(byte[] messageBytes) {
        WebPushPreConditions.checkArgument(messageBytes != null && messageBytes.length > 0,
            "messageBytes should not be null or empty.");

        return new PushMessage(Arrays.copyOf(messageBytes, messageBytes.length));
    }

    /**
     * Creates a PushMessage with the given text.
     * The given text is encoded by using UTF-8.
     *
     * @param messageText the text representing a push message.
     * @return a new PushMessage.
     * @throws IllegalArgumentException if the text is null or empty.
     */
    // BEGIN CHECK STYLE OFF
    public static PushMessage ofUTF8(String messageText) { // END CHECK STYLE OFF
        WebPushPreConditions.checkArgument(messageText != null && messageText.length() > 0,
            "messageText should not be null or empty.");

        return new PushMessage(messageText.getBytes(StandardCharsets.UTF_8));
    }

    // Should not change returned byte arrays.
    byte[] getMessageBytes() {
        return this.messageBytes;
    }
}
