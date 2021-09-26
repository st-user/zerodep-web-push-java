package com.zerodeplibs.webpush;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * An implementation class of {@link EncryptedPushMessage}.
 *
 * @author Tomoki Sato
 *
 * @see Aes128GcmMessageEncryption
 */
class Aes128GcmEncryptedMessage implements EncryptedPushMessage {

    private final byte[] encryptedPayload;

    Aes128GcmEncryptedMessage(byte[] encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    @Override
    public byte[] toBytes() {
        return Arrays.copyOf(encryptedPayload, encryptedPayload.length);
    }

    byte[] extractSalt() {
        return Arrays.copyOfRange(encryptedPayload, 0, 16);
    }

    int extractRecordSize() {
        return ByteBuffer.wrap(encryptedPayload, 16, 4).getInt();
    }

    int extractKeyLength() {
        return encryptedPayload[20];
    }

    byte[] extractUncompressedAsPublicKeyBytes() {
        return Arrays.copyOfRange(encryptedPayload, 21, 21 + extractKeyLength());
    }

    byte[] extractContent() {
        return Arrays.copyOfRange(encryptedPayload, 21 + extractKeyLength(),
            encryptedPayload.length);
    }
}
