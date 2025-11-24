package com.zerodeplibs.webpush;

import java.security.NoSuchAlgorithmException;

/**
 * Static factory methods used to create instances of {@link MessageEncryption}.
 *
 * <div><b>Thread Safety:</b></div>
 *
 * <p>
 * Instances of {@link MessageEncryption} obtained
 * through a factory method of this class are <b>NOT</b> thread-safe.
 * But instances of {@link EncryptedPushMessage} obtained through them are thread-safe.
 * </p>
 *
 * @author Tomoki Sato
 */
public class MessageEncryptions {

    private MessageEncryptions() {
    }

    /**
     * Creates a new {@link MessageEncryption} that uses the "aes128gcm" content encoding.
     *
     * @return a new {@link MessageEncryption}.
     */
    public static MessageEncryption of() {
        try {
            return new Aes128GcmMessageEncryption();
        } catch (NoSuchAlgorithmException e) {
            throw MessageEncryptionException.withDefaultMessage(e);
        }
    }
}
