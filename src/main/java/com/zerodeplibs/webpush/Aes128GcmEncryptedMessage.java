package com.zerodeplibs.webpush;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * An implementation class of {@link EncryptedPushMessage}.
 *
 * @author Tomoki Sato
 * @see Aes128GcmMessageEncryption
 */
class Aes128GcmEncryptedMessage implements EncryptedPushMessage {

    private final byte[] encryptedMessage;

    Aes128GcmEncryptedMessage(byte[] encryptedPayload) {
        this.encryptedMessage = encryptedPayload;
    }

    @Override
    public byte[] toBytes() {
        return Arrays.copyOf(encryptedMessage, encryptedMessage.length);
    }

    @Override
    public int length() {
        return this.encryptedMessage.length;
    }

    @Override
    public String contentEncoding() {
        return "aes128gcm";
    }

    byte[] extractSalt() {
        return Arrays.copyOfRange(encryptedMessage, 0, 16);
    }

    int extractRecordSize() {
        return ByteBuffer.wrap(encryptedMessage, 16, 4).getInt();
    }

    int extractKeyLength() {
        return encryptedMessage[20];
    }

    byte[] extractUncompressedAsPublicKeyBytes() {
        return Arrays.copyOfRange(encryptedMessage, 21, 21 + extractKeyLength());
    }

    byte[] extractContent() {
        return Arrays.copyOfRange(encryptedMessage, 21 + extractKeyLength(),
            encryptedMessage.length);
    }

    /**
     * Compares the given object with this object based on their encrypted octet sequences.
     *
     * @param o an object.
     * @return true if the given object is equal to this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Aes128GcmEncryptedMessage)) {
            return false;
        }
        Aes128GcmEncryptedMessage that = (Aes128GcmEncryptedMessage) o;
        return Arrays.equals(encryptedMessage, that.encryptedMessage);
    }

    /**
     * Returns the hash code value for this object based on its encrypted octet sequence.
     *
     * @return the hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(encryptedMessage);
    }

    @Override
    public String toString() {
        return "Aes128GcmEncryptedMessage{"
            + "contentEncoding='" + contentEncoding() + '\''
            + ", length='" + length() + "'}";
    }
}
