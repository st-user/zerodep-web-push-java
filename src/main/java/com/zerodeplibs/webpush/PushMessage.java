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

    private final byte[] message;

    private PushMessage(byte[] messageBytes) {
        this.message = messageBytes;
    }

    /**
     * Creates a new {@link PushMessage} with the given octet sequence.
     *
     * @param messageBytes the octet sequence representing a push message.
     * @return a new {@link PushMessage}.
     * @throws IllegalArgumentException if the given octet sequence is null or empty.
     */
    public static PushMessage of(byte[] messageBytes) {
        WebPushPreConditions.checkArgument(messageBytes != null && messageBytes.length > 0,
            "messageBytes should not be null or empty.");

        return new PushMessage(Arrays.copyOf(messageBytes, messageBytes.length));
    }

    /**
     * Creates a new {@link PushMessage} with the given text.
     * The given text is encoded by using UTF-8.
     *
     * @param messageText the text representing a push message.
     * @return a new {@link PushMessage}.
     * @throws IllegalArgumentException if the given text is null or empty.
     */
    // BEGIN CHECK STYLE OFF
    public static PushMessage ofUTF8(String messageText) { // END CHECK STYLE OFF
        WebPushPreConditions.checkArgument(messageText != null && messageText.length() > 0,
            "messageText should not be null or empty.");

        return new PushMessage(messageText.getBytes(StandardCharsets.UTF_8));
    }

    // Should not change returned byte arrays.
    byte[] getMessage() {
        return this.message;
    }

    /**
     * Compares the given object with this object based on their underlying messages.
     *
     * @param o an object.
     * @return true if the given object is equal to this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PushMessage)) {
            return false;
        }
        PushMessage message1 = (PushMessage) o;
        return Arrays.equals(getMessage(), message1.getMessage());
    }

    /**
     * Returns the hash code value for this object based on its underlying message.
     *
     * @return the hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(getMessage());
    }

    @Override
    public String toString() {
        return "PushMessage{"
            + "message=" + Arrays.toString(message)
            + '}';
    }
}
